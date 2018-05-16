<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.model.NavigationInfo;

public interface ISpiderService {
	
	/*
	 * 详情页批量获取
	 */
	public  List<JSONObject> fetchAcqDetail(int threadSign,Long serialNo) ;
	
	public Long findNeedSpiderSerialNo();

	public void updateState(List<HashMap<String, Object>> list, List<JSONObject> fetchResults);
	
	/*
	 * 列表页批量获取
	 */
	public Long findNeedSpiderTaskId();

	public void updateNaviState(int id, int state);
	
	public  List<JSONObject> fetchAcqNavi(int threadSign,Long taskId) ;
	
	
	public JSONObject getTaskInfo(Long taskId) ;

	/*
	 * 批量修改列表页状态
	 */
	public void batchUpdateNaviState(String navIdsStr, int status);

	/*
	 * 批量取出待执行的导航数据
	 */
	List<NavigationInfo> fetchAcqNaviForScheduleJobHandler(int threadSign, int limit, int category, int sourceType, int baseLocation);
	
	/*
	 * 根据爬虫大类和基地查询信源类型
	 */
	List<Integer> queryDistinctSourceTypes(int category, int baseLocation);
	
	/*
	 * 更新上次执行时间、下次执行时间、最末批次号
	 */
	void updateNaviExecTimeAndBatchNo(int category, int baseLocation, int threadSign, Date nextExecTime,
			String batchNo);

	public List<NavigationInfo> fetchAcqNaviForTaskSchedulerThread(int threadSign, int limit, int category, int sourceType,
			int baseLocation);
}
=======
package com.ustcinfo.ptp.yunting.service;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public interface ISpiderService {
	
	/*
	 * 详情页批量获取
	 */
	public  List<JSONObject> fetchAcqDetail(int threadSign,Long serialNo) ;
	
	public Long findNeedSpiderSerialNo();

	public void updateState(List<HashMap<String, Object>> list, List<JSONObject> fetchResults);
	
	/*
	 * 列表页批量获取
	 */
	public Long findNeedSpiderTaskId();

	public void updateNaviState(int id, int state);
	
	public  List<JSONObject> fetchAcqNavi(int threadSign,Long taskId) ;
	
	
	public JSONObject getTaskInfo(Long taskId) ;

	/*
	 * 批量修改列表页状态
	 */
	public void batchUpdateNaviState(String navIdsStr, int status);
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
