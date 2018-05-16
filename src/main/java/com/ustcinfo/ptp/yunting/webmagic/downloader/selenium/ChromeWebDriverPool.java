<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.StringUtils;

import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.ProxyHelper;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 * @param
 */
public class ChromeWebDriverPool implements AdvancedSeleniumWebDriverPool{
	
   // private Logger logger = Logger.getLogger(getClass());

    private final int capacity;

    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;
    
    private final String SELENIUM_PATH;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    /**
     * store webDrivers created
     */
    private List<WebDriver> webDriverList = Collections.synchronizedList(new ArrayList<WebDriver>());

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();
    
    private boolean useProxy;
    
    private Map<Integer,String> proxyMap = new HashMap<Integer,String>();

    public ChromeWebDriverPool(int poolSize, String SELENIUM_PATH, boolean useProxy) {
    	 this.capacity = poolSize;
    	 this.SELENIUM_PATH = SELENIUM_PATH;
    	 this.useProxy = useProxy;
	}

    @Override
	public WebDriver get() throws InterruptedException {
        checkRunning();
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < capacity) {
            synchronized (webDriverList) {
                if (webDriverList.size() < capacity) {
                	String proxy=null;
					String chromeDriverPath = SELENIUM_PATH;
					System.getProperties().setProperty("webdriver.chrome.driver", chromeDriverPath);
					DesiredCapabilities capabilities = DesiredCapabilities.chrome();
					ChromeOptions options = new ChromeOptions();
					options.addArguments("--start-maximized");
					options.addArguments("--disable-web-security");
					options.addArguments("--allow-running-insecure-content");
					options.addArguments("--headless");
					//add proxy
					if(useProxy){
						proxy = ProxyHelper.getProxy();
						options.addArguments("--proxy-server=http://"+proxy);
					}
					capabilities.setCapability("chrome.binary", chromeDriverPath);
					capabilities.setCapability(ChromeOptions.CAPABILITY, options);
					ChromeDriver e = new ChromeDriver(capabilities);
					
					if(StringUtils.hasText(proxy)){
						proxyMap.put(e.hashCode(), proxy);
					}
					e.manage().window().setSize(new Dimension(1600,900));
					e.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
					.setScriptTimeout(120, TimeUnit.SECONDS).pageLoadTimeout(120, TimeUnit.SECONDS);
                    innerQueue.add(e);
                    webDriverList.add(e);
                }
            }
        }
        return innerQueue.take();
    }
    
    @Override
    public void returnToPool(WebDriver webDriver) {
    	try {
    		   checkRunning();
    	        innerQueue.add(webDriver);
		} catch (Exception e) {
			
			
		}
     
    }
  
    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }


    @Override
	public void close(WebDriver webDriver) {
		webDriverList.remove(webDriver);
		try {
			webDriver.quit();
		} catch (Exception e) {
			
			webDriver.quit();
		}
		webDriver = null;
		
	}

    @Override
	public void shutdown() {
		 boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
	        if (!b) {
	            throw new IllegalStateException("Already closed!");
	        }
	        for (WebDriver webDriver : webDriverList) {
	            //logger.info("Quit webDriver" + webDriver);
	            ObjectPase.setGetLoggBean("", "", 0, "", "", "", "1", "", "false", 
	            		"Quit webDriver" + webDriver, this.getClass().getName(), "error");
	            try {
					webDriver.quit();
				} catch (Exception e) {
					
					webDriver.quit();
				}
	        }
		
	}

	@Override
	public void removeInvalidProxy(WebDriver driver) {
		ProxyHelper.removeInvalidProxy(proxyMap.get(driver.hashCode()));
	}
    
    
    

}
=======
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.StringUtils;

import com.xxl.job.executor.util.ProxyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 * @param
 */
class ChromeWebDriverPool implements AdvancedSeleniumWebDriverPool{
	
    private Logger logger = Logger.getLogger(getClass());

    private final int capacity;

    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;
    
    private final String SELENIUM_PATH;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    /**
     * store webDrivers created
     */
    private List<WebDriver> webDriverList = Collections.synchronizedList(new ArrayList<WebDriver>());

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();
    
    private boolean useProxy;
    
    private Map<Integer,String> proxyMap = new HashMap<Integer,String>();

    public ChromeWebDriverPool(int poolSize, String SELENIUM_PATH, boolean useProxy) {
    	 this.capacity = poolSize;
    	 this.SELENIUM_PATH = SELENIUM_PATH;
    	 this.useProxy = useProxy;
	}

    @Override
	public WebDriver get() throws InterruptedException {
        checkRunning();
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < capacity) {
            synchronized (webDriverList) {
                if (webDriverList.size() < capacity) {
                	String proxy=null;
					String chromeDriverPath = SELENIUM_PATH;
					System.getProperties().setProperty("webdriver.chrome.driver", chromeDriverPath);
					DesiredCapabilities capabilities = DesiredCapabilities.chrome();
					ChromeOptions options = new ChromeOptions();
					options.addArguments("--start-maximized");
					options.addArguments("--disable-web-security");
					options.addArguments("--allow-running-insecure-content");
					options.addArguments("--headless");
					//add proxy
					if(useProxy){
						proxy = ProxyHelper.getProxy();
						options.addArguments("--proxy-server=http://"+proxy);
					}
					capabilities.setCapability("chrome.binary", chromeDriverPath);
					capabilities.setCapability(ChromeOptions.CAPABILITY, options);
					ChromeDriver e = new ChromeDriver(capabilities);
					
					if(StringUtils.hasText(proxy)){
						proxyMap.put(e.hashCode(), proxy);
					}
					e.manage().window().setSize(new Dimension(1600,900));
					e.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
					.setScriptTimeout(120, TimeUnit.SECONDS).pageLoadTimeout(120, TimeUnit.SECONDS);
                    innerQueue.add(e);
                    webDriverList.add(e);
                }
            }
        }
        return innerQueue.take();
    }
    
    @Override
    public void returnToPool(WebDriver webDriver) {
    	try {
    		   checkRunning();
    	        innerQueue.add(webDriver);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
     
    }
  
    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }


    @Override
	public void close(WebDriver webDriver) {
		webDriverList.remove(webDriver);
		try {
			webDriver.quit();
		} catch (Exception e) {
			e.printStackTrace();
			webDriver.quit();
		}
		webDriver = null;
		
	}

    @Override
	public void shutdown() {
		 boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
	        if (!b) {
	            throw new IllegalStateException("Already closed!");
	        }
	        for (WebDriver webDriver : webDriverList) {
	            logger.info("Quit webDriver" + webDriver);
	            try {
					webDriver.quit();
				} catch (Exception e) {
					e.printStackTrace();
					webDriver.quit();
				}
	        }
		
	}

	@Override
	public void removeInvalidProxy(WebDriver driver) {
		ProxyHelper.removeInvalidProxy(proxyMap.get(driver.hashCode()));
	}
    
    
    

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
