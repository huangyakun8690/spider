package com.ustcinfo.ptp.yunting.model;

/**
 * 回写数据模型
 * @author huangyakun
 *|dockerid|类别|任务id|站点ID|状态|下次更新时间|url|失败次数
 */
public class Dbwriteinfo {
	private String dockId;
	private String type;
	private String id;
	private String siteId;
	private String state;//1是成功 0是失败
	private String publishTime;
	private String url;
	private String failNum;
	private int infoSourceType;
	private String corn;
	private String isWrite;
	private String category;
	private String mark="|";
	

	public Dbwriteinfo(String id, String siteId, String url, String state, String type, 
			String publishTime,String failNum,String dockId) {
		super();
		this.id = id;
		this.siteId = siteId;
		this.url = url;
		this.state = state;
		this.type = type;
		this.publishTime = publishTime;
		this.failNum = failNum;
		this.dockId = dockId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
	
	

	public String getDockId() {
		return dockId;
	}

	public void setDockId(String dockId) {
		this.dockId = dockId;
	}

	public String getFailNum() {
		return failNum;
	}

	public void setFailNum(String failNum) {
		this.failNum = failNum;
	}

	public int getInfoSourceType() {
		return infoSourceType;
	}

	public void setInfoSourceType(int infoSourceType) {
		this.infoSourceType = infoSourceType;
	}

	 
	
	public String getCorn() {
		return corn;
	}

	public void setCorn(String corn) {
		this.corn = corn;
	}
	

	public String getIsWrite() {
		return isWrite;
	}

	public void setIsWrite(String isWrite) {
		this.isWrite = isWrite;
	}
	
	

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	//|dockerid|类别|任务id|站点ID|信源类型|状态|下次更新时间|url|失败次数|基地编号
	public String format() {
		return dockId+mark+
			   type+mark+
			   id+mark+
			   siteId+mark+
			   infoSourceType+mark+
			   state+mark+
			   publishTime+mark+
			   url+mark+
			   failNum+mark+
			   category+mark+"\r\n";
		//return  id+"|"+siteId+"|"+url+"|"+state+"|"+type+"|"+publishTime+"|#\r\n";
	}

}
