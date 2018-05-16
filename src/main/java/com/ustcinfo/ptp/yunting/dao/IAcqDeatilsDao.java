package com.ustcinfo.ptp.yunting.dao;

import java.util.List;

import com.ustcinfo.ptp.yunting.model.AcqDeatilsInfo;

public interface IAcqDeatilsDao {
	
   public void saveAcqDaetil(AcqDeatilsInfo acqDeatilsInfo);
   
   public List<AcqDeatilsInfo> getAcqDeatilsInfoList(int start,int end);
   
   public void updateAcqDaetil(AcqDeatilsInfo acqDeatilsInfo);
}
