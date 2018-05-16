package com.xxl.job.executor.util;

public class MQTaskUtils {

	public static String getNaviTopicName(Integer category, Integer sourceType) {

		return new StringBuilder("Q_").append(category).append("_NAVI_").append(sourceType).toString();
	}
	
	public static String getDetailTopicName(Integer category, Integer sourceType) {

		return new StringBuilder("Q_").append(category).append("_DETAIL_").append(sourceType).toString();
	}

}
