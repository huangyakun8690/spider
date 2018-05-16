package com.xxl.job.executor.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ustcinfo.ptp.yunting.model.AcqPageRule;
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
import com.xxl.job.executor.util.CommonHelper;
import com.xxl.job.executor.util.RegMatcherUtil;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.WebDriverHelper;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

public class LOTPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(2).setSleepTime(1000).setTimeOut(30 * 1000);

	private JSONArray lists;

	private JSONArray details;

	private int pageNum;

	private AdvancedSeleniumDownloader asDownloader;

	private AtomicInteger currentPageNo = new AtomicInteger(1);

	private InfoEntityService infoService;

	private AcqPageRule acqPageRule;

	private ExecutorService executorService;
	
	private NavigationInfo navigationInfo;
	
	//171013 page flip problem
	private List<String> listPageUrls=new ArrayList<String>();
	private JSONArray pageFlipArray;
	
	Integer ac;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public LOTPageProcessor(AdvancedSeleniumDownloader asDownloader, JSONObject jsonObjPageRule,
			CollectionStrategy collectionStrategy, InfoEntityService infoService, AcqPageRule acqPageRule,
			ExecutorService executorService, Site site, Integer ac) {
		this.asDownloader = asDownloader;
		if (site != null) {
			this.site = site;
		} else {
			this.site = Site.me().setRetryTimes(collectionStrategy.getSiteRetryTimes())
					.setSleepTime(collectionStrategy.getSiteSleepTime())
					.setTimeOut(collectionStrategy.getSiteTimeout());
		}
		// resolve each item of the biggest json
		this.lists = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_LINK_JSON);
		this.details = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_FIELD_JSON);
		this.pageNum = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON).getJSONObject(0)
				.getBoolean(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE)
						? jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON).getJSONObject(0)
								.getInt(JsonKeys.ZCD_PAGE_PAGENUM)
						: 1;
		if (this.pageNum > 1) {
			this.pageFlipArray=jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON);
		}

		this.infoService = infoService;
		this.acqPageRule = acqPageRule;
		this.executorService = executorService;
		this.ac=ac;
	}

	@Override
	public void process(Page page) {
		try {
			logger.info("page process begin , url: " + page.getUrl() + ", starttime: "+StringHas.getDateNowStr());
			listPageUrls.add(page.getUrl().get());
			// should consider some failure strategies, such as leave it blank,
			// replace it with default value, discard the current page, etc.
			// catenate the original page into a json then put it into kafka
			String randomKey = UUID.randomUUID().toString();
			// resolve xpathOfList
			List<String> xpathOfListOrTable = new ArrayList<String>();
			for (int i = 0; i < lists.length(); i++) {
				xpathOfListOrTable.add(((JSONObject) lists.get(i)).getString(JsonKeys.ZCD_XPATH));
			}
			List<String> relativeXpathOfColumns = new ArrayList<String>();
			for (int i = 0; i < details.length(); i++) {
				for (String xpath : xpathOfListOrTable) {
					String absXpath = ((JSONObject) details.get(i)).getString(JsonKeys.ZCD_FIELD_XPATH);
					if (absXpath.contains(xpath)) {
						String relaXpath = absXpath.replace(xpath, "");
						relativeXpathOfColumns.add(relaXpath);
					}
				}
			}
			for (String xpath : xpathOfListOrTable) {
				JSONObject pageJSONObj = new JSONObject();
				for (int i = 0; i < details.length(); i++) {
					JSONObject href = (JSONObject) details.get(i);
					String columnXpath = new StringBuffer(xpath).append(relativeXpathOfColumns.get(i)).toString();
//					if (StringUtils.hasText(page.getHtml().xpath(columnXpath).get())
//							&& (!pageJSONObj.has(href.getString(JsonKeys.ZCD_FIELD_NAME)))) {
//						if (Constants.EXTRACT_TEXT.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
//							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
//									page.getHtml().xpath(columnXpath + "/allText()").get());
//						} else {
//							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
//									page.getHtml().xpath(columnXpath + "/@href").get());
//						}
//					}
					if(pageJSONObj.has(JsonKeys.ZCD_FIELD_TYPE)) continue;
					if (Constants.FIELD_OPERATION_GET.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
						pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
								page.getHtml().xpath(columnXpath + "/allText()").get());
					} else if(Constants.FIELD_OPERATION_REG.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))){
						String oriContent = page.getHtml().xpath(columnXpath + "/allText()").get();
						String reg = href.getString(JsonKeys.ZCD_FIELD_REG);
						if(StringUtils.hasText(oriContent)){
							String newContent = RegMatcherUtil.reg(oriContent, reg);
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),newContent);
						}else{
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),"");
						}
					}else if(Constants.EXTRACT_HREF.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))){
						pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
								page.getHtml().xpath(columnXpath + "/@href").get());
					}else if(Constants.FIELD_HTML.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
						pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME), page.getHtml().xpath(columnXpath).get());
					}

					
					else {
						pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
								page.getHtml().xpath(columnXpath + "/allText()").get());
					}

				}
				if (pageJSONObj.length() > 0) {
					if (!pageJSONObj.has("url")) {
						pageJSONObj.put("url", page.getUrl().toString());
					}
					// store in the cache temporarily
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(Constants.STORE_PAGE_HTML, page);
					map.put(Constants.STORE_PAGE_JSON, pageJSONObj);
					InformationCache.getCache().put(randomKey, map);
					logger.info("#LOT# page processor [process detail page], page info has been put into cache.");
				}
				// log
//				XxlJobLogger.log(pageJSONObj.toString());
			}
			// store page info
			List<HashMap<String, Object>> list = InformationCache.getCache().get(randomKey);
			XxlJobLogger.log(" #LOT# page processor [process detail page], page info has been get from cache.");
			logger.info(" #LOT# page processor [process detail page], page info has been get from cache.");
			
			infoService.storePageInfo(list, acqPageRule);
			logger.info(" #LOT# page processor [process detail page], page info has been stored in ES and RDB.");
			XxlJobLogger.log(" #LOT# page processor [process detail page], page info has been stored in ES and RDB.");
			if (currentPageNo.get() < pageNum) {
				// flip over the page
				WebDriver webDriver = null;

				try {
					webDriver = asDownloader.getWebDriver();

					NextPageGenerator npg = NextPageFactory.getNextPageGenerator(ac);
					WebElement element = npg.getNextPageElement(webDriver,currentPageNo.get(), pageFlipArray,listPageUrls);

					Thread.sleep(200);
					Page newPage = asDownloader.download(webDriver,0);
					asDownloader.returnToPool(webDriver);
					XxlJobLogger
							.log("#LOT# page processor [page flip over] starts to process next page, current page No is "
									+ currentPageNo);
					currentPageNo.incrementAndGet();
					process(newPage);
				} catch (Exception e) {
					if (webDriver != null) {
						asDownloader.returnToPool(webDriver);
					}
					//asDownloader.destoryWebDriverPool();
					XxlJobLogger
							.log("#LOT# page processor [page flip over] enncounters exceptions, detail messages are as follows: \n"
									+ AllErrorMessage.getExceptionStackTrace(e));
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			//asDownloader.destoryWebDriverPool();
			logger.info("#LOT# page processor enncounters exceptions, detail messages are as follows: \n"
					+ AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
		}
	}

	@Override
	public Site getSite() {
		return this.site;
	}

	public NavigationInfo getNavigationInfo() {
		return navigationInfo;
	}

	public void setNavigationInfo(NavigationInfo navigationInfo) {
		this.navigationInfo = navigationInfo;
	}
	

}
