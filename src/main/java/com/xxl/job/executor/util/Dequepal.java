package com.xxl.job.executor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.Application;
import com.alibaba.fastjson.JSON;
import com.ustcinfo.ptp.yunting.model.PalpitationInfo;
import com.ustcinfo.ptp.yunting.model.PalpitationStaticInfo;
import com.ustcinfo.ptp.yunting.service.impl.AcqDataOutput2File;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.tpc.framework.core.util.GetSystemValue;

/**
 * 心跳队列
 * 
 * @author huangyakun
 *
 */

public class Dequepal implements Runnable {

	private String desc = "";
	private String appType = "";
	// private static Logger logger = LoggerFactory.getLogger(Dequepal.class);

	// 20180125lly，消息队列状态是否可用
	private static boolean MQ_AVAILABLE = true;

	// 20180130lly，容器化后将宿主机ip存入容器内/etc/hosts，需要获取hostname为spider的host address
	// 容器启动时加参数 --add-host=spider:$(获取宿主机IP的命令)
	// private static String SPIDER_HOSTNAME = "spider";
	private boolean isfor = true;

	public Dequepal(String desc, String appType) {
		super();
		this.desc = desc;
		this.appType = appType;
	}

	/**
	 * 获取心跳队列信息
	 * 
	 * @return
	 */
	public PalpitationInfo setpalInfo(String desc, String type) {
		try {

			PalpitationInfo palpitationInfo = new PalpitationInfo();

			palpitationInfo.setSpiderId(GetSystemValue.getDockerId());
			palpitationInfo.setType(type);// 1是导航 2是目标

			palpitationInfo.setIp(GetSystemValue.getIp());
			palpitationInfo.setDesc(desc);
			palpitationInfo.setAppType(appType);
			palpitationInfo.setBase(String.valueOf(Application.getBaseLocation()));
			String uptimes = StringHas.getDateStr("yyyy-MM-dd HH:mm:ss", "");
			palpitationInfo.setUptime(uptimes);
			palpitationInfo.setStrage("");
			PalpitationStaticInfo staticInfo = new PalpitationStaticInfo();
			if (type.equals("1")) {
				int succ = DequeOuts.getNaviTaskSucckAi().get();
				int fail = DequeOuts.getNaviTaskFailkAi().get();
				DequeOuts.getNaviTaskSucckAi().set(0);
				DequeOuts.getNaviTaskFailkAi().set(0);
				staticInfo.setTotal((succ+ fail) + "");
				staticInfo.setSuccess(succ + "");
				staticInfo.setFailure(fail + "");
				
			}
			if (type.equals("2")) {
				int succ =  DequeOuts.getDetailTaskSucckAi().get() ;
				int fail =  DequeOuts.getDetailTaskFailkAi().get();
				DequeOuts.getDetailTaskSucckAi().set(0);
				DequeOuts.getDetailTaskFailkAi().set(0);
				staticInfo.setTotal((succ + fail) + "");
				staticInfo.setSuccess(succ + "");
				staticInfo.setFailure(fail + "");
			
				
				
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				list = DequeOuts.getStaticlist();
				List<Map<String, Object>> listred = new ArrayList<Map<String, Object>>();
				Iterator<Map<String, Object>> iter = list.iterator();
				while (iter.hasNext()) {
					Map<String, Object> mapst = new HashMap<String, Object>();
					Map<String, Object> item = iter.next();
					mapst = item;
					listred.add(mapst);
					iter.remove();

				}

				staticInfo.setSites(listred);

			}

			palpitationInfo.setStaticInfo(staticInfo);
			ObjectPase.setGetLoggBean("获取心跳数据为：" + JSON.toJSONString(palpitationInfo), Dequepal.class.getName(),
					"info");

			return palpitationInfo;
		} catch (Exception e) {
			
			ObjectPase.setGetLoggBean("获取异常：" + AllErrorMessage.getExceptionStackTrace(e), Dequepal.class.getName(),
					"error");

		}
		return null;

	}

	public void run() {

		while (isfor) {
			try {

				ObjectPase.setGetLoggBean("发送心跳数据", Dequepal.class.getName(), "info");

				// 导航心跳
				PalpitationInfo palpitationInfo = setpalInfo(desc, "1");

				JSONObject jsonObject = new JSONObject(JSON.toJSONString(palpitationInfo));
				Producer.sendMessage(jsonObject, "Q_HeartBeat", "TAG_TASK", "KEYS_" + new Date().getTime(),
						JsonKeys.PRODUCER_MQ_NAME);
				Thread.sleep(500);
				// 详情心跳
				PalpitationInfo deatilPalpitationInfo = setpalInfo(desc, "2");
				// System.out.println(JSON.toJSON(palpitationInfo));
				JSONObject jsonObject2 = new JSONObject(JSON.toJSONString(deatilPalpitationInfo));
				Producer.sendMessage(jsonObject2, "Q_HeartBeat", "TAG_TASK", "KEYS_" + new Date().getTime(),
						JsonKeys.PRODUCER_MQ_NAME);
				//检查文件是否需要重命名
				ObjectPase.setGetLoggBean("检查out目录下是否有超过2小时没有转换的log文件", Dequepal.class.getName(), "info");
				checkLogFile(new DequeWriteOut().getPath(), ".log", ".out");
				MQ_AVAILABLE = true;
				Thread.sleep(5l * 60l * 1000l);
			} catch (Exception e) {
				try {
					Thread.sleep(5l * 60l * 1000l);
				} catch (Exception e1) {
					
				
				}
				MQ_AVAILABLE = false;
				// TODO Auto-generated catch block

				ObjectPase.setGetLoggBean("发送心跳数据异常：" + AllErrorMessage.getExceptionStackTrace(e),
						Dequepal.class.getName(), "error");
				
			}

		}

	}

	/**
	 * 获取消息队列是否可用，供任务调度线程使用
	 * 
	 * @return boolean
	 */
	public static boolean mqIsAvailable() {
		return MQ_AVAILABLE;
	}

	public void checkLogFile(String fileName, String extName, String replaceName) {

		File file = new File(fileName);
		if (!file.exists()) {
			file.mkdir();
		}
		if (file.isDirectory() == false) {
			return;
		}
		String files[];
		files = file.list();
		for (int i = 0; i < files.length; i++) {
			// System.out.println(files[i]);
			File file2 = new File(fileName + File.separator + files[i]);
			// System.out.println(file2.isDirectory());
			if (file2.isDirectory()) { // 是文件夹

				checkLogFile(fileName + File.separator + files[i], extName, replaceName);
			}
			if (files[i].indexOf(extName) != -1) {
				// 查看创建时间和最后一次修改时间
				Path path = Paths.get(fileName + File.separator + files[i]);
				BasicFileAttributeView basicview = Files.getFileAttributeView(path, BasicFileAttributeView.class,
						LinkOption.NOFOLLOW_LINKS);
				try {
					// long createTime = basicview.readAttributes().creationTime().toMillis();
					long endTime = basicview.readAttributes().lastModifiedTime().toMillis();
					long nowTime = System.currentTimeMillis();

					if (nowTime - endTime > 1000 * 60 * 60*2) {
						renameFile(fileName + File.separator + files[i],
								(fileName + File.separator + files[i]).replace(extName, replaceName));
						
						if((fileName + File.separator + files[i]).equals(AcqDataOutput2File.getFileName())) {
							AcqDataOutput2File.setFileName("");
						}
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					
				}

			}
		}
	}

	public void renameFile(String oldFileName, String newFileName) {
		File oldFile = new File(oldFileName);
		File newFile = new File(newFileName);
		boolean flag = oldFile.renameTo(newFile);
		if (flag) {
			System.out.println("File renamed successfully");
		} else {
			System.out.println("Rename operation failed");
		}
	}

}
