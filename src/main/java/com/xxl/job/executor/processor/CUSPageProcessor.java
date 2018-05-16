package com.xxl.job.executor.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.xxl.job.executor.util.RegMatcherUtil;
import com.xxl.job.executor.util.StringHas;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.dao.INavigationDao;
import com.ustcinfo.ptp.yunting.dao.NavigationDaoImpl;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.model.NavigationInfo;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.ExecutorService;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.nextpage.NextPageFactory;
import com.xxl.job.executor.nextpage.NextPageGenerator;
import com.xxl.job.executor.service.jobhandler.AcqJobHandler;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.WebDriverHelper;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class CUSPageProcessor implements PageProcessor {

	private static int SLEEPTIME = 1000;

	private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME).setTimeOut(30 * 1000);

	private JSONArray stageArray;
	/**
	 * 规则定制工具中的场景，分为登录，列表，翻页，详情等
	 */
	private List<String> stageFlagList = new ArrayList<String>();
	/**
	 * 规则定制工具中在每个场景下录制的具体的步骤，分为列表数据，翻页数据，详情字段数据，登录流程等
	 */
	private List<JSONArray> stageDataList = new ArrayList<JSONArray>();

	private AdvancedSeleniumDownloader asDownloader;

	private AtomicInteger currentPageNo = new AtomicInteger(1);

	private CollectionStrategy collectionStrategy;

	private InfoEntityService infoService;

	private AcqPageRule acqPageRule;

	private ExecutorService executorService;

	private WebDriver webDriver = null;

	JSONObject pageRuleObj = new JSONObject();

	boolean acqProcessLAD;
	private NavigationDaoImpl navigationDaoImpl;
	
	private AcqDeatilsDaoImpl acqDeatilsDaoImpl;
	
	private Set<Cookie> cookies;
	
	private CollectionSite conSite =null;
	private AtomicInteger loginFailedTimes = new AtomicInteger(0);

	private Logger logger = LoggerFactory.getLogger(getClass());
	// Three top list
	// one for link
	// one for xpath prefix
	// and the last one for store
	List<String> linkList = new ArrayList<String>();
	List<String> xpathPrefixList = new ArrayList<String>();
	List<Object> storeList = new ArrayList<Object>();

	List<String> tempLinkList = new ArrayList<String>();
	
	List<String> listPageUrlList = new ArrayList<String>();
	
	Integer ac;
	Integer taskID ;
	String COUNT_PER_TASK ;

	private String parentUrl ;

	/**
	 * 自定义爬虫处理类
	 * @param asDownloader 页面下载器，默认在windows上采用chrome，在linux采用phantomJS
	 * @param jsonObjPageRule 配置的页面规则
	 * @param collectionStrategy 采集策略，在任务配置的页面策略中配置
	 * @param infoService 结果持久化方法
	 * @param acqPageRule 采集规则
	 * @param executorService 执行方法
	 * @param ac 定义使用什么方法来处理页面的翻页
	 * @param COUNT_PER_TASK 对于大型任务，定义每个任务最多执行多少页,例如：1-1000， 1000-2000
	 */
	public CUSPageProcessor(AdvancedSeleniumDownloader asDownloader, JSONObject jsonObjPageRule,
			CollectionStrategy collectionStrategy, InfoEntityService infoService, AcqPageRule acqPageRule,
			ExecutorService executorService,Integer ac,String COUNT_PER_TASK,Integer taskID ) {
		this.asDownloader = asDownloader;
		this.collectionStrategy = collectionStrategy;
		this.site = Site.me().setRetryTimes(collectionStrategy.getSiteRetryTimes())
				.setSleepTime(collectionStrategy.getSiteSleepTime()).setTimeOut(collectionStrategy.getSiteTimeout());
		this.stageArray = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_XPATH_JSON);
		for (int i = 0; i < stageArray.length(); i++) {
			stageFlagList.add(((JSONObject) stageArray.get(i)).getString(JsonKeys.ZCD_STAGE_FLAG));
			stageDataList.add(((JSONObject) stageArray.get(i)).getJSONArray(JsonKeys.ZCD_STAGE_DATA));
		}
		this.infoService = infoService;
		this.acqPageRule = acqPageRule;
		this.executorService = executorService;
		this.ac=ac;
		this.COUNT_PER_TASK = COUNT_PER_TASK ;
		this.taskID = taskID ;
	}

	@Override
	public void process(Page page) {
		try {
			logger.info("page process begin , url: " + page.getUrl() + ", starttime: "+StringHas.getDateNowStr());
			webDriver = asDownloader.getWebDriver();
			parentUrl = webDriver.getCurrentUrl() ;
			int startLoopIndex = 0;
			int endLoopIndex = stageFlagList.size() - 1;
			
			//如果待采集页面需要登录，此功能区块实现登录功能
			if (Constants.STAGE_FLAG_LOGIN.equalsIgnoreCase(stageFlagList.get(0))) {
				//XxlJobLogger.log(" #CUS# page processor [login stage], starts to login.");
				logger.info(" #CUS# page processor [login stage], starts to login.");
				startLoopIndex++;
				login(stageDataList.get(0));
				//XxlJobLogger.log(" #CUS# page processor [login stage], logined.");
				logger.info(" #CUS# page processor [login stage], logined.");
			}
			//列表或者表格流程中的详情处理代码区块
			if (Constants.STAGE_FLAG_DETAIL.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
				//XxlJobLogger.log(" #CUS# page processor [build detail info], starts to build detail info.");
				logger.info(" #CUS# page processor [build detail info], starts to build detail info.");
				JSONArray details = buildDetailsJson(stageDataList.get(endLoopIndex));
				pageRuleObj.put(JsonKeys.ZCD_FIELD_JSON, details);
				endLoopIndex--;
				//XxlJobLogger.log(" #CUS# page processor [build detail info], detail info has been built.");
				logger.info("#CUS# page processor [build detail info], detail info has been built.");
			}
			//列表或者表格流程中的下一页处理流程
			if (Constants.STAGE_FLAG_NEXTPAGE.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
				//XxlJobLogger.log(" #CUS# page processor [build page flip info], starts to build page flip info.");
				logger.info("#CUS# page processor [build page flip info], starts to build page flip info.");
				JSONArray pageJsonArray = buildNextPageJson(stageDataList.get(endLoopIndex));
				pageRuleObj.put(JsonKeys.ZCD_PAGE_JSON, pageJsonArray);
				endLoopIndex--;
				logger.info(" #CUS# page processor [build page flip info], page flip info has been built.");
				//XxlJobLogger.log(" #CUS# page processor [build page flip info], page flip info has been built.");
			} else {
				JSONObject nextPageJson = new JSONObject();
				nextPageJson.put(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE, false);
				JSONArray pageJsonArray = new JSONArray();
				pageJsonArray.put(nextPageJson);
				pageRuleObj.put(JsonKeys.ZCD_PAGE_JSON, pageJsonArray);
			}
			//列表或者表格页
			if (Constants.STAGE_FLAG_LT.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
				//XxlJobLogger.log(" #CUS# page processor [build list info], starts to build list info.");
				logger.info("#CUS# page processor [build list info], starts to build list info.");
				JSONArray lists = buildListsJson(stageDataList.get(endLoopIndex));
				pageRuleObj.put(JsonKeys.ZCD_LINK_JSON, lists);
				endLoopIndex--;
				logger.info(" #CUS# page processor [build list info], list info has been built.");
				//XxlJobLogger.log(" #CUS# page processor [build list info], list info has been built.");
			}

			/**
			 * 数据初始化过程，先遍历所有场景中的列表场景，找出所有的列表的href连接，便于后面进行数据处理
			 */
			int search_level =  1 ;
			int persist_index = 0 ;
			if (startLoopIndex <= endLoopIndex) {
				logger.info(" #CUS# page processor [get previous links], starts to get previous links.");
				for (int i = startLoopIndex; i <= endLoopIndex; i++) {
					// stage flag
					if (Constants.STAGE_FLAG_LT.equalsIgnoreCase(stageFlagList.get(i))) {
						listPageUrlList.clear();
						// 先提取第一页的次级列表链接
						if (linkList.size() == 0) {
							goThroughEachLTStep(stageDataList.get(i));
							saveSubAddress(search_level,persist_index,linkList) ;
							persist_index = linkList.size() ;
						}
						try {
							// page flip loop
							if ((i + 1 < stageFlagList.size())
									&& Constants.STAGE_FLAG_NEXTPAGE.equalsIgnoreCase(stageFlagList.get(i + 1))) {
								//获取翻页stage数组
								JSONArray pageFlipArray = stageArray.getJSONObject(i + 1).getJSONArray(JsonKeys.ZCD_STAGE_DATA);
								//获取需爬取的总页数
								int totalPageNum = pageFlipArray.getJSONObject(pageFlipArray.length()-1).getInt(JsonKeys.ZCD_PAGE_PAGENUM_END) == 1 ? 1
										: pageFlipArray.getJSONObject(pageFlipArray.length()-1).getInt(JsonKeys.ZCD_PAGE_PAGENUM_END);
								//在这个while里面不停地翻翻翻
								while (currentPageNo.get() < totalPageNum) {
									listPageUrlList.add(webDriver.getCurrentUrl());
									try {
										getNextPage(pageFlipArray,listPageUrlList);
										goThroughEachLTStep(stageDataList.get(i));
										//考虑到列表-列表-详情模式，此方法实用，如果不是此模式，需要考虑linkList的传值问题
										saveSubAddress(search_level,persist_index,linkList) ;
									} catch (TimeoutException e) {
										XxlJobLogger
										.log(" #CUS# page processor [get previous links] timeout, detail messages are as follows: \n"
												+ AllErrorMessage.getExceptionStackTrace(e));
										logger.error(" #CUS# page processor [get previous links] timeout, detail messages are as follows: \n"
												+ AllErrorMessage.getExceptionStackTrace(e));
										e.printStackTrace();
									}
									persist_index = linkList.size() ;
									currentPageNo.incrementAndGet();
								}
								i++;
								continue;
							}
						} catch (Exception e) {
							XxlJobLogger
							.log(" #CUS# page processor [get previous links - page flip over] encounters exceptions, detail messages are as follows: \n"
									+ AllErrorMessage.getExceptionStackTrace(e));
							logger.error(" #CUS# page processor [get previous links - page flip over] encounters exceptions, detail messages are as follows: \n"
									+ AllErrorMessage.getExceptionStackTrace(e));
							e.printStackTrace();
						}
					}
				}
				XxlJobLogger.log(" #CUS# page processor [get previous links], previous links have been gotten.");	
				logger.info(" #CUS# page processor [get previous links], previous links have been gotten.");
			}
			// run spider
			String currentUrl = webDriver.getCurrentUrl();
			asDownloader.returnToPool(webDriver);
			
			if (linkList.size()==0) {
				runSpider(currentUrl);
			}
		} catch (Exception e) {
			logger.info("#CUS# page processor encounters exceptions, detail messages are as follows: \\n");
			XxlJobLogger
			.log(" #CUS# page processor encounters exceptions, detail messages are as follows: \n"
					+ AllErrorMessage.getExceptionStackTrace(e));
			if (webDriver != null) {
				asDownloader.returnToPool(webDriver);
				//asDownloader.destoryWebDriverPool();
				
			}
			e.printStackTrace();
		}
		
	}
	
	private void saveSubAddress(int level,int begin, List<String> urls){
		for(int i=begin;i<urls.size();i++){
			NavigationInfo navigationInfo = new NavigationInfo();
			//String seqNum= UUID.randomUUID().toString();
			navigationInfo.setSeqNum(acqPageRule.getSiteSerialNumber());
			navigationInfo.setUrl(urls.get(i));
			String data="";
			navigationInfo.setData(data);
			navigationInfo.setLevel(level);
			navigationInfo.setTaskId(this.taskID);
			navigationInfo.setState(1);
			INavigationDao navigationDao= navigationDaoImpl;
			navigationDao.saveNavigationDao(navigationInfo);
		}
	}

	private void runSpider(String link) {
		PageProcessor pageProcessor;
		pageRuleObj.put(JsonKeys.ZCD_TASK_LINK, link);	
		//boolean useProxy = asDownloader.useProxy();
		//asDownloader = new AdvancedSeleniumDownloader(1,useProxy);
		if (acqProcessLAD) {
			 
			LADPageProcessor  ladPageProcessor= new LADPageProcessor(asDownloader, pageRuleObj, collectionStrategy, infoService,
					acqPageRule, executorService,site,ac,COUNT_PER_TASK);
			ladPageProcessor.setConSite(conSite);
			ladPageProcessor.setAcqDeatilsDaoImpl(acqDeatilsDaoImpl);
			pageProcessor =ladPageProcessor;
			
		} else {
			LOTPageProcessor lotPageProcessor= new LOTPageProcessor(asDownloader, pageRuleObj, collectionStrategy, infoService,
					acqPageRule, executorService,site,ac);
			pageProcessor = lotPageProcessor;
		}
		
		XxlJobLogger.log(" #CUS# page processor [run acquisition], acquisition process: "+(acqProcessLAD?"LAD":"LOT"));	
		logger.info("#CUS# page processor [run acquisition], acquisition process: \"+(acqProcessLAD?\"LAD\":\"LOT\")");
		Spider.create(pageProcessor).addUrl(link).thread(collectionStrategy.getThreadNum())
				.setDownloader(asDownloader.setSleepTime(SLEEPTIME)).run();
	}

	private JSONArray buildListsJson(JSONArray jsonArray) {
		JSONArray lists = new JSONArray();
		for (int k = 0; k < jsonArray.length(); k++) {
			JSONObject listsJsonObj = new JSONObject();
			if(Constants.FIELD_OPERATION_CLICK.equalsIgnoreCase(jsonArray.getJSONObject(k).getString(JsonKeys.ZCD_FIELD_NAME))){
				acqProcessLAD=true;
			}
			listsJsonObj.put(JsonKeys.ZCD_XPATH, jsonArray.getJSONObject(k).getString(JsonKeys.ZCD_FIELD_XPATH));
			lists.put(listsJsonObj);
		}
		return lists;
	}

	private JSONArray buildNextPageJson(JSONArray jsonArray) {
		JSONArray jArray = new JSONArray();
		for(int i=0;i<jsonArray.length();i++){
			JSONObject nextPageJson = new JSONObject();
			nextPageJson.put(JsonKeys.ZCD_PAGE_NEXTCLASS,
					jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_CLASS));
			nextPageJson.put(JsonKeys.ZCD_PAGE_NEXTID, jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_ID));
			nextPageJson.put(JsonKeys.ZCD_PAGE_NEXTXPATH,
					jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_XPATH));
			nextPageJson.put(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE, true);
			nextPageJson.put(JsonKeys.ZCD_PAGE_PAGENUM_START,
					jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_PAGENUM_START));
			nextPageJson.put(JsonKeys.ZCD_PAGE_PAGENUM_END,
					jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_PAGENUM_END));
			nextPageJson.put(JsonKeys.ZCD_PAGE_PAGENUM,jsonArray.getJSONObject(jsonArray.length()-1).getString(JsonKeys.ZCD_PAGE_PAGENUM_END));
			nextPageJson.put(JsonKeys.ZCD_PAGE_TYPE, jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_NAME));
			nextPageJson.put(JsonKeys.ZCD_PAGE_INPUT, jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_INPUT));
			jArray.put(nextPageJson);
		}
		return jArray;
	}

	private JSONArray buildDetailsJson(JSONArray jsonArray) {
		JSONArray details = new JSONArray();
		for (int k = 0; k < jsonArray.length(); k++) {
			JSONObject fieldJsonObj = new JSONObject();
			String fieldName;
			switch (((JSONObject) jsonArray.get(k)).getString(JsonKeys.ZCD_FIELD_TYPE)) {
			case Constants.SAVE_AS_TITLE:
				fieldName = Constants.STORE_PAGE_TITLE;
				break;
			case Constants.SAVE_AS_TIME:
				fieldName = Constants.STORE_PAGE_CARRY_TIME;
				break;
			case Constants.SAVE_AS_CONTENT:
				fieldName = Constants.STORE_PAGE_CONTENT;
				break;
			case Constants.SAVE:
				fieldName = ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_KEY).toString();
				break;
			default:
				fieldName = "自定义字段名";
			}
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_NAME, fieldName);
			//fieldJsonObj.put(JsonKeys.ZCD_FIELD_TYPE, Constants.EXTRACT_TEXT);
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_TYPE, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_NAME));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_XPATH, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_XPATH));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_KEY, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_KEY));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_REG, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_REG));
			details.put(fieldJsonObj);
		}
		return details;
	}

	private void login(JSONArray fieldArray) throws Exception {
		String verificationCode = null;
		WebElement loginButton = null;
		// go though each step
		{
			for (int j = 0; j < fieldArray.length(); j++) {
				// find the element
				WebElement element = findElement(fieldArray.getJSONObject(j));
				//operation
				String elementOperation = fieldArray.getJSONObject(j).getString(JsonKeys.ZCD_FIELD_NAME);
				String elementSendKey = fieldArray.getJSONObject(j).getString(JsonKeys.ZCD_FIELD_VALUE);
				// execute operation
				if (Constants.FIELD_OPERATION_INPUT.equalsIgnoreCase(elementOperation)) {
					element.sendKeys(elementSendKey);
				} else if (Constants.FIELD_OPERATION_CLICK.equalsIgnoreCase(elementOperation)) {
					loginButton = element;
					clickElement(element);
				} else if (Constants.FIELD_OPERATION_GETVERICODE.equalsIgnoreCase(elementOperation)) {
					recognizeVerificationCode(element.getAttribute("src"));
				} else if (Constants.FIELD_OPERATION_SENDVERICODE.equalsIgnoreCase(elementOperation)) {
					element.sendKeys(verificationCode);
				} else {
					throw new Exception("Unrecognized operation type : " + elementOperation);
				}
			}
			Thread.sleep(5*1000);
			//login failed
			if(loginButton.isDisplayed())
			{
				logger.info(" #CUS# page processor [login stage], login failed for "+loginFailedTimes.get()+" times.");
				XxlJobLogger.log(" #CUS# page processor [login stage], login failed for "+loginFailedTimes.get()+" times.");
				if(loginFailedTimes.getAndIncrement()<3){
					logger.info( "#CUS# page processor [login stage], retry to login.");
					XxlJobLogger.log(" #CUS# page processor [login stage], retry to login.");
					//retry to login
					login(fieldArray);
				}
				else{
					throw new Exception("Login failed, please verify login data.");
				}
			}
			cookies = webDriver.manage().getCookies();
			 for (Cookie cookie : cookies) { 
		            this.site.addCookie(cookie.getDomain(),cookie.getName(),cookie.getValue());
		        }
			 this.site.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1");
		}
	}

	private WebElement findElement(JSONObject stageStep) throws Exception {
		String elementId = stageStep.getString(JsonKeys.ZCD_FIELD_ID);
		String elementName = stageStep.getString(JsonKeys.ZCD_FIELD_INPUT);
		String elementXpath = stageStep.getString(JsonKeys.ZCD_FIELD_XPATH);
		String elementCss = stageStep.getString(JsonKeys.ZCD_FIELD_CLASS);
		return WebDriverHelper.findElement(webDriver,elementId,elementXpath,elementName,elementCss);
	}

	private void goThroughEachLTStep(JSONArray fieldArray) throws Exception {
		
		// go though the
		for (int j = 0; j < fieldArray.length(); j++) {
			try {
				JSONObject stageStep = (JSONObject) fieldArray.get(j);
				String elementId = stageStep.getString(JsonKeys.ZCD_FIELD_ID);
				String elementName = stageStep.getString(JsonKeys.ZCD_FIELD_INPUT);
				String elementXpath = stageStep.getString(JsonKeys.ZCD_FIELD_XPATH);
				String elementCss = stageStep.getString(JsonKeys.ZCD_FIELD_CLASS);
				String elementOperation = stageStep.getString(JsonKeys.ZCD_FIELD_NAME);
				String elementSendKey = stageStep.getString(JsonKeys.ZCD_FIELD_VALUE);
				// execute the operation
				if (Constants.FIELD_OPERATION_INPUT.equalsIgnoreCase(elementOperation)) {
					// find the element
					WebElement element = WebDriverHelper.findElement(webDriver,elementId,elementXpath,elementName,elementCss);
					element.sendKeys(elementSendKey);
				} else if (Constants.FIELD_OPERATION_CLICK.equalsIgnoreCase(elementOperation)) {
					// find the element
					WebElement element = WebDriverHelper.findElement(webDriver,elementId,elementXpath,elementName,elementCss);
					clickElement(element);
				} else if (Constants.FIELD_OPERATION_GET.equalsIgnoreCase(elementOperation)) {
					// don't know what to do (T.T)
					if (currentPageNo.get() == 1) {
						xpathPrefixList.add(elementXpath);
					}
				} else if(Constants.FIELD_OPERATION_REG.equalsIgnoreCase(elementOperation)){
					List<String> urls = RegMatcherUtil.getUrls(webDriver.getPageSource(),elementXpath) ;
					for(String href:urls){
						href = StringHas.getUrl(parentUrl,href) ;
						if(!JedisUtils.isExisted(href)){
							linkList.add(href);
							JedisUtils.set(href, href);
						}
					}
				}else {
					throw new Exception("Unrecognized operation type : " + elementOperation);
				}

			} catch (Exception e) {
				e.printStackTrace();
				XxlJobLogger
				.log(" #CUS# page processor [goThroughEachLTStep] encounters exceptions, detail messages are as follows: \n"
						+ AllErrorMessage.getExceptionStackTrace(e));
				logger.info(" #CUS# page processor [goThroughEachLTStep] encounters exceptions, detail messages are as follows: \n"
						+ AllErrorMessage.getExceptionStackTrace(e));
				continue;
			}
		}
	}

	private void getNextPage(JSONArray pageFlipArray,List<String> listPageUrls) throws Exception {
//		WebElement element = null;
		NextPageGenerator npg = NextPageFactory.getNextPageGenerator(ac);
		npg.getNextPageElement(webDriver,currentPageNo.get(), buildNextPageJson(pageFlipArray),listPageUrls);
//		if (jsonArray == null || jsonArray.length() < 1)
//			throw new Exception("pageFlipArray is null.");
//		else if (jsonArray.length() == 1) {
//			element = WebDriverHelper.findNextPageElement(webDriver, currentPageNo.get()-1,
//					jsonArray.getJSONObject(0).getString(JsonKeys.ZCD_FIELD_CLASS),
//					jsonArray.getJSONObject(0).getString(JsonKeys.ZCD_FIELD_XPATH),
//					jsonArray.getJSONObject(0).getString(JsonKeys.ZCD_FIELD_ID));
//		} else {
//			for (int i = 0; i < jsonArray.length(); i++) {
//				int pageNumStart = jsonArray.getJSONObject(i).getInt(JsonKeys.ZCD_PAGE_PAGENUM_START);
//				int pageNumEnd = jsonArray.getJSONObject(i).getInt(JsonKeys.ZCD_PAGE_PAGENUM_END);
//				if (currentPageNo.get()-1 < pageNumEnd && currentPageNo.get()-1 >= pageNumStart) {
//					element = WebDriverHelper.findNextPageElement(webDriver, currentPageNo.get()-1,
//							jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_CLASS),
//							jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_XPATH),
//							jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_ID));
//				} else {
//					continue;
//				}
//			}
//		}
//		if (WebDriverHelper.hrefIsValid(element)) {
//			webDriver.get(element.getAttribute("href"));
//		} else {
//			element.click();
//			WebDriverHelper.switchToNewWindow(webDriver);
//		}
		// log
		XxlJobLogger.log(" #CUS# page processor [get next list page url], current pageNo: " + (currentPageNo.get() - 1)
				+ ", next list page url: " + webDriver.getCurrentUrl());
		logger.info(" #CUS# page processor [get next list page url], current pageNo: " + (currentPageNo.get() - 1)
				+ ", next list page url: " + webDriver.getCurrentUrl());
	}

	/**
	 * 在初始程序时，需要先获取到第一个列表的所有有效链接
	 * 如果页面链接有href属性，则直接获取，如果没有，则执行点击操作后，在下一个页面或者页面url
	 * @param element
	 */
	private void clickElement(WebElement element) {
		// maximize the window
		webDriver.manage().window().maximize();
		// if dtl element has href attribute
		if (WebDriverHelper.hrefIsValid(element)) {
			// add the href to target urls
			String href = webDriver.getCurrentUrl() ;
			href = StringHas.getUrl(parentUrl,href) ;
			if(!JedisUtils.isExisted(href)){
				linkList.add(href);
				JedisUtils.set(href, href);
			}
			// log
			XxlJobLogger.log(" #CUS# page processor [get detail page url], current pageNo: " + currentPageNo
					+ ", gets target url: " + element.getAttribute("href"));
			logger.info(" #CUS# page processor [get detail page url], current pageNo: " + currentPageNo
					+ ", gets target url: " + element.getAttribute("href"));
		}
		// // click the element to get dtl url
		else {
			String listPageWindowHandle = webDriver.getWindowHandle();
			element.click();
			// switch to newly opened window
			boolean switched = WebDriverHelper.switchToNewWindow(webDriver);
			// add dtl page url to target urls
			String href = webDriver.getCurrentUrl() ;
			href = StringHas.getUrl(parentUrl,href) ;
			if(!JedisUtils.isExisted(href)){
				linkList.add(href);
				JedisUtils.set(href,href);
			}
			// log
			XxlJobLogger.log(" #CUS# page processor [get detail page url], current pageNo: " + currentPageNo
					+ ", gets target url: " + webDriver.getCurrentUrl());
			logger.info(" #CUS# page processor [get detail page url], current pageNo: " + currentPageNo
					+ ", gets target url: " + webDriver.getCurrentUrl());
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

	private String recognizeVerificationCode(String veriCodeImgUrl) {
		String path = AcqJobHandler.veriCodePath;
		File filed = new File(path);
		if (!filed.exists()) {
			filed.mkdir();
		}
		String imgFileName = System.currentTimeMillis() + ".png";
		String storePath = path + imgFileName;
		InputStream in = WebDriverHelper.saveFile(veriCodeImgUrl);
		File file = new File(storePath);
		try {
			FileOutputStream fot = new FileOutputStream(file);
			int l = -1;
			byte[] tmp = new byte[1024];
			while ((l = in.read(tmp)) > 0) {
				fot.write(tmp, 0, l);
			}
			fot.flush();
			fot.close();
			// recognize the verification code
//			String veriCode = GraphicCTranslator.translate(file, GraphicCTranslator.TYPE_2);
//			System.out.println(veriCode);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(" #CUS# page processor [get previous links - page flip over] encounters exceptions, detail messages are as follows: \n"
					+ AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
			return null;
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info(" #CUS# page processor [get previous links - page flip over] encounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
					e.printStackTrace();
				}
			}
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

	public NavigationDaoImpl getNavigationDaoImpl() {
		return navigationDaoImpl;
	}

	public void setNavigationDaoImpl(NavigationDaoImpl navigationDaoImpl) {
		this.navigationDaoImpl = navigationDaoImpl;
	}

	public AcqDeatilsDaoImpl getAcqDeatilsDaoImpl() {
		return acqDeatilsDaoImpl;
	}

	public void setAcqDeatilsDaoImpl(AcqDeatilsDaoImpl acqDeatilsDaoImpl) {
		this.acqDeatilsDaoImpl = acqDeatilsDaoImpl;
	}

	
	

}
