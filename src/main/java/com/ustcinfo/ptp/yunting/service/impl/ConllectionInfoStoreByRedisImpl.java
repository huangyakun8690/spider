package com.ustcinfo.ptp.yunting.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.StringHas;

public class ConllectionInfoStoreByRedisImpl implements ICollectionInfoStore {

	@Override
	public void store(InfoEntity infoEntity, CollectionSite collectionSite, JSONObject pageJsonObj)
			throws Exception {
		
		
	}

	@Override
	public void store(JSONObject pageJsonObj, CollectionSite collectionSite) {
	
		try {
			ObjectPase.setGetLoggBean("", collectionSite.getSiteUrl(), collectionSite.getInfoSourceType(), "2", "1", "", "1", "", "true", 
					"开始保存数据到队列", this.getClass().getName(), "info");
			String collection= JedisUtils.getv("Acq_mould_" +collectionSite.getCategoryId());
			com.alibaba.fastjson.JSONObject collectionJson=JSON.parseObject(collection);
			String persistTemplate = collectionJson.getString("content");
			String url =  pageJsonObj.get("url") + "";
			persistTemplate = StringHas.encapTemplate(persistTemplate, pageJsonObj.keys(), url) ;
			        Iterator<String> it = (Iterator<String>) pageJsonObj.keys();
					while(it.hasNext()){
						String key = it.next() ;//ext1
						if(persistTemplate.indexOf("${"+key+"}")>-1){
							String keyValue=StringHas.getJsonVlue(pageJsonObj, key);
							keyValue=keyValue.replace("\n", "&#110;");
							keyValue=keyValue.replace("\r", "&#114;");
							keyValue=keyValue.trim();
							keyValue=keyValue.replace(" ", " ");
							keyValue=keyValue.replace(" ", " ");
							keyValue=keyValue.replace("　", "");
							persistTemplate = persistTemplate.replace("${"+key+"}",keyValue.replace("|", "&#124;"));
						}
					}
					
					Map<String,Object> map= new HashMap<>();
					Integer typeNum=collectionSite.getInfoSourceType();
					String type="";
					type=getType(typeNum)+"Spider_"+collectionSite.getId();
					map.put("line", persistTemplate+="#\r\n");
					map.put("type", type);// 加个类型
					map.put("outFormat", collectionSite.getExt2());
					BlockingDeque<Map<String,Object>>  qud= DequeOuts.getOutPropers();
					qud.add(map);
					DequeOuts.setOutPropers(qud);
				
					ObjectPase.setGetLoggBean(collectionSite.getId()+"", collectionSite.getSiteUrl(), collectionSite.getInfoSourceType(), "2", "", "", "", "", "", 
							"保存数据到队列结束", this.getClass().getName(), "info");
			
		} catch (Exception e) {
			
			throw e;
		}
		
	}

	@Override
	public void store(JSONObject pageJsonObj, String CategoryId) throws Exception {
		
	}
	
	public String getType(int sitenfoSourceType) {
		String spiderType="other";
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
		return spiderType;
	}

}
