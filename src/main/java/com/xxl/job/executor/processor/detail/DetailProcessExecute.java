package com.xxl.job.executor.processor.detail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.model.Dbwriteinfo;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;
import com.ustcinfo.ptp.yunting.service.IDBwriteInfo;
import com.ustcinfo.ptp.yunting.service.impl.ConllectionInfoStoreByRedisImpl;
import com.ustcinfo.ptp.yunting.service.impl.DbwriteInofImpl;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.DownloaderFactory;
import com.ustcinfo.tpc.framework.core.util.Md5;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.ExceprRepertory;
import com.xxl.job.executor.util.HttpClient4;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.LoggerBean;
import com.xxl.job.executor.util.LoggerUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.ProcessRule;
import com.xxl.job.executor.util.ProxyHelper;
import com.xxl.job.executor.util.RegMatcherUtil;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.YuntingRedisScheduler;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.UrlUtils;

/**
 * 目标爬虫
 * @author huangyakun
 *
 */
public class DetailProcessExecute {
	
	private YuntingRedisScheduler redisScheduler = null;
	private ICollectionInfoStore collectionInfoStoreService = null;
	private IDBwriteInfo bwriteInfo = null;
	private String fileName = "META-INF/res/resource-development.properties";
	private LoggerBean loggerBean = new LoggerBean();
	private int infoSourceType = 0;
	private String tag = "|";
	private int type = 0;
	 

	public DetailProcessExecute() {
		super();
		initRedisScheduler();
		collectionInfoStoreService = new ConllectionInfoStoreByRedisImpl();
		bwriteInfo = new DbwriteInofImpl();
		type = 2;
	}

	/**
	 * 初始化获取redis
	 */
	public void initRedisScheduler() {

		String redisHost = ReadProperties.readProperties(fileName, "redisHost");
		redisScheduler = new YuntingRedisScheduler(redisHost);
	}

	/**
	 * 
	 * @param topicValue 待爬取数据
	 * @param classif 爬虫类型
	 * @return  执行结果
	 * @throws Exception 返回异常
	 */
    public boolean crawling(String topicValue, String classif) throws Exception {
    	String siteId = "";
    	String url = "";
   
		printLog("获取详情队列数据为：" + topicValue + ",开始时间为：" + StringHas.getDateNowStr(), "info", "", null);
		Long startTime = System.currentTimeMillis();
		com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(topicValue);
		siteId = StringHas.getjsonValue(jsonObject, "siteId");
		url = StringHas.getjsonValue(jsonObject, "url");
		// 开始时间
		Dbwriteinfo dbwriteinfo = null;
		if (JedisUtils.sismember("set_" + UrlUtils.getDomain(url), url)) {
			// redis中已经存在，
			printLog("详情url为：" + url + ", 已存在redis中", "info", "", null);
			return true;
		}
		// 獲取任務
		dbwriteinfo = ObjectPase.getDbwriteinfo(topicValue, classif + "");
		infoSourceType = dbwriteinfo.getInfoSourceType();
		// 添加缓存
		String content= "详情任务开始执行：执行时间为：" + StringHas.getDateNowStr()  ;
		/************************************ 日志打印*********************************************************/
		String msg = url + tag + siteId + tag + type + tag + infoSourceType + tag + "info"+ tag + content + tag+ "1" + tag + tag + tag;
		loggerBean = ObjectPase.setLoggBean(msg);
		loggerBean.setOffSet("1");
		LoggerUtils.info(loggerBean);
		/******************************************************************************************/
		// 爬取数据
		try {
			runspider(url, siteId);
			dbwriteinfo.setState("1");
			bwriteInfo.store(dbwriteinfo);
		} catch (Exception e) {
			JedisUtils.srem("set_" + UrlUtils.getDomain(url), url);
			throw e;
		}

		// 结束时间
		Long endTime = System.currentTimeMillis();
		String times = "";
		times = (endTime - startTime) + "";
		// 打印日志
		content = "详情任务执行结束：结束时间为：" + StringHas.getDateNowStr() + "，执行历时为："  + times + "毫秒" ;
		msg = url + tag + siteId + tag + type + tag + infoSourceType + tag + "info" + tag+ content + tag+ "1"  + tag + tag + tag;
		loggerBean = ObjectPase.setLoggBean(msg);
		loggerBean.setTimeconsume(times);
		loggerBean.setOffSet("2");
		loggerBean.setSuccess("true");
		LoggerUtils.info(loggerBean);
		setSiteList(topicValue);
		return true;
   }

	/**
	 * 
	 * @param url url
	 * @param siteId 站点id
	 * @param infoSourceType 信源类型
	 * @throws Exception 异常
	 */

	public void runspider(String url, String siteId) throws Exception {
		try {
			org.json.JSONObject pageRuleObj = new org.json.JSONObject();
			String pagecontent = "";
			LoggerBean loggerBean = new LoggerBean();
            String content = "";
			if (siteId != null && !"".equals(siteId)) {
				content = "从redis中获取站点规则siteId:" + siteId;
				String msg = url + tag + siteId + tag + type + tag + infoSourceType + tag + "info" + tag + content + siteId + tag+ "" + tag + tag + tag;
				loggerBean = ObjectPase.setLoggBean(msg);
				loggerBean.setTargetUrlNum("1");
				LoggerUtils.info(loggerBean);
				// 获取页面规则
				pagecontent = ObjectPase.getacqPageRuleStr(siteId, null);
				if (pagecontent == null || "".equals(pagecontent)) {
					loggerBean.setMsg("站点规则为空");
					LoggerUtils.info(loggerBean);
					ExceprRepertory.setExcep(5, "");
				}
			} else {
				printLog("队列信息不正确，站点id为空", "error", url, null);
				ExceprRepertory.setExcep(5, "");
			}
			// 组合规则
			org.json.JSONObject jsonObjPageRule = new org.json.JSONObject(pagecontent);
			JSONArray stageArray = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_XPATH_JSON);
			List<String> stageFlagList = new ArrayList<>();
			List<JSONArray> stageDataList = new ArrayList<>();
			for (int i = 0; i < stageArray.length(); i++) {
				stageFlagList.add(((org.json.JSONObject) stageArray.get(i)).getString(JsonKeys.ZCD_STAGE_FLAG));
				stageDataList.add(((org.json.JSONObject) stageArray.get(i)).getJSONArray(JsonKeys.ZCD_STAGE_DATA));
			}
			// 列表或者表格流程中的详情处理代码区块
			if (Constants.STAGE_FLAG_DETAIL.equalsIgnoreCase(stageFlagList.get(stageFlagList.size() - 1))) {
				loggerBean.setMsg("DetailProcessThread [处理详情规则], starts to build detail info,siteId:" + siteId);
				LoggerUtils.info(loggerBean);
				JSONArray details = ProcessRule.buildDetailsJson(stageDataList.get(stageFlagList.size() - 1));
				pageRuleObj.put(JsonKeys.ZCD_FIELD_JSON, details);
			}
			loggerBean = null;
			processDetailPages(url, siteId, pageRuleObj, infoSourceType);
		} catch (Exception e) {
			throw e;

		}

	}



	/**
	 * 爬取过程
	 * @param targetUrls  地址
	 * @param siteId  站点id
	 * @param pageRuleObj 规则
	 * @param infoSourceType 信源类型
	 * @throws Exception   
	 */
	private void processDetailPages(String targetUrls, String siteId, org.json.JSONObject pageRuleObj,
			int infoSourceType) throws Exception {
		
		Downloader dtlDownloader = null;
		String randomKey = Md5.toMD5(targetUrls);
		String content = "";
		CollectionSite collectionSite = null;
		CollectionStrategy collectionStrategy = null;
		
		try {
			content= "从redis中获取站点基本信息siteId:" + siteId;
			String msg = targetUrls + tag + siteId + tag + type + tag + infoSourceType + tag + "info"+ tag+ content + tag+ "" + tag + tag + tag;
			loggerBean = ObjectPase.setLoggBean(msg);
            //获取规则
			collectionSite = ObjectPase.getCollectionSite(siteId, null);
			collectionSite.setSiteUrl(targetUrls);
			LoggerUtils.info(loggerBean);
            //获取策略
			collectionStrategy = ObjectPase.getCollectionStrategy(siteId, null);
			loggerBean.setMsg("从redis中获取站点采集策略siteId:" + siteId);
			LoggerUtils.info(loggerBean);
            //获取代理 
			boolean useProxy = (collectionStrategy.getUseProxy() == 0) ? true : false;
			dtlDownloader = DownloaderFactory.createDownloader(collectionStrategy.getFlag(), useProxy);

			loggerBean.setMsg("开始爬取数据：地址为：" + targetUrls + "爬取时间为：" + StringHas.getDateNowStr());
			LoggerUtils.info(loggerBean);

			// 初始化爬取类
			DetailPageProcessor detailPageProcessor = new DetailPageProcessor(
					pageRuleObj.getJSONArray(JsonKeys.ZCD_FIELD_JSON), randomKey, collectionStrategy);
			detailPageProcessor.setConSite(collectionSite);

			// 开始爬取数据
			Spider.create(detailPageProcessor).setScheduler(redisScheduler).addUrl(targetUrls)
					.thread(collectionStrategy.getThreadNum()).setDownloader(dtlDownloader).run();
			if(targetUrls.equals(collectionSite.getSiteUrl())) {
				// 保存数据 从缓存中取到数据并保存，如果5次取不到则抛出异常。
				saveData(randomKey, targetUrls, msg, collectionSite, 0);
			}else {
				loggerBean.setMsg("开始爬取数据：实例：" + collectionSite.getSiteUrl() + "爬取时间为：" + StringHas.getDateNowStr());
				LoggerUtils.info(loggerBean);
				ExceprRepertory.setExcep(6, "爬取url和站点url不一致！");
			}
			

			loggerBean.setMsg("processDetailPages爬取结束");
			LoggerUtils.info(loggerBean);
			List<HashMap<String, Object>> listError = InformationCache.getCache().get(randomKey + "ERROR");
			// 爬取出现异常
			if (listError != null && listError.size() > 0) {
				ExceprRepertory.setExcep(6, "爬取异常");
			}

		} catch (Exception e) {
			
			JedisUtils.srem("set_" + UrlUtils.getDomain(targetUrls), targetUrls);
			InformationCache.getCache().remove(randomKey);
			throw e;
		
		} finally {

			// remove key from cache
			InformationCache.getCache().remove(randomKey);
			InformationCache.getCache().remove(randomKey + "ERROR");
			// destroy web driver
			destoryDownloader(dtlDownloader);

		}
		// return dbwriteinfo;

	}

	private void destoryDownloader(Downloader dtlDownloader) {
		if ((dtlDownloader instanceof AdvancedSeleniumDownloader) && (null != dtlDownloader)) {
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

	/**
	 * 保存数据 
	 * @param randomKey 
	 * @param targetUrls
	 * @param msg
	 * @param tag
	 * @throws Exception
	 */
	public void saveData(String randomKey, String targetUrls, String msg, CollectionSite collectionSite,int tag) throws Exception {

		LoggerBean loggerBean = new LoggerBean();
		loggerBean = ObjectPase.setLoggBean(msg);
		List<ConcurrentMap<String, Object>> list = InformationCache.getCache().get(randomKey);
		loggerBean.setMsg("DetailProcessThread [获取爬取缓存], page info has been got from cache.");
		LoggerUtils.info(loggerBean);
		
		if (list != null && list.size() > 0 && list.get(0).get(Constants.STORE_PAGE_JSON) != null) {
			InformationCache.getCache().remove(randomKey);
			// 写入out文件
			String uurl = list.get(0).get("url") != null ? list.get(0).get("url") + "" : "";
			try {
				JSONObject pageJsonObj = (org.json.JSONObject) list.get(0).get(Constants.STORE_PAGE_JSON);
				String pageurl = pageJsonObj.get("url") + "";
				if (!pageurl.equals(targetUrls)) {
					loggerBean.setMsg("缓存url：" + pageurl + ",目标url："+ targetUrls + "url不一致！");
					LoggerUtils.info(loggerBean);
					//删除缓存
					JedisUtils.srem("set_" + UrlUtils.getDomain(uurl), uurl);
					JedisUtils.srem("set_" + UrlUtils.getDomain(targetUrls), targetUrls);
					JedisUtils.srem("set_" + UrlUtils.getDomain(pageJsonObj.get("url").toString()),pageJsonObj.get("url").toString());

					ExceprRepertory.setExcep(6, "数据格式不一致");

				}
                //保存
				collectionInfoStoreService.store((org.json.JSONObject) list.get(0).get(Constants.STORE_PAGE_JSON),collectionSite);

			} catch (Exception e) {
				
				InformationCache.getCache().remove(randomKey);
				loggerBean.setMsg("数据存储错误"+AllErrorMessage.getExceptionStackTrace(e));
				LoggerUtils.info(loggerBean);
				
				JedisUtils.srem("set_" + UrlUtils.getDomain(targetUrls), targetUrls);
				ExceprRepertory.setExcep(6, "数据存储错误");
				throw e;
			}

		} else {
			tag++;
			if (tag > 5) {
				InformationCache.getCache().remove(randomKey);
				loggerBean.setMsg("本次未爬取页面");
				
				LoggerUtils.info(loggerBean);
				JedisUtils.srem("set_" + UrlUtils.getDomain(targetUrls), targetUrls);
			
				ExceprRepertory.setExcep(6, "本次未爬取页面");
			} else {
				// 睡眠2秒
				loggerBean.setMsg("未从缓存中获取到数据准备第" + tag + "次重试！");
				LoggerUtils.info(loggerBean);
				Thread.sleep(2000);
				saveData(randomKey, targetUrls, msg, collectionSite, tag);
			}

		}
	}

	/**
	 * 日志
	 * 
	 * @param msg
	 * @param logType
	 */
	public void printLog(String msg, String logType, String url, CollectionSite collectionSite) {
		if (collectionSite != null && collectionSite.getId() != null) {
			ObjectPase.setGetLoggBean(collectionSite.getId() + "", url,
					collectionSite.getInfoSourceType() != null ? collectionSite.getInfoSourceType() : 0, "2", "", "",
					"", "", "", msg, this.getClass().getName(), logType);
		} else {
			ObjectPase.setGetLoggBean("", url, 0, "2", "", "", "", "", "", msg, this.getClass().getName(), logType);
		}

	}

	class DetailPageProcessor implements PageProcessor {
		// private Logger logger = LoggerFactory.getLogger(getClass());
		private static final int SLEEPTIME = 1000 / 2;

		private JSONArray details;

		// private String randomKey;

		// private Downloader dtlDownloader;

		private CollectionSite conSite = null;

		private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME).setTimeOut(30 * 1000);

		private CollectionStrategy collectionStrategy;

		public DetailPageProcessor(JSONArray details, String randomKey, CollectionStrategy collectionStrategy) {

			this.details = details;
			// this.randomKey = randomKey;
			this.collectionStrategy = collectionStrategy;
			setSite();

		}

		public void setSite() {
			// 从数据库中获取代理
			this.site.setUserAgent(
					"User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

			if ((collectionStrategy.getUseProxy() == 0) && collectionStrategy.getFlag() != 0) {
				// 获取ip
				String poxy = ProxyHelper.getProxy();
				// 验证ip
				String[] poxys = poxy.split(":");
				boolean result = HttpClient4.ipTest(poxys[0], Integer.parseInt(poxys[1]), "", "");
				if (result) {
					HttpHost httpHost = new HttpHost(poxys[0], Integer.parseInt(poxys[1]));
					this.site.setHttpProxy(httpHost);
					this.site.setUsernamePasswordCredentials(
							new UsernamePasswordCredentials(ProxyHelper.getUserName(), ProxyHelper.getPassWord()));
				} else {
					setSite();
				}

			}

		}

		@Override
		public void process(Page page) {

			ObjectPase.setGetLoggBean(conSite.getId() + "", page.getUrl().toString(), conSite.getInfoSourceType(), "2",
					"", conSite.getId() + "", "1", "", "",
					"DetailPageProcessor begin , 开始爬取内容 , starttime: " + StringHas.getDateNowStr(),
					this.getClass().getName(), "info");
			
			JSONObject pageJSONObj = new JSONObject();
			String randomKey = Md5.toMD5(page.getUrl().toString());
			
			try {
			
				for (int i = 0; i < details.length(); i++) {
					JSONObject href = (JSONObject) details.get(i);
					if (!pageJSONObj.has(href.getString(JsonKeys.ZCD_FIELD_NAME))) {
						if (pageJSONObj.has(JsonKeys.ZCD_FIELD_TYPE)) {
							continue;
						}
						if (Constants.FIELD_OPERATION_GET.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {

							if (page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()")
									.get() != null) {
								pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME), page.getHtml()
										.xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()").get());
							} else {
								pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME), "");

							}
						} else if (Constants.FIELD_OPERATION_REG.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
							String oriContent = page.getHtml()
									.xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/allText()").get();
							String reg = href.getString(JsonKeys.ZCD_FIELD_REG);
							if (StringUtils.hasText(oriContent)) {
								String fileName = href.getString(JsonKeys.ZCD_FIELD_NAME);
								String newContent = "";
								if (pageJSONObj.has(fileName) && (pageJSONObj.getString(fileName) != null
										&& !"".equals(pageJSONObj.getString(fileName)))) {
									continue;
								} else {
									newContent = RegMatcherUtil.reg(oriContent, reg);
								}
								pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME), newContent);

							} else {
								pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME), "");
							}
						} else if (Constants.FIELD_HTML.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
									page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH)).get());
						} else if(Constants.FIELD_CONSTANTS.equals(href.getString(JsonKeys.ZCD_FIELD_TYPE))) {
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),href.getString(JsonKeys.ZCD_FIELD_VALUE));
						} else {
							pageJSONObj.put(href.getString(JsonKeys.ZCD_FIELD_NAME),
									page.getHtml().xpath(href.getString(JsonKeys.ZCD_FIELD_XPATH) + "/@href").get());
						}
					}

				}
				// contentArray.put(content);
				// System.out.println(contentArray);

				if (!pageJSONObj.has("url")) {
					pageJSONObj.put("url", page.getUrl().toString());
				}
				// store in the cache temporarily
				ConcurrentMap<String, Object> map = new ConcurrentHashMap<String, Object>();
				// HashMap<String, Object> map = new HashMap<String, Object>();
				// map.put(Constants.STORE_PAGE_HTML, page);
				map.put(Constants.STORE_PAGE_JSON, pageJSONObj);
				map.put("url", page.getUrl().toString());
				InformationCache.getCache().put(randomKey, map);
				int num = pageJSONObj.toString().length();
				num = (int) (num * 0.5);

				ObjectPase.setGetLoggBean(conSite.getId() + "", page.getUrl().toString(), conSite.getInfoSourceType(),
						"2", "", conSite.getId() + "", "1", "", "",
						"爬取内容为：" + pageJSONObj.toString().substring(0, num) + "…………", this.getClass().getName(),
						"info");

			} catch (Exception e) {
				
				// 异常队列
				
				InformationCache.getCache().put(randomKey + "ERROR", AllErrorMessage.getExceptionStackTrace(e));
				ObjectPase.setGetLoggBean(conSite.getId() + "", page.getUrl().toString(), conSite.getInfoSourceType(),
						"2", "", conSite.getId() + "", "1", "", "", "爬取异常：" + AllErrorMessage.getExceptionStackTrace(e),
						this.getClass().getName(), "error");
				InformationCache.getCache().remove(randomKey);

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

	}

}
