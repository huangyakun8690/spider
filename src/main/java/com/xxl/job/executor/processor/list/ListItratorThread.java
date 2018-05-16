package com.xxl.job.executor.processor.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.DownloaderFactory;
import com.xxl.job.executor.processor.LADPageProcessor;
import com.xxl.job.executor.processor.LOTPageProcessor;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.YuntingRedisScheduler;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

public class ListItratorThread implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private AcqPageRuleService acqPageRuleService;
	private CollectionSiteService collectionSiteService;
	private CollectionStrategyService collectionStrategyService;
	public static String veriCodePath;
	public static YuntingRedisScheduler redisScheduler;
	private ISpiderService spdierService;
	private String dafaultSerialNum = "dafaultSerialNum";

	private CollectionStrategy collectionStrategy;
	private AcqPageRule acqPageRule = null;
	private AcqDeatilsDaoImpl acqDeatilsDaoImpl;
	private JSONArray lists;
	private Long pre_serialNo = null;
	private boolean terminate = false;
	private JSONObject taskInfoJson = null;
	private CollectionSite site = null;

	public ListItratorThread(CollectionStrategyService collectionStrategyService, ISpiderService spdierService,
			AcqPageRuleService acqPageRuleService, CollectionSiteService collectionSiteService,
			InfoEntityService infoService, AcqDeatilsDaoImpl acqDeatilsDaoImpl) {
		super();
		this.collectionStrategyService = collectionStrategyService;
		this.spdierService = spdierService;
		this.acqPageRuleService = acqPageRuleService;
		this.collectionSiteService = collectionSiteService;
		this.acqDeatilsDaoImpl = acqDeatilsDaoImpl;
	}

	@Override
	public void run() {

		while (!terminate) {
			Long threadSign = Thread.currentThread().getId() + System.currentTimeMillis();
			try {
				if (pre_serialNo == null) {
					pre_serialNo = spdierService.findNeedSpiderTaskId();
				}
				if (pre_serialNo == null) {
					Thread.sleep(1000);
					continue;
				}
				List<JSONObject> fetchResults = spdierService.fetchAcqNavi(threadSign.intValue(), pre_serialNo);
				logger.info("#######导航任务执行：提取任务" + fetchResults.size() + "条，提取时间为：" + StringHas.getDateNowStr());
				Long startTime = (new Date()).getTime();
				if (fetchResults.size() == 0) {
					pre_serialNo = null;
					continue;
				}
				taskInfoJson = spdierService.getTaskInfo(pre_serialNo);
				String[] params = taskInfoJson.getString("executor_param".toUpperCase()).split(",");
				String currentSerialNumber = params[1].replace("SerialNum:", "");
				// 不需要每次都查询和解析规则
				if (!dafaultSerialNum.equals(currentSerialNumber)) {
					acqPageRule = this.acqPageRuleService
							.findByNamedParam(new String[] { "siteSerialNumber", "isValid" },
									new Object[] { currentSerialNumber, 1 })
							.get(0);
					site = this.collectionSiteService.getEntity(acqPageRule.getSiteId());
					collectionStrategy = site.getAcqStrategyId() == null
							? new CollectionStrategy(1, 1000, 10000, 1, 0, 1)
							: this.collectionStrategyService.getEntity(site.getAcqStrategyId());
					// 解析出 LT stage
					JSONArray stageArray = new JSONObject(acqPageRule.getPageRuleContent())
							.getJSONArray(JsonKeys.ZCD_XPATH_JSON);
					for (int i = 0; i < stageArray.length(); i++) {
						if (Constants.STAGE_FLAG_LT
								.equalsIgnoreCase(stageArray.getJSONObject(i).getString(JsonKeys.ZCD_STAGE_FLAG))) {
							lists = stageArray.getJSONObject(i).getJSONArray(JsonKeys.ZCD_STAGE_DATA);
						}
					}
					dafaultSerialNum = currentSerialNumber;
				}
				if (null == lists) {
					logger.info("#######导航任务,页面规则不含有[列表或表格页]");
					continue;
				}
				// 提取出targetUrls和navIds,只启动一个spider
				List<String> targetUrls = new ArrayList<String>();
				List<Long> navIds = new ArrayList<Long>();
				for (JSONObject jsonObject : fetchResults) {
					targetUrls.add(jsonObject.getString("url"));
					navIds.add(jsonObject.getLong("id"));
				}
				runSpider(lists, targetUrls, navIds);
				logger.info("#######导航任务执行结束：结束时间为：" + StringHas.getDateNowStr() + "，执行历时为："
						+ ((new Date()).getTime() - startTime) + "毫秒");
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("ListItratorThread exception ,more exception information:"
						+ AllErrorMessage.getExceptionStackTrace(e));
			}
		}
	}

	// 为避免局部异常被忽略，更新state=3导致数据缺失，在保证数据库acq_details数据与redis数据一致的前提下，使用内存存储异常navId，将每批出错的navId状态置为4
	private void runSpider(JSONArray lists, List<String> targetUrls, List<Long> navIds) {
		String listRandomKey = UUID.randomUUID().toString();
		Downloader listDownloader = null;
		try {
			// 动态渲染时避免长时间使用同一批webdriver导致session过期或其他异常，每次都重新创建downloader
			boolean useProxy = (collectionStrategy.getUseProxy() == 0) ? true : false;
			listDownloader = DownloaderFactory.createDownloader(collectionStrategy.getFlag(), useProxy);
			if (taskInfoJson != null) {
				logger.info(" ListItratorThread [run Spider].");
				Spider.create(
						new ListPageProcessorForScan(lists, site, acqDeatilsDaoImpl, targetUrls, navIds, listRandomKey))
						.addUrl(targetUrls.toArray(new String[] {})).setDownloader(listDownloader)
						.thread(collectionStrategy.getThreadNum()).run();
				// 异常的navId，由processor放入存储
				List<Long> expNavIds = InformationCache.getCache().get(listRandomKey);
				if (null != expNavIds && (!expNavIds.isEmpty())) {
					for (Long expNavId : expNavIds) {
						navIds.remove(expNavId);
					}
					// 更新为异常状态
					batchUpdateNaviState(expNavIds, 4);
				}
				// 更新为已爬取正常状态
				batchUpdateNaviState(navIds, 3);
			}
		} catch (Exception e) {
			logger.error("ListItratorThread [run Spider] exception ,more exception information:"
					+ AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
			batchUpdateNaviState(navIds, 4);
		} finally {
			InformationCache.getCache().remove(listRandomKey);
			destoryDownloader(listDownloader);
		}
	}

	private void batchUpdateNaviState(List<Long> navIds, int state) {
		if(navIds!=null && navIds.size()>0){
		StringBuilder navIdsStr = new StringBuilder();
		for (Long navId : navIds) {
			navIdsStr.append(",").append(navId);
		}
		spdierService.batchUpdateNaviState(navIdsStr.substring(1), state);
		}
	}

	private void destoryDownloader(Downloader downloader) {
		if ((downloader instanceof AdvancedSeleniumDownloader) && (null != downloader)) {
			((AdvancedSeleniumDownloader) downloader).destoryWebDriverPool();
		}
	}
}
