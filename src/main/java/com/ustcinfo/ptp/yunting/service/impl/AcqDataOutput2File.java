package com.ustcinfo.ptp.yunting.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ustcinfo.ptp.yunting.service.IAcqDataOutput;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.DequeWritedb;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.StringHas;

public class AcqDataOutput2File implements IAcqDataOutput {
	private final AtomicInteger ai = new AtomicInteger(1);
	private static String fileName = "";
	private long num = 0;
	private static boolean isFor = true;
	

	@Override
	public void write(String path) {
		
		do {
				try {
					Map<String, Object> map = DequeOuts.getOutPropers().poll();
					if (map == null || map.size() < 0) {
						Thread.sleep(500l * num);
						num=num>10?num:num++;
						continue;
					}
					num=0;
					String files = fileName;
					String outFormat = map.get("outFormat")!=null ? map.get("outFormat")+"":"";
					String type=(map.get("type") != null ? map.get("type").toString() : "");
					String fileType = "";
					String fileName2="";
					if(!files.equals("")) {
						fileName2=files.substring(path.length()+1);
					}
					if(fileName2!=null && !"".equals(fileName2)) {
						String [] filess =fileName2.split("_");
						fileType = filess[2]+"_"+filess[3];
					}
					if ("".equals(files) || files.indexOf(".log") == -1 || files.indexOf(path+File.separator+outFormat)!=0 || fileType.indexOf(type) == -1) {
						//创建文件
						if(!"".equals(files)) {
							renameFile(files,files.replace(".log", ".out"));
						}
						files = StringHas.createFile(path+File.separator+outFormat, (map.get("type") != null ? map.get("type").toString() : ""), ".log");
						AcqDataOutput2File.setFileName(files);
					}
					
					 
					String line = (map.get("line") != null ? map.get("line").toString() : "");
					if (line.indexOf("|id|") != -1) {
						line = line.replace("|id|", "|" + ai.get() + "|");
					}
					
					//写入文件
					StringHas.writeFileOut(line.substring(1), files, path+File.separator+outFormat);
					
					int nextIndex = ai.incrementAndGet();
					if (nextIndex == 101) {
						//创建文件
						renameFile(files,files.replace(".log", ".out"));
						files = StringHas.createFile(path+File.separator+outFormat, (map.get("type") != null ? map.get("type").toString() : ""), ".log");
						AcqDataOutput2File.setFileName(files);
						
						ai.set(1);
					}
					Thread.sleep(200);
				} catch (InterruptedException e) {
					
					Thread.currentThread().interrupt();
					
				} catch (IOException e) {
					ObjectPase.setGetLoggBean(
							"写文件失败：" + AllErrorMessage.getExceptionStackTrace(e), DequeWritedb.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
					
				}
			
		} while (ai.get() <= 100 && isFor);
	
			
	}
	
	
	/**
	 * 重命名文件
	 * @param oldFileName
	 * @param newFileName
	 */
	public void renameFile(String oldFileName, String newFileName) {
		File oldFile = new File(oldFileName);
		File newFile = new File(newFileName);
		boolean flag = oldFile.renameTo(newFile);
		if (flag) {
			ObjectPase.setGetLoggBean(
					"重命名文件成功", DequeWritedb.class.getName(), JsonKeys.LOG_LEVEL_INFO);
		} else {
			ObjectPase.setGetLoggBean(
					"重命名文件失败", DequeWritedb.class.getName(), JsonKeys.LOG_LEVEL_INFO);
		}
	}


	public static String getFileName() {
		return fileName;
	}


	public static void setFileName(String fileName) {
		AcqDataOutput2File.fileName = fileName;
	}


	public static void setFor(boolean isFor) {
		AcqDataOutput2File.isFor = isFor;
	}


	
	

}
