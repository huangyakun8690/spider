package com.xxl.job.executor.util;

import com.ustcinfo.ptp.yunting.support.ExcelException;

public class ExceprRepertory {

	/**
	 * 抛出异常信息
	 * 5 页面规则不存在
	 * 6 系统异常
	 * 
	 * @param num
	 * @param meaasge
	 * @throws Exception
	 */
	public static void setExcep(int num,String meaasge) throws Exception{
		switch (num) {
		case 5:
			throw new ExcelException("5:页面规则为空");
		case 6:
			throw new ExcelException("6:"+meaasge);
		default:
			break;
		}
		
	}
	
	public static void main(String[] args) {
		try {
			ExceprRepertory.setExcep(5, "页面");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}
	}
}
