package com.ustcinfo.ptp.yunting.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ustcinfo.ptp.yunting.model.NavigationInfo;
@Repository
public class NavigationDaoImpl implements INavigationDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void saveNavigationDao(NavigationInfo navigationInfo) {
		String sql="insert into acq_navi(seq_num,url,data,level,task_id,state) values(?,?,?,?,?,?)";
		try {
			jdbcTemplate.update(sql,new Object[]{navigationInfo.getSeqNum(),navigationInfo.getUrl(),
					navigationInfo.getData(),navigationInfo.getLevel(),navigationInfo.getTaskId(),navigationInfo.getState()});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public List<NavigationInfo> getNaviGation(Long id) {
		String querySql="SELECT t.`data`,t.seq_num,t.url,t.level,t.task_id,state FROM acq_navi t  where t.id=?";
		List<NavigationInfo> naviGatList=new ArrayList<NavigationInfo>();
		try {
			List<Map<String,Object>> list= this.jdbcTemplate.queryForList(querySql,new Object[]{id});
			for(Map<String,Object> map:list){
				NavigationInfo navigationInfo= new NavigationInfo();
				navigationInfo.setId(id);
				navigationInfo.setSeqNum(map.get("seq_num")!=null ?map.get("seq_num")+"" :"");
				navigationInfo.setUrl(map.get("url")!=null ?map.get("url")+"" :"");
				navigationInfo.setData(map.get("url")!=null ?map.get("url")+"" :"");
				navigationInfo.setLevel(map.get("level")!=null ?(Integer)(map.get("level")) :null);
				navigationInfo.setTaskId(map.get("task_id")!=null ?(Integer)(map.get("task_id")) :null);
				navigationInfo.setState(map.get("state")!=null ?(Integer)(map.get("state")) :null);
				naviGatList.add(navigationInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return naviGatList;
	}

}
