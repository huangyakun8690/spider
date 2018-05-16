package com.ustcinfo.ptp.yunting.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.json.JSONException;
import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;
import com.ustcinfo.ptp.yunting.model.OutProper;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;

import com.ustcinfo.tpc.framework.core.util.Md5;
import com.xxl.job.executor.util.DequeOuts;

import us.codecraft.webmagic.Page;

public class ShangjiCollectionInfoStoreImpl implements ICollectionInfoStore {
	private static String spilts="|";
	private  static String Code="UTF-8";
	@Override
	public void store(InfoEntity infoEntity,CollectionSite collectionSite,JSONObject pageJsonObj) throws JSONException {
		String contents="";
		String id=Md5.toMD5(infoEntity.getInfoUrl());
		String recType=collectionSite.getInfoSourceType()!= null ? collectionSite.getInfoSourceType()+"" :"";
		String theSource=collectionSite.getSiteName() != null ? collectionSite.getSiteName() : "";
		String reference=infoEntity.getInfoUrl() != null ?infoEntity.getInfoUrl() : "" ;
		String date= System.currentTimeMillis()+"";
		
		String ffdCreate=pageJsonObj.getString("date") != null ? pageJsonObj.getString("date") :"";
		String dreSource=collectionSite.getSiteName() !=null ? collectionSite.getSiteName():"";
		String title=pageJsonObj.getString("title") != null ? pageJsonObj.getString("title") :"";
		String content =pageJsonObj.getString("content") != null ? pageJsonObj.getString("content") :"";
				
		String summary ="";
		
		String recEmotional="";
		String area="";
		String frequencyWord="";
		String likeInfo="";
		String likeInfoCount="";
		
		String screenName="";
		String comments="";
		String reportCount="";
		String readCount="";
		String weiboType="";
		
		String weixinType="";
		String hotValue="";
		String mediaType=collectionSite.getMediaType() !=null? collectionSite.getMediaType()+"": "";
		String keyWord="";
		String alarmLevel="";
		
		String businessType="";


		StringBuilder sb = new StringBuilder() ;
		sb.append(id).append(spilts)
		.append(recType).append(spilts)
		.append(theSource).append(spilts)
		.append(reference).append(spilts)
		.append(date).append(spilts)
		
		.append(ffdCreate).append(spilts)
		.append(Code).append(spilts)
		.append(dreSource).append(spilts)
		.append(title).append(spilts)
		.append(content).append(spilts)
		
		.append(summary).append(spilts)
		.append(recEmotional).append(spilts)
		.append(area).append(spilts)
		.append(frequencyWord).append(spilts)
		.append(likeInfo).append(spilts)
		
		.append(likeInfoCount).append(spilts)
		.append(screenName).append(spilts)
		.append(comments).append(spilts)
		.append(reportCount).append(spilts)
		.append(readCount).append(spilts)
		
		.append(weiboType).append(spilts)
		.append(weixinType).append(spilts)
		.append(hotValue).append(spilts)
		.append(mediaType).append(spilts)
		.append(keyWord).append(spilts) 
		
		.append(alarmLevel).append(spilts)
		.append(businessType).append(spilts).append("#\r\n");
		contents=sb.toString();
		
		Map<String,Object> map= new HashMap<String, Object>();
		map.put("line", contents);
		map.put("type", "shangjiSpider");
		BlockingDeque<Map<String,Object>>  qud= DequeOuts.getOutPropers();
		qud.add(map);
		DequeOuts.setOutPropers(qud);
		//配合商家进行改造，接下来进行修改
//		OutProper outProper= new OutProper(id, recType, theSource, reference, date, 
//				ffdCreate, dreSource, title, content, summary, 
//				recEmotional, area, frequencyWord, likeInfo, likeInfoCount, 
//				screenName, comments, reportCount, readCount, weiboType, 
//				weixinType, hotValue, mediaType, keyWord, alarmLevel, businessType);
//		BlockingDeque<OutProper>  qud= DequeOuts.getOutPropers();
//		qud.add(outProper);
//		DequeOuts.setOutPropers(qud);

	}


}
