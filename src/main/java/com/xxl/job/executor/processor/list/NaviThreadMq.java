package com.xxl.job.executor.processor.list;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.Application;
import com.alibaba.fastjson.JSON;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.model.Dbwriteinfo;
import com.ustcinfo.ptp.yunting.service.IDBwriteInfo;
import com.ustcinfo.ptp.yunting.service.IrocketMqconsumer;
import com.ustcinfo.ptp.yunting.service.impl.DbwriteInofImpl;
import com.ustcinfo.ptp.yunting.service.impl.RocketMqConsumerImpl;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumWebDriverPool;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.ChromeWebDriverPool;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.DownloaderFactory;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.PhantomJSWebDriverPool;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.ExceprRepertory;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.LoggerBean;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.WebDriverHelper;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.utils.UrlUtils;

public class NaviThreadMq implements Runnable {

	private DefaultMQPushConsumer consumer = null;
	private IrocketMqconsumer irocketMqconsumer = null;
	private IDBwriteInfo bwriteInfo = null;
	private String mqCName = "";

	private Integer classif = 1;
	private String types = "";

	private boolean isOk = false;

	private static final String WEBDRIVER_PHANTOMJS = "phantomjs";
	private AtomicInteger loginFailedTimes = new AtomicInteger(0);

	private String parentUrl ;
	List<String> linkList = new ArrayList<String>();
	private AtomicInteger currentPageNo = new AtomicInteger(1);

	private Site sitecok = Site.me().setRetryTimes(2).setSleepTime(1000).setTimeOut(30 * 1000);
    private boolean  isFor=true;

	public NaviThreadMq(String mqCName, Integer classif, String types) {
		super();
		// TODO Auto-generated constructor stub
		irocketMqconsumer = new RocketMqConsumerImpl();
		this.mqCName = mqCName;
		this.classif = classif;
		this.types = types;
		irocketMqconsumer = new RocketMqConsumerImpl();
		bwriteInfo = new DbwriteInofImpl();
	}

	public void getMessage() {
		try {

			ObjectPase.setGetLoggBean("开始扫描导航队列", this.getClass().getName(), "info");
			isOk=true;
			List<String> topList = StringHas.getTopicName(types, classif, "NAVI");
		
			consumer = irocketMqconsumer.getconsumer(mqCName, consumer);
			for (String top : topList) {
				consumer.subscribe(top, "*");
			}
			consumer.registerMessageListener(new MessageListenerConcurrently() {
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,
						ConsumeConcurrentlyContext consumeConcurrentlyContext) {

					try {// 判断redis
						JedisUtils.hget("test");

						for (MessageExt me : list) {
							Dbwriteinfo dbwriteinfo = null;
							LoggerBean loggerBean = new LoggerBean();
							String topValue ="";
							try {
								topValue = new String(me.getBody());
								printLog("开始爬取导航爬虫，数据为：" + topValue + ",开始时间为：" + StringHas.getDateNowStr(), "info","");
								Long startTime = System.currentTimeMillis();
								// 爬取数据
								//獲取任務
								dbwriteinfo = ObjectPase.getDbwriteinfo(topValue, classif+"");
								com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(topValue);
								String siteId = StringHas.getjsonValue(jsonObject, "siteId");
								String url = StringHas.getjsonValue(jsonObject, "url");
								//添加到缓存
								JedisUtils.hset("nav_"+UrlUtils.getDomain(url), url, siteId);
								//JedisUtils.setex("nav_"+UrlUtils.getDomain(url), 12*60);
								//爬取数据
								
								loggerBean.setOffSet("1");
								loggerBean.setTargetUrlNum("1");
								loggerBean.setTimeconsume("");
								loggerBean.setSuccess("");
								loggerBean.setMsg("导航任务开始执行：执行时间为："+ StringHas.getDateNowStr());
								ObjectPase.logger(dbwriteinfo,loggerBean ,this.getClass().getName(), "info");
								dbwriteinfo = sendMessage(dbwriteinfo,url,siteId);
								Long endTime = System.currentTimeMillis();
								String times="";
								times=(endTime - startTime) + "";
								loggerBean.setOffSet("2");
								loggerBean.setTargetUrlNum("1");
								loggerBean.setTimeconsume(times);
								loggerBean.setSuccess("true");
								loggerBean.setMsg("导航任务执行结束：结束时间为："+ StringHas.getDateNowStr() + "，执行历时为：" + times + "毫秒");
								ObjectPase.logger(dbwriteinfo,loggerBean ,this.getClass().getName(), "info");
								//根据cron计算下次调度执行时间,并放到redis中
								// 根据cron计算下次调度执行时间,并放到redis中
								if(dbwriteinfo.getCorn()!=null&&!"".equals(dbwriteinfo.getCorn())) {
									Date nextTime = ObjectPase.getNextScheduleTime(dbwriteinfo.getCorn());
									if (nextTime != null) {
										String dateTime = new SimpleDateFormat("yyyyMMddHHmm").format(nextTime);
										JSONObject jsonObject2 = new JSONObject();
										jsonObject2.put("sourceType", dbwriteinfo.getInfoSourceType());
										jsonObject2.put("spiderType", classif);
										jsonObject2.put("task", topValue);
										
										loggerBean.setOffSet("");
										loggerBean.setTargetUrlNum("");
										loggerBean.setTimeconsume("");
										loggerBean.setSuccess("");
										loggerBean.setMsg("下次任务开始时间为：" + dateTime + ",队列名称为：QW_" + dateTime);
										System.out.println("下次任务开始时间为：" + dateTime + ",队列名称为：QW_" + dateTime);
										ObjectPase.logger(dbwriteinfo,loggerBean ,this.getClass().getName(), "info");
										JedisUtils.lpush("QW_" + dateTime, jsonObject2.toString());

									}
								}
								DequeOuts.getNaviTaskSucckAi().incrementAndGet();
								//tasSucckAi.incrementAndGet();
								//删除缓存
								JedisUtils.del("nav_"+UrlUtils.getDomain(url),url);
								Thread.sleep(1000);
							} catch (Exception e) {
								if(dbwriteinfo.getCorn()!=null&&!"".equals(dbwriteinfo.getCorn())) {
									Date nextTime = ObjectPase.getNextScheduleTime(dbwriteinfo.getCorn());
									if (nextTime != null) {
										String dateTime = new SimpleDateFormat("yyyyMMddHHmm").format(nextTime);
										JSONObject jsonObject2 = new JSONObject();
										jsonObject2.put("sourceType", dbwriteinfo.getInfoSourceType());
										jsonObject2.put("spiderType", classif);
										jsonObject2.put("task", topValue);
										
										loggerBean.setOffSet("");
										loggerBean.setTargetUrlNum("");
										loggerBean.setTimeconsume("");
										loggerBean.setSuccess("");
										loggerBean.setMsg("下次任务开始时间为：" + dateTime + ",队列名称为：QW_" + dateTime);
										ObjectPase.logger(dbwriteinfo,loggerBean ,this.getClass().getName(), "info");
										JedisUtils.lpush("QW_" + dateTime, jsonObject2.toString());

									}
								}
								JedisUtils.del("nav_"+UrlUtils.getDomain(dbwriteinfo.getUrl()),dbwriteinfo.getUrl());
								
								DequeOuts.getNaviTaskFailkAi().incrementAndGet();
								//tasFailkAi.incrementAndGet();
								printLog(AllErrorMessage.getExceptionStackTrace(e), "error", "");
								// 如果异常则进行2次扫描
								if (me.getReconsumeTimes() > 5) {
									if (dbwriteinfo.getFailNum() != null) {
										int numfail = Integer.parseInt(
												(dbwriteinfo.getFailNum().equals("") ? "0" : dbwriteinfo.getFailNum()));
										numfail = numfail + 1;
										dbwriteinfo.setFailNum(numfail + "");
									} else {
										dbwriteinfo.setFailNum("1");
									}
									dbwriteinfo.setState("0");
									bwriteInfo.store(dbwriteinfo);
									return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
								}
								return ConsumeConcurrentlyStatus.RECONSUME_LATER;
							}

						}

					} catch (Exception e) {
						
						printLog("redis异常" + AllErrorMessage.getExceptionStackTrace(e), "error", "");
						isOk = false;
						if (consumer != null) {
							consumer.shutdown();
						}
						return ConsumeConcurrentlyStatus.RECONSUME_LATER;

					}

					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}
			});
			consumer.start();

		} catch (Exception e) {
			
			printLog("mq异常：" + AllErrorMessage.getExceptionStackTrace(e), "error", "");

		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while(isFor) {
				try {
					JedisUtils.hget("test");
					if (!isOk) {
						getMessage();
						isOk = true;
					}
				} catch (Exception e) {
					
					isOk = false;
					if (!isOk) {
						if (consumer != null) {
							consumer.shutdown();
							consumer = null;
						}
					}

				}
				
				Thread.sleep(1000l*60l*5l);
			}
			
		} catch (Exception e) {
			
		}

	}

	/**
	 * 发送消息准备爬取数据
	 * @param json
	 * @throws Exception
	 */
	public Dbwriteinfo sendMessage(Dbwriteinfo dbwriteinfo,String url,String siteId) throws Exception {
	    /*
	     *解 队列信息
	     */
		//Dbwriteinfo dbwriteinfo=null;
		//AcqPageRule acqPageRule = null;
		//CollectionSite site = null;
		JSONObject pageRuleObj = new JSONObject();
		JSONArray lists = null;
		CollectionStrategy collectionStrategy = null;
		LoggerBean  loggerBean = new LoggerBean();
		String pagecontent="";
		try {
			loggerBean.setOffSet("");
			loggerBean.setTargetUrlNum("");
			loggerBean.setTimeconsume("");
			loggerBean.setSuccess("");
		
		    String id = dbwriteinfo.getId(); 
			//String type =dbwriteinfo.getType(); 
	
			List<JSONArray> stageDataList = new ArrayList<JSONArray>();
			if (siteId != null && !"".equals(siteId)) {
				//从redis中获取站点页面规则    
				pagecontent = ObjectPase.getacqPageRuleStr(siteId, null);
				loggerBean.setMsg("从redis中获取站点规则siteId:" + siteId);
				ObjectPase.logger(dbwriteinfo,loggerBean ,this.getClass().getName(), "info");
				if(pagecontent==null || "".equals(pagecontent)) {
					loggerBean.setMsg("站点规则为空");
					ObjectPase.logger(dbwriteinfo,loggerBean ,this.getClass().getName(), "info");
					ExceprRepertory.setExcep(5, "");
				}
				
				/*
				 * 采集策略
				 */
				collectionStrategy = ObjectPase.getCollectionStrategy(siteId, null);
				loggerBean.setMsg("从redis中获取站点采集策略siteId:" + siteId);
				ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
				
				JSONObject jsonObjPageRule = new JSONObject(pagecontent);
				JSONArray stageArray = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_XPATH_JSON);
				for (int i = 0; i < stageArray.length(); i++) {
					if (Constants.STAGE_FLAG_LT
							.equalsIgnoreCase(stageArray.getJSONObject(i).getString(JsonKeys.ZCD_STAGE_FLAG))) {
						lists = stageArray.getJSONObject(i).getJSONArray(JsonKeys.ZCD_STAGE_DATA);
					}
				}

				if (null == lists) {
					//日志
					collectionStrategy = ObjectPase.getCollectionStrategy(siteId, null);
					loggerBean.setMsg("没获取到站点信息,siteId:" + siteId);
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "error");
					
					ExceprRepertory.setExcep(5, "");
				}
				
				List<String> stageFlagList = new ArrayList<String>();
				for (int i = 0; i < stageArray.length(); i++) {
					stageFlagList.add(((JSONObject) stageArray.get(i)).getString(JsonKeys.ZCD_STAGE_FLAG));
					stageDataList.add(((JSONObject) stageArray.get(i)).getJSONArray(JsonKeys.ZCD_STAGE_DATA));
				}
				
				int endLoopIndex = stageFlagList.size() - 1;
				//登录
				if (Constants.STAGE_FLAG_LOGIN.equalsIgnoreCase(stageFlagList.get(0))) {
					
					//startLoopIndex++;
					loggerBean.setMsg("开始登陆");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
					login(stageDataList.get(0),collectionStrategy);
					loggerBean.setMsg("登录结束");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
				}
				//列表或者表格流程中的详情处理代码区块
				if (Constants.STAGE_FLAG_DETAIL.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
					loggerBean.setMsg("开始构建，列表或者表格流程中详情");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
					JSONArray details = buildDetailsJson(stageDataList.get(endLoopIndex));
					pageRuleObj.put(JsonKeys.ZCD_FIELD_JSON, details);
					endLoopIndex--;
					
					loggerBean.setMsg("构建列表或者表格流程中详情结束");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
				}
				//列表或者表格流程中的下一页处理流程
				if (Constants.STAGE_FLAG_NEXTPAGE.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
					loggerBean.setMsg("开始处理翻页规则");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					JSONArray pageJsonArray = buildNextPageJson(stageDataList.get(endLoopIndex));
					pageRuleObj.put(JsonKeys.ZCD_PAGE_JSON, pageJsonArray);
					endLoopIndex--;
					loggerBean.setMsg("处理翻页规则结束");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
				} else {
					JSONObject nextPageJson = new JSONObject();
					nextPageJson.put(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE, false);
					JSONArray pageJsonArray = new JSONArray();
					pageJsonArray.put(nextPageJson);
					pageRuleObj.put(JsonKeys.ZCD_PAGE_JSON, pageJsonArray);
				}
				//列表或者表格页
				if (Constants.STAGE_FLAG_LT.equalsIgnoreCase(stageFlagList.get(endLoopIndex))) {
					loggerBean.setMsg("开始处理列表或者表格页规则");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
					JSONArray lists2 = buildListsJson(stageDataList.get(endLoopIndex));
					pageRuleObj.put(JsonKeys.ZCD_LINK_JSON, lists2);
					endLoopIndex--;
					loggerBean.setMsg("处理列表或者表格页规则结束");
					ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
					
					
				}
				
			
				String urls[]=StringHas.getSiteUrls(url);
				//多url爬取
				for(String url1:urls) {
					if(url1.equals(url)) { //检查url是否是重新生成的
						dbwriteinfo.setIsWrite("0");// 1 回写 0 不回写
					}else {
						dbwriteinfo.setIsWrite("1");
					}
					dbwriteinfo.setUrl(url1);
					List<String> targetUrls = new ArrayList<String>();
					List<String> navIds = new ArrayList<String>();
					targetUrls.add(url1);
					navIds.add(id);
					dbwriteinfo = runSpider(stageArray, targetUrls, navIds, dbwriteinfo, siteId ,pageRuleObj);
					
					// 保存
					if(dbwriteinfo.getIsWrite().equals("1")) {
						bwriteInfo.store(dbwriteinfo);
					}
				}
				
				
				

			} else {
				loggerBean.setMsg("队列信息不正确，站点id为空");
				ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "error");
				
				ExceprRepertory.setExcep(5, "");
			}
			return dbwriteinfo;
		} catch (Exception e) {
			
			dbwriteinfo.setState("0");
			if(dbwriteinfo.getFailNum()!=null ) {
				int numfail= Integer.parseInt((dbwriteinfo.getFailNum().equals("")?"0":dbwriteinfo.getFailNum()));
				numfail = numfail+1;
				dbwriteinfo.setFailNum(numfail+"");
			}else {
				dbwriteinfo.setFailNum("1");
			}
			throw e;
			
		}

	}

	// 为避免局部异常被忽略，更新state=3导致数据缺失，在保证数据库acq_details数据与redis数据一致的前提下，使用内存存储异常navId，将每批出错的navId状态置为4
	private Dbwriteinfo runSpider(JSONArray lists, List<String> targetUrls, List<String> navIds,Dbwriteinfo dbwriteinfo,
			String siteId,JSONObject pageRuleObj) throws Exception {
		String listRandomKey = UUID.randomUUID().toString();
		Downloader listDownloader = null;
		//AdvancedSeleniumDownloader asDownloader = null;
		CollectionSite site = null;
		CollectionStrategy collectionStrategy = null;
		LoggerBean  loggerBean = new LoggerBean();
		try {
			collectionStrategy = ObjectPase.getCollectionStrategy(siteId, null);
			boolean useProxy = (collectionStrategy.getUseProxy() == 0) ? true : false;
			listDownloader = DownloaderFactory.createDownloader(collectionStrategy.getFlag(), useProxy);
			site =   ObjectPase.getCollectionSite(siteId, null);
			loggerBean.setMsg("从redis中获取站点采集策略siteId:" + siteId);
			ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
			
			loggerBean.setMsg("runSpider 导航开始爬取数据，siteId:" + site.getId());
			ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
			
			//ListPageProcessorForScan forScan = new ListPageProcessorForScan(lists, site, null, targetUrls, navIds,
			//		listRandomKey);
			pageRuleObj.put(JsonKeys.ZCD_TASK_LINK, targetUrls.get(0));
			
			CusdPageProcessorNavi cusdPageProcessorNavi = new CusdPageProcessorNavi(listDownloader,  pageRuleObj, collectionStrategy,sitecok, 0, "");
			cusdPageProcessorNavi.setTypes(classif);
			cusdPageProcessorNavi.setConSite(site);
			cusdPageProcessorNavi.setListRandomKey(listRandomKey);
			
			Spider.create(cusdPageProcessorNavi)
						  .addUrl(targetUrls.toArray(new String[] {}))
			              .setDownloader(listDownloader)
					      .thread(collectionStrategy.getThreadNum())
					      .run();

			// 成功的集和，由processor放入存储
			List<Long> expNavIds = InformationCache.getCache().get(listRandomKey);
			if (null != expNavIds && (!expNavIds.isEmpty())) {
				dbwriteinfo.setState("1");
			} else {
				dbwriteinfo.setState("0");
				if(dbwriteinfo.getFailNum()!=null ) {
					int numfail= Integer.parseInt((dbwriteinfo.getFailNum().equals("")?"0":dbwriteinfo.getFailNum()));
					numfail = numfail+1;
					dbwriteinfo.setFailNum(numfail+"");
				}else {
					dbwriteinfo.setFailNum("1");
				}
				ExceprRepertory.setExcep(6, "本次未爬取页面");
				

			}
			
			loggerBean.setMsg( "runSpider 导航爬取数据结束，siteId:" + site.getId());
			ObjectPase.logger(dbwriteinfo, loggerBean, this.getClass().getName(), "info");
			
		}catch (Exception e) {
			
			throw e;
		} finally {
			InformationCache.getCache().remove(listRandomKey);
			destoryDownloader(listDownloader);
		}
		return dbwriteinfo;
	}


	private void destoryDownloader(Downloader dtlDownloader) {
		if ((dtlDownloader instanceof AdvancedSeleniumDownloader)
				&& (null != dtlDownloader)) {
			((AdvancedSeleniumDownloader) dtlDownloader).destoryWebDriverPool();
		}
	}


	public void setSiteList(String json) {
		// 获取站点信息次数
		com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(json);
		List<Map<String, Object>> listSites = DequeOuts.getStaticlist();
		Map<String, Object> mapnew = new HashMap<String, Object>();
		String siteId = StringHas.getjsonValue(jsonObject, "siteId");
		if (listSites.size() > 0) {
			int i = 0;
			for (Map<String, Object> map : listSites) {
				i++;
				if (null!=map.get(siteId)) {
					String nums = map.get(siteId) + "";
					Long num = Long.parseLong(nums);
					map.put(siteId, num + 1);
					break;
				} else if (i == listSites.size()) {

					mapnew.put(siteId, 1);
					listSites.add(mapnew);
					break;
				}
			}
		} else {
			mapnew.put(siteId, 1);
			listSites.add(mapnew);
		}

		DequeOuts.setStaticlist(listSites);
	}

	private JSONArray buildListsJson(JSONArray jsonArray) {
		JSONArray lists = new JSONArray();
		for (int k = 0; k < jsonArray.length(); k++) {
			JSONObject listsJsonObj = new JSONObject();
			if(Constants.FIELD_OPERATION_CLICK.equalsIgnoreCase(jsonArray.getJSONObject(k).getString(JsonKeys.ZCD_FIELD_NAME))){
				//acqProcessLAD=true;
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
			nextPageJson.put(JsonKeys.ZCD_FIELD_REG,jsonArray.getJSONObject(i).getString(JsonKeys.ZCD_FIELD_REG));
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
	
	/**
	 * 登录
	 * @param fieldArray
	 * @throws Exception
	 */
	private void login(JSONArray fieldArray,CollectionStrategy collectionStrategy) throws Exception {
		String verificationCode = null;
		WebElement loginButton = null;
		Set<Cookie> cookies = null;
		WebDriver webDriver = getWebDriver(collectionStrategy);
		// go though each step
		{
			for (int j = 0; j < fieldArray.length(); j++) {
				// find the element
				WebElement element = findElement(fieldArray.getJSONObject(j),webDriver);
				//operation
				String elementOperation = fieldArray.getJSONObject(j).getString(JsonKeys.ZCD_FIELD_NAME);
				String elementSendKey = fieldArray.getJSONObject(j).getString(JsonKeys.ZCD_FIELD_VALUE);
				// execute operation
				if (Constants.FIELD_OPERATION_INPUT.equalsIgnoreCase(elementOperation)) {
					element.sendKeys(elementSendKey);
				} else if (Constants.FIELD_OPERATION_CLICK.equalsIgnoreCase(elementOperation)) {
					loginButton = element;
					clickElement(element,webDriver );
				} else if (Constants.FIELD_OPERATION_GETVERICODE.equalsIgnoreCase(elementOperation)) {
					recognizeVerificationCode(element.getAttribute("src"));
				} else if (Constants.FIELD_OPERATION_SENDVERICODE.equalsIgnoreCase(elementOperation)) {
					element.sendKeys(verificationCode);
				} else {
					throw new Exception("Unrecognized operation type : " + elementOperation);
				}
			}
			Thread.sleep(5l*1000l);
			//login failed
			if(null!=loginButton && loginButton.isDisplayed())
			{
				//logger.info(" #CUS# page processor [login stage], login failed for "+loginFailedTimes.get()+" times.");
				XxlJobLogger.log(" #CUS# page processor [login stage], login failed for "+loginFailedTimes.get()+" times.");
				if(loginFailedTimes.getAndIncrement()<3){
					//logger.info( "#CUS# page processor [login stage], retry to login.");
					XxlJobLogger.log(" #CUS# page processor [login stage], retry to login.");
					//retry to login
					login(fieldArray,collectionStrategy);
				}
				else{
					throw new Exception("Login failed, please verify login data.");
				}
			}
			cookies = webDriver.manage().getCookies();
			 for (Cookie cookie : cookies) { 
		            this.sitecok.addCookie(cookie.getDomain(),cookie.getName(),cookie.getValue());
		        }
			 this.sitecok.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1");
		}
	}
	
	private String recognizeVerificationCode(String veriCodeImgUrl) throws IOException {
		String path = Application.getFilePath();
		File filed = new File(path);
		if (!filed.exists()) {
			filed.mkdir();
		}
		String imgFileName = System.currentTimeMillis() + ".png";
		String storePath = path + imgFileName;
		InputStream in = WebDriverHelper.saveFile(veriCodeImgUrl);
		
		FileOutputStream fot =null;
		try {
			File file = new File(storePath);
			fot= new FileOutputStream(file);
			int l = -1;
			byte[] tmp = new byte[1024];
			while ((l = in.read(tmp)) > 0) {
				fot.write(tmp, 0, l);
			}
			fot.flush();
			fot.close();
			return null;
		} catch (Exception e) {
			
			
		
		} finally {
			if(null != fot) {
				fot.close();
			}
			if (null != in) {
				try {
					in.close();
					
					
				} catch (IOException e) {
					
				}
			}
		}
		return null;
	}
	
	private WebElement findElement(JSONObject stageStep,WebDriver webDriver) throws Exception {
		String elementId = stageStep.getString(JsonKeys.ZCD_FIELD_ID);
		String elementName = stageStep.getString(JsonKeys.ZCD_FIELD_INPUT);
		String elementXpath = stageStep.getString(JsonKeys.ZCD_FIELD_XPATH);
		String elementCss = stageStep.getString(JsonKeys.ZCD_FIELD_CLASS);
		return WebDriverHelper.findElement(webDriver,elementId,elementXpath,elementName,elementCss);
	}
	/**
	 * 在初始程序时，需要先获取到第一个列表的所有有效链接
	 * 如果页面链接有href属性，则直接获取，如果没有，则执行点击操作后，在下一个页面或者页面url
	 * @param element
	 */
	private void clickElement(WebElement element,WebDriver webDriver) {
		// maximize the window
		webDriver.manage().window().maximize();
		// if dtl element has href attribute
		if (WebDriverHelper.hrefIsValid(element)) {
			// add the href to target urls
			String href = element.getAttribute("href");
			href = StringHas.getUrl(parentUrl,href) ;
			if(!JedisUtils.isExisted(href)){
				linkList.add(href);
				JedisUtils.set(href, href);
			}
			// log
			
			//logger.info(" #CUS# page processor [get detail page url], current pageNo: " + currentPageNo
			//		+ ", gets target url: " + element.getAttribute("href"));
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
		    printLog("page processor [get detail page url], current pageNo: " +currentPageNo+", "
		    		+ "gets target url: " + webDriver.getCurrentUrl(), "info", webDriver.getCurrentUrl());
		
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
	
	public WebDriver getWebDriver(CollectionStrategy collectionStrategy) {
		try {
			AdvancedSeleniumWebDriverPool webDriverPool= null;
			webDriverPool=checkInit(webDriverPool,collectionStrategy);
			return webDriverPool.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			
			return null;
		}
	
	}

	private AdvancedSeleniumWebDriverPool checkInit(AdvancedSeleniumWebDriverPool webDriverPool,
			CollectionStrategy collectionStrategy) {
		boolean useProxy = (collectionStrategy.getUseProxy() == 0) ? true : false;
		if (webDriverPool == null) {
			synchronized (this) {
				if (AdvancedSeleniumDownloader.getWEBDRIVER_TYPE().equalsIgnoreCase(WEBDRIVER_PHANTOMJS)) {
					webDriverPool = new PhantomJSWebDriverPool(collectionStrategy.getFlag(), true,
							AdvancedSeleniumDownloader.getSELENIUM_PATH(), useProxy);
				} else {
					webDriverPool = new ChromeWebDriverPool(collectionStrategy.getFlag(),
							AdvancedSeleniumDownloader.getSELENIUM_PATH(), useProxy);
				}
			}
		}
		return webDriverPool;
	}
	
	
	

	public void printLog(String msg, String logType, String url) {
		ObjectPase.setGetLoggBean("", url, 0, "1", "1", "", "1", "", "", msg, this.getClass().getName(), logType);
	}

}
