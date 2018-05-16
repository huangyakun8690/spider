<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.SingleHttpClient;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;

public class ProxyHelper {

	private static List<String> proxyList = new ArrayList<String>();
	private static List<String[]> httpProxyList = new ArrayList<String[]>();
	private static List<String>   httpProxyListp = new ArrayList<String>();
	private static String userName=ReadProperties.readProperties("application.properties", "ipPool.userName");
	private static String passWord=ReadProperties.readProperties("application.properties", "ipPool.passWord");;
	static {
		try {
			//requestForProxy();
			requestForProxyHttp() ;
		} catch (Exception e) {
			
			//logger.info(AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
					"初始化代理异常："  , ProxyHelper.class.getName(), "error");
		}
	}

	public static String getProxy() {
		//Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
		if (proxyList.size() == 0) {
			synchronized (proxyList) {
				try {
					//requestForProxy();
					requestForProxyHttp() ;
				} catch (Exception e) {
					
					
					ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
							"获取代理异常：" +AllErrorMessage.getExceptionStackTrace(e) , ProxyHelper.class.getName(), "error");
			
					try {
						//requestForProxy();
						requestForProxyHttp() ;
					} catch (Exception e1) {
						ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
								"get proxy from tvp.daxiangdaili.com encouters exceptions,"
										+ AllErrorMessage.getExceptionStackTrace(e1)  , ProxyHelper.class.getName(), "error");
						
					}
				}
			}
		}
		String proxy = proxyList.get(new Random().nextInt(proxyList.size()));
		//XxlJobLogger.log("use proxy>>>>>>>>>>" + proxy);
		//logger.info("use proxy>>>>>>>>>>" + proxy);
		ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
				"use proxy>>>>>>>>>>" + proxy , ProxyHelper.class.getName(), "info");
		return proxy;
	}

	public static void removeInvalidProxy(String invalidProxy) {
		//Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
		synchronized (proxyList) {
			proxyList.remove(invalidProxy);
		}
		//logger.info("remove invalid proxy>>>>>>>>>>" + invalidProxy);
		//XxlJobLogger.log("remove invalid proxy>>>>>>>>>>" + invalidProxy);
		ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
				"remove invalid proxy>>>>>>>>>>" + invalidProxy , ProxyHelper.class.getName(), "info");
	}

	public static void requestForProxy() throws Exception {
		//Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
		HttpClient client = SingleHttpClient.getInstance();
		String body = "";
		//http://tvp.daxiangdaili.com/ip/?tid=556744577953778&num=10&delay=5
		//http://www.66ip.cn/mo.php?sxb=&tqsl=10&port=&export=&ktip=&sxa=&submit=%CC%E1++%C8%A1&textarea=
		HttpGet httppost = new HttpGet("http://tvp.daxiangdaili.com/ip/?tid=557699326111675&num=10&delay=5");
		HttpResponse httpresponse = client.execute(httppost);
		HttpEntity entity = httpresponse.getEntity();
		body = EntityUtils.toString(entity);
		EntityUtils.consume(entity);
		//logger.info("get proxy from tvp.daxiangdaili.com>>>>>>>>>>" + body);
		//logger.info(body);
		ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
				"get proxy from tvp.daxiangdaili.com>>>>>>>>>>" + body, ProxyHelper.class.getName(), "info");
		String[] proxyArray = body.split("\r\n");
		System.out.println(body);
		proxyList.clear();
		proxyList.addAll(Arrays.asList(proxyArray));

		
	}
	
	
	
	/**
	 * 查询http代理
	 */
	public static void requestForProxyHttp() {
		Map<String, String> map = new HashMap<String, String>();
		map = JedisUtils.hget("succConfigPoxy");
		String body[] = new String[map.size()];
		if (map != null && map.size() > 0) {
			int i=0;
			for (String in : map.keySet()) {
				
				// map.keySet()返回的是所有key的值
				String str = map.get(in);// 得到每个key多对用value的值
				body[i]=str;
				i++;
			
			}
		}
		proxyList.clear();
		proxyList.addAll(Arrays.asList(body));
		
	}

	public static List<String> getProxyList() {
		return proxyList;
	}

	public static List<String[]> getHttpProxyList() {
		return httpProxyList;
	}

	public static List<String> getHttpProxyListp() {
		return httpProxyListp;
	}

	public static String getUserName() {
		return userName;
	}

	public static String getPassWord() {
		return passWord;
	}



}
=======
package com.xxl.job.executor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.SingleHttpClient;
import com.xxl.job.core.log.XxlJobLogger;

public class ProxyHelper {

	private static List<String> proxyList = new ArrayList<String>();
	static Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
	static {
		try {
			requestForProxy();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
		}
	}

	public static String getProxy() {
		Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
		if (proxyList.size() == 0) {
			synchronized (proxyList) {
				try {
					requestForProxy();
				} catch (Exception e) {
					e.printStackTrace();
					logger.info("get proxy from tvp.daxiangdaili.com encouters exceptions,"
							+ AllErrorMessage.getExceptionStackTrace(e));
			
					try {
						requestForProxy();
					} catch (Exception e1) {
						e1.printStackTrace();
						XxlJobLogger.log("get proxy from tvp.daxiangdaili.com encouters exceptions,"
								+ AllErrorMessage.getExceptionStackTrace(e1));
						logger.info("get proxy from tvp.daxiangdaili.com encouters exceptions,"
								+ AllErrorMessage.getExceptionStackTrace(e1));
						
					}
				}
			}
		}
		String proxy = proxyList.get(new Random().nextInt(proxyList.size()));
		XxlJobLogger.log("use proxy>>>>>>>>>>" + proxy);
		logger.info("use proxy>>>>>>>>>>" + proxy);
		return proxy;
	}

	public static void removeInvalidProxy(String invalidProxy) {
		Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
		synchronized (proxyList) {
			proxyList.remove(invalidProxy);
		}
		logger.info("remove invalid proxy>>>>>>>>>>" + invalidProxy);
		XxlJobLogger.log("remove invalid proxy>>>>>>>>>>" + invalidProxy);
	}

	public static void requestForProxy() throws Exception {
		Logger logger = LoggerFactory.getLogger(ProxyHelper.class);
		HttpClient client = SingleHttpClient.getInstance();
		String body = "";
		HttpGet httppost = new HttpGet("http://tvp.daxiangdaili.com/ip/?tid=556744577953778&num=50&delay=5");
		HttpResponse httpresponse = client.execute(httppost);
		HttpEntity entity = httpresponse.getEntity();
		body = EntityUtils.toString(entity);
		EntityUtils.consume(entity);
		logger.info("get proxy from tvp.daxiangdaili.com>>>>>>>>>>" + body);
		logger.info(body);
		String[] proxyArray = body.split("\r\n");
		proxyList.clear();
		proxyList.addAll(Arrays.asList(proxyArray));
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
