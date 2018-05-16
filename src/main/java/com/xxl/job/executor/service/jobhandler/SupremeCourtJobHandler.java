package com.xxl.job.executor.service.jobhandler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.service.CollectionSiteService;
import com.ustcinfo.ptp.yunting.service.CollectionStrategyService;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.executor.util.RuoKuai;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

@JobHander(value = "supremeCourtJobHandler")
@Service
public class SupremeCourtJobHandler extends IJobHandler {

	// @Autowired
	// private ExecutorService executorService;

	// @Autowired
	// private AcqPageRuleService acqPageRuleService;

	@Autowired
	private CollectionStrategyService collectionStrategyService;

	@Autowired
	private CollectionSiteService collectionSiteService;

	@Autowired
	private InfoEntityService infoService;	

	private static int SLEEPTIME = 1000;

	@Override
	public ReturnT<String> execute(String... params) throws Exception {
		XxlJobLogger.log("[" + params[0] + "] acquisition task starts running.");
		// declare the asDownloader
		AdvancedSeleniumDownloader asDownloader = null;
		try {
			CollectionSite site = this.collectionSiteService.findByNamedParam("serialNumber", params[0]).get(0);
			String siteUrl = site.getSiteUrl();
			CollectionStrategy collectionStrategy = site.getAcqStrategyId() == null
					? new CollectionStrategy(1, 1000, 10000, 1, 0,1)
					: this.collectionStrategyService.getEntity(site.getAcqStrategyId());
			asDownloader = new AdvancedSeleniumDownloader(1,false);
			Spider.create(new SupremeCourtPageProcessor(asDownloader,infoService,site)).addUrl(siteUrl)
					.thread(collectionStrategy.getThreadNum())
					.setDownloader(asDownloader.setSleepTime(SLEEPTIME)).run();
			// end and return
			XxlJobLogger.log("[" + params[0] + "] acquisition task ends running.");
			return ReturnT.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			XxlJobLogger.log(
					"[" + params[0] + "] acquisition task encounters exceptions, detail messages are as follows: \n"
							+ AllErrorMessage.getExceptionStackTrace(e));
			return ReturnT.FAIL;
		} finally {
			// destroy webdriver
			if (null != asDownloader) {
				asDownloader.destoryWebDriverPool();
			}
		}
	}
}

class SupremeCourtPageProcessor implements PageProcessor {
	
	private Site site = Site.me().setRetryTimes(2).setSleepTime(1000).setTimeOut(30 * 1000);

	private AdvancedSeleniumDownloader asDownloader;
	
	private InfoEntityService infoService;
	
	private CollectionSite acqSite;
	
	private boolean isResultList = false;
	
	private String PBENameXpath = "html/body/div[1]/table/tbody/tr[#index#]/td[2]/a/text()";
	
	private String fillingTimeXpath = "html/body/div[1]/table/tbody/tr[#index#]/td[3]/text()";
	
	private String caseNoXpath = "html/body/div[1]/table/tbody/tr[#index#]/td[4]/text()";
	
	private String replacement = "#index#";
	
	private String flipBtnForFirstPageXpath = "html/body/div[1]/div/a[1]";
	
	private String flipBtnForFollowingPageXpath = "html/body/div[1]/div/a[3]";
	
	private AtomicInteger currentPageNo = new AtomicInteger(1);
	
	private int pageNum = 200;

	public SupremeCourtPageProcessor(AdvancedSeleniumDownloader asDownloader,InfoEntityService infoService, CollectionSite acqSite) {
		this.asDownloader = asDownloader;
		this.infoService = infoService;
		this.acqSite = acqSite;
	}

	

	@Override
	public void process(Page page) {
			String randomKey = UUID.randomUUID().toString();
			// doSearch and getResult
			WebDriver webDriver = null;
			boolean resultShowed = false;
			int counter = 5;
			try {
				
				if (isResultList) {
					XxlJobLogger.log("#Supreme Court# page processor [process detail page], starts to extract results, current pageNo: "
									+ (currentPageNo.get()));
					for(int i = 2; i<=11;i++)
					{
						JSONObject pageJSONObj = new JSONObject();
						pageJSONObj.put("被执行人姓名/名称",
								page.getHtml().xpath(PBENameXpath.replace(replacement, String.valueOf(i))).get());
						pageJSONObj.put("立案时间",
								page.getHtml().xpath(fillingTimeXpath.replace(replacement, String.valueOf(i))).get());
						pageJSONObj.put("案号",
								page.getHtml().xpath(caseNoXpath.replace(replacement, String.valueOf(i))).get());
						// store in the cache temporarily
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(Constants.STORE_PAGE_HTML, page);
						map.put(Constants.STORE_PAGE_JSON, pageJSONObj);
						InformationCache.getCache().put(randomKey, map);
						XxlJobLogger.log("#Supreme Court# page processor [process detail page], page info has been put into cache.");
						// end and log
						XxlJobLogger.log(pageJSONObj.toString());
					}
					// store page info
					List<HashMap<String, Object>> list = InformationCache.getCache().get(randomKey);
					XxlJobLogger.log("#Supreme Court# page processor [process detail page], page info has been get from cache.");
					infoService.storePageInfo(list, acqSite);
					if (currentPageNo.getAndIncrement() < pageNum) {
						// flip over the page
						try {
							webDriver = asDownloader.getWebDriver();
							webDriver.findElement(By.xpath((currentPageNo.get()-1)==1?flipBtnForFirstPageXpath:flipBtnForFollowingPageXpath)).click();
							Thread.sleep(100);
							XxlJobLogger.log(" #Supreme Court# page processor [get next page], current pageNo: "
									+ (currentPageNo.get() - 1));
							processNewPage(webDriver);
						} catch (Exception e) {
							if (webDriver != null) {
								asDownloader.returnToPool(webDriver);
							}
							asDownloader.destoryWebDriverPool();
							XxlJobLogger
									.log("#Supreme Court# page processor [page flip over] enncounters exceptions, detail messages are as follows: \n"
											+ AllErrorMessage.getExceptionStackTrace(e));
							e.printStackTrace();
						}
					}
				} else {
					webDriver = asDownloader.getWebDriver();
					XxlJobLogger.log(" #Supreme Court# page processor [search step], starts to run.");
					do {
						doSearch(webDriver);
						resultShowed = findResult(webDriver);
						counter--;
					} while (!resultShowed && counter > 0);
					if(counter==0){
						throw new Exception("Captcha Img recognition has failed for 5 times, acquisition task terminates.");
					}
					isResultList=true;
					processNewPage(webDriver);
					
				}
			} catch (Exception e) {
				if (webDriver != null) {
					asDownloader.returnToPool(webDriver);
				}
				asDownloader.destoryWebDriverPool();
				XxlJobLogger
						.log("#Supreme Court# page processor enncounters exceptions, detail messages are as follows: \n"
								+ AllErrorMessage.getExceptionStackTrace(e));
				e.printStackTrace();
			}
	}

	private void processNewPage(WebDriver webDriver) {
		WebElement webElement = webDriver.findElement(By.xpath("/html"));
		String content = webElement.getAttribute("outerHTML");
		Page newPage = new Page();
		newPage.setRawText(content);
		newPage.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content, webDriver.getCurrentUrl())));
		newPage.setUrl(new PlainText(webDriver.getCurrentUrl()));
		asDownloader.returnToPool(webDriver);
		XxlJobLogger.log("");
		process(newPage);
	}



	private boolean findResult(WebDriver webDriver) {
		try {
			webDriver.findElement(By.id("Resultlist"));
			XxlJobLogger.log(" #Supreme Court# page processor [search step], results have been loaded.");
			return true;
		} catch (NoSuchElementException e) {
			XxlJobLogger.log(" #Supreme Court# page processor [search step], results have not been loaded.");
			XxlJobLogger.log(" #Supreme Court# page processor [search step], switch back to default frame and redo search step.");
			webDriver.switchTo().defaultContent();
			return false;
		}
	}

	private void doSearch(WebDriver webDriver) throws Exception {
		webDriver.manage().window().maximize();
		Thread.sleep(1000);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], switch to search frame.");
		WebElement frameElement = webDriver.findElement(By.name("myYanzm"));
		Point frameLocation = frameElement.getLocation();
		webDriver.switchTo().frame("myYanzm");
		
//		Thread.sleep(500);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], fill in search conditions.");
		webDriver.findElement(By.id("pName")).sendKeys("安徽");
//		Thread.sleep(500);
		new Select(webDriver.findElement(By.id("pProvince"))).selectByVisibleText("安徽");
//		Thread.sleep(500);
		// get another captchaImg
		webDriver.findElement(By.xpath("html/body/form/table/tbody/tr[4]/td[1]/a")).click();
		Thread.sleep(3000);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], capture captcha image.");
		WebElement element = webDriver.findElement(By.id("captchaImg"));
		String veriCode = recognizeCaptchaImg(element,webDriver,frameLocation);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], captcha image code: "+veriCode);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], fill in captcha image code.");
		webDriver.findElement(By.id("pCode")).sendKeys(veriCode);
//		Thread.sleep(500);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], click search button.");
		webDriver.findElement(By.id("button")).click();
		Thread.sleep(2000);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], switch back to default frame.");
		webDriver.switchTo().defaultContent();
//		Thread.sleep(500);
		XxlJobLogger.log(" #Supreme Court# page processor [search step], switch to result frame.");
		webDriver.switchTo().frame("contentFrame");
//		Thread.sleep(500);
	}

	@Override
	public Site getSite() {
		return this.site;
	}

	private String recognizeCaptchaImg(WebElement element, WebDriver webDriver, Point frameLocation) throws Exception {
		String path = AcqJobHandler.veriCodePath;
		String imgFileName = System.currentTimeMillis() + ".png";
		String storePath = path + imgFileName;
		BufferedImage bi = createElementImage(element, webDriver,frameLocation);
		ImageIO.write(bi, "png", new File(storePath));  
		String re=RuoKuai.createByPost("mohuanshijian", "545990aa", "3040", "60", "1",
		"b40ffbee5c1cf4e38028c197eb2fc751", storePath);
		int beginIndex = re.indexOf("<Result>")+"<Result>".length();
		int endIndex = re.indexOf("</Result>");
		String code = re.substring(beginIndex, endIndex);
		System.out.println(code);
		return code;
	}
	
	
	private BufferedImage createElementImage(WebElement webElement,WebDriver webDriver, Point frameLocation)
			 throws IOException {
			 // 获得webElement的位置和大小。
			 Point location = webElement.getLocation();
			 Dimension size = webElement.getSize();
			 // 创建全屏截图。
			 BufferedImage originalImage =
			  ImageIO.read(new ByteArrayInputStream(takeScreenshot(webDriver)));
			 // 截取webElement所在位置的子图。
			 BufferedImage croppedImage = originalImage.getSubimage(
			  location.getX()+frameLocation.getX(),
			  location.getY()+frameLocation.getY(),
			  size.getWidth(),
			  size.getHeight());
			 return croppedImage;
			}
	
	
	private byte[] takeScreenshot(WebDriver webDriver) throws IOException {
		 TakesScreenshot takesScreenshot = (TakesScreenshot) webDriver;
		 return takesScreenshot.getScreenshotAs(OutputType.BYTES);
		}

}
