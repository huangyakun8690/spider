package com.xxl.job.executor.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ustcinfo.ptp.yunting.dao.impl.TconfigProxyImpl;
import com.xxl.job.executor.util.HttpClient4;
import com.xxl.job.executor.util.JedisUtils;

@Component
public class TestIp {
	private List<Map<String,Object>> proxyList=null;
	@Autowired
	private TconfigProxyImpl proxyImpl;
	
	/**
	 * 检测ip
	 */
	@Scheduled(fixedRate = 1000*60*60*12)
	public void getip() {
			proxyList= proxyImpl.getConfigProxy();
			System.out.println(proxyList.size());
			if(proxyList!=null && proxyList.size()>0) {
			for(Map<String, Object> map:proxyList) {
				String proxyAddr = map.get("c_host")!=null ?  map.get("c_host")+"":"";
				String proxyPort = map.get("c_port")!=null ?  map.get("c_port")+"":"0";
				//检测ip
				
				if(!JedisUtils.isExisted("failConfigPoxy", proxyAddr) && !JedisUtils.isExisted("succConfigPoxy", proxyAddr)) {
					
					boolean result = HttpClient4.ipTest(proxyAddr, Integer.parseInt(proxyPort) , "", "");
					if(result) {
							//验证
					     //getAddressByIP(proxyAddr,proxyPort);
					     JedisUtils.hset("succConfigPoxy", proxyAddr, proxyAddr+":"+proxyPort);
					}else {
						JedisUtils.hset("failConfigPoxy", proxyAddr, proxyAddr+":"+proxyPort);
					}
				}
					
			 }
			}
				
	
		}
	
	public static String getAddressByIP(String strIP,String proxyPort) {
		try {
			URL url = new URL("http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip="+strIP);
			URLConnection conn = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line = null;
			StringBuffer result = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			reader.close();
			String ipAddr = result.toString();
			try {
				System.out.println(strIP);
				System.out.println(ipAddr);
				JSONObject obj1= new JSONObject(ipAddr);
				if("0".equals(obj1.get("status").toString())){
					System.out.println(obj1.get("address").toString());
					String address= obj1.get("address").toString();
					String splitAddress [] = address.split("\\|");
					
				if(splitAddress[0].equals("CN")||splitAddress[0].equals("HK")) {
					if(!JedisUtils.isExisted("succConfigPoxy", strIP)) {
						JedisUtils.hset("succConfigPoxy", strIP, strIP+":"+proxyPort);
					}
					
				}
				JSONObject obj2= new JSONObject(obj1.get("content").toString());
				JSONObject obj3= new JSONObject(obj2.get("address_detail").toString());
				return obj3.get("city").toString();
				}else{
					JedisUtils.hset("failConfigPoxy", strIP, strIP+":"+proxyPort);
					return "读取失败";
				}
			} catch (JSONException e) {
				
				JedisUtils.hset("failConfigPoxy", strIP, strIP+":"+proxyPort);
				return "读取失败";
			}
			
		} catch (IOException e) {
			JedisUtils.hset("failConfigPoxy", strIP, strIP+":"+proxyPort);
			return "读取失败";
		}
	}

}
