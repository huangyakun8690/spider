/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import org.junit.Test;

import com.xxl.job.executor.util.LoggerBean;
import com.xxl.job.executor.util.StringHas;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class CommonLoggingOutputTest extends Basejunit {
	private CommonLoggingOutput CommonLoggingOutput = new CommonLoggingOutput();

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.CommonLoggingOutput#log(com.xxl.job.executor.util.LoggerBean)}.
	 */
	@Test
	public void testLog() {
		LoggerBean loggerBean = new LoggerBean();
		loggerBean.setOffSet("1");
		loggerBean.setTargetUrlNum("1");
		loggerBean.setTimeconsume("");
		loggerBean.setSuccess("");
		loggerBean.setMsg("导航任务开始执行：执行时间为："+ StringHas.getDateNowStr());
		CommonLoggingOutput.log(loggerBean);
	}

}
