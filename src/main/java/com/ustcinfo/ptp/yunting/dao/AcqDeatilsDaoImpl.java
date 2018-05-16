package com.ustcinfo.ptp.yunting.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ustcinfo.ptp.yunting.model.AcqDeatilsInfo;
@Repository
public class AcqDeatilsDaoImpl implements IAcqDeatilsDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void saveAcqDaetil(AcqDeatilsInfo acqDeatilsInfo) {
		
		String insertSql="insert into acq_details(site_id,url,state,navi_id,isout) VALUES(?,?,?,?,?)";
		try {
			this.jdbcTemplate.update(insertSql,new Object[]{acqDeatilsInfo.getSiteId(),acqDeatilsInfo.getUrl(),
					acqDeatilsInfo.getState(),acqDeatilsInfo.getNaviId(),acqDeatilsInfo.getIsOut()});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public List<AcqDeatilsInfo> getAcqDeatilsInfoList(int start, int end) {
		String sql="SELECT t.id,t.site_id,t.navi_id,t.url,t.state,t.isout FROM acq_details t where t.state=1 LIMIT ?,?";
		String updateSql="update acq_details t set t.state=? where t.id=?";
		List<AcqDeatilsInfo> acqDeatils =  new ArrayList<AcqDeatilsInfo>();
		try {
			List<Map<String,Object>> list= this.jdbcTemplate.queryForList(sql,new Object[]{start,end});
			for(Map<String,Object> map: list){
				AcqDeatilsInfo acqDeatilsInfo= new AcqDeatilsInfo();
				Long id=Long.parseLong((map.get("id")!=null ? map.get("id")+"" :"0")) ;
				acqDeatilsInfo.setId(id);
				acqDeatilsInfo.setSiteId(Long.parseLong(map.get("site_id")!=null ? map.get("site_id")+"" :"0"));
				acqDeatilsInfo.setNaviId(Long.parseLong(map.get("navi_id")!=null ? map.get("navi_id")+"" :"0"));
				acqDeatilsInfo.setUrl(map.get("url")!=null ? map.get("url")+"" :"");
				acqDeatilsInfo.setState(Integer.parseInt(map.get("state")!=null ? map.get("state")+"" :""));
				acqDeatilsInfo.setIsOut(Integer.parseInt(map.get("isout")!=null ? map.get("isout")+"" :""));
				acqDeatils.add(acqDeatilsInfo);
				this.jdbcTemplate.update(updateSql,new Object[]{2,id});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return acqDeatils;
	}

	@Override
	public void updateAcqDaetil(AcqDeatilsInfo acqDeatilsInfo) {
		String sql="update acq_details t set t.state=? where t.id=?";
		try {
			this.jdbcTemplate.update(sql,new Object[]{acqDeatilsInfo.getState(),acqDeatilsInfo.getId()});
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

}
