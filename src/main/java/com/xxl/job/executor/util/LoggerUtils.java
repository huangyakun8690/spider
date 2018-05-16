package com.xxl.job.executor.util;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.service.ILoggingOutput;



public class LoggerUtils {
	
	private final static String vertical = "|";
	private final static String postfix = "#";
	private static Logger logger = LoggerFactory.getLogger(LoggerUtils.class);
	public static void info(LoggerBean loggerBean) {
//		String msg = convert(loggerBean);
//		logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
//		logger.info(msg);
		
		
		ServiceLoader<ILoggingOutput> loader = ServiceLoader.load(ILoggingOutput.class);
		Iterator<ILoggingOutput> logServices = loader.iterator();
		while(logServices.hasNext()){
			ILoggingOutput service =  logServices.next();
			service.log(loggerBean);
		}
	}
	public static void error(LoggerBean loggerBean) {
		String msg = convert(loggerBean);
		logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.info(msg);
	}

	public static void debug(LoggerBean loggerBean) {
		String msg = convert(loggerBean);
		logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.info(msg);
	}
	public static void warn(LoggerBean loggerBean) {
		String msg = convert(loggerBean);
		logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.info(msg);
	}
	public static void trace(LoggerBean loggerBean) {
		String msg = convert(loggerBean);
		logger = LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
		logger.info(msg);
	}
	public static String convert (LoggerBean LoggerBean){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(LoggerBean.getServerIp() == null ?"":LoggerBean.getServerIp()).append(vertical);
		strBuff.append(LoggerBean.getSpiderType() == null ?"":LoggerBean.getSpiderType()).append(vertical);
		strBuff.append(LoggerBean.getLevel() == null ?"":LoggerBean.getLevel()).append(vertical);
		//strBuff.append(LoggerBean.getDate() == null ?"":LoggerBean.getDate()).append(vertical);
		strBuff.append(LoggerBean.getType() == null ?"":LoggerBean.getType()).append(vertical);
		strBuff.append(LoggerBean.getOffSet() == null ?"":LoggerBean.getOffSet()).append(vertical);
		strBuff.append(LoggerBean.getDockerId() == null ?"":LoggerBean.getDockerId()).append(vertical);
		strBuff.append(LoggerBean.getWebSiteId() == null ?"":LoggerBean.getWebSiteId()).append(vertical);
		strBuff.append(LoggerBean.getUrl() == null ?"":LoggerBean.getUrl()).append(vertical);
		strBuff.append(LoggerBean.getDateStamp() == null ?"":LoggerBean.getDateStamp()).append(vertical);
		strBuff.append(LoggerBean.getNaviGationId() == null ?"":LoggerBean.getNaviGationId()).append(vertical);
		strBuff.append(LoggerBean.getParentWebSiteId() == null ?"":LoggerBean.getParentWebSiteId()).append(vertical);
		strBuff.append(LoggerBean.getTargetUrlNum() == null ?"":LoggerBean.getTargetUrlNum()).append(vertical);
		strBuff.append(LoggerBean.getTimeconsume() == null ?"":LoggerBean.getTimeconsume()).append(vertical);
		strBuff.append(LoggerBean.getSuccess() == null ?"":LoggerBean.getSuccess()).append(vertical);
		strBuff.append(LoggerBean.getMsg() == null ?"":LoggerBean.getMsg()).append(vertical);
		strBuff.append(LoggerBean.getExtend1() == null ?"":LoggerBean.getExtend1()).append(vertical);
		strBuff.append(LoggerBean.getExtend2() == null ?"":LoggerBean.getExtend2()).append(vertical);
		strBuff.append(LoggerBean.getExtend3() == null ?"":LoggerBean.getExtend3()).append(vertical);
		strBuff.append(postfix);
		return new String(strBuff);
		
	}
}
