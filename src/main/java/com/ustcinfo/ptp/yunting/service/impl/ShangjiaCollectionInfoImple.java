package com.ustcinfo.ptp.yunting.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.json.JSONException;
import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.model.CollectionInfoStoreEntity;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;

import com.ustcinfo.tpc.framework.core.util.Md5;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.StringHas;

import us.codecraft.webmagic.Page;

public class ShangjiaCollectionInfoImple implements ICollectionInfoStore {

	@Override
	public void store(InfoEntity infoEntity, CollectionSite collectionSite, JSONObject pageJsonObj)
			throws JSONException {
		CollectionInfoStoreEntity entity = new CollectionInfoStoreEntity() ;
		entity.setExt1(Md5.toMD5(infoEntity.getInfoUrl()));
		entity.setExt2(collectionSite.getInfoSourceType()!= null ? collectionSite.getInfoSourceType()+"" :"");
		entity.setExt3(collectionSite.getSiteName() != null ? collectionSite.getSiteName() : "") ;//站点名称
		entity.setExt4(infoEntity.getInfoUrl() != null ?infoEntity.getInfoUrl() : "" ) ;
		entity.setExt5(System.currentTimeMillis()+"") ;
		//pageJsonObj.has(key);
		entity.setExt6( StringHas.checkJson(pageJsonObj, "name")==true ? ( pageJsonObj.getString("name") != null ? pageJsonObj.getString("name") :""):"") ;
		entity.setExt7(collectionSite.getSiteName() !=null ? collectionSite.getSiteName():"") ;
		entity.setExt8( StringHas.checkJson(pageJsonObj, "zy")==true ? ( pageJsonObj.getString("zy") != null ? pageJsonObj.getString("zy") :""):"") ;
		entity.setExt9( StringHas.checkJson(pageJsonObj, "lxr")==true ? ( pageJsonObj.getString("lxr") != null ? pageJsonObj.getString("lxr") :""):"") ;
		entity.setExt10(collectionSite.getMediaType() !=null? collectionSite.getMediaType()+"": "");
		
		entity.setExt11(StringHas.checkJson(pageJsonObj, "email")==true ? ( pageJsonObj.getString("email") != null ? pageJsonObj.getString("email") :""):"");
		
		entity.setExt12(StringHas.checkJson(pageJsonObj, "phone")==true ? ( pageJsonObj.getString("phone") != null ? pageJsonObj.getString("phone") :""):"");
		
		entity.setExt13(StringHas.checkJson(pageJsonObj, "address")==true ? ( pageJsonObj.getString("address") != null ? pageJsonObj.getString("address") :""):"");
		entity.setExt14(StringHas.checkJson(pageJsonObj, "url")==true ? ( pageJsonObj.getString("url") != null ? pageJsonObj.getString("url") :""):"");
		
		StringBuilder sb = new StringBuilder() ;
		sb.append(entity.getExt1()).append("|")
		.append(entity.getExt2()).append("|")
		.append(entity.getExt3()).append("|")
		.append(entity.getExt4()).append("|")
		.append(entity.getExt5()).append("|")
		.append(entity.getExt6()).append("|")
		.append(entity.getExt7()).append("|")
		.append(entity.getExt8()).append("|")
		.append(entity.getExt9()).append("|")
		.append(entity.getExt10()).append("|")
		.append(entity.getExt11()).append("|")
		.append(entity.getExt12()).append("|")
		.append(entity.getExt13()).append("|")
		.append(entity.getExt14())
		.append("|#\r\n");
		Map<String,Object> map= new HashMap<String, Object>();
		map.put("line", sb.toString());
		map.put("type", "shangjiaSpider");
		BlockingDeque<Map<String,Object>>  qud=DequeOuts.getOutPropers();
		qud.add(map);
		DequeOuts.setOutPropers(qud);
	}

}
