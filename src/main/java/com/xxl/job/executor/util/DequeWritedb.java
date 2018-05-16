package com.xxl.job.executor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.GetSystemValue;

import jodd.util.StringUtil;

/**
 * 
 * @author huangyakun
 *
 */

public class DequeWritedb implements Runnable {
	///private Logger logger = LoggerFactory.getLogger(getClass());

	public final AtomicInteger ai2 =  new AtomicInteger(1);
	private static String fileNamedb="";
	private static String pathdb = "";
	private int  num=0;
	public DequeWritedb(String pathdb) {
		super();
		DequeWritedb.pathdb =pathdb;
	}

	@Override
	public void run() {
		try {
			while (ai2.get() <= 500) {
				String dbWriteline= DequeOuts.getDbWritepropers().poll();
				if(StringUtil.isBlank(dbWriteline)) {
					if(num<10) {
						num++;
					}
					
					Thread.sleep(500l*num);
					continue;
				}
				String files = fileNamedb;
				if ("".equals(files) || files.indexOf(".task") == -1 || files.indexOf("task") == -1) {
					files = createFile("task",".task");
				}
				try {
					/*FileWriter fw = new FileWriter(files, true);
					fw.write(ai2+"|"+dbWriteline);
					fw.close();
					*/
					writeFile(ai2+"|"+dbWriteline,files,pathdb);
					
					int nextIndex = ai2.incrementAndGet();
					if (nextIndex == 501) {
						files = createFile("task",".task");
						ai2.set(1);
					}
					Thread.sleep(200);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					
				}
			}
			
		} catch (Exception e) {
			
			//logger.info(AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean("写文件报错:"+AllErrorMessage.getExceptionStackTrace(e), DequeWritedb.class.getName(), "info");
		}
	}
	
	

	public static String createFile(String type,String extName)  {
		//Logger logger = LoggerFactory.getLogger(DequeWritedb.class);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		//ReadProperties read = new ReadProperties();
		String pro = GetSystemValue.getexecutorPort();
		String proName = type;
		String ip = "";
		Thread current = Thread.currentThread();
		long threadId = current.getId();
		File file = new File(pathdb);
		if (!file.exists()) {
			file.mkdir();
		}
		String files[];
		files = file.list();
		int fileNum = 1;
		for (int i = 0; i < files.length; i++) {
			if (files[i].indexOf(extName) != -1) {
                fileNum++;

			}
		}
		ip = GetSystemValue.getIp();
		String fileName2 = pathdb+ File.separator + sdf.format(new Date()) + "_" + pro + "_" + proName + "_" + ip + "_"
				+ threadId + "_" + (fileNum) + extName;
		// new File(fileName2);
		ObjectPase.setGetLoggBean(
				"文件名：" + fileName2, DequeWritedb.class.getName(), "info");
		//logger.info(StringHas.getSpiderHead()+",文件名：" + fileName2);
		fileNamedb = fileName2;
		return fileName2;

	}
	
	 private void writeFile(String content, String fileName,String path) throws IOException {
	        File f = new File(path);
	        boolean pool=f.setWritable(true);
	        if(!pool) {
	        	ObjectPase.setGetLoggBean("写文件报错:", DequeWritedb.class.getName(), "info");
	        }
	        if (!f.exists()) {  //如果该路径不存在，就创建该路径
	            f.mkdir();
	        }
	        
	        String filePath = fileName;  //得到完整文件路径
	        FileOutputStream fos = null;
	        FileChannel fc_out = null;
	        try {
	            fos = new FileOutputStream(filePath, true);
	            fc_out = fos.getChannel();
	            ByteBuffer buf = ByteBuffer.wrap(content.getBytes());
	            buf.put(content.getBytes());
	            buf.flip();
	            fc_out.write(buf);
	            
	        } catch (Exception e) {
	            
	        } finally {
	        	if (null != fos) {
	                fos.close();
	            }
	            if (null != fc_out) {
	                fc_out.close();
	            }
	            
	        }
	    } 

}
