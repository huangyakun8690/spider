package com.xxl.job.executor.processor.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.CollectionStrategy;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.InformationCache;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumDownloader;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.AdvancedSeleniumWebDriverPool;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.ChromeWebDriverPool;
import com.ustcinfo.ptp.yunting.webmagic.downloader.selenium.PhantomJSWebDriverPool;
import com.xxl.job.executor.nextpage.NextPageFactory;
import com.xxl.job.executor.nextpage.NextPageGenerator;
import com.xxl.job.executor.util.HttpClient4;
import com.xxl.job.executor.util.JedisUtils;
import com.xxl.job.executor.util.MQTaskUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.Producer;
import com.xxl.job.executor.util.ProxyHelper;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.WebDriverHelper;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.UrlUtils;

public class CusdPageProcessorNavi implements PageProcessor {
	// private Logger logger = LoggerFactory.getLogger(getClass());

	private static int SLEEPTIME = 1000 / 2;

	private Site site = Site.me().setRetryTimes(2).setSleepTime(SLEEPTIME).setTimeOut(30 * 1000);

	private JSONArray lists;

	private int pageNum;


	private Downloader asDownloader;

	private AtomicInteger currentPageNo = new AtomicInteger(1);

	private CollectionSite conSite = null;


	// 171013 page flip problem
	private List<String> listPageUrls = new ArrayList<String>();
	private JSONArray pageFlipArray;

	private long navId = 0l;
	WebDriver webDriver = null;

	Integer ac;
	String COUNT_PER_TASK;
	private Integer type = 1;

	private String listRandomKey;

	private CollectionStrategy collectionStrategy;
	private AdvancedSeleniumWebDriverPool webDriverPool;

	// private static String WEBDRIVER_CHROME="chrome";

	private static String WEBDRIVER_PHANTOMJS = "phantomjs";

	private AdvancedSeleniumDownloader asDownloaderli;

	private String id = "";
	
	
	/**
	 * 列表和详情页爬虫处理类
	 * 
	 * @param asDownloader
	 *            页面下载器，默认在windows上采用chrome，在linux采用phantomJS
	 * @param jsonObjPageRule
	 *            配置的页面规则
	 * @param collectionStrategy
	 *            采集策略，在任务配置的页面策略中配置
	 * @param infoService
	 *            结果持久化方法
	 * @param acqPageRule
	 *            采集规则
	 * @param executorService
	 *            执行方法
	 * @param ac
	 *            定义使用什么方法来处理页面的翻页
	 * @param COUNT_PER_TASK
	 *            对于大型任务，定义每个任务最多执行多少页,例如：1-1000， 1000-2000
	 */
	public CusdPageProcessorNavi(Downloader asDownloader, JSONObject jsonObjPageRule,
			CollectionStrategy collectionStrategy, Site site, Integer ac, 
			String COUNT_PER_TASK) {
		this.asDownloader = asDownloader;
		if (site != null) {
			this.site = site;
		} else {
			this.site = Site.me().setRetryTimes(collectionStrategy.getSiteRetryTimes())
					.setSleepTime(collectionStrategy.getSiteSleepTime())
					.setTimeOut(collectionStrategy.getSiteTimeout());
		}

		this.collectionStrategy = collectionStrategy;
		jsonObjPageRule.getString(JsonKeys.ZCD_TASK_LINK);
		this.lists = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_LINK_JSON);
		this.pageNum = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON).getJSONObject(0)
				.getBoolean(JsonKeys.ZCD_PAGE_NEEDNEXTPAGE)
						? jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON).getJSONObject(0)
								.getInt(JsonKeys.ZCD_PAGE_PAGENUM)
						: 1;
		if (this.pageNum > 1) {
			this.pageFlipArray = jsonObjPageRule.getJSONArray(JsonKeys.ZCD_PAGE_JSON);
			this.ac = ac;
			this.COUNT_PER_TASK = COUNT_PER_TASK;
		}
		asDownloaderli = new AdvancedSeleniumDownloader(1, (collectionStrategy.getUseProxy() == 0) ? true : false);
		// 设置代理
		
		try {
			setSite();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}

	}

	
	public void setSite() {
		//从数据库中获取代理
		//System.out.println("是否代理："+collectionStrategy.getUseProxy());
		  this.site.setUserAgent("User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
	  	   
		if ((collectionStrategy.getUseProxy() == 0) && collectionStrategy.getFlag() != 0) {
			 //获取ip
	        String poxy=ProxyHelper.getProxy();
	        //验证ip
	        String poxys[] = poxy.split(":");
	        boolean result = HttpClient4.ipTest(poxys[0], Integer.parseInt(poxys[1]) , "", ""); 
	        if(result) {
	        	HttpHost httpHost = new HttpHost(poxys[0], Integer.parseInt(poxys[1]));
	        
	  	        this.site.setHttpProxy(httpHost);
	  	        this.site.setUserAgent("User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
	  	        this.site.setUsernamePasswordCredentials(new UsernamePasswordCredentials(ProxyHelper.getUserName(), ProxyHelper.getPassWord()));
	  	        //this.site.addHeader("X-Forwarded-For", getRandomIp());
	  	        
	        }else {
	        	setSite();
	        }
	       
		}
		
		
	}
	
	
	/* 
     * 随机生成国内IP地址 
     */  
    public String getRandomIp() {  
        // ip范围  
        int[][] range = { { 607649792, 608174079 },// 36.56.0.0-36.63.255.255  
                { 1038614528, 1039007743 },// 61.232.0.0-61.237.255.255  
                { 1783627776, 1784676351 },// 106.80.0.0-106.95.255.255  
                { 2035023872, 2035154943 },// 121.76.0.0-121.77.255.255  
                { 2078801920, 2079064063 },// 123.232.0.0-123.235.255.255  
                { -1950089216, -1948778497 },// 139.196.0.0-139.215.255.255  
                { -1425539072, -1425014785 },// 171.8.0.0-171.15.255.255  
                { -1236271104, -1235419137 },// 182.80.0.0-182.92.255.255  
                { -770113536, -768606209 },// 210.25.0.0-210.47.255.255  
                { -569376768, -564133889 }, // 222.16.0.0-222.95.255.255  
        };  
  
        Random rdint = new Random();  
        int index = rdint.nextInt(10);  
        String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));  
        return ip;  
    }
    /* 
     * 将十进制转换成ip地址 
     */  
    public String num2ip(int ip) {  
        int[] b = new int[4];  
        String x = "";  
  
        b[0] = (int) ((ip >> 24) & 0xff);  
        b[1] = (int) ((ip >> 16) & 0xff);  
        b[2] = (int) ((ip >> 8) & 0xff);  
        b[3] = (int) (ip & 0xff);  
        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);  
        return x;  
    }  
	
	
	

	@Override
	public void process(Page page) {
		System.out.println(page.getRequest());
		long begin = System.currentTimeMillis();
		printLog("开始根据规则爬取目标url", "info", page.getUrl().toString());
		try {
			List<String> targetUrls = new ArrayList<String>();
			try {
				if (collectionStrategy.getFlag() == 0) { // 动态渲染模式
					//System.out.println("动态渲染");
					if (webDriver == null) {
						webDriver = ((AdvancedSeleniumDownloader) asDownloader).getWebDriver();
					}
					String parentUrl = webDriver.getCurrentUrl();
					// extract all dtl page urls
					int nullNum = 0;
					for (int i = 0; i < lists.length(); i++) {
						// maximize the window
						try {

							webDriver.manage().window().maximize();
							// get the dtl element
							WebElement element = null;
							String iXpath = lists.getJSONObject(i).getString(JsonKeys.ZCD_XPATH);
							try {
								element = WebDriverHelper.findElement(webDriver, null, iXpath, null, null);
							} catch (StaleElementReferenceException e) {
								element = WebDriverHelper.findElement(webDriver, null, iXpath, null, null);
							}
							if (element == null) {
								nullNum++;
								printLog("元素Cannot find element by xpath: " + iXpath
										+ ", please check whether this is the last page and this exception can be ignored or not.",
										"info", page.getUrl().toString());
								if (nullNum > 5) {// 5次都没有取到数据 说明列表没有那么多 不在爬取
									nullNum = 0;
									break;
								}
								continue;
							}
							// if dtl element has href attribute
							if (WebDriverHelper.hrefIsValid(element)) {
								// add the href to target urls
								String href = element.getAttribute("href");
								if (href == null) {
									printLog("ListPageProcessorForScan# href 为空，当前url = {" + parentUrl + "}， " + "xpath = {"
											+ iXpath + "}", "info", page.getUrl().toString());
									continue;
								}
								href = StringHas.getUrl(parentUrl, href);
								printLog("爬取到的目标url为： " + href, "info", page.getUrl().toString());
								if (!JedisUtils.isExisted(href)) {
									targetUrls.add(href);
									JedisUtils.set(href, href);
								} else {
									printLog("爬取到目标url： " + href + "已经存在", "info", page.getUrl().toString());
								}
								// XxlJobLogger.log(" #LAD# page processor [get detail
								// page url], current pageNo: " + currentPageNo
								// + ", gets target url: " + href);
							}
							// // click the element to get dtl url
							else {
								String listPageWindowHandle = webDriver.getWindowHandle();
								element.click();
								// switch to newly opened window
								boolean switched = WebDriverHelper.switchToNewWindow(webDriver);
								// add dtl page url to target urls
								String href = webDriver.getCurrentUrl();
								if (href == null) {
									printLog("ListPageProcessorForScan# href 为空，当前url = {" + parentUrl + "}， " + "xpath = {"
											+ iXpath + "}", "info", page.getUrl().toString());
								} else {
									printLog("爬取到的目标url为： " + href, "info", page.getUrl().toString());
									href = StringHas.getUrl(parentUrl, href);
									if (!JedisUtils.isExisted(href)) {
										targetUrls.add(href);
										JedisUtils.set(href, href);
									} else {
										// 发送信息到日志平台
										// ComsLoggerUtils.sendMessage("","爬取到目标url： " + href + "已经存在");
										printLog("爬取到目标url： " + href + "已经存在", "info", page.getUrl().toString());

									}
								}

								if (switched) {
									// close the current window
									webDriver.close();
									// swtich back to list page
									webDriver.switchTo().window(listPageWindowHandle);
								} else {
									webDriver.navigate().back();
								}
							}
						} catch (Exception e) {
							
						}
					}
				} else {// 非动态渲染模式
					for (int i = 0; i < lists.length(); i++) {
						try {
							String iXpath = lists.getJSONObject(i).getString(JsonKeys.ZCD_XPATH);
							String href = page.getHtml().xpath(iXpath + "/@href").get();

							int nullNum = 0;
							if (href == null) {
								nullNum++;
								printLog("ListPageProcessorForScan# href 为空，当前url = {" + page.getUrl() + "}， " + "xpath = {"
										+ iXpath + "}", "info", page.getUrl().toString());

								if (nullNum > 5) {// 异常5次
									nullNum = 0;
									break;
								}
							} else {
								String parentUrl = page.getUrl().toString();
								printLog("爬取到的目标url为： " + href, "info", page.getUrl().toString());
								href = StringHas.getUrl(parentUrl, href);
								if (!JedisUtils.isExisted(href)) {
									targetUrls.add(href);
									JedisUtils.set(href, href);
								} else {
									printLog("爬取到目标url： " + href + "已经存在", "info", page.getUrl().toString());

								}
							}
						} catch (Exception e) {
							
							
						}
						

					}

				}

				// 循环遍历一个页面后，获取到的所有详情地址就持久化到库中，这样就不必要等待其他页面，这个在列表-详情中比较实用，在列表-列表-详情中不是全部实用
				// logger.info("targetUrls insert into db
				// targetUrls大小："+targetUrls.size()+page.getUrl());
				printLog("爬取到的url放队列中,目标url集合大小： " + targetUrls.size(), "info", page.getUrl().toString());

				saveDetailPages(targetUrls, id,page.getUrl().toString());
				targetUrls.clear();
				// allTargets.add(targetUrls);
				// if is last list page
				Thread.sleep(1000);

				if (currentPageNo.get() < pageNum) {
					//InformationCache.getCache().remove(listRandomKey);
					if (webDriver == null) {
						webDriver = getWebDriver();
					}
					// 获取ip
					setSite();
					listPageUrls.add(page.getUrl().toString());

					NextPageGenerator npg = NextPageFactory.getNextPageGenerator(ac);

					boolean get_next_page_success = true;
					do {
						try {
							get_next_page_success = true;
							npg.getNextPageElement(webDriver, currentPageNo.get(), pageFlipArray, listPageUrls);
						} catch (Exception e) {
							
							get_next_page_success = false;
							currentPageNo.incrementAndGet();
							// 发送信息到日志平台
							printLog(
									" #CusdPageProcessorNavi# page processor [获取下一页url异常], next pageNo: "
											+ (currentPageNo.get()) + ", detail messages are as follows:"
											+ AllErrorMessage.getExceptionStackTrace(e),
									"error", page.getUrl().toString());

						}
					} while (!get_next_page_success && currentPageNo.get() < pageNum);

					String currentPageUrl = webDriver.getCurrentUrl();
					Page newPage;
					if (collectionStrategy.getFlag() != 0) {//
						currentPageNo.incrementAndGet();
					
						page.addTargetRequest(currentPageUrl);
						
						printLog("开始爬取下一页，当前页数为: " + (currentPageNo.get()) + ", 下一页url为: " + webDriver.getCurrentUrl(),
								"info", page.getUrl().toString());

					} else {
						if (webDriver.getCurrentUrl().equalsIgnoreCase(currentPageUrl)) {

							newPage = asDownloaderli.download(webDriver, 0);
						} else {
							newPage = asDownloaderli.download(webDriver, 10);
						}
						currentPageNo.incrementAndGet();
						printLog("开始爬取下一页，当前页数为: " + (currentPageNo.get()) + ", 下一页url为: " + webDriver.getCurrentUrl(),
								"info", page.getUrl().toString());

						process(newPage);
						
					}

					

				}
				
				else {
					if (collectionStrategy.getFlag() == 0) {
						((AdvancedSeleniumDownloader) asDownloader).returnToPool(webDriver);
					}
				}
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				printLog(
						"# CusdPageProcessorNavi processor [get detail page url] encounters exceptions, detail messages are as follows: "
								+ AllErrorMessage.getExceptionStackTrace(exception),
						"error", page.getUrl().toString());
				throw new InterruptedException();

			} catch (Exception e) {
				if (webDriver != null) {
					if (collectionStrategy.getFlag() == 0) {
						((AdvancedSeleniumDownloader) asDownloader).returnToPool(webDriver);
					}

				}
				// asDownloader.destoryWebDriverPool();
				printLog(
						"#CusdPageProcessorNavi# page processor [get detail page url] encounters exceptions, detail messages are as follows: "
								+ AllErrorMessage.getExceptionStackTrace(e),
						"error", page.getUrl().toString());

				
				if (targetUrls.size() > 0) {
					saveDetailPages(targetUrls, id,page.getUrl().toString());
					targetUrls.clear();
				}
			}
		} catch (Exception e) {
			printLog("#CusdPageProcessorNavi# page processor enncounters exceptions, detail messages are as follows: "
					+ AllErrorMessage.getExceptionStackTrace(e), "error", page.getUrl().toString());
			if (collectionStrategy.getFlag() == 0) {
				((AdvancedSeleniumDownloader) asDownloader).destoryWebDriverPool();
			}

		} finally {
			long end = System.currentTimeMillis();
			printLog("爬取目标地址结束  , url: " + page.getUrl() + ", endtime: " + StringHas.getDateNowStr() + ", time:"
					+ (end - begin), "info", page.getUrl().toString());
			destoryDownloader(asDownloaderli);
		}

	}
	
	

	private void destoryDownloader(Downloader dtlDownloader) {
		if ((dtlDownloader instanceof AdvancedSeleniumDownloader) && (null != dtlDownloader)) {
			((AdvancedSeleniumDownloader) dtlDownloader).destoryWebDriverPool();
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
		boolean useProxy = (collectionStrategy.getUseProxy() == 0) ? true : false;
		if (webDriverPool == null) {
			synchronized (this) {
				if (AdvancedSeleniumDownloader.getWEBDRIVER_TYPE().equalsIgnoreCase(WEBDRIVER_PHANTOMJS)) {
					webDriverPool = new PhantomJSWebDriverPool(collectionStrategy.getFlag(), true,
							AdvancedSeleniumDownloader.getSELENIUM_PATH(), useProxy);
				} else {
					webDriverPool = new ChromeWebDriverPool(collectionStrategy.getFlag(),
							AdvancedSeleniumDownloader.getSELENIUM_PATH(), useProxy);
				}
			}
		}
	}

	@Override
	public Site getSite() {
		return this.site;
	}

	public CollectionSite getConSite() {
		return conSite;
	}

	public void setConSite(CollectionSite conSite) {
		this.conSite = conSite;
	}



	public long getNavId() {
		return navId;
	}

	public void setNavId(long navId) {
		this.navId = navId;
	}

	public Integer getTypes() {
		return type;
	}

	public void setTypes(Integer types) {
		this.type = types;
	}

	public String getListRandomKey() {
		return listRandomKey;
	}

	public void setListRandomKey(String listRandomKey) {
		this.listRandomKey = listRandomKey;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void saveDetailPages(List<String> targetUrls, String id,String url) throws Exception {
		// IAcqDeatilsDao acqDeatilsDao = acqDeatilsDaoImpl;
		InformationCache.getCache().put(listRandomKey, 1);
		if (targetUrls != null && targetUrls.size() > 0) {
			InformationCache.getCache().put(listRandomKey, targetUrls.size());
		}
		String siteId = JedisUtils.hget("nav_"+UrlUtils.getDomain(url),url);
		CollectionSite collectionSite=null;
		if(siteId!=null) {
			collectionSite= ObjectPase.getCollectionSite(siteId, null);
		}else {
			collectionSite=conSite;
		}
		List<Map<String, Object>> messageList = new ArrayList<Map<String, Object>>();
		for (String str : targetUrls) {
			Map<String, Object> messageMap = new HashMap<String, Object>();
			JSONObject jsonObject = new JSONObject();
			
			jsonObject.put("id", id);
			jsonObject.put("siteId", collectionSite.getId());
			jsonObject.put("url", str);
			// jsonObject.put("serial_num", serial_num);
			jsonObject.put("state", "2");
			jsonObject.put("type", "2");
			messageMap.put("jsonObject", jsonObject);
			messageMap.put("topicName", MQTaskUtils.getDetailTopicName(this.type, collectionSite.getInfoSourceType()));
			messageMap.put("tag", "acq_deatil_tag");
			messageMap.put("keys", "acq_deatil_" + UUID.randomUUID().toString());
			messageList.add(messageMap);

		}

		Producer.sendMessageList(messageList, JsonKeys.PRODUCER_MQ_NAME);
	}

	/**
	 * 日志
	 * 
	 * @param msg
	 * @param logType
	 */
	public void printLog(String msg, String logType, String url) {
		ObjectPase.setGetLoggBean(conSite.getId() + "", url, conSite.getInfoSourceType(), "1", "1", "", "1", "", "",
				msg, this.getClass().getName(), logType);
	}

}
