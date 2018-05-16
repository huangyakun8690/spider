<<<<<<< HEAD
/**
 * 
 */
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.xxl.job.executor.util.ProxyHelper;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 */
public class PhantomJSWebDriverPool implements AdvancedSeleniumWebDriverPool {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private int CAPACITY = 5;

	private boolean useProxy;

	private AtomicInteger refCount = new AtomicInteger(0);

	/**
	 * store webDrivers available
	 */
	private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>(CAPACITY);

	private AtomicBoolean shutdowned = new AtomicBoolean(false);

	private String PHANTOMJS_PATH;
	private DesiredCapabilities caps = DesiredCapabilities.phantomjs();
	
	private Map<Integer,String> proxyMap = new HashMap<Integer,String>();

	/**
	 * 
	 * @param poolsize
	 * @param loadImg
	 *            是否加载图片，默认不加载
	 * @param useProxy
	 */
	public PhantomJSWebDriverPool(int poolsize, boolean loadImg, String phantomjsPath, boolean useProxy) {
		this.CAPACITY = poolsize;
		this.useProxy = useProxy;
		innerQueue = new LinkedBlockingDeque<WebDriver>(poolsize);
		PHANTOMJS_PATH = phantomjsPath;
		caps.setJavascriptEnabled(true);
		caps.setCapability("webStorageEnabled", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOMJS_PATH);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
		ArrayList<String> cliArgsCap = new ArrayList<String>();
		cliArgsCap.add("--web-security=false");
		cliArgsCap.add("--ssl-protocol=any");
		cliArgsCap.add("--ignore-ssl-errors=true");
		cliArgsCap.add("--load-images=true");
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS, new String[] { "--logLevel=INFO" });

	}

	@SuppressWarnings("unchecked")
	@Override
	public WebDriver get() throws InterruptedException {
		WebDriver poll = innerQueue.poll();
		if (poll != null) {
			return poll;
		}
		if (refCount.get() < CAPACITY) {
			synchronized (innerQueue) {
				if (refCount.get() < CAPACITY) {
					String proxy=null;
					if (useProxy) {
						proxy = ProxyHelper.getProxy();
						((ArrayList<String>) caps.getCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS))
								.add("--proxy="+proxy);
					}
					WebDriver mDriver = new PhantomJSDriver(caps);
					if(StringUtils.hasText(proxy)){
						proxyMap.put(mDriver.hashCode(), proxy);
					}
					mDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
							.setScriptTimeout(60, TimeUnit.SECONDS).pageLoadTimeout(60, TimeUnit.SECONDS);
					innerQueue.add(mDriver);
					refCount.incrementAndGet();
				}
			}
		}
		return innerQueue.take();
	}

	@Override
	public void returnToPool(WebDriver webDriver) {
		if (shutdowned.get()) {
			webDriver.quit();
			webDriver = null;
		} else {
			Set<String> handles = webDriver.getWindowHandles();
			if (handles.size() > 1) {
				int index = 0;
				for (String handle : handles) {
					if (index == 0) {
						index++;
						continue;
					}
					webDriver.close();
					index++;
				}
			}
			synchronized (shutdowned) {
				if (!shutdowned.get()) {
					innerQueue.add(webDriver);
				} else {
					webDriver.quit();
					webDriver = null;
				}
			}
		}
	}

	@Override
	public void close(WebDriver webDriver) {
		refCount.decrementAndGet();
		try {
			webDriver.quit();
		} catch (Exception e) {
			
			webDriver.quit();
		}
		webDriver = null;
	}

	@Override
	public void shutdown() {
		synchronized (shutdowned) {
			shutdowned.set(true);
		}
		try {
			for (WebDriver driver : innerQueue) {
				close(driver);
			}
			innerQueue.clear();
			refCount.set(0);
		} catch (Exception e) {
			logger.warn("webdriverpool关闭失败", e);
		}
	}
	
	public void removeInvalidProxy(WebDriver driver) {
			ProxyHelper.removeInvalidProxy(proxyMap.get(driver.hashCode()));
	}
}
=======
/**
 * 
 */
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.xxl.job.executor.util.ProxyHelper;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 */
public class PhantomJSWebDriverPool implements AdvancedSeleniumWebDriverPool {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private int CAPACITY = 5;

	private boolean useProxy;

	private AtomicInteger refCount = new AtomicInteger(0);

	/**
	 * store webDrivers available
	 */
	private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>(CAPACITY);

	private AtomicBoolean shutdowned = new AtomicBoolean(false);

	private String PHANTOMJS_PATH;
	private DesiredCapabilities caps = DesiredCapabilities.phantomjs();
	
	private Map<Integer,String> proxyMap = new HashMap<Integer,String>();

	/**
	 * 
	 * @param poolsize
	 * @param loadImg
	 *            是否加载图片，默认不加载
	 * @param useProxy
	 */
	public PhantomJSWebDriverPool(int poolsize, boolean loadImg, String phantomjsPath, boolean useProxy) {
		this.CAPACITY = poolsize;
		this.useProxy = useProxy;
		innerQueue = new LinkedBlockingDeque<WebDriver>(poolsize);
		PHANTOMJS_PATH = phantomjsPath;
		caps.setJavascriptEnabled(true);
		caps.setCapability("webStorageEnabled", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOMJS_PATH);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
		ArrayList<String> cliArgsCap = new ArrayList<String>();
		cliArgsCap.add("--web-security=false");
		cliArgsCap.add("--ssl-protocol=any");
		cliArgsCap.add("--ignore-ssl-errors=true");
		cliArgsCap.add("--load-images=true");
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS, new String[] { "--logLevel=INFO" });

	}

	@SuppressWarnings("unchecked")
	@Override
	public WebDriver get() throws InterruptedException {
		WebDriver poll = innerQueue.poll();
		if (poll != null) {
			return poll;
		}
		if (refCount.get() < CAPACITY) {
			synchronized (innerQueue) {
				if (refCount.get() < CAPACITY) {
					String proxy=null;
					if (useProxy) {
						proxy = ProxyHelper.getProxy();
						((ArrayList<String>) caps.getCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS))
								.add("--proxy="+proxy);
					}
					WebDriver mDriver = new PhantomJSDriver(caps);
					if(StringUtils.hasText(proxy)){
						proxyMap.put(mDriver.hashCode(), proxy);
					}
					mDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
							.setScriptTimeout(60, TimeUnit.SECONDS).pageLoadTimeout(60, TimeUnit.SECONDS);
					innerQueue.add(mDriver);
					refCount.incrementAndGet();
				}
			}
		}
		return innerQueue.take();
	}

	@Override
	public void returnToPool(WebDriver webDriver) {
		if (shutdowned.get()) {
			webDriver.quit();
			webDriver = null;
		} else {
			Set<String> handles = webDriver.getWindowHandles();
			if (handles.size() > 1) {
				int index = 0;
				for (String handle : handles) {
					if (index == 0) {
						index++;
						continue;
					}
					webDriver.close();
					index++;
				}
			}
			synchronized (shutdowned) {
				if (!shutdowned.get()) {
					innerQueue.add(webDriver);
				} else {
					webDriver.quit();
					webDriver = null;
				}
			}
		}
	}

	@Override
	public void close(WebDriver webDriver) {
		refCount.decrementAndGet();
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
		synchronized (shutdowned) {
			shutdowned.set(true);
		}
		try {
			for (WebDriver driver : innerQueue) {
				close(driver);
			}
			innerQueue.clear();
			refCount.set(0);
		} catch (Exception e) {
			logger.warn("webdriverpool关闭失败", e);
		}
	}
	
	public void removeInvalidProxy(WebDriver driver) {
			ProxyHelper.removeInvalidProxy(proxyMap.get(driver.hashCode()));
	}
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
