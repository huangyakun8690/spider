<<<<<<< HEAD
package com.xxl.job.executor.nextpage;

import java.util.List;

import com.xxl.job.executor.util.CommonHelper;
import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xxl.job.executor.util.WebDriverHelper;
import org.springframework.util.StringUtils;

public class NumMatchNextPageGenerator implements NextPageGenerator {

	@Override
	public WebElement getNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray,List<String> listPageUrls) throws Exception {
		WebElement parentElement = WebDriverHelper.findNextPageElement(webDriver, currentPageNo,
				pageFlipArray);
		List<WebElement> alist = parentElement.findElements(By.tagName("a"));
		for(WebElement a : alist){
			WebElement span = a.findElement(By.tagName("span"));
			if(String.valueOf(currentPageNo+1).equals(span.getAttribute("innerText"))) {
				if (WebDriverHelper.hrefIsValid(a)) {
					webDriver.get(a.getAttribute("href"));
				} else {
					a.click();
					WebDriverHelper.switchToNewWindow(webDriver);
				}
				return a;
			}
		}
		String nextPageUrl = CommonHelper.findNextPageByUrlComparison(currentPageNo,
				listPageUrls);
		if (!StringUtils.hasText(nextPageUrl))
			throw new Exception(
					"#LAD# page processor [get next list page url], cannot find page flip element , current pageNo: "
							+ currentPageNo);
		webDriver.get(nextPageUrl);
		return null;
	}

}
=======
package com.xxl.job.executor.nextpage;

import java.util.List;

import com.xxl.job.executor.util.CommonHelper;
import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xxl.job.executor.util.WebDriverHelper;
import org.springframework.util.StringUtils;

public class NumMatchNextPageGenerator implements NextPageGenerator {

	@Override
	public WebElement getNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray,List<String> listPageUrls) throws Exception {
		WebElement parentElement = WebDriverHelper.findNextPageElement(webDriver, currentPageNo,
				pageFlipArray);
		List<WebElement> alist = parentElement.findElements(By.tagName("a"));
		for(WebElement a : alist){
			WebElement span = a.findElement(By.tagName("span"));
			if(String.valueOf(currentPageNo+1).equals(span.getAttribute("innerText"))) {
				if (WebDriverHelper.hrefIsValid(a)) {
					webDriver.get(a.getAttribute("href"));
				} else {
					a.click();
					WebDriverHelper.switchToNewWindow(webDriver);
				}
				return a;
			}
		}
		String nextPageUrl = CommonHelper.findNextPageByUrlComparison(currentPageNo,
				listPageUrls);
		if (!StringUtils.hasText(nextPageUrl))
			throw new Exception(
					"#LAD# page processor [get next list page url], cannot find page flip element , current pageNo: "
							+ currentPageNo);
		webDriver.get(nextPageUrl);
		return null;
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
