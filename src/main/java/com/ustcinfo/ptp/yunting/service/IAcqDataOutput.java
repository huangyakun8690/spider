package com.ustcinfo.ptp.yunting.service;


/**
 * 采集数据输出接口
 * @author Ramboo
 *
 */
public interface IAcqDataOutput {

	/**
	 * 
	 * @param path  输出路径
	 */
	void write(String path);
	
}
