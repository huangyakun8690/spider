package com.ustcinfo.ptp.yunting.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;
import com.ustcinfo.tpc.framework.core.util.Md5;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.StringHas;

import us.codecraft.webmagic.Page;

public class CollectionInfoStoreImpl implements ICollectionInfoStore {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Override
	public void store(InfoEntity infoEntity, CollectionSite collectionSite, JSONObject pageJsonObj)
			throws JSONException {
		logger.info("开始保存数据到队列");
		String persistTemplate = collectionSite.getExt1();
		persistTemplate = StringHas.encapTemplate(persistTemplate, pageJsonObj.keys(), infoEntity.getInfoUrl()) ;
		
		// 1|中移-${}|${}|function(md5Tourl)|fucntion(now,yyy-MM-dd)ow|#\n\r
		String persistTemplates[] =persistTemplate.split("\\|");
		Iterator<String> it = (Iterator<String>) pageJsonObj.keys();
		//ext1:12,ext2:67,ext3:9,ext4:0
		// |12|6|ext3|ext4|ext5$$ext6|ext7|ext8|ext9|ext10|||||
		while(it.hasNext()){
			String key = it.next() ;//ext1
			if(persistTemplate.indexOf("${"+key+"}")>-1){
				String keyValue=StringHas.getJsonVlue(pageJsonObj, key);
				persistTemplate = persistTemplate.replace("${"+key+"}",keyValue.replace("|", ""));
			}
		}
		
		Map<String,Object> map= new HashMap<String, Object>();
		Integer typeNum=collectionSite.getInfoSourceType();
		String type="";
		if(9==typeNum){
			type="shangjiSpider"+collectionSite.getId();
		}else if(10==typeNum){
			type="shangjiaSpider"+collectionSite.getId();
		}else{
			//其他类型暂不考虑
			type="otherSpider"+collectionSite.getId();
		}
		
		map.put("line", persistTemplate+="#\r\n");
		map.put("type", type);// 加个类型
		BlockingDeque<Map<String,Object>>  qud= DequeOuts.getOutPropers();
		qud.add(map);
		DequeOuts.setOutPropers(qud);
		logger.info("保存数据到队列结束");

		
	}
	
	
	
}
