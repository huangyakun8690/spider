package com.xxl.job.executor.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.xxl.job.executor.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.dao.IAcqDeatilsDao;
import com.ustcinfo.ptp.yunting.model.AcqDeatilsInfo;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.model.NavigationInfo;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.ExecutorService;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.nextpage.NextPageFactory;
import com.xxl.job.executor.nextpage.NextPageGenerator;

public class LADPageProcessor implements PageProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static int SLEEPTIME = 1000 / 2;

	private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME)
			.setTimeOut(30 * 1000);

	private JSONArray lists;

	private int pageNum;

	private AdvancedSeleniumDownloader asDownloader;

	private AtomicInteger currentPageNo = new AtomicInteger(1);

	private CollectionSite conSite = null;

	private AcqDeatilsDaoImpl acqDeatilsDaoImpl;
	// 171013 page flip problem
	private List<String> listPageUrls = new ArrayList<String>();
	private JSONArray pageFlipArray;

	private long navId=0l;
	WebDriver webDriver = null;

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
	 * @param infoService
	 *            结果持久化方法
	 * @param acqPageRule
	 *            采集规则
	 * @param executorService
	 *            执行方法
	 * @param ac
	 *            定义使用什么方法来处理页面的翻页
	 * @param COUNT_PER_TASK
	 *            对于大型任务，定义每个任务最多执行多少页,例如：1-1000， 1000-2000
	 */
	public LADPageProcessor(AdvancedSeleniumDownloader asDownloader,
			JSONObject jsonObjPageRule, CollectionStrategy collectionStrategy,
			InfoEntityService infoService, AcqPageRule acqPageRule,
			ExecutorService executorService, Site site, Integer ac,
			String COUNT_PER_TASK) {
		this.asDownloader = asDownloader;
		if (site != null) {
			this.site = site;
		} else {
			this.site = Site.me()
					.setRetryTimes(collectionStrategy.getSiteRetryTimes())
					.setSleepTime(collectionStrategy.getSiteSleepTime())
					.setTimeOut(collectionStrategy.getSiteTimeout());
		}
		jsonObjPageRule.getString(JsonKeys.ZCD_TASK_LINK);
		this.lists = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_LINK_JSON);
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

	}

	@Override
	public void process(Page page) {
		long begin  = System.currentTimeMillis() ;
		logger.info("page process begin , url: " + page.getUrl() + ", starttime: "+StringHas.getDateNowStr());
		
		try {
			List<String> targetUrls = new ArrayList<String>();

			try {
				if (webDriver == null) {
					webDriver = asDownloader.getWebDriver();
				}
				String parentUrl = webDriver.getCurrentUrl() ;
				// extract all dtl page urls
				for (int i = 0; i < lists.length(); i++) {
					// maximize the window
					webDriver.manage().window().maximize();
					// get the dtl element
					WebElement element = null;
					String iXpath = lists.getJSONObject(i).getString(
							JsonKeys.ZCD_XPATH);
					try {
						element = WebDriverHelper.findElement(webDriver, null,
								iXpath, null, null);
					} catch (StaleElementReferenceException e) {
						element = WebDriverHelper.findElement(webDriver, null,
								iXpath, null, null);
					}
					if (element == null) {
						logger.info("Cannot find element by xpath: "
										+ iXpath
										+ ", please check whether this is the last page and this exception can be ignored or not.");
						continue;
					}
					// if dtl element has href attribute
					if (WebDriverHelper.hrefIsValid(element)) {
						// add the href to target urls
						String href = element.getAttribute("href") ;
						href = StringHas.getUrl(parentUrl,href) ;
						if(!RedissionUtils.isExisted("detail_map",href)){
							targetUrls.add(href);
							RedissionUtils.set("detail_map",href, href);
						}
						// XxlJobLogger.log(" #LAD# page processor [get detail
						// page url], current pageNo: " + currentPageNo
						// + ", gets target url: " + href);
					}
					// // click the element to get dtl url
					else {
						String listPageWindowHandle = webDriver
								.getWindowHandle();
						element.click();
						// switch to newly opened window
						boolean switched = WebDriverHelper
								.switchToNewWindow(webDriver);
						// add dtl page url to target urls
						String href = webDriver.getCurrentUrl() ;
						if(!RedissionUtils.isExisted("detail_map",href)){
							targetUrls.add(href);
							RedissionUtils.set("detail_map",href, href);
						}

						// XxlJobLogger.log(" #LAD# page processor [get detail
						// page url], current pageNo: " + currentPageNo
						// + ", gets target url: " + webDriver.getCurrentUrl());
						// switch/navigate back to list page window
						if (switched) {
							// close the current window
							webDriver.close();
							// swtich back to list page
							webDriver.switchTo().window(listPageWindowHandle);
						} else {
							webDriver.navigate().back();
						}
					}
				}
				//循环遍历一个页面后，获取到的所有详情地址就持久化到库中，这样就不必要等待其他页面，这个在列表-详情中比较实用，在列表-列表-详情中不是全部实用
				logger.info("targetUrls insert into db");
				saveDetailPages(targetUrls);
				targetUrls.clear();
				//allTargets.add(targetUrls);
				// if is last list page
				if (currentPageNo.get() < pageNum) {
					listPageUrls.add(webDriver.getCurrentUrl());

					NextPageGenerator npg = NextPageFactory
							.getNextPageGenerator(ac);
					
					boolean get_next_page_success = true ;
					do{
						try{
							get_next_page_success = true ;
							WebElement element = npg.getNextPageElement(webDriver,
									currentPageNo.get(), pageFlipArray, listPageUrls);
						}catch(Exception e){
							e.printStackTrace();
							get_next_page_success = false ;
							currentPageNo.incrementAndGet();
							logger.info(" #LAD# page processor [get next list page url error], next pageNo: "
									+ (currentPageNo.get())+", detail messages are as follows:"+AllErrorMessage.getExceptionStackTrace(e)) ;
						}
					}while(!get_next_page_success && currentPageNo.get() < pageNum);
					
					String currentPageUrl = webDriver.getCurrentUrl();
					Page newPage;

					if (webDriver.getCurrentUrl().equalsIgnoreCase(
							currentPageUrl)) {
						newPage = asDownloader.download(webDriver, 0);
					} else {
						newPage = asDownloader.download(webDriver, 10);
					}
					// asDownloader.returnToPool(webDriver);
					// webDriver.getCurrentUrl();
					// log
					logger.info(" #LAD# page processor [get next list page url], current pageNo: "
									+ (currentPageNo.get())
									+ ", next list page url: "
									+ webDriver.getCurrentUrl());
					currentPageNo.incrementAndGet();
					process(newPage);
					
					
				}
				// here is the last list page, add dtl page urls to
				// targetRequests
				else {
					asDownloader.returnToPool(webDriver);
				}
			} catch (InterruptedException exception) {
				exception.printStackTrace();
				logger.info(" #LAD# page processor [get detail page url] encounters exceptions, detail messages are as follows: \n"
						+ AllErrorMessage.getExceptionStackTrace(exception));
				throw new InterruptedException();

			} catch (Exception e) {
				if (webDriver != null) {
					asDownloader.returnToPool(webDriver);
				}
				//asDownloader.destoryWebDriverPool();
				logger.info(" #LAD# page processor [get detail page url] encounters exceptions, detail messages are as follows: \n"
								+ AllErrorMessage.getExceptionStackTrace(e));
				e.printStackTrace();
				if (targetUrls.size() > 0) {
					 saveDetailPages(targetUrls);
					 targetUrls.clear();
				}
			}
		} catch (Exception e) {
			logger.info(" #LAD# page processor enncounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			asDownloader.destoryWebDriverPool();
		}finally{
			long end = System.currentTimeMillis() ;
			logger.info("page process end , url: " + page.getUrl() + ", endtime: "+StringHas.getDateNowStr()+", time:"+(end-begin));
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
