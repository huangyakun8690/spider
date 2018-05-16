<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;

public class WebDriverHelper {

	public static boolean hrefIsValid(WebElement element) {
		return "a".equalsIgnoreCase(element.getTagName()) && StringUtils.hasText(element.getAttribute("href"))
				&& !element.getAttribute("href").endsWith("#") && !element.getAttribute("href").contains("javascript");
	}

	public static boolean switchToNewWindow(WebDriver webDriver) {
		// get current handle
		String currentWindow = webDriver.getWindowHandle();
		// get handles of all windows
		Set<String> handles = webDriver.getWindowHandles();
		// go through handles of all windows
		Iterator<String> it = handles.iterator();
		while (it.hasNext()) {
			String next = it.next();
			if (currentWindow.equalsIgnoreCase(next)) {
				continue;
			}
			webDriver.switchTo().window(next);
			return true;
		}
		return false;
	}

	public static WebElement findElement(WebDriver webDriver, String elementId, String elementXpath, String elementName,
			String elementCss) throws Exception {
		WebElement element = null;
		if (StringUtils.hasText(elementId) && waitForElementPresence(webDriver, By.id(elementId), null)) {
			element = webDriver.findElement(By.id(elementId));
		}
		if (element == null && StringUtils.hasText(elementXpath)
				&& waitForElementPresence(webDriver, By.xpath(elementXpath), null)) {
			element = webDriver.findElement(By.xpath(elementXpath));
		}
		if (element == null && StringUtils.hasText(elementName)
				&& waitForElementPresence(webDriver, By.name(elementName), null)) {
			element = webDriver.findElement(By.name(elementName));
		}
		if (element == null && StringUtils.hasText(elementCss)
				&& waitForElementPresence(webDriver, By.cssSelector(elementCss), null)) {
			element = webDriver.findElement(By.cssSelector(elementCss));
		}
		return element;
	}

	public static WebElement findNextPageElement(WebDriver webDriver, int currentPageNo, String elementCss,
			String elementXpath, String elementId) throws Exception {
		webDriver.manage().window().maximize();
		WebElement element = null;
		if (element == null && StringUtils.hasText(elementId)
				&& waitForElementPresence(webDriver, By.id(elementId), currentPageNo)) {
			element = webDriver.findElement(By.id(elementId));
		}
		if (element == null && StringUtils.hasText(elementXpath)
				&& waitForElementPresence(webDriver, By.xpath(elementXpath), currentPageNo)) {
			element = webDriver.findElement(By.xpath(elementXpath));
		}
		if (element == null && StringUtils.hasText(elementCss)
				&& waitForElementPresence(webDriver, By.cssSelector(elementCss), currentPageNo)) {
			List<WebElement> elements = webDriver.findElements(By.cssSelector(elementCss));
			if (elements.size() == 1) {
				element = elements.get(0);
			}
		}
		return element;
	}

	private static boolean waitForElementPresence(WebDriver webDriver, By by, Integer currentPageNo) {
		
		try {
			new WebDriverWait(webDriver, 10).until(ExpectedConditions.presenceOfElementLocated(by));
			return true;
		} catch (Exception e) {
			if (currentPageNo == null) {
						//logger.info(" #LAD# page processor [locate element], >>>>>>>>>>>>>>>>>>>>>cannot find element by " + by);
				
				ObjectPase.setGetLoggBean("", "", 0, "1",
						"", "", "", "", "",
						"#LAD# page processor [locate element], >>>>>>>>>>>>>>>>>>>>>cannot find element by " + by,
						WebDriverHelper.class.getName(), "info");
			} else {
				
				
				ObjectPase.setGetLoggBean("", "", 0, "1",
						"", "", "", "", "",
						"#LAD# page processor [locate page flip element], >>>>>>>>>>>>>>>>>>>>>cannot find page flip element by "+ by + ", current pageNo: " + (currentPageNo),
						WebDriverHelper.class.getName(), "info");
			}
			return false;
		}
	}

	public static InputStream saveFile(String url) {
		//Logger logger = LoggerFactory.getLogger(WebDriverHelper.class);
		HttpURLConnection httpUrl = null;
		URL imageUrl = null;
		InputStream in = null;

		try {
			imageUrl = new URL(url);
			httpUrl = (HttpURLConnection) imageUrl.openConnection();
			httpUrl.setRequestMethod("GET");
			httpUrl.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64)");
			httpUrl.setReadTimeout(5 * 1000);
			httpUrl.connect();
			in = httpUrl.getInputStream();
			return in;
		} catch (Exception e) {
			
			ObjectPase.setGetLoggBean("", "", 0, "", "", "", "", "", "", 
					WebDriverHelper.class.getName()+"saveFile 方法异常："+AllErrorMessage.getExceptionStackTrace(e), WebDriverHelper.class.getName(), "error");
	
			//logger.info(AllErrorMessage.getExceptionStackTrace(e));
			
		}
		return null;
	}

	public static WebElement findNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray)
			throws Exception {
		if (pageFlipArray == null || pageFlipArray.length() < 1)
			throw new Exception("pageFlipArray is null.");
		else if (pageFlipArray.length() == 1) {
			return findNextPageElement(webDriver, currentPageNo,
					pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_NEXTCLASS),
					pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_NEXTXPATH),
					pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_NEXTID));
		} else {
			for (int i = 0; i < pageFlipArray.length(); i++) {
				int pageNumStart = pageFlipArray.getJSONObject(i).getInt(JsonKeys.ZCD_PAGE_PAGENUM_START);
				int pageNumEnd = pageFlipArray.getJSONObject(i).getInt(JsonKeys.ZCD_PAGE_PAGENUM_END);
				if (currentPageNo < pageNumEnd && currentPageNo >= pageNumStart) {
					return findNextPageElement(webDriver, currentPageNo,
							pageFlipArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_NEXTCLASS),
							pageFlipArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_NEXTXPATH),
							pageFlipArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_NEXTID));
				} else {
					continue;
				}
			}
		}
		return null;
	}
}
=======
package com.xxl.job.executor.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.core.log.XxlJobLogger;

public class WebDriverHelper {

	public static boolean hrefIsValid(WebElement element) {
		return "a".equalsIgnoreCase(element.getTagName()) && StringUtils.hasText(element.getAttribute("href"))
				&& !element.getAttribute("href").endsWith("#") && !element.getAttribute("href").contains("javascript");
	}

	public static boolean switchToNewWindow(WebDriver webDriver) {
		// get current handle
		String currentWindow = webDriver.getWindowHandle();
		// get handles of all windows
		Set<String> handles = webDriver.getWindowHandles();
		// go through handles of all windows
		Iterator<String> it = handles.iterator();
		while (it.hasNext()) {
			String next = it.next();
			if (currentWindow.equalsIgnoreCase(next)) {
				continue;
			}
			webDriver.switchTo().window(next);
			return true;
		}
		return false;
	}

	public static WebElement findElement(WebDriver webDriver, String elementId, String elementXpath, String elementName,
			String elementCss) throws Exception {
		WebElement element = null;
		if (StringUtils.hasText(elementId) && waitForElementPresence(webDriver, By.id(elementId), null)) {
			element = webDriver.findElement(By.id(elementId));
		}
		if (element == null && StringUtils.hasText(elementXpath)
				&& waitForElementPresence(webDriver, By.xpath(elementXpath), null)) {
			element = webDriver.findElement(By.xpath(elementXpath));
		}
		if (element == null && StringUtils.hasText(elementName)
				&& waitForElementPresence(webDriver, By.name(elementName), null)) {
			element = webDriver.findElement(By.name(elementName));
		}
		if (element == null && StringUtils.hasText(elementCss)
				&& waitForElementPresence(webDriver, By.cssSelector(elementCss), null)) {
			element = webDriver.findElement(By.cssSelector(elementCss));
		}
		return element;
	}

	public static WebElement findNextPageElement(WebDriver webDriver, int currentPageNo, String elementCss,
			String elementXpath, String elementId) throws Exception {
		webDriver.manage().window().maximize();
		WebElement element = null;
		if (element == null && StringUtils.hasText(elementId)
				&& waitForElementPresence(webDriver, By.id(elementId), currentPageNo)) {
			element = webDriver.findElement(By.id(elementId));
		}
		if (element == null && StringUtils.hasText(elementXpath)
				&& waitForElementPresence(webDriver, By.xpath(elementXpath), currentPageNo)) {
			element = webDriver.findElement(By.xpath(elementXpath));
		}
		if (element == null && StringUtils.hasText(elementCss)
				&& waitForElementPresence(webDriver, By.cssSelector(elementCss), currentPageNo)) {
			List<WebElement> elements = webDriver.findElements(By.cssSelector(elementCss));
			if (elements.size() == 1) {
				element = elements.get(0);
			}
		}
		return element;
	}

	private static boolean waitForElementPresence(WebDriver webDriver, By by, Integer currentPageNo) {
		Logger logger = LoggerFactory.getLogger(WebDriverHelper.class);
		try {
			new WebDriverWait(webDriver, 10).until(ExpectedConditions.presenceOfElementLocated(by));
			return true;
		} catch (Exception e) {
			if (currentPageNo == null) {
				XxlJobLogger.log(
						" #LAD# page processor [locate element], >>>>>>>>>>>>>>>>>>>>>cannot find element by " + by);
				logger.info(" #LAD# page processor [locate element], >>>>>>>>>>>>>>>>>>>>>cannot find element by " + by);
			} else {
				XxlJobLogger
						.log(" #LAD# page processor [locate page flip element], >>>>>>>>>>>>>>>>>>>>>cannot find page flip element by "
								+ by + ", current pageNo: " + (currentPageNo));
				logger.info(" #LAD# page processor [locate page flip element], >>>>>>>>>>>>>>>>>>>>>cannot find page flip element by "
						+ by + ", current pageNo: " + (currentPageNo));
			}
			return false;
		}
	}

	public static InputStream saveFile(String url) {
		Logger logger = LoggerFactory.getLogger(WebDriverHelper.class);
		HttpURLConnection httpUrl = null;
		URL imageUrl = null;
		InputStream in = null;

		try {
			imageUrl = new URL(url);
			httpUrl = (HttpURLConnection) imageUrl.openConnection();
			httpUrl.setRequestMethod("GET");
			httpUrl.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64)");
			httpUrl.setReadTimeout(5 * 1000);
			httpUrl.connect();
			in = httpUrl.getInputStream();
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
			
		}
		return null;
	}

	public static WebElement findNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray)
			throws Exception {
		if (pageFlipArray == null || pageFlipArray.length() < 1)
			throw new Exception("pageFlipArray is null.");
		else if (pageFlipArray.length() == 1) {
			return findNextPageElement(webDriver, currentPageNo,
					pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_NEXTCLASS),
					pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_NEXTXPATH),
					pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_NEXTID));
		} else {
			for (int i = 0; i < pageFlipArray.length(); i++) {
				int pageNumStart = pageFlipArray.getJSONObject(i).getInt(JsonKeys.ZCD_PAGE_PAGENUM_START);
				int pageNumEnd = pageFlipArray.getJSONObject(i).getInt(JsonKeys.ZCD_PAGE_PAGENUM_END);
				if (currentPageNo < pageNumEnd && currentPageNo >= pageNumStart) {
					return findNextPageElement(webDriver, currentPageNo,
							pageFlipArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_NEXTCLASS),
							pageFlipArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_NEXTXPATH),
							pageFlipArray.getJSONObject(i).getString(JsonKeys.ZCD_PAGE_NEXTID));
				} else {
					continue;
				}
			}
		}
		return null;
	}
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
