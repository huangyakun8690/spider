package com.ustcinfo.ptp.yunting.service.impl;

import org.springframework.kafka.core.KafkaTemplate;

import com.ustcinfo.ptp.yunting.service.ILoggingOutput;
import com.xxl.job.executor.util.LoggerBean;
import com.xxl.job.executor.util.LoggerUtils;
import com.xxl.job.executor.util.SpringContextUtil;

public class KafKaLoggingOutput implements ILoggingOutput {
	@Override
	public void log(LoggerBean loggerBean) {
		try {
			String msg = LoggerUtils.convert(loggerBean);
			KafkaTemplate<String, String> kafkaTemplate = (KafkaTemplate<String, String>) SpringContextUtil.getBean(KafkaTemplate.class);
			kafkaTemplate.send("LOG_TOPIC", msg);
		} catch (Exception e) {
			e.getMessage();
		}
	}

}
