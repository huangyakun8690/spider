package com.xxl.job.executor.processor.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.dao.IAcqDeatilsDao;
import com.ustcinfo.ptp.yunting.model.AcqDeatilsInfo;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.util.RedissionUtils;
import com.xxl.job.executor.util.StringHas;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ListPageProcessorForScan implements PageProcessor {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static int SLEEPTIME = 1000 / 5;

	private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME).setTimeOut(30 * 1000);

	private JSONArray lists;

	private CollectionSite conSite = null;

	private AcqDeatilsDaoImpl acqDeatilsDaoImpl;
	
	private List<String> listUrls;
	
	private List<Long> navIds;
	
	private String listRandomKey;

	/**
	 * 列表和详情页爬虫处理类
	 * 
	 * @param navIds
	 * @param navIds
	 * @param listRandomKey 
	 * @param acqDeatilsDaoImpl2
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

	public ListPageProcessorForScan(JSONArray lists, CollectionSite conSite, AcqDeatilsDaoImpl acqDeatilsDaoImpl,
			List<String> listUrls, List<Long> navIds, String listRandomKey) {
		this.lists = lists;
		this.conSite = conSite;
		this.acqDeatilsDaoImpl = acqDeatilsDaoImpl;
		this.listUrls = listUrls;
		this.navIds = navIds;
		this.listRandomKey = listRandomKey;
		
	}

	@Override
	public void process(Page page) {
		String href = null;
		String parentUrl = page.getUrl().toString();
		Long navId = navIds.get(listUrls.indexOf(parentUrl));
			try {
				// extract all dtl page urls
				for (int i = 0; i < lists.length(); i++) {
					String iXpath = lists.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_XPATH);
					// System.out.println(page.getHtml());
					href = page.getHtml().xpath(iXpath + "/@href").get();
					if (null == href) {
						logger.error("#ListPageProcessorForScan# href 为空，当前url = {}， xpath = {}",parentUrl,iXpath);
						InformationCache.getCache().put(listRandomKey,navId);
						continue;
					}
					href = StringHas.getUrl(parentUrl, href);
					if (!RedissionUtils.isExisted(href)) {
						//之前也是一条一条存储，现在改为先存数据库，再入redis，极少概率下会重复，可后期做去重处理
						saveDetailPage(parentUrl,href,navId);
						RedissionUtils.add(href);
					}
				}
			} catch (Exception e) {
				InformationCache.getCache().put(listRandomKey,navId);
				logger.error(
						" #ListPageProcessorForScan# page processor [get detail page url] encounters exceptions, detail messages are as follows: \n"
								+ AllErrorMessage.getExceptionStackTrace(e));
				e.printStackTrace();
			}
	}

	private void saveDetailPage(String parentUrl, String targetUrl, Long navId) {
		AcqDeatilsInfo acqDeatilsInfo = new AcqDeatilsInfo();
		acqDeatilsInfo.setIsOut(0);
		acqDeatilsInfo.setSiteId(this.conSite.getId());
		acqDeatilsInfo.setNaviId(navId);
		acqDeatilsInfo.setUrl(targetUrl);
		acqDeatilsInfo.setState(1);
		acqDeatilsDaoImpl.saveAcqDaetil(acqDeatilsInfo);
	}

	@Override
	public Site getSite() {
		return this.site;
	}

//	public void saveDetailPages(String parentUrl, List<String> targetUrls) {
//		for (String targetUrl : targetUrls) {
//			saveDetailPage(parentUrl,targetUrl);
//		}
//	}

}
