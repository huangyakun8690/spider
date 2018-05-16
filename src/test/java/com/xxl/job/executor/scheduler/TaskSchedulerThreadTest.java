/**
 * 
 */
package com.xxl.job.executor.scheduler;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ustcinfo.ptp.yunting.service.ISpiderService;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class TaskSchedulerThreadTest extends Basejunit{

	private TaskSchedulerThread schedulerThreadTest=null;
	@Autowired
	private ISpiderService spiderService;
	/**
	 * Test method for {@link com.xxl.job.executor.scheduler.TaskSchedulerThread#TaskSchedulerThread()}.
	 */
	@Test
	public final void testTaskSchedulerThread() {
		
	}

	/**
	 * Test method for {@link com.xxl.job.executor.scheduler.TaskSchedulerThread#TaskSchedulerThread(com.ustcinfo.ptp.yunting.service.ISpiderService, int, int, java.lang.String, int)}.
	 */
	@Test
	public final void testTaskSchedulerThreadISpiderServiceIntIntStringInt() {
		schedulerThreadTest= new TaskSchedulerThread(spiderService, 0, 3, "1-2-3", 20);
	}

	/**
	 * Test method for {@link com.xxl.job.executor.scheduler.TaskSchedulerThread#run()}.
	 */
	@Test
	public final void testRun() {
		schedulerThreadTest= new TaskSchedulerThread(spiderService, 0, 3, "1-2-3", 20);
		schedulerThreadTest.setFor(false);
		schedulerThreadTest.run();
		
	}

	/**
	 * Test method for {@link com.xxl.job.executor.scheduler.TaskSchedulerThread#beginDispath()}.
	 */
	@Test
	public final void testBeginDispath() {
		schedulerThreadTest= new TaskSchedulerThread(spiderService, 0, 3, "1-2-3", 20);
		schedulerThreadTest.beginDispath();
	}

}
