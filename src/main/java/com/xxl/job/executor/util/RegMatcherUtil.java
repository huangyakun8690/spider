<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;

public class RegMatcherUtil {
	

	/**
	 * 正则表达式，提取网页中商家，商机等信息
	 * @param str  网页内容，例如：医院网址：http://www.pumch.ac.cn联系电话：010-65296114 65124875医院地址：北京市东城区帅府园1号医院邮编：100730乘车路线：乘1、4、52、54、57、20、106、108、110、111、116、807、813、814公车可到
	 * @param reg  正则表达式，例如：reg(医院网址：(.+?)医院地址) 或者 reg((医.+?址))[1]
	 * @return
	 */
	public static String reg(String str, String reg){
		str=str.replace(" ", " ");
		str=str.replace("　", " ");
		if("".equals(reg)) return str ;
		int endIdx = reg.lastIndexOf(")");
		String patternCont = reg.substring(reg.indexOf("reg(")+4,endIdx) ;
		String patternIndex = reg.substring(endIdx+1) ;
		if(!"".equals(patternIndex)){
			patternIndex = patternIndex.replace("[", "") ;
			patternIndex = patternIndex.replace("]", "") ;
		}
		try{
			return reg(str,patternCont,"".equals(patternIndex)?1:Integer.parseInt(patternIndex)) ;
		}catch(Exception e){
			ObjectPase.setGetLoggBean("正则验证异常："+AllErrorMessage.getExceptionStackTrace(e),RegMatcherUtil.class.getName() , "error");
			return null ;
		}
	}
	
	/**
	 * 正则表达式，提取网页中商家，商机等信息
	 * @param str  网页内容，例如：医院网址：http://www.pumch.ac.cn联系电话：010-65296114 65124875医院地址：北京市东城区帅府园1号医院邮编：100730乘车路线：乘1、4、52、54、57、20、106、108、110、111、116、807、813、814公车可到
	 * @param pattern 正则表达式 ，例如：(医.+?址)，医院网址：(.+?)医院地址
	 * @param index 某些表达式可能会匹配出多个内容，index标识选取某一个，编号从1开始
	 * @return
	 */
	public static String reg(String str, String pattern,int index){
		  if("".equals(pattern)) return str ;
		// 创建 Pattern 对象
	      Pattern r = Pattern.compile(pattern);
	      // 现在创建 matcher 对象
	      Matcher m = r.matcher(str);
	      int step = 1;
	      while (m.find()) {
	    	  //if(step++==index)
	    		  return m.group(index);
	      }
	      return null ;
	}
	
	public static List<String> getUrls(String sources, String pattern){
		if("".equals(pattern)) return new ArrayList<String>() ;
		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);
		// 现在创建 matcher 对象
		List<String> list= new ArrayList<String>();
		Matcher m = r.matcher(sources);
		while (m.find()) {
			list.add(m.group(1)) ;
		}
		return list ;
	}


	public static void main(String[] args){
		String source = 
			"  七、公告发布媒体：《西乡县政府信息网》。\r\n" + 
			"      联 系 人：侯 鹏       联 系 电 话：13259295131   0916-2231616\r\n" + 
			"       西乡县高川镇人民政府           招标代理机构：陕西恒瑞项目管理有限公司  \r\n" + 
			"                               二〇一七年八月十五日";
		String content = "reg((\\d{3,4}-\\{7,8}|\\d{11,12}|\\d{5}-\\d{8}))[1]";
	//	String reg = "reg(((电话：\\s*)|(联系人：\\s*))([\u4e00-\u9fa5]+))[4]";
		//System.out.println(reg(content,reg));
		
		//String pattern = "reg((招\\s*标\\s*人：\\s*)([\u4e00-\u9fa5]+))[2]";
		String pattern = "reg(((采\\s*购\\s*人)|(招\\s*标\\s*人)|(招\\s*标\\s*单\\s*位)|(采\\s*购\\s*单\\s*位)|(发\\s*包\\s*人)|(发\\s*包\\s*单\\s*位))\\s*(名\\s*称)?[：\\s*:\\s*]?([\\u4e00-\\u9fa5]+))[9]";
		String pattern2 = "reg(((招\\s*标\\s*联\\s*系\\s*人)|(联\\s*系\\s*人))\\s*(名\\s*称)?\\s*[：:]\\s*([\\s*\\u4e00-\\u9fa5]{1,4})([联\\s*]))[5]";
		
		//		List<String> list=getUrls(source,pattern) ;
//		for(String line :list){
//			System.out.println(line) ;
//		}
		
		//source=source.replace(" ",   " ");
		
		 
		System.out.println(reg(source,pattern2)); 
	}
}
=======
package com.xxl.job.executor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegMatcherUtil {
	

	/**
	 * 正则表达式，提取网页中商家，商机等信息
	 * @param str  网页内容，例如：医院网址：http://www.pumch.ac.cn联系电话：010-65296114 65124875医院地址：北京市东城区帅府园1号医院邮编：100730乘车路线：乘1、4、52、54、57、20、106、108、110、111、116、807、813、814公车可到
	 * @param reg  正则表达式，例如：reg(医院网址：(.+?)医院地址) 或者 reg((医.+?址))[1]
	 * @return
	 */
	public static String reg(String str, String reg){
		if("".equals(reg)) return str ;
		int endIdx = reg.lastIndexOf(")");
		String patternCont = reg.substring(reg.indexOf("reg(")+4,endIdx) ;
		String patternIndex = reg.substring(endIdx+1) ;
		if(!"".equals(patternIndex)){
			patternIndex = patternIndex.replace("[", "") ;
			patternIndex = patternIndex.replace("]", "") ;
			patternIndex = patternIndex.trim();
		}
		try{
			return reg(str,patternCont,"".equals(patternIndex)?1:Integer.parseInt(patternIndex)) ;
		}catch(Exception e){
			e.printStackTrace( );
			return null ;
		}
	}
	
	/**
	 * 正则表达式，提取网页中商家，商机等信息
	 * @param str  网页内容，例如：医院网址：http://www.pumch.ac.cn联系电话：010-65296114 65124875医院地址：北京市东城区帅府园1号医院邮编：100730乘车路线：乘1、4、52、54、57、20、106、108、110、111、116、807、813、814公车可到
	 * @param pattern 正则表达式 ，例如：(医.+?址)，医院网址：(.+?)医院地址
	 * @param index 某些表达式可能会匹配出多个内容，index标识选取某一个，编号从1开始
	 * @return
	 */
	public static String reg(String str, String pattern,int index){
		  if("".equals(pattern)) return str ;
		// 创建 Pattern 对象
	      Pattern r = Pattern.compile(pattern);
	      // 现在创建 matcher 对象
	      Matcher m = r.matcher(str);
	      int step = 1;
	      while (m.find()) {
	    	  //if(step++==index)
	    		  return m.group(index);
	      }
	      return null ;
	}
	
	public static List<String> getUrls(String sources, String pattern){
		if("".equals(pattern)) return new ArrayList<String>() ;
		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);
		// 现在创建 matcher 对象
		List<String> list= new ArrayList<String>();
		Matcher m = r.matcher(sources);
		while (m.find()) {
			list.add(m.group(1)) ;
		}
		return list ;
	}

	public static void main(String[] args){
		String source = "招 标 人：上虞区岭南乡人民政府" ;
		String content = "联系人： 姜先生 联系机构： 曹女士 电话： 13506828586 电话： 0513-86546409";
		String reg = "reg(((电话：\\s*)|(联系人：\\s*))([\u4e00-\u9fa5]+))[4]";
		System.out.println(reg(content,reg));

		//String pattern = "reg((招\\s*标\\s*人：\\s*)([\u4e00-\u9fa5]+))[2]";
		String pattern = "reg(((采\\s*购\\s*人)|(招\\s*标\\s*人)|(招\\s*标\\s*单\\s*位)|(采\\s*购\\s*单\\s*位)|(发\\s*包\\s*人)|(发\\s*包\\s*单\\s*位))\\s*(名\\s*称)?[：\\s*:\\s*]?([\\u4e00-\\u9fa5]+))[9]";
		//		List<String> list=getUrls(source,pattern) ;
//		for(String line :list){
//			System.out.println(line) ;
//		}
		System.out.println(reg(source,pattern));
	}
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
