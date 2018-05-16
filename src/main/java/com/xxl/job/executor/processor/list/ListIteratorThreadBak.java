package com.xxl.job.executor.processor.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.service.AcqPageRuleService;
import com.ustcinfo.ptp.yunting.service.CollectionSiteService;
import com.ustcinfo.ptp.yunting.service.CollectionStrategyService;
import com.ustcinfo.ptp.yunting.service.ISpiderService;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.processor.LADPageProcessor;
import com.xxl.job.executor.processor.LOTPageProcessor;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.YuntingRedisScheduler;
//这个类暂时也不用了
//lly 171219
public class ListIteratorThreadBak  implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private AcqPageRuleService acqPageRuleService;
	private CollectionSiteService collectionSiteService;
	private CollectionStrategyService collectionStrategyService;
	private InfoEntityService infoService;
	public static String veriCodePath;
	public static YuntingRedisScheduler redisScheduler;
	private ISpiderService spdierService ;
	
	private CollectionStrategy collectionStrategy  ;
	private AdvancedSeleniumDownloader asDownloader = null;
	private JSONObject pageRuleObj = new JSONObject();
	private AcqPageRule acqPageRule =null ;
	private AcqDeatilsDaoImpl  acqDeatilsDaoImpl ;
	
	private Long pre_serialNo = null ;
	private boolean terminate = false ;
	private JSONObject taskInfoJson =null ;
	private boolean acqProcessLAD ;
	private static int SLEEPTIME = 1000;
	private CollectionSite site = null ; 
	
	public ListIteratorThreadBak(
			CollectionStrategyService collectionStrategyService,
			ISpiderService spdierService,
			AcqPageRuleService acqPageRuleService,
			CollectionSiteService collectionSiteService,
			InfoEntityService infoService, 
			AcqDeatilsDaoImpl acqDeatilsDaoImpl) {
		super();
		this.collectionStrategyService = collectionStrategyService ;
		this.spdierService = spdierService ;
		this.acqPageRuleService = acqPageRuleService;
		this.collectionSiteService = collectionSiteService;
		this.infoService = infoService;
		this.acqDeatilsDaoImpl = acqDeatilsDaoImpl ;
	}

	@Override
	public void run() {
		
		while(!terminate){
			Long threadSign = Thread.currentThread().getId() +System.currentTimeMillis();
			try{
				if(pre_serialNo==null){
					pre_serialNo = spdierService.findNeedSpiderTaskId() ;
				}
				
				if(pre_serialNo==null){
					Thread.sleep(1000);
					continue ;
				}
				
				List<JSONObject> fetchResults = spdierService.fetchAcqNavi(threadSign.intValue(), pre_serialNo) ;
				logger.info("#######导航任务执行：提取任务"+fetchResults.size()+"条，提取时间为："+StringHas.getDateNowStr());
				Long date1=(new Date()).getTime();
				if(fetchResults.size()==0){
					pre_serialNo = null ;
					continue ;
				}
				taskInfoJson = spdierService.getTaskInfo(pre_serialNo);
				String[] params = taskInfoJson.getString("executor_param".toUpperCase()).split(",") ;
				String serialNumber = params[1].replace("SerialNum:", "");
				acqPageRule = this.acqPageRuleService
						.findByNamedParam(new String[] { "siteSerialNumber", "isValid" }, new Object[] { serialNumber, 1 })
						.get(0);
				site = this.collectionSiteService.getEntity(acqPageRule.getSiteId());
				collectionStrategy = site.getAcqStrategyId() == null
						? new CollectionStrategy(1, 1000, 10000, 1, 0, 1)
							: this.collectionStrategyService.getEntity(site.getAcqStrategyId());
				boolean useProxy=(collectionStrategy.getUseProxy()==0)?true:false;
				asDownloader = new AdvancedSeleniumDownloader(1,useProxy);
				
				JSONObject jsonObjPageRule = new JSONObject(acqPageRule.getPageRuleContent());
				JSONArray stageArray = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_XPATH_JSON);
				List<String> stageFlagList = new ArrayList<String>() ;
				List<JSONArray> stageDataList = new ArrayList<JSONArray>() ;
				for (int i = 0; i < stageArray.length(); i++) {
					stageFlagList.add(((JSONObject) stageArray.get(i)).getString(JsonKeys.ZCD_STAGE_FLAG));
					stageDataList.add(((JSONObject) stageArray.get(i)).getJSONArray(JsonKeys.ZCD_STAGE_DATA));
				}
				
				int endLoopIndex = stageFlagList.size() - 1;

				//列表或者表格流程中的详情处理代码区块
				if (Constants.STAGE_FLAG_DETAIL.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
					logger.info(" #CUS# page processor [build detail info], starts to build detail info.");
					JSONArray details = buildDetailsJson(stageDataList.get(endLoopIndex));
					pageRuleObj.put(JsonKeys.ZCD_FIELD_JSON, details);
					endLoopIndex--;
					logger.info(" #CUS# page processor [build detail info], detail info has been built.");
				}
				//列表或者表格流程中的下一页处理流程
				if (Constants.STAGE_FLAG_NEXTPAGE.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
					logger.info(" #CUS# page processor [build page flip info], starts to build page flip info.");
					JSONArray pageJsonArray = buildNextPageJson(stageDataList.get(endLoopIndex));
					pageRuleObj.put(JsonKeys.ZCD_PAGE_JSON, pageJsonArray);
					endLoopIndex--;
					logger.info(" #CUS# page processor [build page flip info], page flip info has been built.");
				} else {
					JSONObject nextPageJson = new JSONObject();
					nextPageJson.put(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE, false);
					JSONArray pageJsonArray = new JSONArray();
					pageJsonArray.put(nextPageJson);
					pageRuleObj.put(JsonKeys.ZCD_PAGE_JSON, pageJsonArray);
				}
				//列表或者表格页
				if (Constants.STAGE_FLAG_LT.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
					logger.info(" #CUS# page processor [build list info], starts to build list info.");
					JSONArray lists = buildListsJson(stageDataList.get(endLoopIndex));
					pageRuleObj.put(JsonKeys.ZCD_LINK_JSON, lists);
					endLoopIndex--;
					logger.info(" #CUS# page processor [build list info], list info has been built.");
				}
				for(JSONObject jsonObject:fetchResults){
					runSpider(jsonObject.getString("url"),jsonObject.getInt("id")) ;
				}
				
				destoryDownloader(asDownloader);
				Long date2=(new Date()).getTime();
				logger.info("#######导航任务执行结束：结束时间为："+StringHas.getDateNowStr()+"执行历时为："+(date2-date1)+"毫秒");
				Thread.sleep(7*1000);
			}catch(Exception e){
				e.printStackTrace();
				logger.info("ListIteratorThread exception ,more exception information: ."+AllErrorMessage.getExceptionStackTrace(e));
			}
		}
	}
	
	private void runSpider(String link,Integer id) {
		PageProcessor pageProcessor = null;
		pageRuleObj.put(JsonKeys.ZCD_TASK_LINK, link);	
		logger.info("开始爬取导航数据：runSpider"+StringHas.getDateNowStr());
		if(taskInfoJson!=null){
			String[] params = taskInfoJson.getString("executor_param".toUpperCase()).split(",") ;
			Integer AC = Integer.valueOf(params[2].replace("AC:", ""));
			//String COUNT_PER_TASK = params[3].replace("CPT:", "");
			//Integer taskID = Integer.valueOf(params[0].replace("taskId:", ""));
			
			if (acqProcessLAD) {
				logger.info("爬取LADPageProcessor");
				LADPageProcessor  ladPageProcessor= new LADPageProcessor(asDownloader, pageRuleObj, collectionStrategy, infoService,
						acqPageRule, null,null,AC,"1000-2000");
				ladPageProcessor.setConSite(site);
				ladPageProcessor.setAcqDeatilsDaoImpl(acqDeatilsDaoImpl);
				ladPageProcessor.setNavId(id);
				pageProcessor =ladPageProcessor;
				
			} else {
				logger.info("爬取lotPageProcessor");
				LOTPageProcessor lotPageProcessor= new LOTPageProcessor(asDownloader, pageRuleObj, collectionStrategy, infoService,
						acqPageRule, null,null,AC);
				pageProcessor = lotPageProcessor;
			}
			
			logger.info(" #CUS# page processor [run acquisition], acquisition process: "+(acqProcessLAD?"LAD":"LOT"));	
			Spider.create(pageProcessor).addUrl(link).thread(collectionStrategy.getThreadNum())
					.setDownloader(asDownloader.setSleepTime(SLEEPTIME)).run();;
			
			spdierService.updateNaviState(id, 3);
		}
		
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
	
	private void destoryDownloader(Downloader dtlDownloader) {
		if ((dtlDownloader instanceof AdvancedSeleniumDownloader) && (null != dtlDownloader)) {
			((AdvancedSeleniumDownloader) dtlDownloader).destoryWebDriverPool();
		}
	}
}
