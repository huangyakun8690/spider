package com.ustcinfo.ptp.yunting.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.service.IrocketMqconsumer;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.Producer;


public class RocketMqConsumerImpl implements IrocketMqconsumer {
	
	private DefaultMQPushConsumer consumer = null;
	private static int initialState = 0;
	private String producerAddr = ReadProperties.readProperties("application.properties", "mqAddr");
	private String offset="offset";
  
	public static void setInitialState(int initialState) {
		RocketMqConsumerImpl.initialState = initialState;
	}
	@Override
	public DefaultMQPushConsumer consumer(String mqConName) {
		
		try {
			
			if (initialState == 0 || consumer==null) {
				consumer = new DefaultMQPushConsumer(mqConName);
				ObjectPase.setGetLoggBean("mq地址 "+producerAddr , "", JsonKeys.LOG_LEVEL_INFO);
				consumer.setNamesrvAddr(producerAddr);
				consumer.setVipChannelEnabled(false);
				consumer.setInstanceName(UUID.randomUUID().toString());
				consumer.setConsumeMessageBatchMaxSize(1);
				consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
				RocketMqConsumerImpl.setInitialState(1);
			}
		} catch (Exception e) {
			ObjectPase.setGetLoggBean("获取mq消费异常" +AllErrorMessage.getExceptionStackTrace(e), "", JsonKeys.LOG_LEVEL_ERROR);
			
		}

		return consumer;
	}
	@Override
	public DefaultMQPushConsumer getconsumer(String mqConName,DefaultMQPushConsumer consumer) {
		
		
		try {
			if(initialState ==0) {
				ObjectPase.setGetLoggBean("获取mq消费服务" , "", JsonKeys.LOG_LEVEL_INFO);
				ObjectPase.setGetLoggBean("mq地址 "+producerAddr , "", JsonKeys.LOG_LEVEL_INFO);
				RocketMqConsumerImpl.setInitialState(3);
			}
			    consumer = new DefaultMQPushConsumer(mqConName);
				consumer.setNamesrvAddr(producerAddr);
				consumer.setVipChannelEnabled(false);
				consumer.setInstanceName(UUID.randomUUID().toString());
				consumer.setConsumeMessageBatchMaxSize(1);
				consumer.setPullInterval(5);
				consumer.setConsumeThreadMin(4);
				consumer.setConsumeThreadMax(4);
				consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
				
			
		} catch (Exception e) {
			
			ObjectPase.setGetLoggBean("获取mq消费异常"+AllErrorMessage.getExceptionStackTrace(e), "", JsonKeys.LOG_LEVEL_ERROR);
			
		}

	
		return consumer;
	}

	@Override
	/**
	 * 获取队列信息
	 */
	public List<String> getMessage(String topicName,String consumerName) {
		
		ObjectPase.setGetLoggBean("开始拉取队列"+topicName+"数据", "", JsonKeys.LOG_LEVEL_INFO);
		List<String> messageList = new ArrayList<>();
		DefaultMQPullConsumer consumerList = new DefaultMQPullConsumer();
		try {
			consumerList.setConsumerGroup(consumerName);
	    	ObjectPase.setGetLoggBean(
					"mq地址"+producerAddr , "", JsonKeys.LOG_LEVEL_INFO);
	    	consumerList.setNamesrvAddr(producerAddr);
	    	consumerList.setVipChannelEnabled(false);
	     
	    	consumerList.start();
	    
	        Set<MessageQueue> mqs = consumerList.fetchSubscribeMessageQueues(topicName);
	     
	       for (MessageQueue mq : mqs) {
	    	      setList(consumerList, mq,messageList);
	            }
	        
	       
	       
		} catch (Exception e) {
			ObjectPase.setGetLoggBean(
					"获取mq消费异常："+AllErrorMessage.getExceptionStackTrace(e), "", JsonKeys.LOG_LEVEL_ERROR);
			if(e.getMessage().indexOf("Can not find Message Queue for this topic")!=-1) {
				//创建 此队列
				JSONObject jsonObject = new JSONObject();
				try {
					Producer.sendMessage( jsonObject, topicName, "", "", "con");
				} catch (Exception e1) {
					ObjectPase.setGetLoggBean(
							"获取mq消费异常 "+AllErrorMessage.getExceptionStackTrace(e), "", JsonKeys.LOG_LEVEL_ERROR);
				}
			}
			
		}finally {
			consumerList.shutdown();
		}
	
	
		return messageList;
	}
	
	private void setList(DefaultMQPullConsumer consumerList, MessageQueue mq,List<String> messageList) {
	      try {
              PullResult pullResult =
              		consumerList.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 2);
              List<MessageExt> list=pullResult.getMsgFoundList();
             
              if(list!=null&&list.size()<100){
                  for(MessageExt msg:list){
                  	if(        new String(msg.getBody())!=null
                  			&& !new String(msg.getBody()).equals("") 
                  			&& !new String(msg.getBody()).equals("null")
                  			&& !new String(msg.getBody()).equals("{}")) {
                  		messageList.add(new String(msg.getBody()));
                  	}
                  	 
                  }
              }
              putMessageQueueOffset(mq, pullResult.getNextBeginOffset());

          }
          catch (Exception e) {
              ObjectPase.setGetLoggBean(
  					"获取mq消费异常 "+AllErrorMessage.getExceptionStackTrace(e), "", JsonKeys.LOG_LEVEL_ERROR);
          }
	}
	
	  private  void putMessageQueueOffset(MessageQueue mq, long offset) {
    	  JedisUtils.hset(mq.toString(),this.offset,offset+"");
    }
 
 
    private  long getMessageQueueOffset(MessageQueue mq) {
        if(JedisUtils.hget(mq.toString(),offset)!=null) {
        	return Long.parseLong(JedisUtils.hget(mq.toString(),"offset"));
        }
 
        return 0;
    }
    
   
 

}
