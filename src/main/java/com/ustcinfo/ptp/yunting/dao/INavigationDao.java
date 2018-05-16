package com.ustcinfo.ptp.yunting.dao;

import java.util.List;

import com.ustcinfo.ptp.yunting.model.NavigationInfo;

public interface INavigationDao {

	public void saveNavigationDao(NavigationInfo navigationInfo);
	
	public List<NavigationInfo> getNaviGation(Long id);
}
