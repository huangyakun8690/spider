package com.ustcinfo.ptp.yunting.service;

import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;





public interface IrocketMqconsumer {
	public DefaultMQPushConsumer consumer(String mqConName); 
	public DefaultMQPushConsumer getconsumer(String mqConName,DefaultMQPushConsumer consumer);	

	public List<String> getMessage(String topicName,String consumerName);
	
	
}
