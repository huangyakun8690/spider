<<<<<<< HEAD
package com.xxl.job.executor.nextpage;

import java.util.List;

import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.CommonHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xxl.job.executor.util.WebDriverHelper;
import org.springframework.util.StringUtils;

//马鞍山
public class CharactorMatchNextPageGenerator implements NextPageGenerator{

	@Override
	public WebElement getNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray,List<String> listPageUrls) throws Exception{
		
		WebElement parentElement = WebDriverHelper.findNextPageElement(webDriver, currentPageNo,
				pageFlipArray);
		List<WebElement> list = parentElement.findElements(By.tagName("a"));
		for(WebElement ele : list){
			if("下一页".equals(ele.getAttribute("innerText"))){
				if (WebDriverHelper.hrefIsValid(ele)) {
					webDriver.get(ele.getAttribute("href"));
				} else {
					ele.click();
					WebDriverHelper.switchToNewWindow(webDriver);
				}
				return ele;
			}else{
				WebElement sonElement = ele.findElement(By.tagName("img"));
				String msgSrc = sonElement.getAttribute("src");
				JSONObject nextPageRule = pageFlipArray.getJSONObject(0) ;
				String imgStr = nextPageRule.getString(JsonKeys.ZCD_PAGE_INPUT);
				if(imgStr.equals(msgSrc)){
					if (WebDriverHelper.hrefIsValid(ele)) {
						webDriver.get(ele.getAttribute("href"));
					} else {
						ele.click();
						WebDriverHelper.switchToNewWindow(webDriver);
					}
				}
			}

		}
		String nextPageUrl = CommonHelper.findNextPageByUrlComparison(currentPageNo,listPageUrls);
		if (!StringUtils.hasText(nextPageUrl))
			throw new Exception("#LAD# page processor [get next list page url], cannot find page flip element , current pageNo: "+ currentPageNo);
		webDriver.get(nextPageUrl);
		return null;
	}

}
=======
package com.xxl.job.executor.nextpage;

import java.util.List;

import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.CommonHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.xxl.job.executor.util.WebDriverHelper;
import org.springframework.util.StringUtils;

//马鞍山
public class CharactorMatchNextPageGenerator implements NextPageGenerator{

	@Override
	public WebElement getNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray,List<String> listPageUrls) throws Exception{
		
		WebElement parentElement = WebDriverHelper.findNextPageElement(webDriver, currentPageNo,
				pageFlipArray);
		List<WebElement> list = parentElement.findElements(By.tagName("a"));
		for(WebElement ele : list){
			if("下一页".equals(ele.getAttribute("innerText"))){
				if (WebDriverHelper.hrefIsValid(ele)) {
					webDriver.get(ele.getAttribute("href"));
				} else {
					ele.click();
					WebDriverHelper.switchToNewWindow(webDriver);
				}
				return ele;
			}else{
				WebElement sonElement = ele.findElement(By.tagName("img"));
				String msgSrc = sonElement.getAttribute("src");
				JSONObject nextPageRule = pageFlipArray.getJSONObject(0) ;
				String imgStr = nextPageRule.getString(JsonKeys.ZCD_PAGE_INPUT);
				if(imgStr.equals(msgSrc)){
					if (WebDriverHelper.hrefIsValid(ele)) {
						webDriver.get(ele.getAttribute("href"));
					} else {
						ele.click();
						WebDriverHelper.switchToNewWindow(webDriver);
					}
				}
			}

		}
		String nextPageUrl = CommonHelper.findNextPageByUrlComparison(currentPageNo,listPageUrls);
		if (!StringUtils.hasText(nextPageUrl))
			throw new Exception("#LAD# page processor [get next list page url], cannot find page flip element , current pageNo: "+ currentPageNo);
		webDriver.get(nextPageUrl);
		return null;
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
