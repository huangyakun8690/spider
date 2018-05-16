<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import org.openqa.selenium.WebDriver;

import com.xxl.job.executor.util.ProxyHelper;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 * @param
 */
public interface AdvancedSeleniumWebDriverPool {

	public WebDriver get() throws InterruptedException;

	public void returnToPool(WebDriver webDriver);

	public void close(WebDriver webDriver);

	public void shutdown();
	
	public void removeInvalidProxy(WebDriver driver);

}
=======
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import org.openqa.selenium.WebDriver;

import com.xxl.job.executor.util.ProxyHelper;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-7-11
 * @version 1.0
 * @param
 */
public interface AdvancedSeleniumWebDriverPool {

	public WebDriver get() throws InterruptedException;

	public void returnToPool(WebDriver webDriver);

	public void close(WebDriver webDriver);

	public void shutdown();
	
	public void removeInvalidProxy(WebDriver driver);

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
