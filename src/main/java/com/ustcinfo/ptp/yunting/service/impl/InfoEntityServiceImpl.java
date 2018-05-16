package com.ustcinfo.ptp.yunting.service.impl;

import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.starit.common.dao.hibernate4.HibernateBaseDao;
import com.starit.common.dao.service.BaseServiceImpl;
import com.starit.common.dao.support.Pagination;
import com.ustcinfo.ptp.yunting.dao.InfoEntityDAO;
import com.ustcinfo.ptp.yunting.dao.InfoOriPageDAO;
import com.ustcinfo.ptp.yunting.model.AcqPageRule;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;
import com.ustcinfo.ptp.yunting.service.CollectionSiteService;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;



import us.codecraft.webmagic.Page;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-08-21
 * @version 1.0
 */
@Service
public class InfoEntityServiceImpl extends BaseServiceImpl<InfoEntity, Long> implements InfoEntityService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	InfoOriPageDAO infoOriPageDAO;
	
	@Autowired 
	InfoEntityDAO infoEntityDAO;
	
	@Value("${jdbc.url}")
	private String jdbcUrl;
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
//	CollectionSiteDAO collectionSiteDAO;
	CollectionSiteService collectionSiteService;
	
//	@Override
//	public void storePageInfo(Page page, JSONStringer pageJSONStringer) {
//		try {
//			// store original page into ES
//			String randomPageId = UUID.randomUUID().toString();
//			infoOriPageDAO.saveOriPage(randomPageId,page);
//			InfoEntity infoEntity = new InfoEntity();
//			infoEntity.set
//			infoEntityDAO.save(infoEntity);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	@Override
	public HibernateBaseDao<InfoEntity, Long> getHibernateBaseDao() {
		return this.infoEntityDAO;
	}

	@Override
	@Transactional
	public void storePageInfo(List<HashMap<String, Object>> list, AcqPageRule acqPageRule) throws JSONException {
		//get site info
		CollectionSite collectionSite = collectionSiteService.getEntity(acqPageRule.getSiteId());
		this.storePageInfo(list, collectionSite, acqPageRule.getPageRuleId());
		
	}

	@Override
	public Pagination<Object> pageList(int start, int length, long siteId) {
		// TODO Auto-generated method stub
		return this.infoEntityDAO.pageList(start, length, siteId);
	}

	@Override
	@Transactional
	public void storePageInfo(List<HashMap<String, Object>> list, CollectionSite collectionSite) throws JSONException {
		this.storePageInfo(list, collectionSite, null);
		
	}

	private void storePageInfo(List<HashMap<String, Object>> list, CollectionSite collectionSite, Long acqPageRuleId) throws JSONException{
				//go through the map list
				if(list!=null && list.size()>0){
				for(HashMap<String,Object> map : list )
				{	
					try {
						String randomPageId = UUID.randomUUID().toString();
//					Page page = (Page)map.get(Constants.STORE_PAGE_HTML);
						
						JSONObject pageJsonObj = (JSONObject)map.get(Constants.STORE_PAGE_JSON);
						InfoEntity infoEntity = new InfoEntity();
						//if title contains keywords
//					if(pageJsonObj.has(Constants.STORE_PAGE_TITLE)){
//					String title = pageJsonObj.get(Constants.STORE_PAGE_TITLE).toString();
//					if(StringUtils.hasText(collectionSite.getKeyWords())){
//					String[] keywords = collectionSite.getKeyWords().split("，");
//					boolean storePage = false;
//					for(String keyword:keywords){
//						if(title.contains(keyword)){
//							storePage=true;
//							infoEntity.setKeyWords(StringUtils.hasText(infoEntity.getKeyWords())? (infoEntity.getKeyWords()+"，"+keyword):keyword);
//						}
//					}
//					if(!storePage){
//						continue;
//					}
//					}
//					}
//					this.infoOriPageDAO.saveOriPage(randomPageId, page);
						
						infoEntity.setCategoryName(collectionSite.getCategoryName());
						infoEntity.setDownloadTime(new Date());
//					infoEntity.setInfoUrl(page.getUrl().toString());
						infoEntity.setInfoUrl(pageJsonObj.getString("url"));
//					infoEntity.setKeyWords(collectionSite.getKeyWords());
						infoEntity.setPageId(randomPageId);
						infoEntity.setPageInfo(pageJsonObj.toString());
						//glue type 
						infoEntity.setPageRuleId(acqPageRuleId==null?0L:acqPageRuleId);
						infoEntity.setSiteSerialNumber(collectionSite.getSerialNumber());
						infoEntity.setSiteId(collectionSite.getId());
						infoEntity.setSiteName(collectionSite.getSiteName());
						infoEntity.setThemeName(collectionSite.getThemeName());
						//lly170911, add theme id and category id
						infoEntity.setCategoryId(collectionSite.getCategoryId());
						infoEntity.setThemeId(collectionSite.getThemeId());
						//extract common properties
						if(pageJsonObj.has(Constants.STORE_PAGE_TITLE))
						infoEntity.setTitle(pageJsonObj.get(Constants.STORE_PAGE_TITLE).toString());
						if(pageJsonObj.has(Constants.STORE_PAGE_CARRY_TIME))
						infoEntity.setCarryTime(pageJsonObj.get(Constants.STORE_PAGE_CARRY_TIME).toString());
						if(pageJsonObj.has(Constants.STORE_PAGE_CONTENT))
						infoEntity.setContent(pageJsonObj.get(Constants.STORE_PAGE_CONTENT).toString());
						if(pageJsonObj.has(Constants.STORE_PAGE_SUMMARY))
						infoEntity.setSummary(pageJsonObj.get(Constants.STORE_PAGE_SUMMARY).toString());
							
						
						ICollectionInfoStore collectionInfoStoreService = null ;
//					if(9==collectionSite.getInfoSourceType() || 10==collectionSite.getInfoSourceType()){
							collectionInfoStoreService =new  CollectionInfoStoreImpl() ;
//					}else{
//						//其他类型暂不考虑
//					}
						
						
						if(null!=collectionInfoStoreService)
							collectionInfoStoreService.store(infoEntity, collectionSite,pageJsonObj);	
						
						else
							logger.info("系统暂未开发此类型的持久化代码，请检查核心代码。。。。");
						
						//WriteInout.write(outProper);
						this.infoEntityDAO.save(infoEntity);
					} catch (Exception e) {
						logger.error("持久化pageInfo异常，listSize={},map={}, 异常信息：{}",list.size(),map.keySet(),AllErrorMessage.getExceptionStackTrace(e));
						e.printStackTrace();
						continue;
					}
				}
				}
	}

	@Override
	public List<Map<String, Object>> countEntityInfoNums() {
		// TODO Auto-generated method stub
		String sql = "SELECT SITE_ID as siteId,count(*) as num FROM info_entity group by site_id";
		List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql);
		return list;
	}

	@Override
	public List<Map<String,Object>> acqNumsListByField(int start, int length, String fieldName) {
		// TODO Auto-generated method stub
		return infoEntityDAO.acqNumsListByField(start, length, fieldName);
	}
}
