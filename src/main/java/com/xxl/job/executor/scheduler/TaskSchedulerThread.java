package com.xxl.job.executor.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.ustcinfo.ptp.yunting.model.NavigationInfo;
import com.ustcinfo.ptp.yunting.service.ISpiderService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.Dequepal;
import com.xxl.job.executor.util.MQTaskUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.Producer;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2018-01-23
 * @version 1.0
 */
@Service
public class TaskSchedulerThread implements Runnable {
	private ISpiderService spiderService;
	//基地编号
	private int baseLocation;
	//爬虫大类
	private int category;
	//信源类型
	List<Integer> sourceTypeList = new ArrayList<>();
	//单批次导航数据条数
	private int batchSizeLimit;
	//批次号模板
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private boolean isFor=true;
	public TaskSchedulerThread() {
	}

	public TaskSchedulerThread(ISpiderService spiderService, int baseLocation, int category, String sourceTypes,
			int batchSizeLimit) {
		super();
		this.spiderService = spiderService;
		this.baseLocation = baseLocation;
		this.category = category;
		this.batchSizeLimit = batchSizeLimit;
		ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
				 "任务调度线程开始运行，执行参数：单批次导航数据条数："+batchSizeLimit+getTaskParameterInfo(sourceTypes), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_INFO);
		// 解析信源类型
		try {
			List<String> sourceTypeStr = Arrays.asList(sourceTypes.split("-"));
			CollectionUtils.collect(sourceTypeStr, new Transformer() {
				public java.lang.Object transform(java.lang.Object input) {
					return new Integer((String) input);
				}
			}, sourceTypeList);
		} catch (Exception e) {
			ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
					"解析信源类型异常，将扫描所有信源类型，详细异常信息如下：" + AllErrorMessage.getExceptionStackTrace(e), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_INFO);
			}
	}

	@Override
	public void run() {
		while (isFor) {
			try {
				if(!Dequepal.mqIsAvailable()){
					ObjectPase.setGetLoggBean("任务调度线程，感知消息队列不可用，60秒后继续尝试。", Dequepal.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
					Thread.sleep(60l * 1000l);
				}
				beginDispath();
				Thread.sleep(60l * 1000l);
			} catch (Exception e) {
				ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
						"任务调度线程运行异常，异常详细信息如下：" + AllErrorMessage.getExceptionStackTrace(e), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
				
			}
		}
	}
	
	public void beginDispath() {
		try {

			if (sourceTypeList.isEmpty()) {
				sourceTypeList = spiderService.queryDistinctSourceTypes(category, baseLocation);
			}
			for (Integer sourceType : sourceTypeList) {
				ObjectPase.setGetLoggBean("开始查询需要执行调度的导航数据"+getTaskParameterInfo(sourceType), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_INFO);
				
				
				Long threadSign = Thread.currentThread().getId() + System.currentTimeMillis();
				// 查找全部的信源类型
				// 获取单批次导航数据，条件为category，sourceType，条件state=0 or
				
				// 并将本批次导航数据设为state=4
				List<NavigationInfo> naviBatchList = spiderService.fetchAcqNaviForTaskSchedulerThread(
						threadSign.intValue(), batchSizeLimit, category, sourceType, baseLocation);
				// 没有需要执行调度任务的导航信息，继续遍历其他类型
				if (naviBatchList == null || naviBatchList.isEmpty()) {
				
					ObjectPase.setGetLoggBean("未查询到需要执行调度的导航数据"+getTaskParameterInfo(sourceType), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_INFO);
					
					continue;
				}
				// 生成批次号
				String batchNo = sdf.format(new Date());
				// 将放入消息队列的任务集合
				List<Map<String, Object>> messageList = new ArrayList<>();
				// 每个导航数据封装任务对象并组装消息
				for (NavigationInfo navi : naviBatchList) {
					// 消息中的任务对象
					JSONObject naviObj = new JSONObject();
					naviObj.put(JsonKeys.SCHEDULE_TASK_ID, navi.getId());
					naviObj.put(JsonKeys.SCHEDULE_TASK_SITEID, navi.getSiteId());
					naviObj.put(JsonKeys.SCHEDULE_TASK_STATE, navi.getState());
					naviObj.put(JsonKeys.SCHEDULE_TASK_URL, navi.getUrl());
					naviObj.put(JsonKeys.SCHEDULE_TASK_BATCHNO, batchNo);
					naviObj.put(JsonKeys.SCHEDULE_TASK_TYPE, 1);
					
					Map<String, Object> messageMap = new HashMap<>();
					messageMap.put("jsonObject", naviObj);
					messageMap.put("topicName", MQTaskUtils.getNaviTopicName(category, sourceType));
					messageMap.put("tag", "acq_navi_tag");
					messageMap.put("keys", "siteId_" + navi.getSiteId());
				
					ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
							"封装Message：" + messageMap.toString(), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_INFO);
					messageList.add(messageMap);
				}
				Producer.sendMessageList(messageList, "scheduleJobProducer");
				
				ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
						"导航任务信息已入队列，批次号：" + batchNo + getTaskParameterInfo(sourceType), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_INFO);
			}
		} catch (Exception e) {
			ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
					"任务调度线程运行异常，异常详细信息如下：" + AllErrorMessage.getExceptionStackTrace(e), Dequepal.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
		
		}
	}

	private String getTaskParameterInfo(Object sourceType) {
		return new StringBuilder("，基地编号：").append(baseLocation).append("，爬虫大类：").append(category).append("，信源类型：")
				.append(sourceType).toString();
	}

	public void setFor(boolean isFor) {
		this.isFor = isFor;
	}
	
	
}
