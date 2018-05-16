<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * Use Redis as url scheduler for distributed crawlers.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.2.0
 */
public class YuntingRedisScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover {

    private JedisPool pool;
    private JedisCluster jedisCluster;

    private static final String QUEUE_PREFIX = "queue_";

    private static final String SET_PREFIX = "set_";

    private static final String ITEM_PREFIX = "item_";

    public YuntingRedisScheduler(String host) {
    	JedisPoolConfig conf = new JedisPoolConfig();
		conf.setMaxIdle(100);
		conf.setMaxWaitMillis(1000l*5l);
		conf.setTestOnBorrow(false);
		
		String[] ips = host.split(",");
		if(ips.length>1){
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>(ips.length);
			for(String ip:ips){
				String[] split = ip.split(":");
				jedisClusterNodes.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
			}
			jedisCluster = new JedisCluster(jedisClusterNodes, 30000, conf);
			
		}else{
			String hostip = host.split(":")[0].trim();
			int port = Integer.parseInt(host.split(":")[1].trim());
			pool = new JedisPool(conf,hostip,port);
		}
		
		setDuplicateRemover(this);
    }

    public YuntingRedisScheduler(JedisPool pool,JedisCluster cluster) {
        this.pool = pool;
        this.jedisCluster = cluster;
        setDuplicateRemover(this);
    }
    
    public YuntingRedisScheduler(JedisPool pool) {
        this.pool = pool;
        setDuplicateRemover(this);
    }

    @Override
    public void resetDuplicateCheck(Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        
    	        try {
    	            jedis.del(getSetKey(task));
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    		jedisCluster.del(getSetKey(task));
//    		try {
//				jedisCluster.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				
//			} 
    		
    	}
       
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
    	 boolean isDuplicate = false;
    	if(pool!=null){
    		Jedis jedis = pool.getResource();
            try {
                isDuplicate = jedis.sismember(getSetKey(task), request.getUrl());
                if (!isDuplicate) {
                    jedis.sadd(getSetKey(task), request.getUrl());
                }
                return isDuplicate;
            } finally {
                pool.returnResource(jedis);
            }
    	}else{
    		try{
    			isDuplicate = jedisCluster.sismember(getSetKey(task), request.getUrl());
       		 	if (!isDuplicate) {
       			 jedisCluster.sadd(getSetKey(task), request.getUrl());
                }
                return isDuplicate;
    		}finally{
//    			try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					
//				}
    		}
    		 
    	}
        

    }

    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        
    	        try {
    	            jedis.rpush(getQueueKey(task), request.getUrl());
    	            if (request.getExtras() != null) {
    	                String field = DigestUtils.shaHex(request.getUrl());
    	                String value = JSON.toJSONString(request);
    	                jedis.hset((ITEM_PREFIX + task.getUUID()), field, value);
    	            }
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    		 try {
    			 jedisCluster.rpush(getQueueKey(task), request.getUrl());
 	            if (request.getExtras() != null) {
 	                String field = DigestUtils.shaHex(request.getUrl());
 	                String value = JSON.toJSONString(request);
 	               jedisCluster.hset((ITEM_PREFIX + task.getUUID()), field, value);
 	            }
 	        } finally {
// 	        	try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					
//				}
 	        }
    	}
    	
       
    }

    @Override
    public synchronized Request poll(Task task) {
    	
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        try {
    	            String url = jedis.lpop(getQueueKey(task));
    	            if (url == null) {
    	                return null;
    	            }
    	            String key = ITEM_PREFIX + task.getUUID();
    	            String field = DigestUtils.shaHex(url);
    	            byte[] bytes = jedis.hget(key.getBytes(), field.getBytes());
    	            if (bytes != null) {
    	                Request o = JSON.parseObject(new String(bytes), Request.class);
    	                return o;
    	            }
    	            Request request = new Request(url);
    	            return request;
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
	        try {
	            String url = jedisCluster.lpop(getQueueKey(task));
	            if (url == null) {
	                return null;
	            }
	            String key = ITEM_PREFIX + task.getUUID();
	            String field = DigestUtils.shaHex(url);
	            String bytes = jedisCluster.hget(key, field);
	            if (bytes != null) {
	                Request o = JSON.parseObject(bytes, Request.class);
	                return o;
	            }
	            Request request = new Request(url);
	            return request;
	        } finally {
//	        	try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					
//				}
	        }
    	}
    	
       
    }

    protected String getSetKey(Task task) {
        return SET_PREFIX + task.getUUID();
    }

    protected String getQueueKey(Task task) {
        return QUEUE_PREFIX + task.getUUID();
    }

    @Override
    public int getLeftRequestsCount(Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        try {
    	            Long size = jedis.llen(getQueueKey(task));
    	            return size.intValue();
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    		 try {
 	            Long size = jedisCluster.llen(getQueueKey(task));
 	            return size.intValue();
 	        } finally {
// 	        	try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					
//				}
 	        }
    	}
    	
       
    }

    @Override
    public int getTotalRequestsCount(Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        try {
    	            Long size = jedis.scard(getQueueKey(task));
    	            return size.intValue();
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    	        try {
    	            Long size = jedisCluster.scard(getQueueKey(task));
    	            return size.intValue();
    	        } finally {
//    	        	try {
//						jedisCluster.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						
//					}
    	        }
    	}
       
    }
}
=======
package com.xxl.job.executor.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * Use Redis as url scheduler for distributed crawlers.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.2.0
 */
public class YuntingRedisScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover {

    private JedisPool pool;
    private JedisCluster jedisCluster;

    private static final String QUEUE_PREFIX = "queue_";

    private static final String SET_PREFIX = "set_";

    private static final String ITEM_PREFIX = "item_";

    public YuntingRedisScheduler(String host) {
    	JedisPoolConfig conf = new JedisPoolConfig();
		conf.setMaxIdle(100);
		conf.setTestOnBorrow(false);
		
		String[] ips = host.split(",");
		if(ips.length>1){
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>(ips.length);
			for(String ip:ips){
				String[] split = ip.split(":");
				jedisClusterNodes.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
			}
			jedisCluster = new JedisCluster(jedisClusterNodes, 30000, conf);
			
		}else{
			String hostip = host.split(":")[0].trim();
			int port = Integer.parseInt(host.split(":")[1].trim());
			pool = new JedisPool(conf,hostip,port);
		}
		
		setDuplicateRemover(this);
    }

    public YuntingRedisScheduler(JedisPool pool,JedisCluster cluster) {
        this.pool = pool;
        this.jedisCluster = cluster;
        setDuplicateRemover(this);
    }
    
    public YuntingRedisScheduler(JedisPool pool) {
        this.pool = pool;
        setDuplicateRemover(this);
    }

    @Override
    public void resetDuplicateCheck(Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        
    	        try {
    	            jedis.del(getSetKey(task));
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    		jedisCluster.del(getSetKey(task));
//    		try {
//				jedisCluster.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
    		
    	}
       
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
    	 boolean isDuplicate = false;
    	if(pool!=null){
    		Jedis jedis = pool.getResource();
            try {
                isDuplicate = jedis.sismember(getSetKey(task), request.getUrl());
                if (!isDuplicate) {
                    jedis.sadd(getSetKey(task), request.getUrl());
                }
                return isDuplicate;
            } finally {
                pool.returnResource(jedis);
            }
    	}else{
    		try{
    			isDuplicate = jedisCluster.sismember(getSetKey(task), request.getUrl());
       		 	if (!isDuplicate) {
       			 jedisCluster.sadd(getSetKey(task), request.getUrl());
                }
                return isDuplicate;
    		}finally{
//    			try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
    		}
    		 
    	}
        

    }

    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        
    	        try {
    	            jedis.rpush(getQueueKey(task), request.getUrl());
    	            if (request.getExtras() != null) {
    	                String field = DigestUtils.shaHex(request.getUrl());
    	                String value = JSON.toJSONString(request);
    	                jedis.hset((ITEM_PREFIX + task.getUUID()), field, value);
    	            }
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    		 try {
    			 jedisCluster.rpush(getQueueKey(task), request.getUrl());
 	            if (request.getExtras() != null) {
 	                String field = DigestUtils.shaHex(request.getUrl());
 	                String value = JSON.toJSONString(request);
 	               jedisCluster.hset((ITEM_PREFIX + task.getUUID()), field, value);
 	            }
 	        } finally {
// 	        	try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
 	        }
    	}
    	
       
    }

    @Override
    public synchronized Request poll(Task task) {
    	
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        try {
    	            String url = jedis.lpop(getQueueKey(task));
    	            if (url == null) {
    	                return null;
    	            }
    	            String key = ITEM_PREFIX + task.getUUID();
    	            String field = DigestUtils.shaHex(url);
    	            byte[] bytes = jedis.hget(key.getBytes(), field.getBytes());
    	            if (bytes != null) {
    	                Request o = JSON.parseObject(new String(bytes), Request.class);
    	                return o;
    	            }
    	            Request request = new Request(url);
    	            return request;
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
	        try {
	            String url = jedisCluster.lpop(getQueueKey(task));
	            if (url == null) {
	                return null;
	            }
	            String key = ITEM_PREFIX + task.getUUID();
	            String field = DigestUtils.shaHex(url);
	            String bytes = jedisCluster.hget(key, field);
	            if (bytes != null) {
	                Request o = JSON.parseObject(bytes, Request.class);
	                return o;
	            }
	            Request request = new Request(url);
	            return request;
	        } finally {
//	        	try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
	        }
    	}
    	
       
    }

    protected String getSetKey(Task task) {
        return SET_PREFIX + task.getUUID();
    }

    protected String getQueueKey(Task task) {
        return QUEUE_PREFIX + task.getUUID();
    }

    @Override
    public int getLeftRequestsCount(Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        try {
    	            Long size = jedis.llen(getQueueKey(task));
    	            return size.intValue();
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    		 try {
 	            Long size = jedisCluster.llen(getQueueKey(task));
 	            return size.intValue();
 	        } finally {
// 	        	try {
//					jedisCluster.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
 	        }
    	}
    	
       
    }

    @Override
    public int getTotalRequestsCount(Task task) {
    	if(pool!=null){
    		 Jedis jedis = pool.getResource();
    	        try {
    	            Long size = jedis.scard(getQueueKey(task));
    	            return size.intValue();
    	        } finally {
    	            pool.returnResource(jedis);
    	        }
    	}else{
    	        try {
    	            Long size = jedisCluster.scard(getQueueKey(task));
    	            return size.intValue();
    	        } finally {
//    	        	try {
//						jedisCluster.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
    	        }
    	}
       
    }
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
