package com.xxl.job.executor.util;



import java.io.Serializable;

public class LoggerBean implements Serializable{
	
	/** serialVersionUID*/
	private static final long serialVersionUID = 8161523307599531290L;
	/** 服务器IP  logstash生成*/
	private String ServerIp;
	/** 爬虫类型   shangji商机，shangjia商家 等，*/
	private String SpiderType;
	/** =日志级别(info 普通日志，error 异常日志)*/
	private String Level;
	/** =logstash 采集时间*/
	private String Date;
	/** 1、导航，2目标*/
	private String Type;
	/** 1、 开始，2 结束*/
	private String OffSet;
	/** 容器id*/
	private String DockerId;
	/** 信源id*/
	private String WebSiteId;
	/** 采集url*/
	private String Url;
	/** 业务日志时间戳*/
	private String DateStamp;
	/** 导航ID*/
	private String NaviGationId;
	/** 父站ID*/
	private String ParentWebSiteId;
	/** 生成目标任务数量*/
	private String TargetUrlNum;
	/** 任务耗时（结束时计算）*/
	private String Timeconsume;
	/** 1、TRUE 2、FALSE*/
	private String Success;
	/** 内容*/
	private String Msg;
	/** 冗余字段1*/
	private String Extend1;
	/** 冗余字段2*/
	private String Extend2;
	/** 冗余字段3*/
	private String Extend3;
	
	
	
	public LoggerBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public LoggerBean(String serverIp, String spiderType, String level, String date, String type, String offSet,
			String dockerId, String webSiteId, String url, String dateStamp, String naviGationId,
			String parentWebSiteId, String targetUrlNum, String timeconsume, String success, String msg, String extend1,
			String extend2, String extend3) {
		super();
		ServerIp = serverIp;
		SpiderType = spiderType;
		Level = level;
		Date = date;
		Type = type;
		OffSet = offSet;
		DockerId = dockerId;
		WebSiteId = webSiteId;
		Url = url;
		DateStamp = dateStamp;
		NaviGationId = naviGationId;
		ParentWebSiteId = parentWebSiteId;
		TargetUrlNum = targetUrlNum;
		Timeconsume = timeconsume;
		Success = success;
		Msg = msg;
		Extend1 = extend1;
		Extend2 = extend2;
		Extend3 = extend3;
	}
	public String getServerIp() {
		return ServerIp;
	}
	public void setServerIp(String serverIp) {
		ServerIp = serverIp;
	}
	public String getSpiderType() {
		return SpiderType;
	}
	public void setSpiderType(String spiderType) {
		SpiderType = spiderType;
	}
	public String getLevel() {
		return Level;
	}
	public void setLevel(String level) {
		Level = level;
	}
	public String getDate() {
		return Date;
	}
	public void setDate(String date) {
		Date = date;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getOffSet() {
		return OffSet;
	}
	public void setOffSet(String offSet) {
		OffSet = offSet;
	}
	public String getDockerId() {
		return DockerId;
	}
	public void setDockerId(String dockerId) {
		DockerId = dockerId;
	}
	public String getWebSiteId() {
		return WebSiteId;
	}
	public void setWebSiteId(String webSiteId) {
		WebSiteId = webSiteId;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	public String getDateStamp() {
		return DateStamp;
	}
	public void setDateStamp(String dateStamp) {
		DateStamp = dateStamp;
	}
	public String getNaviGationId() {
		return NaviGationId;
	}
	public void setNaviGationId(String naviGationId) {
		NaviGationId = naviGationId;
	}
	public String getParentWebSiteId() {
		return ParentWebSiteId;
	}
	public void setParentWebSiteId(String parentWebSiteId) {
		ParentWebSiteId = parentWebSiteId;
	}
	
	public String getTargetUrlNum() {
		return TargetUrlNum;
	}
	public void setTargetUrlNum(String targetUrlNum) {
		TargetUrlNum = targetUrlNum;
	}
	public String getTimeconsume() {
		return Timeconsume;
	}
	public void setTimeconsume(String timeconsume) {
		Timeconsume = timeconsume;
	}
	public String getSuccess() {
		return Success;
	}
	public void setSuccess(String success) {
		Success = success;
	}
	public String getMsg() {
		return Msg;
	}
	public void setMsg(String msg) {
		Msg = msg;
	}
	public String getExtend1() {
		return Extend1;
	}
	public void setExtend1(String extend1) {
		Extend1 = extend1;
	}
	public String getExtend2() {
		return Extend2;
	}
	public void setExtend2(String extend2) {
		Extend2 = extend2;
	}
	public String getExtend3() {
		return Extend3;
	}
	public void setExtend3(String extend3) {
		Extend3 = extend3;
	}
	@Override
	public String toString() {
		return "Begindoc [ServerIp=" + ServerIp + ", SpiderType=" + SpiderType + ", Level=" + Level + ", Date=" + Date
				+ ", Type=" + Type + ", OffSet=" + OffSet + ", DockerId=" + DockerId + ", WebSiteId=" + WebSiteId
				+ ", Url=" + Url + ", DateStamp=" + DateStamp + ", NaviGationId=" + NaviGationId + ", ParentWebSiteId="
				+ ParentWebSiteId + ", TargetUrlNum=" + TargetUrlNum + ", Timeconsume=" + Timeconsume + ", Success="
				+ Success + ", Msg=" + Msg + ", Extend1=" + Extend1 + ", Extend2=" + Extend2 + ", Extend3=" + Extend3
				+ ", getServerIp()=" + getServerIp() + ", getSpiderType()=" + getSpiderType() + ", getLevel()="
				+ getLevel() + ", getDate()=" + getDate() + ", getType()=" + getType() + ", getOffSet()=" + getOffSet()
				+ ", getDockerId()=" + getDockerId() + ", getWebSiteId()=" + getWebSiteId() + ", getUrl()=" + getUrl()
				+ ", getDateStamp()=" + getDateStamp() + ", getNaviGationId()=" + getNaviGationId()
				+ ", getParentWebSiteId()=" + getParentWebSiteId() + ", getTargetUrlNum()=" + getTargetUrlNum()
				+ ", getTimeconsume()=" + getTimeconsume() + ", getSuccess()=" + getSuccess() + ", getMsg()=" + getMsg()
				+ ", getExtend1()=" + getExtend1() + ", getExtend2()=" + getExtend2() + ", getExtend3()=" + getExtend3()
				+ ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString()
				+ "]";
	}
	
}
