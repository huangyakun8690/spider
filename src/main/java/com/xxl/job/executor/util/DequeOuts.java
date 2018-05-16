<<<<<<< HEAD
package com.xxl.job.executor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 存儲緩衝
 * 
 * @author huangyakun
 *
 */
public class DequeOuts {

	private static BlockingDeque<Map<String, Object>> OutPropers = new LinkedBlockingDeque<Map<String, Object>>();

	private static BlockingDeque<String> OutProperShangji = new LinkedBlockingDeque<String>();

	private static BlockingDeque<String> DbWritepropers = new LinkedBlockingDeque<String>();

	private static List<Map<String, Object>> staticlist = new ArrayList<Map<String, Object>>();

	private static AtomicInteger naviTaskSucckAi = new AtomicInteger(0);
	private static AtomicInteger naviTaskFailkAi = new AtomicInteger(0);

	private static AtomicInteger detailTaskSucckAi = new AtomicInteger(0);
	private static AtomicInteger detailTaskFailkAi = new AtomicInteger(0);

	public static BlockingDeque<Map<String, Object>> getOutPropers() {
		return OutPropers;
	}

	public static void setOutPropers(BlockingDeque<Map<String, Object>> outPropers) {
		OutPropers = outPropers;
	}

	public static BlockingDeque<String> getOutProperShangji() {
		return OutProperShangji;
	}

	public static void setOutProperShangji(BlockingDeque<String> outProperShangji) {
		OutProperShangji = outProperShangji;
	}

	public static BlockingDeque<String> getDbWritepropers() {
		return DbWritepropers;
	}

	public static void setDbWritepropers(BlockingDeque<String> dbWritepropers) {
		DbWritepropers = dbWritepropers;
	}

	public static List<Map<String, Object>> getStaticlist() {
		return staticlist;
	}

	public static void setStaticlist(List<Map<String, Object>> list) {
		DequeOuts.staticlist = list;
	}

	public static AtomicInteger getNaviTaskSucckAi() {
		return naviTaskSucckAi;
	}

	public static void setNaviTaskSucckAi(AtomicInteger naviTaskSucckAi) {
		DequeOuts.naviTaskSucckAi = naviTaskSucckAi;
	}

	public static AtomicInteger getNaviTaskFailkAi() {
		return naviTaskFailkAi;
	}

	public static void setNaviTaskFailkAi(AtomicInteger naviTaskFailkAi) {
		DequeOuts.naviTaskFailkAi = naviTaskFailkAi;
	}

	public static AtomicInteger getDetailTaskSucckAi() {
		return detailTaskSucckAi;
	}

	public static void setDetailTaskSucckAi(AtomicInteger detailTaskSucckAi) {
		DequeOuts.detailTaskSucckAi = detailTaskSucckAi;
	}

	public static AtomicInteger getDetailTaskFailkAi() {
		return detailTaskFailkAi;
	}

	public static void setDetailTaskFailkAi(AtomicInteger detailTaskFailkAi) {
		DequeOuts.detailTaskFailkAi = detailTaskFailkAi;
	}

}
=======
package com.xxl.job.executor.util;


import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 存儲緩衝
 * @author huangyakun
 *
 */
public class DequeOuts {
	
	private static BlockingDeque<Map<String,Object>> OutPropers = new LinkedBlockingDeque<Map<String,Object>>();
	
	private static BlockingDeque<String> OutProperShangji = new LinkedBlockingDeque<String>();

	public static BlockingDeque<Map<String,Object>> getOutPropers() {
		return OutPropers;
	}

	public static void setOutPropers(BlockingDeque<Map<String,Object>> outPropers) {
		OutPropers = outPropers;
	}

	public static BlockingDeque<String> getOutProperShangji() {
		return OutProperShangji;
	}

	public static void setOutProperShangji(BlockingDeque<String> outProperShangji) {
		OutProperShangji = outProperShangji;
	}
	
	
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
