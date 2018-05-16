<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.ustcinfo.ptp.yunting.support.SerializeUtils;



@Entity
@Table(name="acq_navi")
public class NavigationInfo implements java.io.Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8368027180917901003L;

	private Long id;
	private String seqNum;
	private String url;
	private String data;
	private Integer level ;
	private Integer taskId ;
	private Integer state ;
	private Integer isout ;
	private Long siteId;
	private Date nextExecTime;
	private Date prevExecTime;
	private Integer needScan;
	private String lastBatchNo;
	private Integer category;
	private Long parentId;
	private Integer sourceType;
	private Integer machineLocation;
	
	
	@Id 
    @GeneratedValue(generator = "tableGenerator")     
 	@GenericGenerator(name = "tableGenerator", strategy="com.ustcinfo.ptp.yunting.support.SequenceGenerator")
    @Column(name="ID", unique=true, nullable=false, precision=20, scale=0)
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="seq_num")
	public String getSeqNum() {
		return seqNum;
	}
	
	public void setSeqNum(String seqNum) {
		this.seqNum = seqNum;
	}

	@Column(name="url")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Column(name="data")
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	@Column(name="level")
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	@Column(name="task_id")
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	@Column(name="state")
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	@Column(name="isout")
	public Integer getIsout() {
		return isout;
	}
	public void setIsout(Integer isout) {
		this.isout = isout;
	}
	
	@Column(name="site_id")
	public Long getSiteId() {
		return siteId;
	}
	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}
	
	@Column(name="next_exec_time")
	public Date getNextExecTime() {
		return nextExecTime;
	}
	public void setNextExecTime(Date nextExecTime) {
		this.nextExecTime = nextExecTime;
	}
	
	@Column(name="prev_exec_time")
	public Date getPrevExecTime() {
		return prevExecTime;
	}
	public void setPrevExecTime(Date prevExecTime) {
		this.prevExecTime = prevExecTime;
	}
	
	@Column(name="need_scan")
	public Integer getNeedScan() {
		return needScan;
	}
	public void setNeedScan(Integer needScan) {
		this.needScan = needScan;
	}
	
	@Column(name="last_batch_no")
	public String getLastBatchNo() {
		return lastBatchNo;
	}
	public void setLastBatchNo(String lastBatchNo) {
		this.lastBatchNo = lastBatchNo;
	}
	
	@Column(name="category")
	public Integer getCategory() {
		return category;
	}
	public void setCategory(Integer category) {
		this.category = category;
	}
	
	@Column(name="parent_id")
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	@Column(name="source_type")
	public Integer getSourceType() {
		return sourceType;
	}
	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}
	
	@Column(name="machine_location")
	public Integer getMachineLocation() {
		return machineLocation;
	}
	public void setMachineLocation(Integer machineLocation) {
		this.machineLocation = machineLocation;
	}
	
	@Override
	public String toString(){
		return SerializeUtils.serialize(this);
	}

}
=======
package com.ustcinfo.ptp.yunting.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="acq_navi")
public class NavigationInfo implements java.io.Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8368027180917901003L;

	private Long id;
	private String seqNum;
	private String url;
	private String data;
	private Integer level ;
	private Integer taskId ;
	private Integer state ;
	private Integer isout ;
	
	@Id 
    @GeneratedValue(generator = "tableGenerator")     
 	@GenericGenerator(name = "tableGenerator", strategy="com.ustcinfo.ptp.yunting.support.SequenceGenerator")
    @Column(name="ID", unique=true, nullable=false, precision=20, scale=0)
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="seq_num")
	public String getSeqNum() {
		return seqNum;
	}
	
	public void setSeqNum(String seqNum) {
		this.seqNum = seqNum;
	}

	@Column(name="url")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Column(name="data")
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	@Column(name="level")
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	@Column(name="task_id")
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	@Column(name="state")
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	@Column(name="isout")
	public Integer getIsout() {
		return isout;
	}
	public void setIsout(Integer isout) {
		this.isout = isout;
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
