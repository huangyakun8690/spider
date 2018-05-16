/**
 * 
 */
package com.xxl.job.executor.processor.list;

import static org.junit.Assert.fail;

import org.junit.Test;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class NaviThreadMqTest extends Basejunit{

	private String mqCName = "测试";
	private Integer classif = 1;
	private String types = "1-2-3";
	private NaviThreadMq naviThreadMq = new NaviThreadMq(mqCName, classif, types);
	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#NaviThreadMq(java.lang.String, java.lang.Integer, java.lang.String)}.
	 */
	@Test
	public void testNaviThreadMq() {
		mqCName="group1";
		naviThreadMq= new NaviThreadMq(mqCName, classif, types);
	}

	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#getMessage()}.
	 */
	@Test
	public void testGetMessage() {
		mqCName="group1";
		naviThreadMq= new NaviThreadMq(mqCName, classif, types);
		naviThreadMq.getMessage();
	}

	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#run()}.
	 */
	@Test
	public void testRun() {
		//naviThreadMq.run();
	}

	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#sendMessage(com.ustcinfo.ptp.yunting.model.Dbwriteinfo, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSendMessage() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#setSiteList(java.lang.String)}.
	 */
	@Test
	public void testSetSiteList() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#getWebDriver(com.ustcinfo.ptp.yunting.model.CollectionStrategy)}.
	 */
	@Test
	public void testGetWebDriver() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.xxl.job.executor.processor.list.NaviThreadMq#printLog(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testPrintLog() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#Object()}.
	 */
	@Test
	public void testObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#getClass()}.
	 */
	@Test
	public void testGetClass() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public void testEquals() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#clone()}.
	 */
	@Test
	public void testClone() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#notify()}.
	 */
	@Test
	public void testNotify() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#notifyAll()}.
	 */
	@Test
	public void testNotifyAll() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long)}.
	 */
	@Test
	public void testWaitLong() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long, int)}.
	 */
	@Test
	public void testWaitLongInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#wait()}.
	 */
	@Test
	public void testWait() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link java.lang.Object#finalize()}.
	 */
	@Test
	public void testFinalize() {
		fail("Not yet implemented");
	}

}
