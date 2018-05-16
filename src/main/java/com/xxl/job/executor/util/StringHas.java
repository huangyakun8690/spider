<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.GetSystemValue;
import com.ustcinfo.tpc.framework.core.util.Md5;
import com.ustcinfo.tpc.framework.core.util.ReadProperties;




public  class StringHas {

	//private static Logger logger = LoggerFactory.getLogger(StringHas.class);
	// lly171221添加用正则获取根域名
	// 定义正则表达式，域名的根需要自定义，这里不全
	private static final String REGEX_TOP_DOMAIN_PATTERN = "[\\w-]+\\.(com.cn|net.cn|gov.cn|org\\.nz|org.cn|com|net|org|gov|cc|biz|info|cn|co|:\\d{1,5})\\b()*";
	private static final String REGEX_IP_PORT_PATTERN = "\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}:\\d{1,5}";
	private static Pattern PATTERN_DOMAIN=Pattern.compile(REGEX_TOP_DOMAIN_PATTERN, Pattern.CASE_INSENSITIVE);
	private static Pattern PATTERN_IP_PORT=Pattern.compile(REGEX_IP_PORT_PATTERN, Pattern.CASE_INSENSITIVE);
	
	public static boolean checkJson(JSONObject pageJsonObj, String name) {
		if (pageJsonObj != null) {
			return pageJsonObj.has(name);
		}
		return false;

	}

	public static String getJsonVlue(JSONObject pageJsonObj, String name) {
		if (pageJsonObj != null) {
			return checkJson(pageJsonObj, name) == true
					? (pageJsonObj.getString(name) != null ? pageJsonObj.getString(name) : "") : "";

		}
		return "";

	}

	public static String getDateStr(String dateStr,String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateStr);
			return sdf.format(new Date());
		} catch (Exception e) {
			
			//logger.info(AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
					AllErrorMessage.getExceptionStackTrace(e), StringHas.class.getName(), "error");
			
			return System.currentTimeMillis() + "";
		}
	}

	
	
	public static String encapTemplate(String template, Iterator<String> it,String url){
		String temp = template;
		Map<String,Object> keyList = new HashMap<String,Object>();
		while(it.hasNext()){
			keyList.put(it.next(),"") ;
		}

		String[] templateNode = template.split("\\|");
		for (String node : templateNode) {
			if (node.contains("$")) {
				String pattern = "\\$\\{(.+?)}";
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(node);
				if (m.find()) {
					String currentNodeParam = m.group(1);
					if (keyList.get(currentNodeParam) == null) {
						temp = temp.replace("${" + currentNodeParam + "}", "");
					}
				}
			} else if (node.contains("zy_f(")) {
				node = node.replace("zy_f(", "").replace(")", "");
				if (node.equals("md5Tourl")) {
					temp = temp.replace("|zy_f(md5Tourl)|", "|" + Md5.toMD5(url) + "|");
				} else if (node.contains("now")) {
					String[] exp = node.split(",");
					if (exp.length == 1) {
						temp = temp.replace("|zy_f(" + node + ")|", "|" + System.currentTimeMillis() + "|");
					} else if (exp.length >= 2) {
						temp = temp.replace("|zy_f(" + node + ")|", "|" + getDateStr(exp[1],"") + "|");
					}
				} else if (node.equals("url")) {
					temp = temp.replace("|zy_f(url)|", "|" + url + "|");
				} else if (node.contains("date")) {
					String[] exp = node.split(",");
					if (exp.length == 1) {
						temp = temp.replace("|zy_f(" + node + ")|", "|" + System.currentTimeMillis() + "|");
					} else if (exp.length >= 2) {
						temp = temp.replace("|zy_f("+node+")|", "|" + getDateLongTime(exp[1]) + "|");
					}
				
				} else if(node.contains("json")) {
					//把信息传进来，自行拼接
					/*zy_f(json,ext1,ext2,ext3,ext4)
					 * 
					 * */
					
				}
			}
		}

		return temp;
	}

	/**
	 * 根据url获取网站域名
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	// public static String getDomain(String url){
	// if(StringUtils.hasText(url)){
	// java.net.URL urlObj = null;
	// try {
	// urlObj = new java.net.URL(url);
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// 
	// logger.info(AllErrorMessage.getExceptionStackTrace(e));
	// }
	// String host =
	// urlObj.getHost()+(urlObj.getPort()==-1?"":(":"+urlObj.getPort()));//
	// 获取主机名
	// return host;
	// }else{
	// return null;
	// }
	// }

	// lly171221 获取url根域名，否则有些网站计算的根域名不对，一个网站会在redis中产生多个set，无法去重
	// 正则可以添加
	public static String getDomain(String url) throws IllegalArgumentException {
		if (!StringUtils.hasText(url)) {
			throw new IllegalArgumentException("StringHas 获取域名方法错误，传入url为空。");
		}
		String domain = url;
		try {
			Matcher matcher = PATTERN_DOMAIN.matcher(url);
			matcher.find();
			domain = matcher.group();
		} catch (Exception e) {
			try {
				Matcher matcher = PATTERN_IP_PORT.matcher(url);
				matcher.find();
				domain = matcher.group();
			} catch (Exception e1) {
				//错误用新的正则解析
				String ipMatch="\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";
				Pattern ipPattern=Pattern.compile(ipMatch, Pattern.CASE_INSENSITIVE);
				try {
					Matcher matcher = ipPattern.matcher(url);
					matcher.find();
					domain = matcher.group();
				} catch (Exception e2) {
					
					ObjectPase.setGetLoggBean(
							"StringHas 获取域名方法错误，url：{}，异常信息：{}"+AllErrorMessage.getExceptionStackTrace(e), StringHas.class.getName(), "error");
					
				}
				//logger.error("StringHas 获取域名方法错误，url：{}，异常信息：{}",url, AllErrorMessage.getExceptionStackTrace(e));
				
			}
		}
		return domain;
	}

	public static String[] getSiteUrls(String url) {
		String[] urls = null;
		if (url.contains("<url>")) {
			url = url.replaceAll("<url>", "");
			urls = url.split("</url>");
		} else {
			urls = new String[1];
			urls[0] = url;
		}

		return urls;
	}

	public static String getUrl(String parenturl, String href) {
		if (href.startsWith("http://") || href.startsWith("https://"))
			return href;

		if (href.startsWith("/")) {
			java.net.URL urlObj = null;
			try {
				urlObj = new java.net.URL(parenturl);
				String newPath = urlObj.getProtocol() + "://"
						+ (urlObj.getHost() + (urlObj.getPort() == -1 ? "" : (":" + urlObj.getPort()))) + href;
				return newPath;
			} catch (MalformedURLException e) {
				
				//logger.info(AllErrorMessage.getExceptionStackTrace(e));
				ObjectPase.setGetLoggBean(
						"StringHas 获取域名方法错误，url：{"+href+"}，异常信息：{}"+AllErrorMessage.getExceptionStackTrace(e), StringHas.class.getName(), "error");
		
				return href;
			}
		}

		if (href.startsWith("./")) {
			if (parenturl.lastIndexOf("?") == -1) {
				return parenturl + href.replace("./", "/");
			} else {
				return parenturl.substring(0, parenturl.lastIndexOf("?")) + href.replace("./", "/");
			}

		}

		if (href.startsWith("../")) {
			String tmp = parenturl.substring(parenturl.indexOf("://") + 3,
					parenturl.lastIndexOf("?") > -1 ? parenturl.lastIndexOf("?") : parenturl.length());
			String[] cnts = tmp.split("/");
			int len = cnts.length;
			int idex = 0;
			while (href.indexOf("../", idex) > -1) {
				len--;
				idex += href.indexOf("../", idex) + 3;
			}
			String finalPath = "http://";
			java.net.URL urlObj = null;
			try {
				urlObj = new java.net.URL(parenturl);
				if (urlObj.getProtocol().equals("https"))
					finalPath = "https://";
			} catch (MalformedURLException e) {
				
				//logger.info(AllErrorMessage.getExceptionStackTrace(e));
				ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
						"StringHas 获取域名方法错误，url：{}，异常信息：{}"+AllErrorMessage.getExceptionStackTrace(e), StringHas.class.getName(), "error");
		
				return href;
			}
			if (len <= 0) {
				finalPath += cnts[0] + "/";
			} else {
				for (int i = 0; i < len; i++) {
					finalPath += cnts[i] + "/";
				}
			}

			finalPath += href.replaceAll("../", "");
			return finalPath;
		}

		if (parenturl.lastIndexOf("?") > -1) {
			parenturl = parenturl.substring(0, parenturl.lastIndexOf("?")) + "/" + href;
		}

		String tmp = parenturl.substring(parenturl.indexOf("://") + 3,
				parenturl.lastIndexOf("?") > -1 ? parenturl.lastIndexOf("?") : parenturl.length());
		String[] cnts = tmp.split("/");
		int length = cnts.length;
		if (cnts[cnts.length - 1].contains(".")) {
			String pageType = cnts[cnts.length - 1].substring(cnts[cnts.length - 1].lastIndexOf(".") + 1);
			if ("htm|html|jsp|asp|do|action|aspx".contains(pageType)) {
				length = length - 1;
			}
		}
		String finalPath = "http://";
		java.net.URL urlObj = null;
		try {
			urlObj = new java.net.URL(parenturl);
			if (urlObj.getProtocol().equals("https"))
				finalPath = "https://";
		} catch (MalformedURLException e) {
			
			//logger.info(AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
					"StringHas 获取域名方法错误，url：{}，异常信息：{}"+AllErrorMessage.getExceptionStackTrace(e), StringHas.class.getName(), "error");
	
			return href;
		}
		for (int i = 0; i < length; i++) {
			finalPath += cnts[i] + "/";
		}
		return finalPath + href;
	}

	public static String getNextPageUrl(String xpath, String input, int currentPageNo) {
		String pattern = "$\\{(.+?)}";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(xpath);
		List<String> params = new ArrayList<String>();
		while (m.find()) {
			params.add(m.group(1));
		}

		String nextPageUrl = null;
		if (input.indexOf("[{") > -1) {
			// TODO:解析json
			// JSONArray jsonArray = JSONObject.parseArray(input) ;
			// 太复杂，暂不实现
		} else if (input.contains("-")) {
			// TODO:解析范围页码
			String[] ym = input.split("-");
			int begin = Integer.parseInt(ym[0]);
			int end = Integer.parseInt(ym[1]);
			if (begin < end) {
				if (currentPageNo < end) {
					nextPageUrl = xpath.replace("${pageNo}", (currentPageNo + 1) + "");
				}
			} else {
				if (currentPageNo > end) {
					nextPageUrl = xpath.replace("${pageNo}", (currentPageNo - 1) + "");
				}
			}

		} else {
			int end = Integer.parseInt(input);
			if (currentPageNo < end) {
				nextPageUrl = xpath.replace("${pageNo}", (currentPageNo + 1) + "");
			}
		}

		return nextPageUrl;
	}

	public static String getDateNowStr() {
		return getDateStr("yyyy-MM-dd HH:mm:SS","");
	}
	
	public static String getDateLongTime(String date) {
		//Pattern p = Pattern.compile("(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}[-|\\/|月|\\.]\\d{1,2}([日|号])?(\\s)*(\\d{1,2}([点|时])?((:)?\\d{1,2}(分)?((:)?\\d{1,2}(秒)?)?)?)?(\\s)*(PM|AM)?)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);  
		Date dateTime=null;
		String reg1="[0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日";
		String reg2="[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}";
		String reg3="[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}";
		try {
			 Pattern r = Pattern.compile(reg1);
			 Pattern r2 = Pattern.compile(reg2);
			 Pattern r3 = Pattern.compile(reg3);
			 Matcher m = r.matcher(date);
			 Matcher m2 = r2.matcher(date);
			 Matcher m3 = r3.matcher(date);
			if(m.find()) {
				
				dateTime   =new SimpleDateFormat("yyyy年MM月dd日").parse(date);
			}
			if(m2.find()) {
				dateTime   =new SimpleDateFormat("yyyy/MM/dd").parse(date);
			}
			if(m3.find()) {
				dateTime   =new SimpleDateFormat("yyyy-MM-dd").parse(date);
			}
			if(dateTime!=null) {
				return dateTime.getTime()+"";
			}
			
		} catch (ParseException e) {
				// TODO Auto-generated catch block
				
		} 
			
		  
		  return null;
	}
	
	public static String getjsonValue(com.alibaba.fastjson.JSONObject pageJsonObj, String name) {
		String values="";
		try {
			if(pageJsonObj!=null && pageJsonObj.getString(name)!=null) {
				return pageJsonObj.getString(name)!=null ?  pageJsonObj.getString(name)+"" :"";
			}
		} catch (Exception e) {
			
			
		}
		return values;
	}
	
	public static String getMapValue(Map<String,Object> map,String keys) {
		String value=map.get(keys)!=null ?map.get(keys)+"":"";
		return value;
	}
	
	public static String getSpiderHead() {
		String spiderId =ReadProperties.readProperties("application.properties", "spiderId");
		String ip = ReadProperties.readProperties("application.properties", "xxl.job.executor.ip");
		String prot = ReadProperties.readProperties("application.properties", "xxl.job.executor.port");
		String loghead="爬虫id:"+spiderId+",ip:"+ip+",端口："+prot;
		return loghead;
	}
	
	/**
	 * 获取队列名称
	 * @param type
	 * @param classIf
	 * @return
	 */
	public static List<String> getTopicName(String types,Integer type,String classIf) {
		 List<String> results = new ArrayList<String>();
		try {
			String typess[]=types.split("-");
			
			if(classIf.equals("NAVI")) {
				for(String Str:typess) {
					results.add(MQTaskUtils.getNaviTopicName(type, Integer.parseInt(Str)));
				}
			}
			
			if(classIf.equals("DETAIL")) {
				for(String Str:typess) {
					results.add(MQTaskUtils.getDetailTopicName(type, Integer.parseInt(Str)));
				}
			}
			
			
		} catch (Exception e) {
			
			
			ObjectPase.setGetLoggBean("", "",  0, "",
					"", "", "0", "", "false", 
					"获取队列名字异常"+AllErrorMessage.getExceptionStackTrace(e), StringHas.class.getName(), "error");
		}
		
		return  results;
	}
	
	/**
	 * 写文件
	 * @param args
	 */
	public static boolean writeFileOut(String content, String fileName,String path) throws IOException {
	        File f = new File(path);
	        boolean pool=f.setWritable(true);
	        if(!pool) {
	        	ObjectPase.setGetLoggBean("", "",  0, "",
						"", "", "0", "", "", 
						"写文件权限未赋予成功", StringHas.class.getName(), "info");
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
	            return true;
	        } catch (IOException e) {
	            
	        } finally {
	        	if (null != fos) {
	                fos.close();
	            }
	            if (null != fc_out) {
	                fc_out.close();
	            }
	            
	        }
	        return false;
	    } 
	
	/**
	 * 创建文件
	 * @param type
	 * @param extName
	 * @return
	 */
	public static String createFile(String pathdb,String type,String extName)  {
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
             fileNum++;
		}
		ip = GetSystemValue.getIp();
		String fileName2 = pathdb+ File.separator + sdf.format(new Date()) + "_" + pro + "_" + proName + "_" + ip + "_"
				+ threadId + "_" + (fileNum) + extName;
		// new File(fileName2);
		ObjectPase.setGetLoggBean(
				"文件名：" + fileName2, DequeWritedb.class.getName(), "info");
		//logger.info(StringHas.getSpiderHead()+",文件名：" + fileName2);
		//fileNamedb = fileName2;
		return fileName2;

	}
	


}
=======
package com.xxl.job.executor.util;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.tpc.framework.core.util.Md5;


import us.codecraft.webmagic.Page;

public  class StringHas {

	private static Logger logger = LoggerFactory.getLogger(StringHas.class);
	// lly171221添加用正则获取根域名
	// 定义正则表达式，域名的根需要自定义，这里不全
	private static final String RE_TOP = "[\\w-]+\\.(com.cn|net.cn|gov.cn|org\\.nz|org.cn|com|net|org|gov|cc|biz|info|cn|co)\\b()*";

	private static Pattern pattern=Pattern.compile(RE_TOP, Pattern.CASE_INSENSITIVE);
	
	public static boolean checkJson(JSONObject pageJsonObj, String name) {
		if (pageJsonObj != null) {
			return pageJsonObj.has(name);
		}
		return false;

	}

	public static String getJsonVlue(JSONObject pageJsonObj, String name) {
		if (pageJsonObj != null) {
			return checkJson(pageJsonObj, name) == true
					? (pageJsonObj.getString(name) != null ? pageJsonObj.getString(name) : "") : "";

		}
		return "";

	}

	public static String getDateStr(String dateStr) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateStr);
			return sdf.format(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
			return System.currentTimeMillis() + "";
		}
	}

	
	
	public static String encapTemplate(String template, Iterator<String> it,String url){
		String temp = template;
		Map<String,Object> keyList = new HashMap<String,Object>();
		while(it.hasNext()){
			keyList.put(it.next(),"") ;
		}

		String[] templateNode = template.split("\\|");
		for (String node : templateNode) {
			if (node.contains("$")) {
				String pattern = "\\$\\{(.+?)}";
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(node);
				if (m.find()) {
					String currentNodeParam = m.group(1);
					if (keyList.get(currentNodeParam) == null) {
						temp = temp.replace("${" + currentNodeParam + "}", "");
					}
				}
			} else if (node.contains("zy_f(")) {
				node = node.replace("zy_f(", "").replace(")", "");
				if (node.equals("md5Tourl")) {
					temp = temp.replace("|zy_f(md5Tourl)|", "|" + Md5.toMD5(url) + "|");
				} else if (node.contains("now")) {
					String[] exp = node.split(",");
					if (exp.length == 1) {
						temp = temp.replace("|zy_f(" + node + ")|", "|" + System.currentTimeMillis() + "|");
					} else if (exp.length >= 2) {
						temp = temp.replace("|zy_f(" + node + ")|", "|" + getDateStr(exp[1]) + "|");
					}
				} else if (node.equals("url")) {
					temp = temp.replace("|zy_f(url)|", "|" + url + "|");
				}
			}
		}

		return temp;
	}

	/**
	 * 根据url获取网站域名
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	// public static String getDomain(String url){
	// if(StringUtils.hasText(url)){
	// java.net.URL urlObj = null;
	// try {
	// urlObj = new java.net.URL(url);
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// logger.info(AllErrorMessage.getExceptionStackTrace(e));
	// }
	// String host =
	// urlObj.getHost()+(urlObj.getPort()==-1?"":(":"+urlObj.getPort()));//
	// 获取主机名
	// return host;
	// }else{
	// return null;
	// }
	// }

	// lly171221 获取url根域名，否则有些网站计算的根域名不对，一个网站会在redis中产生多个set，无法去重
	// 正则可以添加
	public static String getDomain(String url) throws IllegalArgumentException {
		if (!StringUtils.hasText(url)) {
			throw new IllegalArgumentException("StringHas 获取域名方法错误，传入url为空。");
		}
		String domain = url;
		try {
			Matcher matcher = pattern.matcher(url);
			matcher.find();
			domain = matcher.group();
		} catch (Exception e) {
			logger.error("StringHas 获取域名方法错误，异常信息：{}", AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
		}
		return domain;
	}

	public static String[] getSiteUrls(String url) {
		String[] urls = null;
		if (url.contains("<url>")) {
			url = url.replaceAll("<url>", "");
			urls = url.split("</url>");
		} else {
			urls = new String[1];
			urls[0] = url;
		}

		return urls;
	}

	public static String getUrl(String parenturl, String href) {
		if (href.startsWith("http://") || href.startsWith("https://"))
			return href;

		if (href.startsWith("/")) {
			java.net.URL urlObj = null;
			try {
				urlObj = new java.net.URL(parenturl);
				String newPath = urlObj.getProtocol() + "://"
						+ (urlObj.getHost() + (urlObj.getPort() == -1 ? "" : (":" + urlObj.getPort()))) + href;
				return newPath;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.info(AllErrorMessage.getExceptionStackTrace(e));
				return href;
			}
		}

		if (href.startsWith("./")) {
			if (parenturl.lastIndexOf("?") == -1) {
				return parenturl + href.replace("./", "/");
			} else {
				return parenturl.substring(0, parenturl.lastIndexOf("?")) + href.replace("./", "/");
			}

		}

		if (href.startsWith("../")) {
			String tmp = parenturl.substring(parenturl.indexOf("://") + 3,
					parenturl.lastIndexOf("?") > -1 ? parenturl.lastIndexOf("?") : parenturl.length());
			String[] cnts = tmp.split("/");
			int len = cnts.length;
			int idex = 0;
			while (href.indexOf("../", idex) > -1) {
				len--;
				idex += href.indexOf("../", idex) + 3;
			}
			String finalPath = "http://";
			java.net.URL urlObj = null;
			try {
				urlObj = new java.net.URL(parenturl);
				if (urlObj.getProtocol().equals("https"))
					finalPath = "https://";
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.info(AllErrorMessage.getExceptionStackTrace(e));
				return href;
			}
			if (len <= 0) {
				finalPath += cnts[0] + "/";
			} else {
				for (int i = 0; i < len; i++) {
					finalPath += cnts[i] + "/";
				}
			}

			finalPath += href.replaceAll("../", "");
			return finalPath;
		}

		if (parenturl.lastIndexOf("?") > -1) {
			parenturl = parenturl.substring(0, parenturl.lastIndexOf("?")) + "/" + href;
		}

		String tmp = parenturl.substring(parenturl.indexOf("://") + 3,
				parenturl.lastIndexOf("?") > -1 ? parenturl.lastIndexOf("?") : parenturl.length());
		String[] cnts = tmp.split("/");
		int length = cnts.length;
		if (cnts[cnts.length - 1].contains(".")) {
			String pageType = cnts[cnts.length - 1].substring(cnts[cnts.length - 1].lastIndexOf(".") + 1);
			if ("htm|html|jsp|asp|do|action|aspx".contains(pageType)) {
				length = length - 1;
			}
		}
		String finalPath = "http://";
		java.net.URL urlObj = null;
		try {
			urlObj = new java.net.URL(parenturl);
			if (urlObj.getProtocol().equals("https"))
				finalPath = "https://";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
			return href;
		}
		for (int i = 0; i < length; i++) {
			finalPath += cnts[i] + "/";
		}
		return finalPath + href;
	}

	public static String getNextPageUrl(String xpath, String input, int currentPageNo) {
		String pattern = "$\\{(.+?)}";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(xpath);
		List<String> params = new ArrayList<String>();
		while (m.find()) {
			params.add(m.group(1));
		}

		String nextPageUrl = null;
		if (input.indexOf("[{") > -1) {
			// TODO:解析json
			// JSONArray jsonArray = JSONObject.parseArray(input) ;
			// 太复杂，暂不实现
		} else if (input.contains("-")) {
			// TODO:解析范围页码
			String[] ym = input.split("-");
			int begin = Integer.parseInt(ym[0]);
			int end = Integer.parseInt(ym[1]);
			if (begin < end) {
				if (currentPageNo < end) {
					nextPageUrl = xpath.replace("${pageNo}", (currentPageNo + 1) + "");
				}
			} else {
				if (currentPageNo > end) {
					nextPageUrl = xpath.replace("${pageNo}", (currentPageNo - 1) + "");
				}
			}

		} else {
			int end = Integer.parseInt(input);
			if (currentPageNo < end) {
				nextPageUrl = xpath.replace("${pageNo}", (currentPageNo + 1) + "");
			}
		}

		return nextPageUrl;
	}

	public static String getDateNowStr() {
		return getDateStr("yyyy-MM-dd HH:mm:SS");
	}

	public static void main(String args[]) {
		// System.out.println(getDomain("https://www.baidu.com/zhujianlin1990/article/details/51469359"));
		// String url =
		// "<url>http://23123?safas&page=1</url><url>http://23123?safas&page=2</url><url>http://23123?safas&page=3</url>"
		// ;
		// String[] urls = getSiteUrls(url) ;
		// for(String u:urls){
		// System.out.println(u) ;
		// }

		System.out.println(getUrl("http://www.baidu.com/aa/1", "../detail-93.co"));
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
