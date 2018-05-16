package com.xxl.job.executor.processor;

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.dao.IAcqDeatilsDao;
import com.ustcinfo.ptp.yunting.model.AcqDeatilsInfo;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.ExecutorService;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.nextpage.NextPageFactory;
import com.xxl.job.executor.nextpage.NextPageGenerator;
import com.xxl.job.executor.nextpage.RulePageGenerator;
import com.xxl.job.executor.service.jobhandler.AcqJobHandler;
import com.xxl.job.executor.util.RedissionUtils;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.WebDriverHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticListPageProcessor implements PageProcessor {

	private static int SLEEPTIME = 1000 / 2;

	private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME)
			.setTimeOut(30 * 1000);

	private JSONArray lists;

	private JSONArray details;

	private int pageNum;

	private Downloader asDownloader;

	private AtomicInteger currentPageNo = new AtomicInteger(1);

	private CollectionStrategy collectionStrategy;

	private AcqPageRule acqPageRule;

	private CollectionSite conSite = null;

	private AcqDeatilsDaoImpl acqDeatilsDaoImpl;
	// 171013 page flip problem
	private JSONArray pageFlipArray;

	private long navId=0l;

	Integer ac;
	String COUNT_PER_TASK;

	/**
	 * 列表和详情页爬虫处理类
	 *
	 * @param asDownloader
	 *            页面下载器，默认在windows上采用chrome，在linux采用phantomJS
	 * @param jsonObjPageRule
	 *            配置的页面规则
	 * @param collectionStrategy
	 *            采集策略，在任务配置的页面策略中配置
	 * @param acqPageRule
	 *            执行方法
	 * @param ac
	 *            定义使用什么方法来处理页面的翻页
	 * @param COUNT_PER_TASK
	 *            对于大型任务，定义每个任务最多执行多少页,例如：1-1000， 1000-2000
	 */
	public StaticListPageProcessor(Downloader asDownloader,
                                   JSONObject jsonObjPageRule,
								   CollectionStrategy collectionStrategy,
                                   AcqPageRule acqPageRule,
                                   Integer ac,
                                   String COUNT_PER_TASK) {
		this.asDownloader = asDownloader;
		this.collectionStrategy = collectionStrategy;
		this.lists = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_LINK_JSON);
		this.details = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_FIELD_JSON);
		this.pageNum = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON)
				.getJSONObject(0).getBoolean(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE) ? jsonObjPageRule
				.getJSONArray(JsonKeys.ZCD_PAGE_JSON).getJSONObject(0)
				.getInt(JsonKeys.ZCD_PAGE_PAGENUM)
				: 1;
		if (this.pageNum > 1) {
			this.pageFlipArray = jsonObjPageRule
					.getJSONArray(JsonKeys.ZCD_PAGE_JSON);
			this.ac = ac;
			this.COUNT_PER_TASK = COUNT_PER_TASK;
		}
		this.acqPageRule = acqPageRule;
	}

	@Override
	public void process(Page page) {
		try {
			List<String> targetUrls = new ArrayList<String>();

			try {
				String parentUrl = page.getUrl().toString() ;
				// extract all dtl page urls
				for (int i = 0; i < lists.length(); i++) {
					String iXpath = lists.getJSONObject(i).getString(JsonKeys.ZCD_XPATH);
					String href = page.getHtml().xpath(iXpath+ "/@href").get() ;

					href = StringHas.getUrl(parentUrl,href) ;
					if(!RedissionUtils.isExisted("detail_map",href)){
						targetUrls.add(href);
						RedissionUtils.set("detail_map",href, href);
					}

				}
				//循环遍历一个页面后，获取到的所有详情地址就持久化到库中，这样就不必要等待其他页面，这个在列表-详情中比较实用，在列表-列表-详情中不是全部实用
				saveDetailPages(targetUrls);
				targetUrls.clear();
				// if is last list page
				if (currentPageNo.get() < pageNum) {
					NextPageGenerator npg = new RulePageGenerator();
					JSONObject nextPageRule = pageFlipArray.getJSONObject(0) ;

					String nextPageUrl= StringHas.getNextPageUrl(nextPageRule.getString(JsonKeys.ZCD_PAGE_NEXTXPATH),nextPageRule.getString(JsonKeys.ZCD_PAGE_INPUT), currentPageNo.get()) ;
					Request request = new Request() ;
					request.setUrl(nextPageUrl);
					Task task = new Task() {
						@Override
						public String getUUID() {
							return java.util.UUID.randomUUID().toString();
						}

						@Override
						public Site getSite() {
							return site;
						}
					};
					Page newPage = asDownloader.download(request,task) ;
					currentPageNo.incrementAndGet();
					process(newPage);

				}

			} catch (Exception e) {
				XxlJobLogger
						.log(" #LAD# page processor [get detail page url] encounters exceptions, detail messages are as follows: \n"
								+ AllErrorMessage.getExceptionStackTrace(e));
				if (targetUrls.size() > 0) {
					 saveDetailPages(targetUrls);
					targetUrls.clear();
				}
			}
		} catch (Exception e) {
			XxlJobLogger
					.log(" #LAD# page processor enncounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
		}

	}

	private void destoryDownloader(Downloader dtlDownloader) {
		if ((dtlDownloader instanceof AdvancedSeleniumDownloader)
				&& (null != dtlDownloader)) {
			((AdvancedSeleniumDownloader) dtlDownloader).destoryWebDriverPool();
		}
	}

	@Override
	public Site getSite() {
		return this.site;
	}

	public CollectionSite getConSite() {
		return conSite;
	}

	public void setConSite(CollectionSite conSite) {
		this.conSite = conSite;
	}

	public AcqDeatilsDaoImpl getAcqDeatilsDaoImpl() {
		return acqDeatilsDaoImpl;
	}

	public void setAcqDeatilsDaoImpl(AcqDeatilsDaoImpl acqDeatilsDaoImpl) {
		this.acqDeatilsDaoImpl = acqDeatilsDaoImpl;
	}

	
	public long getNavId() {
		return navId;
	}

	public void setNavId(long navId) {
		this.navId = navId;
	}

	public void saveDetailPages(List<String> targetUrls) {
		IAcqDeatilsDao acqDeatilsDao = acqDeatilsDaoImpl;
		for (String str : targetUrls) {
			AcqDeatilsInfo acqDeatilsInfo = new AcqDeatilsInfo();
			acqDeatilsInfo.setIsOut(0);
			acqDeatilsInfo.setSiteId(this.conSite.getId());
			acqDeatilsInfo.setNaviId(navId);
			acqDeatilsInfo.setUrl(str);
			acqDeatilsInfo.setState(1);
			acqDeatilsDao.saveAcqDaetil(acqDeatilsInfo);
		}
	}

}
