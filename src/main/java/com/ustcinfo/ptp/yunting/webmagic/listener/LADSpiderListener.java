<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.webmagic.listener;

import com.xxl.job.core.log.XxlJobLogger;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;

public class LADSpiderListener implements SpiderListener {

	@Override
	public void onError(Request arg0) {
		XxlJobLogger
		.log("spider error: "
				+ arg0.getUrl()
				+ ", please check ..... ");

	}

	@Override
	public void onSuccess(Request arg0) {
		XxlJobLogger
		.log("spider process success: "
				+ arg0.getUrl()
				+ " ");
	}

}
=======
package com.ustcinfo.ptp.yunting.webmagic.listener;

import com.xxl.job.core.log.XxlJobLogger;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;

public class LADSpiderListener implements SpiderListener {

	@Override
	public void onError(Request arg0) {
		XxlJobLogger
		.log("spider error: "
				+ arg0.getUrl()
				+ ", please check ..... ");

	}

	@Override
	public void onSuccess(Request arg0) {
		XxlJobLogger
		.log("spider process success: "
				+ arg0.getUrl()
				+ " ");
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
