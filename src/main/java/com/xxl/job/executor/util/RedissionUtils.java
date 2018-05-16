package com.xxl.job.executor.util;

import java.io.IOException;
import java.util.Properties;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
<<<<<<< HEAD
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;

/**
 * Created by lisen on 2017/12/14.
 */
public class RedissionUtils {

<<<<<<< HEAD
	//private static Logger logger = LoggerFactory.getLogger(RedissionUtils.class);

	private static RedissonClient redisClient;
	
	private static String  DETAIL_SET_PREFIX = "detail_set_";
=======
	private static Logger logger = LoggerFactory.getLogger(RedissionUtils.class);

	private static RedissonClient redisClient;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

	static {
		Config config = new Config();
		Properties p = new Properties();
		try {
			p.load(JedisUtils.class.getClassLoader()
					.getResourceAsStream("META-INF/res/resource-development.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
<<<<<<< HEAD
			
=======
			e.printStackTrace();
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
		}
		String host = p.get("redisHost").toString();

		String[] ips = host.split(",");
		if (ips.length > 1) {
			config.useClusterServers().setScanInterval(2000).addNodeAddress(ips);
		} else {
			config.useSingleServer().setAddress(host);
		}
		config.setThreads(200);
		config.setNettyThreads(200);
		redisClient = Redisson.create(config);
	}

	public static RLock getLock(String key) {
		return redisClient.getLock(key);
	}

	public static void set(String mapName, String key, String value) {
		RMap<String, String> map = redisClient.getMap(mapName);
		map.put(key, value);
	}

	public static String get(String mapName, String key) {
		if (isExisted(mapName, key)) {
			return (String) redisClient.getMap(mapName).get(key);
		} else {
			return null;
		}
	}

	public static boolean isExisted(String mapName, String key) {
		RMap<String, String> map = redisClient.getMap(mapName);
		if (null == map)
			return false;

		return map.get(key) == null ? false : true;
	}

	/**
	 * 判断是否重复，使用set lly171219
	 * 
	 * @param value
	 * @return boolean
	 */
	public static boolean isExisted(String value) {
<<<<<<< HEAD
		RSet<String> rset = redisClient.getSet(DETAIL_SET_PREFIX+StringHas.getDomain(value));
=======
		RSet<String> rset = redisClient.getSet(StringHas.getDomain(value));
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
		if (null == rset) {
			return false;
		}
		return rset.contains(value);
	}

	public static void main(String[] args) {
<<<<<<< HEAD

		System.out.println(isExisted("test", "key1"));
=======
        set("test","key","value");
		System.out.println(isExisted("test", "key"));
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
	}

	/**
	 * 向set添加成员 lly171219
	 * 
	 * @param value
	 * @return
	 */
	public static void add(String value) {
<<<<<<< HEAD
		RSet<String> rset = redisClient.getSet(DETAIL_SET_PREFIX+StringHas.getDomain(value));
=======
		RSet<String> rset = redisClient.getSet(StringHas.getDomain(value));
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
		// RLock rLock = getLock(StringHas.getDomain(value));
		// rLock.lock(3*1000, TimeUnit.SECONDS);
		if (!rset.contains(value)) {
			try {
				rset.add(value);
			} catch (Exception e) {
<<<<<<< HEAD
				ObjectPase.setGetLoggBean("", "", 0, "", "","", "", "", "", 
						"RedissionUtils 向set：{}中添加成员失败，异常信息：{}"+AllErrorMessage.getExceptionStackTrace(e), RedissionUtils.class.getName(), "error");
				
				
=======
				logger.error("RedissionUtils 向set：{}中添加成员失败，异常信息：{}", StringHas.getDomain(value),
						AllErrorMessage.getExceptionStackTrace(e));
				e.printStackTrace();
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
			}
		}
		// rLock.unlock();
	}

}
