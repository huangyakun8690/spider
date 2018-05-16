<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ustcinfo.ptp.yunting.model.NavigationInfo;
import com.ustcinfo.ptp.yunting.service.ISpiderService;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.xxl.job.executor.util.RedissionUtils;

@Service
public class SpiderServiceImpl implements ISpiderService {

	@Value("${jdbc.url}")
	private String jdbcUrl;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<JSONObject> fetchAcqDetail(int threadSign, Long siteId) {
		String sql = "update acq_details set state=?,isout=? where state=?";

		if (null == siteId) {
			siteId = findNeedSpiderSerialNo();
		}
		if (null == siteId) {
			return new ArrayList<JSONObject>();
		} else {
			sql += " and site_id=?" ;
			List<JSONObject> res = new ArrayList<JSONObject>();
			RLock lock = RedissionUtils.getLock("detail_lock");
			lock.lock(3l * 1000l, TimeUnit.SECONDS);
			sql+= " limit ? ";
			jdbcTemplate.update(sql,new Object[] {2,threadSign,1,siteId,50});
			String fetchSql = "select * from acq_details where state=2 and isout=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(fetchSql,new Object[] {threadSign});

			for (Map<String, Object> map : list) {
				JSONObject jsonObject = new JSONObject();
				Iterator<Entry<String, Object>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					jsonObject.put(entry.getKey(), entry.getValue());
				}
				res.add(jsonObject);
			}
			lock.unlock();
			return res;
		}
	}

	@Override
	public Long findNeedSpiderSerialNo() {
		String sql = "select site_id, count(1) as cnt from acq_details  where state=1 group by site_id limit 1 ";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		if (list.size() == 0) {
			return null;
		} else {
			Map<String, Object> map = list.get(0);
			return (Long) map.get("site_id");
		}
	}

	@Override
	public void updateState(List<HashMap<String, Object>> list, List<JSONObject> fetchResults) {

		Map<String, Object> tempMap = new HashMap<String, Object>();

		for (HashMap<String, Object> map : list) {
			// Page page = (Page)map.get(Constants.STORE_PAGE_HTML);
			// String url = page.getUrl().toString() ;
			String url = ((JSONObject) map.get(Constants.STORE_PAGE_JSON)).getString("url").toString();
			for (JSONObject jsonObject : fetchResults) {
				if (jsonObject.getString("url").equals(url)) {
					update(jsonObject.getInt("id"), 3);
					tempMap.put(url, url);
				}
			}
		}
		for (JSONObject jsonObject : fetchResults) {
			if (tempMap.get(jsonObject.getString("url")) == null) {
				update(jsonObject.getInt("id"), 4);
			}
		}
	}

	public void update(int acqDetailsId, int state) {
		String updateSql = "update acq_details set state=? where id=?";
		this.jdbcTemplate.update(updateSql, state, acqDetailsId);
	}

	@Override
	public Long findNeedSpiderTaskId() {
		// select task_id,count(1) from acq_navi where state=1 and task_id=
		// group by task_id
		String sql = "select task_id, count(1) as cnt from acq_navi  where state=1 group by task_id limit 1 ";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		if (list.size() == 0) {
			return null;
		} else {
			Map<String, Object> map = list.get(0);
			return Long.valueOf(map.get("task_id").toString());
		}
	}

	@Override
	public void updateNaviState(int id, int state) {
		String updateSql = "update acq_navi set state=? where id=?";
		this.jdbcTemplate.update(updateSql, state, id);
	}

	@Override
	public List<JSONObject> fetchAcqNavi(int threadSign, Long taskId) {
		String sql = "update acq_navi set state=2,isout=?  where state=1";

		if (null == taskId) {
			taskId = findNeedSpiderTaskId();
		}
		if (null == taskId) {
			return new ArrayList<JSONObject>();
		} else {
			sql += " and task_id=?";
			List<JSONObject> res = new ArrayList<JSONObject>();
			RLock lock = RedissionUtils.getLock("list_lock");
			lock.lock(3l * 1000l, TimeUnit.SECONDS);
			sql+=" limit 50";
			jdbcTemplate.update(sql,new Object[] {threadSign,taskId});
			String fetchSql = "select * from acq_navi where state=2 and isout=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(fetchSql,new Object[]{threadSign});
			for (Map<String, Object> map : list) {
				JSONObject jsonObject = new JSONObject();
				Iterator<Entry<String, Object>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					jsonObject.put(entry.getKey(), entry.getValue());
				}
				res.add(jsonObject);
			}
			lock.unlock();
			return res;
		}
	}

	@Override
	public JSONObject getTaskInfo(Long taskId) {
		String taskSql = "select * from acq_job_qrtz_trigger_info where id=?";
		Map<String, Object> map = this.jdbcTemplate.queryForMap(taskSql, taskId);
		JSONObject jsonObject = new JSONObject();
		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}

	@Override
	public void batchUpdateNaviState(String navIdsStr, int status) {
		String updateSql = new StringBuilder("update acq_navi set state=? where id in (").append(navIdsStr).append(")")
				.toString();
		this.jdbcTemplate.update(updateSql, status);

	}

	@Override
	public List<NavigationInfo> fetchAcqNaviForScheduleJobHandler(int threadSign, int limit, int category, int sourceType,
			int baseLocation) {
		String sql = "update acq_navi set state=2,isout = ? where category = ? and machine_location = ? and source_type = ? and (state = 1 or (state=3 and next_exec_time<=now())) limit ?";
		RLock lock=null;
		try {
			lock = RedissionUtils.getLock("list_lock");
			lock.lock(3l * 1000l, TimeUnit.SECONDS);
			jdbcTemplate.update(sql, threadSign, category, baseLocation, sourceType,limit);
			String fetchSql = "select * from acq_navi where category = ? and machine_location = ? and isout = ? and state=2";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(fetchSql, category, baseLocation, threadSign);
			if (list == null || list.isEmpty())
				return null;
			List<NavigationInfo> naviList = new ArrayList<NavigationInfo>();
			for (Map<String, Object> map : list) {
				NavigationInfo navigationInfo = new NavigationInfo();
				navigationInfo.setId((Long) map.get("id"));
				navigationInfo.setSeqNum(map.get("seq_num") != null ? map.get("seq_num") + "" : "");
				navigationInfo.setUrl(map.get("url") != null ? map.get("url") + "" : "");
				navigationInfo.setData(map.get("url") != null ? map.get("url") + "" : "");
				navigationInfo.setLevel(map.get("level") != null ? (Integer) (map.get("level")) : null);
				navigationInfo.setTaskId(map.get("task_id") != null ? (Integer) (map.get("task_id")) : null);
				navigationInfo.setSiteId(map.get("site_id") != null ? (Long) (map.get("site_id")) : null);
				navigationInfo
						.setNextExecTime(map.get("next_exec_time") != null ? (Date) (map.get("next_exec_time")) : null);
				navigationInfo
						.setPrevExecTime(map.get("prev_exec_time") != null ? (Date) (map.get("prev_exec_time")) : null);
				navigationInfo.setNeedScan(map.get("need_scan") != null ? (Integer) (map.get("state")) : null);
				naviList.add(navigationInfo);
			}
			return naviList;
		} catch (Exception e) {
			return null;
		} finally {
			if(null != lock) {
				lock.unlock();
			}
		}
		
		
	}

	@Override
	public void updateNaviExecTimeAndBatchNo(int category, int baseLocation, int threadSign, Date nextExecTime,
			String batchNo) {
		String updateSql = "update acq_navi set prev_exec_time = ?, next_exec_time = ?, last_batch_no = ? where category = ? and machine_location = ? and isout = ? and state = 2";
		this.jdbcTemplate.update(updateSql, new Date(), nextExecTime, batchNo, category, baseLocation, threadSign);

	}

	@Override
	public List<Integer> queryDistinctSourceTypes(int category, int baseLocation) {
		return this.jdbcTemplate.queryForList(
				"select distinct source_type from acq_navi where category = ? and machine_location = ?", Integer.class,
				category, baseLocation);
	}

	@Override
	public List<NavigationInfo> fetchAcqNaviForTaskSchedulerThread(int threadSign, int limit, int category, int sourceType,
			int baseLocation) {
		String sql = "update acq_navi set state=4,isout = ? where category = ? and machine_location = ? and source_type = ? and (state = 0 or ((state = 1 or state = 2) and next_exec_time<=now())) limit ?";
		RLock lock = null;
		try {
			lock = RedissionUtils.getLock("list_lock");
			lock.lock(3l, TimeUnit.SECONDS);
			
			jdbcTemplate.update(sql, threadSign, category, baseLocation, sourceType,limit);
			String fetchSql = "select * from acq_navi where category = ? and machine_location = ? and isout = ? and state = 4";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(fetchSql, category, baseLocation, threadSign);
			if (list == null || list.isEmpty())
				return null;
			List<NavigationInfo> naviList = new ArrayList<NavigationInfo>();
			for (Map<String, Object> map : list) {
				NavigationInfo navigationInfo = new NavigationInfo();
				navigationInfo.setId((Long) map.get("id"));
				navigationInfo.setSeqNum(map.get("seq_num") != null ? map.get("seq_num") + "" : "");
				navigationInfo.setUrl(map.get("url") != null ? map.get("url") + "" : "");
				navigationInfo.setData(map.get("url") != null ? map.get("url") + "" : "");
				navigationInfo.setLevel(map.get("level") != null ? (Integer) (map.get("level")) : null);
				navigationInfo.setTaskId(map.get("task_id") != null ? (Integer) (map.get("task_id")) : null);
				navigationInfo.setSiteId(map.get("site_id") != null ? (Long) (map.get("site_id")) : null);
				navigationInfo
						.setNextExecTime(map.get("next_exec_time") != null ? (Date) (map.get("next_exec_time")) : null);
				navigationInfo
						.setPrevExecTime(map.get("prev_exec_time") != null ? (Date) (map.get("prev_exec_time")) : null);
				navigationInfo.setNeedScan(map.get("need_scan") != null ? (Integer) (map.get("state")) : null);
				naviList.add(navigationInfo);
			}
			return naviList;
		} catch (Exception e) {
			return null;
		}finally {
			if(null != lock) {
				lock.unlock();
			}
			
		}
		
		
	}

}
=======
package com.ustcinfo.ptp.yunting.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;

import org.hibernate.annotations.Synchronize;
import org.json.JSONObject;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ustcinfo.ptp.yunting.model.InfoEntity;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;
import com.ustcinfo.ptp.yunting.service.ISpiderService;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.xxl.job.executor.util.RedissionUtils;

import us.codecraft.webmagic.Page;

@Service
public class SpiderServiceImpl implements ISpiderService {

	@Value("${jdbc.url}")
	private String jdbcUrl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<JSONObject> fetchAcqDetail(int threadSign,Long siteId) {
		String sql = "update acq_details set state=2,isout="+threadSign+"  where state=1" ;
		
		if(null==siteId){
			siteId = findNeedSpiderSerialNo() ;
		}
		if(null==siteId){
			return new ArrayList<JSONObject>() ;
		}else{
			sql += " and site_id="+siteId ;
			List<JSONObject> res = new ArrayList<JSONObject>() ;
			RLock lock = RedissionUtils.getLock("detail_lock") ;
			lock.lock(3*1000, TimeUnit.SECONDS);
				jdbcTemplate.execute(sql+" limit 50 ");
				String fetchSql = "select * from acq_details where state=2 and isout="+threadSign ;
				List<Map<String, Object>> list = jdbcTemplate.queryForList(fetchSql) ;

				for(Map<String, Object> map:list){
					JSONObject jsonObject =new JSONObject() ;
					Iterator<Entry<String, Object>> it = map.entrySet().iterator() ;
					while(it.hasNext()){
						Entry<String, Object> entry= it.next() ;
						jsonObject.put(entry.getKey(), entry.getValue()) ;
					}
					res.add(jsonObject) ;
				}
			lock.unlock();
			return res ;
		}
	}
	
	@Override
	public Long findNeedSpiderSerialNo(){
		String sql = "select site_id, count(1) as cnt from acq_details  where state=1 group by site_id limit 1 " ;
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql) ;
		if(list.size()==0){
			return null ;
		}else{
			Map<String,Object> map = list.get(0) ;
			return (Long) map.get("site_id") ;
		}
	}

	@Override
	public void updateState(List<HashMap<String, Object>> list, List<JSONObject> fetchResults) {
		
		Map<String,Object> tempMap = new HashMap<String,Object>() ;
		
		for(HashMap<String,Object> map : list )
		{	
//			Page page = (Page)map.get(Constants.STORE_PAGE_HTML);
//			String url = page.getUrl().toString() ;
			String url = ((JSONObject)map.get(Constants.STORE_PAGE_JSON)).getString("url").toString();
			for(JSONObject jsonObject :fetchResults){
				if(jsonObject.getString("url").equals(url)){
					update(jsonObject.getInt("id"),3) ;
					tempMap.put(url, url) ;
				}
			}
		}
		for(JSONObject jsonObject :fetchResults){
			if(tempMap.get(jsonObject.getString("url"))==null){
				update(jsonObject.getInt("id"),4) ;
			}
		}
	}
	
	public void update(int acqDetailsId,int state){
		String updateSql = "update acq_details set state=? where id=?" ;
		this.jdbcTemplate.update(updateSql, state,acqDetailsId) ;
	}

	@Override
	public Long findNeedSpiderTaskId() {
		// select task_id,count(1) from acq_navi where state=1 and task_id= group by task_id
		String sql = "select task_id, count(1) as cnt from acq_navi  where state=1 group by task_id limit 1 " ;
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql) ;
		if(list.size()==0){
			return null ;
		}else{
			Map<String,Object> map = list.get(0) ;
			return Long.valueOf(map.get("task_id").toString()) ;
		}	
	}

	@Override
	public void updateNaviState(int id, int state) {
		String updateSql = "update acq_navi set state=? where id=?" ;
		this.jdbcTemplate.update(updateSql, state,id) ;
	}

	@Override
	public List<JSONObject> fetchAcqNavi(int threadSign, Long taskId) {
		String sql = "update acq_navi set state=2,isout="+threadSign+"  where state=1" ;
		
		if(null==taskId){
			taskId = findNeedSpiderTaskId() ;
		}
		if(null==taskId){
			return new ArrayList<JSONObject>() ;
		}else{
			sql += " and task_id="+taskId ;
			List<JSONObject> res = new ArrayList<JSONObject>() ;
			RLock lock = RedissionUtils.getLock("list_lock") ;
			lock.lock(3*1000, TimeUnit.SECONDS);
				jdbcTemplate.execute(sql+" limit 50");
				String fetchSql = "select * from acq_navi where state=2 and isout="+threadSign;
				List<Map<String, Object>> list = jdbcTemplate.queryForList(fetchSql) ;
				for(Map<String, Object> map:list){
					JSONObject jsonObject =new JSONObject() ;
					Iterator<Entry<String, Object>> it = map.entrySet().iterator() ;
					while(it.hasNext()){
						Entry<String, Object> entry= it.next() ;
						jsonObject.put(entry.getKey(), entry.getValue()) ;
					}
					res.add(jsonObject) ;
				}
			lock.unlock();
			return res ;
		}
	}

	@Override
	public JSONObject getTaskInfo(Long taskId) {
		String taskSql = "select * from acq_job_qrtz_trigger_info where id=?" ;
		Map<String,Object> map = this.jdbcTemplate.queryForMap(taskSql,taskId);
		JSONObject jsonObject =new JSONObject() ;
		Iterator<Entry<String, Object>> it = map.entrySet().iterator() ;
		while(it.hasNext()){
			Entry<String, Object> entry= it.next() ;
			jsonObject.put(entry.getKey(), entry.getValue()) ;
		}
		return jsonObject;
	}
	
	
	@Override
	public void batchUpdateNaviState(String navIdsStr, int status) {
		String updateSql = new StringBuilder("update acq_navi set state=? where id in (").append(navIdsStr).append(")").toString();
		this.jdbcTemplate.update(updateSql, status) ;
		
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
