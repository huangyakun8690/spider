package com.xxl.job.executor.processor.detail;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.util.RegMatcherUtil;
import com.xxl.job.executor.util.StringHas;

public class DetailPageProcessor implements PageProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static int SLEEPTIME = 1000 / 2;

	private JSONArray details;

	private String randomKey;

//	private Downloader dtlDownloader;
	
	private CollectionSite conSite =null;

	private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME).setTimeOut(30 * 1000);

	public DetailPageProcessor(JSONArray details, String randomKey) {
//		this.dtlDownloader = dtlDownloader;
		this.details = details;
		this.randomKey = randomKey;
	}

	@Override
	public void process(Page page) {
		try {
			logger.info("DetailPageProcessor begin , url: " + page.getUrl() + ", starttime: "+StringHas.getDateNowStr());
			JSONObject pageJSONObj = new JSONObject();
//			JSONArray contentArray = new JSONArray();
//			JSONObject content = new JSONObject();
			for (int i = 0; i < details.length(); i++) {
				JSONObject href = (JSONObject) details.get(i);
				if (!pageJSONObj.has(href.getString(JsonKeys.ZCD_FIELD_NAME))) {
					if(pageJSONObj.has(JsonKeys.ZCD_FIELD_TYPE)) continue;
					if (Constants.FIELD_OPERATION_GET.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
						
						if(page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()").get()!=null){
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
									page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()").get());
						}else{
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
									"");
			
						}
					} else if(Constants.FIELD_OPERATION_REG.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))){
						String oriContent = page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()").get();
						String reg = href.getString(JsonKeys.ZCD_FIELD_REG);
						if(StringUtils.hasText(oriContent)){
							String newContent = RegMatcherUtil.reg(oriContent, reg);
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),newContent);
						}else{
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),"");
						}
					}else if(Constants.FIELD_HTML.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
						pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME), page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH)).get());
					}
					else {
						pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
								page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/@href").get());
					}
				}
				
//				if(StringUtils.hasText(href.getString(JsonKeys.ZCD_FIELD_KEY))){
//					content.put(href.getString(JsonKeys.ZCD_FIELD_KEY), page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()").get());
//				}
				
			}
//			contentArray.put(content);
//			System.out.println(contentArray);
			
			if (!pageJSONObj.has("url")) {
				pageJSONObj.put("url", page.getUrl().toString());
			}
			// store in the cache temporarily
			HashMap<String, Object> map = new HashMap<String, Object>();
			//map.put(Constants.STORE_PAGE_HTML, page);
			map.put(Constants.STORE_PAGE_JSON, pageJSONObj);
			InformationCache.getCache().put(randomKey, map);
			// XxlJobLogger.log("#LAD# page processor [process detail page],
			// page info has been put into cache.");
			// end and log
			logger.info(pageJSONObj.toString().substring(0, 40) + "...");
		} catch (Exception e) {
			logger.info(" DetailPageProcessor [process detail page] encounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
		}
	}

	public PageProcessor setRamdomKey(String randomKey) {
		this.randomKey = randomKey;
		return this;
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

	
}
