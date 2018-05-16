package com.ustcinfo.ptp.yunting.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.service.ILoggingOutput;
import com.xxl.job.executor.util.LoggerBean;
import com.xxl.job.executor.util.LoggerUtils;

public class CommonLoggingOutput implements ILoggingOutput {
	
	@Override
	public void log(LoggerBean loggerBean) {
		String msg = LoggerUtils.convert(loggerBean);
		Logger logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.info(msg);
	}

}
