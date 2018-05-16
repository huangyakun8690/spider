package com.xxl.job.executor.util;


import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.service.IrocketMqProducer;
import com.ustcinfo.ptp.yunting.service.impl.RocketMqProducerImpl;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;

public class Producer {
	
	//private static DefaultMQProducer producer = null;
	
	/**
	 * 发送消息
	 * @param jsonObject
	 * @param topicName
	 * @param tag
	 * @param keys
	 * @param producerName
	 * @return
	 * @throws Exception 
	 */
	public static SendResult sendMessage(JSONObject jsonObject,String topicName,String tag,String keys,String producerName) throws Exception {
		//Logger logger = LoggerFactory.getLogger(Producer.class);
		IrocketMqProducer irocketMqProducer = new RocketMqProducerImpl();
		DefaultMQProducer producer =irocketMqProducer.getcreateProducter(JsonKeys.PRODUCER_MQ_NAME);
		try {
			//producer.start();
			ObjectPase.setGetLoggBean( 
					"发送生产者消息:"+jsonObject.toString(), Producer.class.getName(), "info");
			//logger.info(StringHas.getSpiderHead()+",发送生产者消息");
			Message msg = new Message(topicName, tag,keys,jsonObject.toString().getBytes());
			SendResult sendResult =producer.send(msg);
			ObjectPase.setGetLoggBean( 
					"发送生产者消息成功", Producer.class.getName(), "info");
			return sendResult;
		} catch (Exception e) {
			
			//logger.info(StringHas.getSpiderHead()+",发送消息异常："+AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean(
					",发送消息异常："+AllErrorMessage.getExceptionStackTrace(e), Producer.class.getName(), "error");
			
			throw e;
		}finally {
			Thread.sleep(200);
			producer.shutdown();
		}

	}
	
	/**
	 * 发送多条消息
	 * @param messageList
	 * @param producerName
	 * @throws Exception 
	 */
	public static void  sendMessageList(List<Map<String,Object>> messageList,String producerName) throws Exception {
		IrocketMqProducer irocketMqProducer = new RocketMqProducerImpl();
		DefaultMQProducer producer =irocketMqProducer.getcreateProducter(JsonKeys.PRODUCER_MQ_NAME);
		//Logger logger = LoggerFactory.getLogger(Producer.class);
		try {
			//producer.start();
			ObjectPase.setGetLoggBean( 
					"批量发送生产者消息", Producer.class.getName(), "info");
			//logger.info(StringHas.getSpiderHead()+",批量发送生产者消息");
			//this.producerName=producerName;
	        int num =0;
			for(Map<String,Object> map:messageList) {
				num++;
				if(num>200) {
					Thread.sleep(1000l*60l*1l);
					num=0;
				}
				ObjectPase.setGetLoggBean( 
						"批量发送生产者消息"+map, Producer.class.getName(), "info");
				Message msg = new Message(StringHas.getMapValue(map, "topicName"), StringHas.getMapValue(map, "tag"),
						StringHas.getMapValue(map, "keys"),StringHas.getMapValue(map, "jsonObject").getBytes());
				SendResult sendResult=producer.send(msg);
				System.out.println(sendResult);
			}
			//logger.info(StringHas.getSpiderHead()+",发送生产者消息成功");
			ObjectPase.setGetLoggBean(
					"发送生产者消息成功", Producer.class.getName(), "info");
		} catch (Exception e) {
			
			//logger.info(StringHas.getSpiderHead()+",发送消息异常："+AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean(
					",发送消息异常："+AllErrorMessage.getExceptionStackTrace(e), Producer.class.getName(), "error");
			
			throw e;
		}finally {
			Thread.sleep(200);
			producer.shutdown();
		}
		
	}
	public Producer() {
		
		
	}
	
	
    public static void main(String[] args) throws Exception {
        
            DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
            producer.setNamesrvAddr("192.168.1.140:9876");
            producer.setVipChannelEnabled(false);
            producer.start();
     
            for (int i = 0; i < 5; i++) {
                try {
                    Message msg = new Message("TopicTest",// topic
                        "TagA",// tag
                        ("Hello RocketMQ " + i).getBytes()// body
                            );
                    SendResult sendResult = producer.send(msg);
                    System.out.println(sendResult);
                    Thread.sleep(6000);
                }
                catch (Exception e) {
                    
                    Thread.sleep(3000);
                }
            }
     
            producer.shutdown();
        
    }
    
   
 


}