package com.xxl.job.executor.processor.detail;

import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.Dbwriteinfo;
import com.ustcinfo.ptp.yunting.service.IDBwriteInfo;
import com.ustcinfo.ptp.yunting.service.IrocketMqconsumer;
import com.ustcinfo.ptp.yunting.service.impl.DbwriteInofImpl;
import com.ustcinfo.ptp.yunting.service.impl.RocketMqConsumerImpl;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.StringHas;

public class DetailProcessThreadNew2 implements Runnable {

	
	private String mqCName = "";
	
	private IrocketMqconsumer irocketMqconsumer = null;
	
	private IDBwriteInfo bwriteInfo = null;


	private Integer classif = 1;
	private String types = "";
	private DefaultMQPushConsumer consumer = null;
	private boolean isOk = false;
	private boolean isfor=true;

	public DetailProcessThreadNew2(String mqCName, Integer classif, String types) {
		super();
		this.mqCName = mqCName;

		this.classif = classif;
		this.types = types;

		irocketMqconsumer = new RocketMqConsumerImpl();
		bwriteInfo = new DbwriteInofImpl();
		
	
	}
	/**
	 * 获取详情队列信息
	 */
	public void getMessages() {
		try {//
			consumer = null;
			ObjectPase.setGetLoggBean("开始扫描目标队列", this.getClass().getName(), "info");
			List<String> topList = StringHas.getTopicName(types, classif, "DETAIL");
			consumer = irocketMqconsumer.getconsumer(mqCName, consumer);
			for (String top : topList) {
				consumer.subscribe(top, "*");
			}
			consumer.registerMessageListener(new MessageListenerConcurrently() {
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,
						ConsumeConcurrentlyContext consumeConcurrentlyContext) {
					try {// 判断redis是否失去连接peek() 
						JedisUtils.hget("test");
						for (MessageExt me : list) {
							String topValue = new String(me.getBody());
							Dbwriteinfo dbwriteinfo =  ObjectPase.getDbwriteinfo(topValue, classif+"");;
							printLog("获取详情队列数据为：" + topValue + ",开始时间为：" + StringHas.getDateNowStr(), "info", "",null);
							
							try {// 数据获取
								DetailProcessExecute  detailProcessExecute = new DetailProcessExecute();
								//爬取数据
								boolean result= detailProcessExecute.crawling(topValue, classif+"");
								if(result) {
									DequeOuts.getDetailTaskSucckAi().incrementAndGet();
									return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
								}else {
									DequeOuts.getDetailTaskFailkAi().incrementAndGet();
									return ConsumeConcurrentlyStatus.RECONSUME_LATER;
								}
							  
							} catch (Exception e) {
								//增加失败次数
								DequeOuts.getDetailTaskFailkAi().incrementAndGet();	
								printLog("爬取详情异常：" + AllErrorMessage.getExceptionStackTrace(e), "error", "", null);
								// 如果异常则进行二次扫描
                                 if(me.getReconsumeTimes() > 5) {// 失败5次放弃扫描
                                	 if (null!=dbwriteinfo && null!=dbwriteinfo.getFailNum()) {
 										int numfail = Integer.parseInt(
 												(dbwriteinfo.getFailNum().equals("") ? "0" : dbwriteinfo.getFailNum()));
 										numfail = numfail + 1;
 										dbwriteinfo.setFailNum(numfail + "");
 									} else {
 										if(null==dbwriteinfo) {
 											dbwriteinfo=ObjectPase.getDbwriteinfo(topValue, classif+"");
 											dbwriteinfo.setFailNum("1");
 										}
 										
 									}
 									dbwriteinfo.setState("0");
 									bwriteInfo.store(dbwriteinfo); 
 									return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                                 }
                                 return ConsumeConcurrentlyStatus.RECONSUME_LATER;
									

							}
						}
					
						
					} catch (Exception e) {
						printLog("redis异常：" + AllErrorMessage.getExceptionStackTrace(e), "error", "", null);
						isOk = false;
						if (consumer != null) {
							consumer.shutdown();
						}
					

					}

				
					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}

			

			});

			consumer.start();
		} catch (Exception e) {
			if(null!=consumer ) {
				consumer.shutdown();
			}
			
			printLog("获取队列异常：" + AllErrorMessage.getExceptionStackTrace(e), "error", "", null);

		}

	}

	

	@Override
	public void run() {
		printLog("开始获取详情队列数据", "info", "", null);
		try {
			while (isfor) {
				try {
					JedisUtils.hget("test");
					if (!isOk) {
						getMessages();
						isOk = true;
					}

				} catch (Exception e) {
					 isOk = false;
					if (!isOk && null !=consumer) {
						consumer.shutdown();
						consumer = null;
					}

					
				}
				
				Thread.sleep(1000*60*5l);
			}
		} catch (Exception e) {
		}

	}
	


	/**
	 * 日志
	 * 
	 * @param msg
	 * @param logType
	 */
	
	public void printLog(String msg, String logType, String url, CollectionSite collectionSite) {
		if (collectionSite != null && collectionSite.getId() != null) {
			ObjectPase.setGetLoggBean(collectionSite.getId() + "", url,
					collectionSite.getInfoSourceType() != null ? collectionSite.getInfoSourceType() : 0, "2", "", "",
					"", "", "", msg, this.getClass().getName(), logType);
		} else {
			ObjectPase.setGetLoggBean("", url, 0, "2", "", "", "", "", "", msg, this.getClass().getName(), logType);
		}

	}


}


