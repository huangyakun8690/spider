<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * jedis工具类
 *
 */
public class JedisUtils {
	private static JedisPool pool =null;
	private static JedisCluster jedisCluster =null;
	
	/**
	 * 初始化数据源
	 */
	
	
	static {
		
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(500);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(1000l * 10l);
		config.setTestOnBorrow(true);
		
		Properties p = new Properties();
		try {
			p.load(JedisUtils.class.getClassLoader()
					.getResourceAsStream("META-INF/res/resource-development.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			
			ObjectPase.setGetLoggBean("", "", 0, "2", "1", "", "0", "", "", 
					AllErrorMessage.getExceptionStackTrace(e), JedisUtils.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
		}
		String host = p.get("redisHost").toString();
		
		String[] ips = host.split(",");
		if(ips.length>1){
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>(ips.length);
			for(String ip:ips){
				String[] split = ip.split(":");
				jedisClusterNodes.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
			}
			jedisCluster = new JedisCluster(jedisClusterNodes, 30000, config);
			
		}else{
			String hostip = host.split(":")[0].trim();
			int port = Integer.parseInt(host.split(":")[1].trim());
			pool = new JedisPool(config,hostip,port);
		}
	}
	
	
	/**
	 * 创建jedis
	 * @return
	 */
	public static Jedis createJedis(){
		if (null == pool) {
			return null;
		}
		return pool.getResource();
	}
	
	
	/**
	 * 判断是否重复
	 * @param key
	 * @return
	 */
	public static boolean isExisted(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hexists(StringHas.getDomain(key),key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hexists(StringHas.getDomain(key),key);
		}
	}
	
	/**
	 * 判断是否重复
	 * @param key
	 * @return
	 */
	public static boolean isExisted(String key, String value){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hexists(key,value);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hexists(key,key);
		}
	}
	
	
	
	/**
	 * redis增加数据set方法
	 * @param key
	 * @param value
	 */
	public static void set(String key,String value){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.hset(StringHas.getDomain(key),key,value);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.hset(StringHas.getDomain(key),key,value);
		}
	}
	
	
	/**
	 * 根据key值获取数据
	 * @param key
	 * @return
	 */
	public static String get(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hget(StringHas.getDomain(key),key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hget(StringHas.getDomain(key),key);
		}
	}
	
	
	public static void del(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.hdel(StringHas.getDomain(key),key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.hdel(StringHas.getDomain(key),key);
		}
	}
	
	public static void del(String key,String fileid){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.hdel(key,fileid);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.hdel(key,fileid);
		}
	}
	
	public static String hget(String key,String field) {
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hget(key,field);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hget(key,field);
		}
	}
	
	public static String getv(String key) {
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.get(key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.get(key);
		}
	}
	public static void delv(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.del(key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.del(key);
		}
	}
	
    public static Map<String,String> hget(String key) {
    	if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hgetAll(key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hgetAll(key);
		}
	}
	
	
	/**
	 * redis增加数据set方法
	 * @param key
	 * @param value
	 */
	public static void hset(String key,String filed,String value){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.hset(key,filed,value);
				
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.hset(key,filed,value);
		}
	}
	
	
	/**
	 * 设置过期时间
	 * @param key
	 * @param seconds
	 * @param value
	 */
	public static void setex(String key,int seconds){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				
				jedis.expire(key, seconds);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.expire(key, seconds);
		}
	}
	
	
	
	/**
	 * plush
	 * @param key
	 * @param value
	 */
	public static void lpush(String key,String value) {
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.lpush(key, value);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.lpush(key, value);
		}
	}
	
	
	/**
	 * 删除 set 值
	 * @param key
	 * @param members
	 */
	public static void  srem(String key, String... members) {
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.srem(key, members);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			jedisCluster.srem(key, members);
		}
	}
	
	public static boolean  sismember(String key, String value) {
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.sismember(key, value);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			return jedisCluster.sismember(key, value);
		}
	}
		

}
=======
package com.xxl.job.executor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * jedis工具类
 *
 */
public class JedisUtils {
	static Logger logger = LoggerFactory.getLogger(JedisUtils.class);
	public static JedisPool pool;
	public static JedisCluster jedisCluster;
	
	/**
	 * 初始化数据源
	 */
	
	
	static {
		
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(300);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(1000 * 10);
		config.setTestOnBorrow(true);
		
		Properties p = new Properties();
		try {
			p.load(JedisUtils.class.getClassLoader()
					.getResourceAsStream("META-INF/res/resource-development.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
		}
		String host = p.get("redisHost").toString();
		
		String[] ips = host.split(",");
		if(ips.length>1){
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>(ips.length);
			for(String ip:ips){
				String[] split = ip.split(":");
				jedisClusterNodes.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
			}
			jedisCluster = new JedisCluster(jedisClusterNodes, 30000, config);
			
		}else{
			String hostip = host.split(":")[0].trim();
			int port = Integer.parseInt(host.split(":")[1].trim());
			pool = new JedisPool(config,hostip,port);
		}
	}
	
	
	/**
	 * 创建jedis
	 * @return
	 */
	public static Jedis createJedis(){
		if (null == pool) {
			return null;
		}
		return pool.getResource();
	}
	
	
	/**
	 * 判断是否重复
	 * @param key
	 * @return
	 */
	public static boolean isExisted(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hexists(StringHas.getDomain(key),key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hexists(StringHas.getDomain(key),key);
		}
	}
	
	
	
	/**
	 * redis增加数据set方法
	 * @param key
	 * @param value
	 */
	public static void set(String key,String value){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.hset(StringHas.getDomain(key),key,value);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.hset(StringHas.getDomain(key),key,value);
		}
	}
	
	
	/**
	 * 根据key值获取数据
	 * @param key
	 * @return
	 */
	public static String get(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				return jedis.hget(StringHas.getDomain(key),key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			return jedisCluster.hget(StringHas.getDomain(key),key);
		}
	}
	
	
	public static void del(String key){
		if(null != pool){
			Jedis jedis = pool.getResource();
			try{
				jedis.hdel(StringHas.getDomain(key),key);
			}finally{
				pool.returnResource(jedis);
			}
		}else{
			
			jedisCluster.hdel(StringHas.getDomain(key),key);
		}
	}
	
	
	
	
	public static void main(String args[]){
		String name = JedisUtils.get("name");
		JedisUtils.del("age");
		System.out.println(name);
		System.out.println(JedisUtils.isExisted("nam"));
	}
	
	

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
