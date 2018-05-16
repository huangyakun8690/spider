/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ustcinfo.ptp.yunting.service.ISpiderService;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class SpiderServiceImplTest extends Basejunit{

	@Autowired
	private ISpiderService spiderService;
	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#fetchAcqDetail(int, java.lang.Long)}.
	 */
	@Test
	public void testFetchAcqDetail() {
		spiderService.fetchAcqDetail(1, 123l);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#findNeedSpiderSerialNo()}.
	 */
	@Test
	public void testFindNeedSpiderSerialNo() {
		spiderService.findNeedSpiderSerialNo();
	}


	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#updateState(java.util.List, java.util.List)}.
	 */
	@Test
	public void testUpdateState() {
		List<HashMap<String, Object>> list= new ArrayList<>();
		List<JSONObject> fetchResults =  new ArrayList<>();
		spiderService.updateState(list, fetchResults);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#update(int, int)}.
	 */
	@Test
	public void testUpdate() {
		
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#findNeedSpiderTaskId()}.
	 */
	@Test
	public void testFindNeedSpiderTaskId() {
		spiderService.findNeedSpiderTaskId();
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#updateNaviState(int, int)}.
	 */
	@Test
	public void testUpdateNaviState() {
		spiderService.updateNaviState(121, 2);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#fetchAcqNavi(int, java.lang.Long)}.
	 */
	@Test
	public void testFetchAcqNavi() {
		spiderService.fetchAcqNavi(45697, 123l);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#getTaskInfo(java.lang.Long)}.
	 */
	@Test
	public void testGetTaskInfo() {
		spiderService.getTaskInfo(121l);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#batchUpdateNaviState(java.lang.String, int)}.
	 */
	@Test
	public void testBatchUpdateNaviState() {
		spiderService.batchUpdateNaviState(12+"", 2);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#fetchAcqNaviForScheduleJobHandler(int, int, int, int, int)}.
	 */
	@Test
	public void testFetchAcqNaviForScheduleJobHandler() {
		
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#updateNaviExecTimeAndBatchNo(int, int, int, java.util.Date, java.lang.String)}.
	 */
	@Test
	public void testUpdateNaviExecTimeAndBatchNo() {
		spiderService.updateNaviExecTimeAndBatchNo(1, 2, 156, new Date(), "2");
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#queryDistinctSourceTypes(int, int)}.
	 */
	@Test
	public void testQueryDistinctSourceTypes() {
		spiderService.queryDistinctSourceTypes(1, 0);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.SpiderServiceImpl#fetchAcqNaviForTaskSchedulerThread(int, int, int, int, int)}.
	 */
	@Test
	public void testFetchAcqNaviForTaskSchedulerThread() {
		spiderService.fetchAcqNaviForTaskSchedulerThread(1, 50, 2, 2, 4);
	}

}
