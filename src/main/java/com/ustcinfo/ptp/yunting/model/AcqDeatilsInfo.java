package com.ustcinfo.ptp.yunting.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

public class AcqDeatilsInfo  implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long id ;
	
	private Long siteId;
	
	private String url ;
	
	private int state;
	
    private long naviId;
    
    private int isOut;

    
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

	@Column(name="site_id")
	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}
	@Column(name="url")
	public String getUrl() {
		return url;
	}

	
	public void setUrl(String url) {
		this.url = url;
	}
	@Column(name="state")
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Column(name="navi_id")
	public long getNaviId() {
		return naviId;
	}

	public void setNaviId(long naviId) {
		this.naviId = naviId;
	}

	@Column(name="isout")
	public int getIsOut() {
		return isOut;
	}

	public void setIsOut(int isOut) {
		this.isOut = isOut;
	}
    
    
    
}
