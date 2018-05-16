package com.xxl.job.executor.service.jobhandler;




import javax.annotation.PostConstruct;

import com.xxl.job.executor.util.StringHas;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.dao.NavigationDaoImpl;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.service.AcqPageRuleService;
import com.ustcinfo.ptp.yunting.service.CollectionSiteService;
import com.ustcinfo.ptp.yunting.service.CollectionStrategyService;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.ExecutorService;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.processor.CUSPageProcessor;
import com.xxl.job.executor.processor.LADPageProcessor;
import com.xxl.job.executor.processor.LOTPageProcessor;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.YuntingRedisScheduler;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

@JobHander(value = "acqJobHandler")
@Service
public class AcqJobHandler extends IJobHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ExecutorService executorService;

	@Autowired
	private AcqPageRuleService acqPageRuleService;

	@Autowired
	private CollectionStrategyService collectionStrategyService;

	@Autowired
	private CollectionSiteService collectionSiteService;

	@Autowired
	private InfoEntityService infoService;

	@Value("${redisHost}")
	private String redisHost;
	
	
	public static String veriCodePath;
	
	@Value("${file.path}")
	private String filePath;

	public static YuntingRedisScheduler redisScheduler;
	
	public DequeOuts DequeOut;

	@PostConstruct
	public void initRedisScheduler() {
		redisScheduler = new YuntingRedisScheduler(redisHost);
		veriCodePath = filePath;
	}
	@Autowired
	private NavigationDaoImpl avigationDaoImpl ;
	
	@Autowired
	private AcqDeatilsDaoImpl  acqDeatilsDaoImpl;

	@Override
	public ReturnT<String> execute(String... params) throws Exception {
		String serialNumber="";
		Integer AC=null;
		String COUNT_PER_TASK =null;//对于大型任务，定义每个任务最多执行多少页,例如：1-1000， 1000-2000
		Integer taskID = null ;
		try {
			DequeOut= new DequeOuts();
			taskID = Integer.valueOf(params[0].replace("taskId:", ""));
			serialNumber = params[1].replace("SerialNum:", "");
			AC = Integer.valueOf(params[2].replace("AC:", ""));
			if(params.length>=4) {
				COUNT_PER_TASK = params[3].replace("CPT:", "");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			XxlJobLogger.log(AllErrorMessage.getExceptionStackTrace(e1));
		}
		
		XxlJobLogger.log("[" + serialNumber + "] acquisition task starts running.");
		logger.info("[" + serialNumber + "] acquisition task starts running.");
		// declare the asDownloader
		AdvancedSeleniumDownloader asDownloader = null;
		try {
			
			AcqPageRule acqPageRule = this.acqPageRuleService
					.findByNamedParam(new String[] { "siteSerialNumber", "isValid" }, new Object[] { serialNumber, 1 })
					.get(0);
			CollectionSite site = this.collectionSiteService.getEntity(acqPageRule.getSiteId());
			CollectionStrategy collectionStrategy = site.getAcqStrategyId() == null
					? new CollectionStrategy(1, 1000, 10000, 1, 0, 1)
					: this.collectionStrategyService.getEntity(site.getAcqStrategyId());
			Long acqProcessId = acqPageRule.getAcqProcessId();
			XxlJobLogger.log("\nSiteName : [" + site.getSiteName() + "]\nPageRuleVersion : [" + acqPageRule.getVersion()
					+ "]\nAcqProcessID : [" + acqPageRule.getAcqProcessId() + "]\nSiteId : [" + site.getId()
					+ "]\nAcqStrategyID : [" + site.getAcqStrategyId() +"]");
			logger.info("\nSiteName : [" + site.getSiteName() + "]\nPageRuleVersion : [" + acqPageRule.getVersion()
			+ "]\nAcqProcessID : [" + acqPageRule.getAcqProcessId() + "]\nSiteId : [" + site.getId()
			+ "]\nAcqStrategyID : [" + site.getAcqStrategyId() +"]");
			logger.info("\nPageRuleContent: " + acqPageRule.getPageRuleContent());
			XxlJobLogger.log("\nPageRuleContent: " + acqPageRule.getPageRuleContent());
			// resolve listPageUrl
			JSONObject jsonObjPageRule = new JSONObject(acqPageRule.getPageRuleContent());

//			String listPageUrl = jsonObjPageRule.getString(JsonKeys.ZCD_TASK_LINK);

			PageProcessor pageProcessor;
			
			boolean useProxy=(collectionStrategy.getUseProxy()==0)?true:false;
			String[] urls = StringHas.getSiteUrls(site.getSiteUrl()) ;
			asDownloader = new AdvancedSeleniumDownloader(urls.length,useProxy);
			// LAD
			if (Constants.PROCESS_ID_LAD == acqProcessId) {
				pageProcessor = new LADPageProcessor(asDownloader, jsonObjPageRule,
						collectionStrategy,infoService,acqPageRule,executorService,null,AC,COUNT_PER_TASK);
			}
			// LOT
			else if (Constants.PROCESS_ID_LOT == acqProcessId){
				pageProcessor = new LOTPageProcessor(asDownloader,
						jsonObjPageRule, collectionStrategy,infoService,acqPageRule,executorService,null,AC);
				
			}
			// CUS
			else {
				CUSPageProcessor cusPageProcessor=new CUSPageProcessor(asDownloader,
				jsonObjPageRule, collectionStrategy,infoService,acqPageRule,executorService,AC,COUNT_PER_TASK,taskID);
				cusPageProcessor.setConSite(site);
				cusPageProcessor.setNavigationDaoImpl(avigationDaoImpl);
				cusPageProcessor.setAcqDeatilsDaoImpl(acqDeatilsDaoImpl);
				pageProcessor = cusPageProcessor;
			}
			Spider.create(pageProcessor).addUrl(urls).thread(collectionStrategy.getThreadNum()).setDownloader(asDownloader).run();
			// end and return
			XxlJobLogger.log("[" + serialNumber + "] acquisition task ends running.");
			logger.info("[" + serialNumber + "] acquisition task ends running.");
			//asDownloader.destoryWebDriverPool();
			return ReturnT.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			XxlJobLogger.log(
					"[" + serialNumber + "] acquisition task encounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			logger.info(
					"[" + serialNumber + "] acquisition task encounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			return ReturnT.FAIL;
		} finally {
			// destroy webdriver
			if (null != asDownloader) {
				asDownloader.destoryWebDriverPool();
				XxlJobLogger.log("[" + serialNumber + "] asDownloader 销毁成功！ \n");
				logger.info("[" + serialNumber + "] asDownloader 销毁成功！ \n");
			}
		}
	}
}