<<<<<<< HEAD
/**
 * 
 */
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.xxl.job.executor.util.ObjectPase;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 * @param
 */
public class AdvancedSeleniumDownloader implements Downloader {
	private int sleepTime = 500;
	
	private AdvancedSeleniumWebDriverPool webDriverPool;
	
	private static String SELENIUM_PATH;
	
	private int poolSize;
	
	private boolean useProxy;
	
	private final static int DEFAULT_CAPACITY = 5;
	
	private static String WEBDRIVER_TYPE;
	
	private static String WEBDRIVER_PHANTOMJS="phantomjs";
	
	private static int minValidPageLength=1000;
	
	static {
		try {
			Properties p = new Properties();
			p.load(AdvancedSeleniumDownloader.class.getClassLoader()
					.getResourceAsStream("META-INF/res/resource-development.properties"));
			SELENIUM_PATH = p.get("selenium.path").toString();
			WEBDRIVER_TYPE = p.get("webdriver.type").toString();
		} catch (IOException e) {
			
			ObjectPase.setGetLoggBean("", "",0, "", "", "", "", "", "", 
					"AdvancedSeleniumDownloader [init] encounters exceptions, exception messages are as follows: "
							+ AllErrorMessage.getExceptionStackTrace(e), AdvancedSeleniumDownloader.class.getName(), "error");
		}
	}

	public AdvancedSeleniumDownloader(int poolSize,boolean useProxy) {
		this.poolSize = poolSize;
		this.useProxy = useProxy;
	}
	
	public AdvancedSeleniumDownloader(boolean useProxy) {
		this(DEFAULT_CAPACITY,useProxy);
	}

	

	
	public static String getWEBDRIVER_TYPE() {
		return WEBDRIVER_TYPE;
	}

	public static void setWEBDRIVER_TYPE(String wEBDRIVER_TYPE) {
		WEBDRIVER_TYPE = wEBDRIVER_TYPE;
	}
	
	

	public static String getSELENIUM_PATH() {
		return SELENIUM_PATH;
	}

	public static void setSELENIUM_PATH(String sELENIUM_PATH) {
		SELENIUM_PATH = sELENIUM_PATH;
	}

	@Override
	public Page download(Request request, Task task) {
		return this.download(request, task,30);
	}
	
	public Page download(Request request, Task task,int counter) {
		Page page = new Page();
		WebDriver webDriver = null;
		try {
			checkInit();
			webDriver = webDriverPool.get();
			ObjectPase.setGetLoggBean("", "",0, "", "", "", "", "", "", 
					"downloading page " + request.getUrl()
							, AdvancedSeleniumDownloader.class.getName(), "info");
			webDriver.get(request.getUrl());
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				
			}
			
			WebDriver.Options manage = webDriver.manage();
			Site site = task.getSite();
			if (site.getCookies() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DATE, 1);
				webDriver.manage().deleteAllCookies();
				for (Entry<String, Map<String, String>> cookiesForDomain : site.getAllCookies().entrySet()) {
					for (Entry<String, String> cookieEntry : cookiesForDomain.getValue().entrySet()) {
						Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue(), cookiesForDomain.getKey(), "/",
								cal.getTime());
						manage.addCookie(cookie);
					}
				}
				webDriver.get(request.getUrl());
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					
				}
			}
			manage.window().maximize();
			WebElement webElement = webDriver.findElement(By.xpath("/html"));
			String content = webElement.getAttribute("outerHTML");
			if(content.trim().length()<minValidPageLength)
				throw new org.openqa.selenium.TimeoutException();
			page.setRawText(content);
			page.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content, request.getUrl())));
			page.setUrl(new PlainText(request.getUrl()));
			page.setRequest(request);
			webDriverPool.returnToPool(webDriver);

			return page;
		}catch(org.openqa.selenium.TimeoutException te) {
			ObjectPase.setGetLoggBean("", "",0, "", "", "", "", "", "", 
					"AdvancedSeleniumDownloader [download], page didn't load cause proxy is invalid."
							, AdvancedSeleniumDownloader.class.getName(), "info");

			webDriverPool.removeInvalidProxy(webDriver);
			webDriverPool.close(webDriver);
			if(counter>0){
			counter--;
			return download(request, task,counter);
			}
			else {
				throw te;
			}
		}
		catch (Exception e) {
			ObjectPase.setGetLoggBean("", "",0, "", "", "", "", "", "", 
					"AdvancedSeleniumDownloader [download] encounters exceptions, "
							+ "AdvancedSeleniumWebDriverPool will be destoryed immediately, exception messages are as follows: "
									+ AllErrorMessage.getExceptionStackTrace(e)
							, AdvancedSeleniumDownloader.class.getName(), "info");
			webDriverPool.close(webDriver);
			destoryWebDriverPool();
			return page;
		}
	}
	
	
	public Page download(WebDriver webDriver,int counter) {

		Page page = new Page();
		try {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				
			}
			WebElement webElement = webDriver.findElement(By.xpath("/html"));
			String content = webElement.getAttribute("outerHTML");
			if(content.trim().length()<minValidPageLength)
				throw new org.openqa.selenium.TimeoutException();
			page.setRawText(content);
			page.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content, webDriver.getCurrentUrl())));
			page.setUrl(new PlainText(webDriver.getCurrentUrl()));

			return page;
		}catch(org.openqa.selenium.TimeoutException te) {
			ObjectPase.setGetLoggBean("", "",0, "", "", "", "", "", "", 
					"AdvancedSeleniumDownloader [download], page didn't load cause proxy is invalid."
							, AdvancedSeleniumDownloader.class.getName(), "info");
	
			if(webDriver!=null) {
				webDriverPool.removeInvalidProxy(webDriver);
			}
			
			webDriverPool.close(webDriver);
			if(counter>0){
			counter--;
			return download(webDriver,counter);
			}
			else {
				throw te;
			}
		}
		catch (Exception e) {
			ObjectPase.setGetLoggBean("", "",0, "", "", "", "", "", "", 
					"AdvancedSeleniumDownloader [download] encounters exceptions, "
							+ "AdvancedSeleniumWebDriverPool will be destoryed immediately, exception messages are as follows: "
									+ AllErrorMessage.getExceptionStackTrace(e)
							, AdvancedSeleniumDownloader.class.getName(), "error");
			
			
			webDriverPool.close(webDriver);
			destoryWebDriverPool();
			return page;
		}
	}


	public WebDriver getWebDriver() {
		try {
			checkInit();
			return this.webDriverPool.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			
			return null;
		}
	}

	private void checkInit() {
		if (webDriverPool == null) {
			synchronized (this) {
				if(WEBDRIVER_TYPE.equalsIgnoreCase(WEBDRIVER_PHANTOMJS))
				{
					webDriverPool = new PhantomJSWebDriverPool(poolSize,true,SELENIUM_PATH,useProxy);
				}
				else{
					webDriverPool = new ChromeWebDriverPool(poolSize,SELENIUM_PATH,useProxy);
				}
			}
		}
	}

	public void destoryWebDriverPool() {
		if (webDriverPool != null) {
			try {
				webDriverPool.shutdown();
			} catch (Exception e) {
				
				webDriverPool.shutdown();
			}
			webDriverPool = null;
		}
	}

	public void returnToPool(WebDriver webDriver) {
		webDriverPool.returnToPool(webDriver);
	}

	@Override
	public void setThread(int threadNum) {
		// TODO Auto-generated method stub
	}

	public Downloader setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}
	
	public boolean useProxy(){
		return this.useProxy;
	}
=======
/**
 * 
 */
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.webmagic.ext.SeleniumAction;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.util.ProxyHelper;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 * @param
 */
public class AdvancedSeleniumDownloader implements Downloader {
	
	private static final Logger log = LoggerFactory.getLogger(SeleniumDownloader.class);
	
	private int sleepTime = 500;
	
	private AdvancedSeleniumWebDriverPool webDriverPool;
	
	private static String SELENIUM_PATH;
	
	private int poolSize;
	
	private boolean useProxy;
	
	private final static int DEFAULT_CAPACITY = 5;
	
	private static String WEBDRIVER_TYPE;
	
	private static String WEBDRIVER_CHROME="chrome";
	
	private static String WEBDRIVER_PHANTOMJS="phantomjs";
	
	private static int minValidPageLength=1000;
	
	static {
		try {
			Properties p = new Properties();
			p.load(AdvancedSeleniumDownloader.class.getClassLoader()
					.getResourceAsStream("META-INF/res/resource-development.properties"));
			SELENIUM_PATH = p.get("selenium.path").toString();
			WEBDRIVER_TYPE = p.get("webdriver.type").toString();
		} catch (IOException e) {
			e.printStackTrace();
			XxlJobLogger.log( "AdvancedSeleniumDownloader [init] encounters exceptions, exception messages are as follows: \n"
					+ AllErrorMessage.getExceptionStackTrace(e));
		}
	}

	public AdvancedSeleniumDownloader(int poolSize,boolean useProxy) {
		this.poolSize = poolSize;
		this.useProxy = useProxy;
	}
	
	public AdvancedSeleniumDownloader(boolean useProxy) {
		this(DEFAULT_CAPACITY,useProxy);
	}

	
	
	
	@Override
	public Page download(Request request, Task task) {
		return this.download(request, task,30);
	}
	
	public Page download(Request request, Task task,int counter) {
		Page page = new Page();
		WebDriver webDriver = null;
		try {
			checkInit();
			webDriver = webDriverPool.get();
			log.info("downloading page " + request.getUrl());
//			String js = "var url =  '"+request.getUrl()+"';" +
//		            "var page = this; page.onResourceReceived = function(response)"
//		            + " {if (response.stage !== \"end\" || response.url != url) return; page.tag = response;};";
//			((JavascriptExecutor)webDriver).executeScript(js);
			webDriver.get(request.getUrl());
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		    Map<String, Object> result = (Map<String, Object>) ((JavascriptExecutor)webDriver).executeScript("var page = this; return page.tag;");
//		    System.out.println(result);
			
			WebDriver.Options manage = webDriver.manage();
			Site site = task.getSite();
			if (site.getCookies() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DATE, 1);
				webDriver.manage().deleteAllCookies();
				for (Entry<String, Map<String, String>> cookiesForDomain : site.getAllCookies().entrySet()) {
					for (Entry<String, String> cookieEntry : cookiesForDomain.getValue().entrySet()) {
						Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue(), cookiesForDomain.getKey(), "/",
								cal.getTime());
						manage.addCookie(cookie);
					}
				}
				webDriver.get(request.getUrl());
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			manage.window().maximize();
			WebElement webElement = webDriver.findElement(By.xpath("/html"));
			String content = webElement.getAttribute("outerHTML");
			if(content.trim().length()<minValidPageLength)
				throw new org.openqa.selenium.TimeoutException();
			page.setRawText(content);
			page.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content, request.getUrl())));
			page.setUrl(new PlainText(request.getUrl()));
			page.setRequest(request);
			webDriverPool.returnToPool(webDriver);
//			XxlJobLogger.log("get page>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n"+content);
			return page;
		}catch(org.openqa.selenium.TimeoutException te) {
			XxlJobLogger.log( "AdvancedSeleniumDownloader [download], page didn't load cause proxy is invalid.");
			webDriverPool.removeInvalidProxy(webDriver);
			webDriverPool.close(webDriver);
			if(counter>0){
			counter--;
			return download(request, task,counter);
			}
			else {
				throw te;
			}
		}
		catch (Exception e) {
			XxlJobLogger.log( "AdvancedSeleniumDownloader [download] encounters exceptions, "
					+ "AdvancedSeleniumWebDriverPool will be destoryed immediately, exception messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
			webDriverPool.close(webDriver);
			destoryWebDriverPool();
			return page;
		}
	}
	
	
	public Page download(WebDriver webDriver,int counter) {
//		int counter=5;
		Page page = new Page();
		try {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			WebElement webElement = webDriver.findElement(By.xpath("/html"));
			String content = webElement.getAttribute("outerHTML");
			if(content.trim().length()<minValidPageLength)
				throw new org.openqa.selenium.TimeoutException();
			page.setRawText(content);
			page.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content, webDriver.getCurrentUrl())));
			page.setUrl(new PlainText(webDriver.getCurrentUrl()));
//			XxlJobLogger.log("get page>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n"+page.getHtml().get());
			return page;
		}catch(org.openqa.selenium.TimeoutException te) {
			XxlJobLogger.log( "AdvancedSeleniumDownloader [download], page didn't load cause proxy is invalid.");
			webDriverPool.removeInvalidProxy(webDriver);
			webDriverPool.close(webDriver);
			if(counter>0){
			counter--;
			return download(webDriver,counter);
			}
			else {
				throw te;
			}
		}
		catch (Exception e) {
			XxlJobLogger.log( "AdvancedSeleniumDownloader [download] encounters exceptions, "
					+ "AdvancedSeleniumWebDriverPool will be destoryed immediately, exception messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			e.printStackTrace();
			webDriverPool.close(webDriver);
			destoryWebDriverPool();
			return page;
		}
	}


	public WebDriver getWebDriver() {
		try {
			checkInit();
			return this.webDriverPool.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void checkInit() {
		if (webDriverPool == null) {
			synchronized (this) {
				if(WEBDRIVER_TYPE.equalsIgnoreCase(WEBDRIVER_PHANTOMJS))
				{
					webDriverPool = new PhantomJSWebDriverPool(poolSize,true,SELENIUM_PATH,useProxy);
				}
				else{
					webDriverPool = new ChromeWebDriverPool(poolSize,SELENIUM_PATH,useProxy);
				}
			}
		}
	}

	public void destoryWebDriverPool() {
		if (webDriverPool != null) {
			try {
				webDriverPool.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
				webDriverPool.shutdown();
			}
			webDriverPool = null;
		}
	}

	public void returnToPool(WebDriver webDriver) {
		webDriverPool.returnToPool(webDriver);
	}

	@Override
	public void setThread(int threadNum) {
		// TODO Auto-generated method stub
	}

	public Downloader setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}
	
	public boolean useProxy(){
		return this.useProxy;
	}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
}