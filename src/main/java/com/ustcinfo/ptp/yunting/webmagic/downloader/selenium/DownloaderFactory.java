<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import com.ustcinfo.ptp.yunting.support.Constants;

import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-10-11
 * @version 1.0
 * @param
 */
public class DownloaderFactory {
	
	public static Downloader createDownloader(int type,boolean useProxy){
		
		if(Constants.DYNAMIC_RENDERING_N==type){
			return new HttpClientDownloader();
		}
		else{
			return new AdvancedSeleniumDownloader(useProxy);
		}
		
	}
}
=======
package com.ustcinfo.ptp.yunting.webmagic.downloader.selenium;

import com.ustcinfo.ptp.yunting.support.Constants;

import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2017-10-11
 * @version 1.0
 * @param
 */
public class DownloaderFactory {
	
	public static Downloader createDownloader(int type,boolean useProxy){
		
		if(Constants.DYNAMIC_RENDERING_N==type){
			return new HttpClientDownloader();
		}
		else{
			return new AdvancedSeleniumDownloader(useProxy);
		}
		
	}
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
