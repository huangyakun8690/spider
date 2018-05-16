package com.xxl.job.executor.processor.detail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;

import com.Application;
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
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.DownloaderFactory;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.service.jobhandler.AcqJobHandler;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.YuntingRedisScheduler;

public class DetailProcessThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private AcqPageRuleService acqPageRuleService;
	private CollectionStrategyService collectionStrategyService;
	private CollectionSiteService collectionSiteService;
	private InfoEntityService infoService;
	public static String veriCodePath;
	public static YuntingRedisScheduler redisScheduler;
	private ISpiderService spdierService;

	private CollectionStrategy collectionStrategy;
	private JSONObject pageRuleObj = new JSONObject();
	private AcqPageRule acqPageRule = null;

	private Long siteId = 1582769913l;
	private boolean terminate = false;

	public DetailProcessThread(ISpiderService spdierService, AcqPageRuleService acqPageRuleService,
			CollectionStrategyService collectionStrategyService, CollectionSiteService collectionSiteService,
			InfoEntityService infoService) {
		super();
		this.spdierService = spdierService;
		this.acqPageRuleService = acqPageRuleService;
		this.collectionStrategyService = collectionStrategyService;
		this.collectionSiteService = collectionSiteService;
		this.infoService = infoService;
	}

	@Override
	public void run() {
		logger.info("开始扫描详情表");
		while (!terminate) {
			Long threadSign = Thread.currentThread().getId() + System.currentTimeMillis();
			try {
				if (siteId == null) {
					siteId = spdierService.findNeedSpiderSerialNo();
				}

				if (siteId == null) {
					Thread.sleep(1000);
					continue;
				}

				List<JSONObject> fetchResults = spdierService.fetchAcqDetail(threadSign.intValue(), siteId);

				logger.info("#######详情任务执行：提取任务" + fetchResults.size() + "条，提取时间为：" + StringHas.getDateNowStr());
				Long date1 = (new Date()).getTime();

				if (fetchResults.size() == 0) {
					siteId = null;
					continue;
				}

				acqPageRule = this.acqPageRuleService
						.findByNamedParam(new String[] { "siteId", "isValid" }, new Object[] { siteId, 1 }).get(0);
				CollectionSite site = this.collectionSiteService.getEntity(siteId);
				collectionStrategy = site.getAcqStrategyId() == null ? new CollectionStrategy(1, 1000, 10000, 1, 0, 1)
						: this.collectionStrategyService.getEntity(site.getAcqStrategyId());

				JSONObject jsonObjPageRule = new JSONObject(acqPageRule.getPageRuleContent());
				JSONArray stageArray = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_XPATH_JSON);
				List<String> stageFlagList = new ArrayList<String>();
				List<JSONArray> stageDataList = new ArrayList<JSONArray>();
				for (int i = 0; i < stageArray.length(); i++) {
					stageFlagList.add(((JSONObject) stageArray.get(i)).getString(JsonKeys.ZCD_STAGE_FLAG));
					stageDataList.add(((JSONObject) stageArray.get(i)).getJSONArray(JsonKeys.ZCD_STAGE_DATA));
				}
				// 列表或者表格流程中的详情处理代码区块
				if (Constants.STAGE_FLAG_DETAIL.equalsIgnoreCase(stageFlagList.get(stageFlagList.size() - 1))) {
					logger.info(" DetailProcessThread [build detail info], starts to build detail info.");
					JSONArray details = buildDetailsJson(stageDataList.get(stageFlagList.size() - 1));
					pageRuleObj.put(JsonKeys.ZCD_FIELD_JSON, details);
					logger.info(" DetailProcessThread [build detail info], detail info has been built.");
				}

				List<String> targetUrls = new ArrayList<String>();
				for (JSONObject jsonObject : fetchResults) {
					targetUrls.add(jsonObject.getString("url"));
				}
				processDetailPages(targetUrls, fetchResults);
				Long date2 = (new Date()).getTime();
				logger.info("#######任务结束：结束时间为：" + StringHas.getDateNowStr() + "历时：" + (date2 - date1) + "毫秒");

				Thread.sleep(1000);
			} catch (Exception e) {
				XxlJobLogger.log("异常信息为：" + AllErrorMessage.getExceptionStackTrace(e));
				logger.info("异常信息为：" + AllErrorMessage.getExceptionStackTrace(e));
			} finally {
			}
		}
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
			// fieldJsonObj.put(JsonKeys.ZCD_FIELD_TYPE,
			// Constants.EXTRACT_TEXT);
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_TYPE, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_NAME));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_XPATH, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_XPATH));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_KEY, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_KEY));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_REG, ((JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_REG));
			details.put(fieldJsonObj);
		}
		return details;
	}

	private void processDetailPages(List<String> targetUrls, List<JSONObject> fetchResults) {
		Downloader dtlDownloader = null;
		logger.info(" DetailProcessThread detail page size: " + targetUrls.size());
		String randomKey = UUID.randomUUID().toString();
		try {
			logger.info("开始爬取数据：数据量为" + targetUrls.size() + "爬取时间为：" + StringHas.getDateNowStr());
			boolean useProxy = (collectionStrategy.getUseProxy() == 0) ? true : false;
			dtlDownloader = DownloaderFactory.createDownloader(collectionStrategy.getFlag(), useProxy);
			DetailPageProcessor detailPageProcessor = new DetailPageProcessor(pageRuleObj.getJSONArray(JsonKeys.ZCD_FIELD_JSON), randomKey);

			Spider.create(detailPageProcessor).setScheduler(AcqJobHandler.redisScheduler)
					.addUrl((String[]) targetUrls.toArray(new String[targetUrls.size()]))
					.thread(collectionStrategy.getThreadNum()).setDownloader(dtlDownloader).run();
			// store page info
			List<HashMap<String, Object>> list = InformationCache.getCache().get(randomKey);
			logger.info(" DetailProcessThread [process detail page], page info has been got from cache.");

			infoService.storePageInfo(list, acqPageRule);
			if (list != null && list.size() > 0) {
				spdierService.updateState(list, fetchResults);
			} else {
				logger.error("本次未爬取页面");
			}
			logger.info(" DetailProcessThread [process detail page], page info has been stored in ES and RDB.");
		} catch (Exception e) {
			logger.error(
					" DetailProcessThread [process detail page] encounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
		} finally {
			// remove key from cache
			InformationCache.getCache().remove(randomKey);
			// destroy web driver
			destoryDownloader(dtlDownloader);
		}
	}

	private void destoryDownloader(Downloader dtlDownloader) {
		if ((dtlDownloader instanceof AdvancedSeleniumDownloader) && (null != dtlDownloader)) {
			((AdvancedSeleniumDownloader) dtlDownloader).destoryWebDriverPool();
		}
	}

	public boolean isTerminate() {
		return terminate;
	}

	public void setTerminate(boolean terminate) {
		this.terminate = terminate;
	}

}
