package com.ustcinfo.ptp.yunting.service.impl;

import java.util.UUID;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import com.ustcinfo.ptp.yunting.service.IrocketMqProducer;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;
import com.xxl.job.executor.util.ObjectPase;

public class RocketMqProducerImpl implements IrocketMqProducer {
	private DefaultMQProducer producer = null;
	private int initialState = 0;
	@Override
	public  DefaultMQProducer createProducter(String mq) {
		if(producer == null){
	           producer = new DefaultMQProducer(mq);          
	     }
		 if(initialState == 0){
			 ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
						"获取mq生产者", RocketMqProducerImpl.class.getName(), "info");
			String producerAddr=ReadProperties.readProperties("application.properties", "mqAddr");
			 ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
          		   "生产服务地址为："+producerAddr, RocketMqProducerImpl.class.getName(), "info");
		
			producer.setNamesrvAddr(producerAddr);
			producer.setInstanceName(UUID.randomUUID().toString());
	        producer.setVipChannelEnabled(false);
	           try {
	               producer.start();
	              
	           } catch (MQClientException e) {
	             
	               
	               ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
	            		   "异常信息为："+AllErrorMessage.getExceptionStackTrace(e), RocketMqProducerImpl.class.getName(), "error");
	               return null;
	           }

	           initialState = 1;
	       }
		 
		return producer;
		
	}
	
	
	public  DefaultMQProducer getcreateProducter(String mq) {
	        ObjectPase.setGetLoggBean("获取mq生产者", RocketMqProducerImpl.class.getName(), "info");
			String producerAddr=ReadProperties.readProperties("application.properties", "mqAddr");
			DefaultMQProducer producerp = new DefaultMQProducer(mq);  
			producerp.setNamesrvAddr(producerAddr);
			producerp.setInstanceName(UUID.randomUUID().toString());
			producerp.setVipChannelEnabled(false);
			producerp.setRetryTimesWhenSendFailed(5);
	           try {
	        	   producerp.start();
	              
	               ObjectPase.setGetLoggBean(
	            		   "生产服务地址为："+producerAddr, RocketMqProducerImpl.class.getName(), "info");
	           } catch (MQClientException e) {
	              
	               
	               ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
	            		   "异常信息为："+AllErrorMessage.getExceptionStackTrace(e), RocketMqProducerImpl.class.getName(), "error");
	             return null;
	           }
		return producerp;
		
	}


  


  

}
