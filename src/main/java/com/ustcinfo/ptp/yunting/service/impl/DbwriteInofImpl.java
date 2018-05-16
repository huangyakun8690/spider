package com.ustcinfo.ptp.yunting.service.impl;

import java.util.concurrent.BlockingDeque;

import org.springframework.kafka.core.KafkaTemplate;

import com.alibaba.fastjson.JSON;
import com.ustcinfo.ptp.yunting.model.Dbwriteinfo;
import com.ustcinfo.ptp.yunting.service.IDBwriteInfo;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.SpringContextUtil;

/**
 * 数据回写，写进队列
 * @author huangyakun
 *
 */
public class DbwriteInofImpl implements IDBwriteInfo {
	private String isKafka= ReadProperties.readProperties("application.properties", "iskafka");
	@Override
	public void store(Dbwriteinfo dbwriteinfo) {
	
		//数据回写
		try {
			 if(dbwriteinfo!=null) {
				 if("true".equals(isKafka)) {
					 KafkaTemplate<String, String> kafkaTemplate = (KafkaTemplate<String, String>) SpringContextUtil.getBean(KafkaTemplate.class);
					 kafkaTemplate.send("task_out","|"+dbwriteinfo.format());
				 }else if("false".equals(isKafka)) {
					 BlockingDeque<String> qudue= DequeOuts.getDbWritepropers();
			    	 qudue.add(dbwriteinfo.format());
			    	
			    	 ObjectPase.setGetLoggBean(dbwriteinfo.getSiteId(), dbwriteinfo.getUrl(), dbwriteinfo.getInfoSourceType(), dbwriteinfo.getType(),
								"", dbwriteinfo.getId()+"","1",   "", "", 
								"存入回写数据队列为："+JSON.toJSONString(dbwriteinfo)
								,this.getClass().getName(),
								"info");
			    	 DequeOuts.setDbWritepropers(qudue);
				 }else {
					 BlockingDeque<String> qudue= DequeOuts.getDbWritepropers();
					 qudue.add(dbwriteinfo.format());
			    	
			    	 ObjectPase.setGetLoggBean(dbwriteinfo.getSiteId(), dbwriteinfo.getUrl(), dbwriteinfo.getInfoSourceType(), dbwriteinfo.getType(),
								"", dbwriteinfo.getId()+"","1",   "", "", 
								"存入回写数据队列为："+JSON.toJSONString(dbwriteinfo)
								,this.getClass().getName(),
								"info");
			    	 DequeOuts.setDbWritepropers(qudue);
			    	 KafkaTemplate<String, String> kafkaTemplate = (KafkaTemplate<String, String>) SpringContextUtil.getBean(KafkaTemplate.class);
					 kafkaTemplate.send("task_out","|"+dbwriteinfo.format());
				 }
		    	  
		    	 
		       }
		} catch (Exception e) {
			
			ObjectPase.setGetLoggBean(dbwriteinfo.getSiteId(), dbwriteinfo.getUrl(), dbwriteinfo.getInfoSourceType(), dbwriteinfo.getType(),
					"", dbwriteinfo.getId()+"","1",   "", "false", 
					"存入回写数据队列为异常："+AllErrorMessage.getExceptionStackTrace(e),this.getClass().getName(),
					"error");
			
		}
      
	}

}
