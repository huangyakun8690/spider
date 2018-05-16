package com.xxl.job.executor.util;

import java.util.Date;

import org.springframework.scheduling.support.CronSequenceGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.model.Dbwriteinfo;
import com.ustcinfo.tpc.framework.core.util.GetSystemValue;


public class ObjectPase {
	
	//private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 获取页面规则
	 * @param siteId
	 * @return
	 */
	public static AcqPageRule getacqPageRule(String siteId,String pagecontents) {
	
		AcqPageRule acqPageRule= new AcqPageRule();
		String pagecontent ="";
		if(pagecontents==null ) {
			pagecontent = JedisUtils.hget("site_" + siteId, "pagecontent");
		}else {
			pagecontent = pagecontents;
		}
		
		
		if (pagecontent != null && !"".equals(pagecontent)) {
			acqPageRule.setPageRuleContent(pagecontent);
			if(siteId!=null ) {
				acqPageRule.setSiteId(Long.parseLong(siteId));
			}
			return acqPageRule;
		}else {
			return null;
		}
	}
	
	/**
	 * 获取页面规则
	 * @param siteId
	 * @return
	 */
	public static String getacqPageRuleStr(String siteId,String pagecontents) {
	
		//AcqPageRule acqPageRule= new AcqPageRule();
		String pagecontent ="";
		if(pagecontents==null ) {
			pagecontent = JedisUtils.hget("site_" + siteId, "pagecontent");
		}else {
			pagecontent = pagecontents;
		}
		
		return pagecontent;
		
	}
	
	/**
	 * 获取页面策略
	 * @param siteId
	 * @return
	 */
	public static CollectionStrategy getCollectionStrategy(String siteId,JSONObject jsonStrategy) {
		CollectionStrategy collectionStrategy= new CollectionStrategy();
		String strategy = JedisUtils.hget("site_" + siteId, "strategy");
		if(jsonStrategy==null) {
			 jsonStrategy = JSON.parseObject(strategy);
		}
		//collectionStrategy = JSON.parseObject(strategy, CollectionStrategy.class) ; 
		String threadNum = StringHas.getjsonValue(jsonStrategy, "threadNum");
		String sleepTime = StringHas.getjsonValue(jsonStrategy, "siteSleepTime");
		String timeOut = StringHas.getjsonValue(jsonStrategy, "siteTimeout");
		String SiteRetryTimes = StringHas.getjsonValue(jsonStrategy, "siteRetryTimes");
		String flag = StringHas.getjsonValue(jsonStrategy, "flag");
		String useProxy = StringHas.getjsonValue(jsonStrategy, "useProxy");
		collectionStrategy.setThreadNum(Integer.parseInt(threadNum.equals("") ? "1" : threadNum));
		collectionStrategy.setSiteSleepTime(Integer.parseInt(sleepTime.equals("") ? "1000" : sleepTime));
		collectionStrategy.setSiteTimeout(Integer.parseInt(timeOut.equals("") ? "10000" : timeOut));
		collectionStrategy
				.setSiteRetryTimes(Integer.parseInt(SiteRetryTimes.equals("") ? "1" : SiteRetryTimes));
		collectionStrategy.setFlag(Integer.parseInt(flag.equals("") ? "0" : flag));
		collectionStrategy.setUseProxy(Integer.parseInt(useProxy.equals("") ? "1" : useProxy));
		return collectionStrategy;
	}
	
	/**
	 * 获取站点信息
	 * @param siteId
	 * @return
	 */
	public static CollectionSite getCollectionSite(String siteId,JSONObject collectionj) {
		CollectionSite collectionSite = new CollectionSite();
		if(collectionj==null) {
			String collection= JedisUtils.hget("site_" + siteId,"basicinfo");
			collectionj = JSON.parseObject(collection);
			collectionSite.setId(Long.parseLong(siteId) );
		}
		collectionSite.setExt1(StringHas.getjsonValue(collectionj, "ext1"));
		collectionSite.setInfoSourceType(Integer.parseInt(StringHas.getjsonValue(collectionj, "infoSourceType")));
		collectionSite.setSiteName(StringHas.getjsonValue(collectionj, "siteName"));
		collectionSite.setMediaType(Integer.parseInt(StringHas.getjsonValue(collectionj, "mediaType")));
		collectionSite.setCronExpression(StringHas.getjsonValue(collectionj, "cron"));
		collectionSite.setExt2(StringHas.getjsonValue(collectionj, "ext2"));
		collectionSite.setSiteUrl(StringHas.getjsonValue(collectionj, "siteUrl"));
		collectionSite.setCategoryId(Long.parseLong(StringHas.getjsonValue(collectionj, "categoryId")));
		return collectionSite;
	}
	
	/**
	 * 獲取Dbwriteinfo
	 * @param siteValue
	 * @param classif
	 * @return
	 */
	public static Dbwriteinfo getDbwriteinfo(String siteValue,String classif ) {
		Dbwriteinfo dbwriteinfo = null;
		try {
			com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(siteValue);
			String id = StringHas.getjsonValue(jsonObject, "id");
			String siteId = StringHas.getjsonValue(jsonObject, "siteId");
			String url = StringHas.getjsonValue(jsonObject, "url");
			String type = StringHas.getjsonValue(jsonObject, "type");
			String failNum = StringHas.getjsonValue(jsonObject, "failNum");
			failNum = (failNum == null || failNum.equals("")) ? "0" : failNum;
			String longtime=(System.currentTimeMillis() + (1000l * 60l * 60l * 24l)) + "";
			dbwriteinfo = new Dbwriteinfo(id, siteId, url, "0", type,longtime, "0",GetSystemValue.getDockerId());
			dbwriteinfo.setCategory(classif + "");
			String collection= JedisUtils.hget("site_" + siteId,"basicinfo");
			JSONObject collectionj = JSON.parseObject(collection);
			dbwriteinfo.setInfoSourceType(Integer.parseInt(StringHas.getjsonValue(collectionj, "infoSourceType")));
			dbwriteinfo.setCorn(StringHas.getjsonValue(collectionj, "cron"));
			dbwriteinfo.setFailNum(failNum);
			Date nextTime = getNextScheduleTime(dbwriteinfo.getCorn());
			dbwriteinfo.setPublishTime(nextTime.getTime()+"");
			
		} catch (Exception e) {
			
		}
		return dbwriteinfo;
	}
	
	
	
	// 根据cron计算下次调度执行时间
   public static Date getNextScheduleTime(String cron) {
			CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
			Date nextTimePoint = cronSequenceGenerator.next(new Date());
			return nextTimePoint;
	}
	
	/**
	 * 拼接日志
	 * @param msg
	 * @param className
	 * @param logType
	 */
	public static void logger(Dbwriteinfo dbwriteinfo,LoggerBean loggerBean,String className,String logType){
		setGetLoggBean(dbwriteinfo.getSiteId(),
				       dbwriteinfo.getUrl(),
				       dbwriteinfo.getInfoSourceType(),
				       dbwriteinfo.getType(),
				       loggerBean.getOffSet(),
				       dbwriteinfo.getId(),
				       loggerBean.getTargetUrlNum(),
				       loggerBean.getTimeconsume(),
				       loggerBean.getSuccess(),
				       loggerBean.getMsg(),
				       className,
				       logType) ;
	}
	
	/**
	 * 拼接日志
	 * @param msg
	 * @param className
	 * @param logType
	 */
	public static void setGetLoggBean(String msg,String className,String logType){
		setGetLoggBean("","",0,"","","","","","",msg,className,logType) ;
	}

	
	/**
	 * 拼接日志
	 * @param siteId 站点id
	 * @param url 地址
	 * @param sitenfoSourceType 类型 
	 * @param type  1导航 2详情
	 * @param offset 1、 开始，2 结束
	 * @param id  信源id
	 * @param targetUrlNum 生成目标任务数量
	 * @param timeconsume 任务耗时（结束时计算）
	 * @param success 成功 失败
	 * @param msg 内容
	 * @param className 类名
	 * @param 日志类型
	 * @return
	 */
	public static LoggerBean setGetLoggBean(String siteId,String url,int sitenfoSourceType,
        String type,String offset,String id,String targetUrlNum,String timeconsume,
        String success,String msg,String className,String logType) {
		//1.新闻，2.微博，3.微信，4.APP，5.报刊，6.论坛，7.博客，8.视频，9.商机
		
		String spiderType="";
		switch (sitenfoSourceType) {
		case 1:
			spiderType="news";
			break;
		case 2:
			spiderType="weibo";
			break;
		case 3:
			spiderType="WeChat";
			break;
		case 4:
			spiderType="APP";
			break;
		case 5:
			spiderType="journal";
			break;
		case 6:
			spiderType="forum";
			break;
		case 7:
			spiderType="Blog";
			break;
		case 8:
			spiderType="video";
			break;
		case 9:
			spiderType="shangji";
			break;
		case 10:
			spiderType="shangjia";
			break;
		default:
			break;
		}
		LoggerBean loggerBean = new LoggerBean();
	
		loggerBean.setServerIp(GetSystemValue.getIp());
		loggerBean.setSpiderType(spiderType);
		loggerBean.setLevel(logType);
		loggerBean.setDate(StringHas.getDateNowStr());
		loggerBean.setType(type);
		loggerBean.setOffSet(offset);
		loggerBean.setDockerId(GetSystemValue.getDockerId());
		loggerBean.setWebSiteId(siteId);
		loggerBean.setUrl(url);
		loggerBean.setDateStamp(System.currentTimeMillis()+"");
		loggerBean.setNaviGationId(id);
		loggerBean.setParentWebSiteId("");
		loggerBean.setTargetUrlNum(targetUrlNum);//容器化改造之后都是一条
		loggerBean.setTimeconsume(timeconsume);
		loggerBean.setSuccess(success);
		loggerBean.setMsg(msg);
		LoggerUtils.info(loggerBean);
	
		
		//ComsLoggerUtils.sendMessage(LoggerUtils.convert(loggerBean));
		return loggerBean;
	}
	
	/**
	 * 拼接日志
	 * 
	 * @return
	 */
	public static LoggerBean setLoggBean(String logCon) {
		//1.新闻，2.微博，3.微信，4.APP，5.报刊，6.论坛，7.博客，8.视频，9.商机
		String logs[] = logCon.split("\\|");
		String url="";
		String siteId="";
		String type="";
		int sitenfoSourceType=0;
		String logType="";
		String msg="";
		String targetUrlNum="";
		String timeconsume="";
		String success="";
		
		for(int i=0;i<logs.length;i++) {
			switch(i) {
			case 0:
				url=logs[i];
				break;
			case 1:
				siteId=logs[i];
				break;
			case 2:
				type=logs[i];
				break;
			case 3:
				sitenfoSourceType=Integer.parseInt(logs[i]);
				break;
			case 4:
				logType = logs[i];
				break;
			case 5:
				msg = logs[i];
				break;
			case 6:
				targetUrlNum = logs[i];
				break;
			case 7:
				timeconsume = logs[i];
				break;
			case 8:
				success = logs[i];
				break;
			}
			
		}
		String spiderType="";
		switch (sitenfoSourceType) {
		case 1:
			spiderType="news";
			break;
		case 2:
			spiderType="weibo";
			break;
		case 3:
			spiderType="WeChat";
			break;
		case 4:
			spiderType="APP";
			break;
		case 5:
			spiderType="journal";
			break;
		case 6:
			spiderType="forum";
			break;
		case 7:
			spiderType="Blog";
			break;
		case 8:
			spiderType="video";
			break;
		case 9:
			spiderType="shangji";
			break;
		case 10:
			spiderType="shangjia";
			break;
		default:
			break;
		}
		LoggerBean loggerBean = new LoggerBean();
	
		loggerBean.setServerIp(GetSystemValue.getIp());
		loggerBean.setSpiderType(spiderType);
		loggerBean.setLevel(logType);
		loggerBean.setDate(StringHas.getDateNowStr());
		loggerBean.setType(type);
		//loggerBean.setOffSet(offset);
		loggerBean.setDockerId(GetSystemValue.getDockerId());
		loggerBean.setWebSiteId(siteId);
		loggerBean.setUrl(url);
		loggerBean.setDateStamp(System.currentTimeMillis()+"");
		//loggerBean.setNaviGationId(id);
		//loggerBean.setParentWebSiteId("");
		loggerBean.setTargetUrlNum(targetUrlNum);//容器化改造之后都是一条
		loggerBean.setTimeconsume(timeconsume);
		loggerBean.setSuccess(success);
		loggerBean.setMsg(msg);
		//LoggerUtils.info(loggerBean);
		//ComsLoggerUtils.sendMessage(LoggerUtils.convert(loggerBean));
		return loggerBean;
	}
	
	public static void main(String[] args) {
		System.out.println(getNextScheduleTime("0 0 0 1 * ?"));
	}
	
}
