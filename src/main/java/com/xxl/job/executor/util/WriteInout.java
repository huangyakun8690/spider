package com.xxl.job.executor.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ustcinfo.ptp.yunting.model.OutProper;
import com.ustcinfo.tpc.framework.core.util.DequeOuts;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;

/****
 * 
 * @author liping
 *
#BEGINDOC
#RECNO=序号
#ID=唯一标识（url的md5值）
#RECTYPE=信源类型（1.新闻，2.微博，3.微信，4.APP，5.报刊，6.论坛，7.博客，8.视频，9.商机）
#THESOURCE=采集来源
#REFERENCE=原文链接地址
#DATE=采集时间，时间戳格式，如1444879817
#FFDCREATE=发布时间，时间戳格式，如1444878845
#LANGUAGETYPE=编码字符集
#DRESOURCE=原始来源
#TITLE=标题
#CONTENT=内容
#ABSTRACT=摘要
#RECEMOTIONAL=情感类型（1.正面，0.中立，-1.负面）
#AREA=地域信息
#FREQUENCYWORD=高频词
#LIKEINFO=相似舆情的链接地址，多个用“,”
#LIKEINFOCOUNT=相似舆情数量
#SCREEN_NAME=作者名称
#COMMENTS=评论数
#REPORTCOUNT=转发数
#READCOUNT=浏览量
#WEIBOTYPE=微博用户类型，如果不是微博此字段可以为空（1.蓝V，2.橙V，3.微博达人，4.草根大V）
#WEIXINTYPE=微信用户类型，如果不是微信此字段的值可以为空（1.认证用户，2.自媒体用户）
#HOTVALUE=舆情热度值
#MEDIATYPE=媒体类型（1.全国性媒体，2.地方性媒体）
#KEYWORD=对应词条配置表的ID值
#ALARMLEVEL=告警级别
#ENDDOC
 */
public class WriteInout {
	/* 1、所有数据都先写到一个固定文件里面 文件名叫 Spider.log
	 * 2、等 Spider.log 写满100条之后 重命名为 *.out 文件
	 * 3、然后就没了
	 * atomInteger
	 * 队列
	 * 
	 * 文件路径 在项目下面
	 * */
	public static  AtomicInteger ai = new AtomicInteger(1) ;
	private static String fileName="";
	public static  void write(String line,String type){
		
		//判断map是否为空
		//获取最后一个 开始的序号
		//判断是否是新建文件

		try {
			String files = fileName;
			if(line==null ||"".equals(line)){
				return ;
			}
			if("".equals(files)||files.indexOf(".out")==-1 ){
				files=createFile(type);
			}
			while(ai.get()<=100){
				FileWriter fw = new FileWriter(files,true);
				fw.write(ai.get()+"|"+line);
				fw.close();
				int nextIndex = ai.incrementAndGet() ;
				if(nextIndex==101){
					files=createFile(type);
					ai.set(1);
				}
				Thread.sleep(200);
			}
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}

	public static  String createFile(String type){
		SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMddHHmmss");
		 ReadProperties read=new ReadProperties();
		  String pro=read.readProperties("application.properties","xxl.job.executor.port");
		  String proName=type;
		  String ip=read.readProperties("application.properties","xxl.job.executor.ip");
		  Thread current = Thread.currentThread();  
		  long threadId=current.getId();
		  File file=new File("./");
         String files[];
         files=file.list();
         int fileNum=1;
         for(int i=0;i<files.length;i++)
         {
           if(files[i].indexOf(".out") != -1){
           	 fileNum++;
           }
         } 
         String fileName2 = sdf.format(new Date())+"_"+pro+"_"+proName+"_"+ip+"_"+threadId+"_"+(fileNum)+".out";
         //new File(fileName2);
         System.out.println("文件名："+fileName2);
         fileName=fileName2;
         return  fileName2;
         
	}
	 
	
}
