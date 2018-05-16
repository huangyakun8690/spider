<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.GetSystemValue;


/**
 * 
 * @author huangyakun
 *
 */

public class DequeWrite implements Runnable {
	//private Logger logger = LoggerFactory.getLogger(getClass());
	public final AtomicInteger ai  = new AtomicInteger(1);	
	private static String fileName = "";
	private static String path = "";
    private int  num=0;
 
	public DequeWrite(String path) {
		super();
		DequeWrite.path = path;
	
	}

	@Override
	public void run() {
		try {
			/*
			 * 
			 * */
			while (ai.get() <= 100) {

				Map<String, Object> map = DequeOuts.getOutPropers().poll();
				if (map == null || map.size() < 0) {
					if(num<10) {
						num++;
					}
					
					Thread.sleep(500l*num);
					
					continue;
				}
				num=0;
				String files = fileName;
				String type=(map.get("type") != null ? map.get("type").toString() : "");
				String fileType = "";
				if(files!=null && !"".equals(files)) {
					String filess[] =files.split("_");
					fileType = filess[2]+"_"+filess[3];
				}
				System.out.println(fileType);
				if ("".equals(files) || files.indexOf(".out") == -1 || fileType.indexOf(type) == -1) {
					files = createFile((map.get("type") != null ? map.get("type").toString() : ""),".out");
				}
				// if(map.isEmpty()){
				// continue;
				// }
				//写入文件
				FileWriter fw = new FileWriter(files, true);
				try {
					String line = (map.get("line") != null ? map.get("line").toString() : "");
					if (line.indexOf("|id|") != -1) {
						line = line.replace("|id|", "|" + ai.get() + "|");
					}
			
					fw.write(line.substring(1));
				} catch (Exception e) {
					
				}finally {
					fw.close();
				}
				
				
				int nextIndex = ai.incrementAndGet();
				if (nextIndex == 101) {
					files = createFile((map.get("type") != null ? map.get("type").toString() : ""),".out");
					ai.set(1);
				}
				Thread.sleep(200);
			}
			
			
			
			
			
		} catch (Exception e) {
			
			ObjectPase.setGetLoggBean(
					"写文件报错:"+AllErrorMessage.getExceptionStackTrace(e), DequeWritedb.class.getName(), "info");
	
			//logger.info(StringHas.getSpiderHead()+",写入文件异常，"+AllErrorMessage.getExceptionStackTrace(e));
		}
	}

	/**
	 * 创建文件
	 * @param type
	 * @param extName
	 * @return
	 */
	public static String createFile(String type,String extName) {
		//Logger logger = LoggerFactory.getLogger(DequeWrite.class);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String pro = GetSystemValue.getexecutorPort();
		String proName = type;
		String ip = "";
		Thread current = Thread.currentThread();
		long threadId = current.getId();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		String files[];
		files = file.list();
		int fileNum = 1;
		for (int i = 0; i < files.length; i++) {
			if (files[i].indexOf(extName) != -1) {
				if (files[i].indexOf(extName) != -1) {
					if (files[i].indexOf(proName) != -1) {
						fileNum++;
					}
				}
			}
		}
		ip= GetSystemValue.getIp();
		
		String fileName2 = path + File.separator + sdf.format(new Date()) + "_" + pro + "_" + proName + "_" + ip + "_"
				+ threadId + "_" + (fileNum) + extName;
		// new File(fileName2);
		//logger.info(StringHas.getSpiderHead()+"文件名：" + fileName2);
		ObjectPase.setGetLoggBean(
				"文件名：" + fileName2, DequeWritedb.class.getName(), "info");
		fileName = fileName2;
		return fileName2;

	}

}
=======
package com.xxl.job.executor.util;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;
import com.xxl.job.core.log.XxlJobLogger;

/**
 * 
 * @author huangyakun
 *
 */

public class DequeWrite implements Runnable {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public final AtomicInteger ai = new AtomicInteger(1);
	private static String fileName = "";
	private static String path = "";

	public DequeWrite(String path) {
		super();
		this.path = path;
	}

	@Override
	public void run() {
		try {
			/*
			 * 
			 * */

			while (ai.get() <= 100) {

				Map<String, Object> map = DequeOuts.getOutPropers().poll();
				if (map == null || map.size() < 0) {
					continue;
				}
				String files = fileName;
				String type=(map.get("type") != null ? map.get("type").toString() : "");
				if ("".equals(files) || files.indexOf(".out") == -1 || files.indexOf(type) == -1) {
					files = createFile((map.get("type") != null ? map.get("type").toString() : ""));
				}
				// if(map.isEmpty()){
				// continue;
				// }

				FileWriter fw = new FileWriter(files, true);
				String line = (map.get("line") != null ? map.get("line").toString() : "");
				if (line.indexOf("|id|") != -1) {
					line = line.replace("|id|", "|" + ai.get() + "|");
				}
				logger.info("写入文件数据为：" + line.substring(1));
				fw.write(line.substring(1));
				fw.close();
				int nextIndex = ai.incrementAndGet();
				if (nextIndex == 101) {
					files = createFile((map.get("type") != null ? map.get("type").toString() : ""));
					ai.set(1);
				}
				Thread.sleep(200);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
		}
	}

	public static String createFile(String type) {
		Logger logger = LoggerFactory.getLogger(DequeWrite.class);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		ReadProperties read = new ReadProperties();
		String pro = read.readProperties("application.properties", "xxl.job.executor.port");
		String proName = type;
		String ip = read.readProperties("application.properties", "xxl.job.executor.ip");
		Thread current = Thread.currentThread();
		long threadId = current.getId();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		String files[];
		files = file.list();
		int fileNum = 1;
		for (int i = 0; i < files.length; i++) {
			if (files[i].indexOf(".out") != -1) {
				if (files[i].indexOf(".out") != -1) {
					if (files[i].indexOf(proName) != -1) {
						fileNum++;
					}
				}
			}
		}
		String fileName2 = path + File.separator + sdf.format(new Date()) + "_" + pro + "_" + proName + "_" + ip + "_"
				+ threadId + "_" + (fileNum) + ".out";
		// new File(fileName2);
		logger.info("文件名：" + fileName2);
		fileName = fileName2;
		return fileName2;

	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
