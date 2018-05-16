<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;

public class CommonHelper {
	static Logger logger = LoggerFactory.getLogger(CommonHelper.class);
	public static String findNextPageByUrlComparison(int currentPageNo, List<String> listPageUrls) {
		try {
			int charIndex;
			if (listPageUrls == null || listPageUrls.size() < 2 || listPageUrls.size() != currentPageNo) {
				return null;
			} else {
				charIndex = getDifferentCharIndex(listPageUrls.get(currentPageNo - 2), listPageUrls.get(currentPageNo - 1));
			}
			// 1.the same 2.completely different
			if (charIndex < 1) {
				return null;
			}
			String lastPageUrl = listPageUrls.get(currentPageNo - 1);
			int pageNoLength = String.valueOf(currentPageNo).length();
			if (lastPageUrl.substring(charIndex, charIndex + pageNoLength).equalsIgnoreCase(String.valueOf(currentPageNo))){
				return new StringBuffer(lastPageUrl.subSequence(0, charIndex))
						.append(currentPageNo).append(lastPageUrl
								.subSequence(charIndex + String.valueOf(currentPageNo + 1).length(), lastPageUrl.length()))
						.toString();
			}
			return null;
		}
		catch (Exception e) {
			
			//logger.info(AllErrorMessage.getExceptionStackTrace(e));
			ObjectPase.setGetLoggBean("", "", 0, "2", "1", "", "0", "", "", 
					AllErrorMessage.getExceptionStackTrace(e), CommonHelper.class.getName(), "error");
			return null;
		}
	}

	private static int getDifferentCharIndex(String str1, String str2) {
		char[] c1 = str1.toCharArray();
		char[] c2 = str2.toCharArray();
		int index = c1.length < c2.length ? c1.length : c2.length;
		for (int i = 0; i < index; i++) {
			if (c1[i] != c2[i]) {
				return i;
			}
		}
		return -1;
	}
}
=======
package com.xxl.job.executor.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ustcinfo.ptp.yunting.support.AllErrorMessage;

public class CommonHelper {
	static Logger logger = LoggerFactory.getLogger(CommonHelper.class);
	public static String findNextPageByUrlComparison(int currentPageNo, List<String> listPageUrls) {
		try {
			int charIndex;
			if (listPageUrls == null || listPageUrls.size() < 2 || listPageUrls.size() != currentPageNo) {
				return null;
			} else {
				charIndex = getDifferentCharIndex(listPageUrls.get(currentPageNo - 2), listPageUrls.get(currentPageNo - 1));
			}
			// 1.the same 2.completely different
			if (charIndex < 1) {
				return null;
			}
			String lastPageUrl = listPageUrls.get(currentPageNo - 1);
			int pageNoLength = String.valueOf(currentPageNo).length();
			if (lastPageUrl.substring(charIndex, charIndex + pageNoLength).equalsIgnoreCase(String.valueOf(currentPageNo))){
				return new StringBuffer(lastPageUrl.subSequence(0, charIndex))
						.append(currentPageNo).append(lastPageUrl
								.subSequence(charIndex + String.valueOf(currentPageNo + 1).length(), lastPageUrl.length()))
						.toString();
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.info(AllErrorMessage.getExceptionStackTrace(e));
			return null;
		}
	}

	private static int getDifferentCharIndex(String str1, String str2) {
		char[] c1 = str1.toCharArray();
		char[] c2 = str2.toCharArray();
		int index = c1.length < c2.length ? c1.length : c2.length;
		for (int i = 0; i < index; i++) {
			if (c1[i] != c2[i]) {
				return i;
			}
		}
		return -1;
	}
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
