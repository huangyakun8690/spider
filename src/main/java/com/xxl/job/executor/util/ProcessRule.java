/**
 * 
 */
package com.xxl.job.executor.util;

import org.json.JSONArray;

import com.ustcinfo.ptp.yunting.support.Constants;
import com.ustcinfo.ptp.yunting.support.JsonKeys;

/**
 * @author huangyakun
 *
 */
public class ProcessRule {

	/**
	 * 组合规则
	 * 
	 * @param jsonArray 规则模板
	 * @return 返回规则
	 */
	public static JSONArray buildDetailsJson(JSONArray jsonArray) {
		JSONArray details = new JSONArray();
		for (int k = 0; k < jsonArray.length(); k++) {
			org.json.JSONObject fieldJsonObj = new org.json.JSONObject();
			String fieldName;
			switch (((org.json.JSONObject) jsonArray.get(k)).getString(JsonKeys.ZCD_FIELD_TYPE)) {
			case Constants.SAVE_AS_TITLE:
				fieldName = Constants.STORE_PAGE_TITLE;
				break;
			case Constants.SAVE_AS_TIME:
				fieldName = Constants.STORE_PAGE_CARRY_TIME;
				break;
			case Constants.SAVE_AS_CONTENT:
				fieldName = Constants.STORE_PAGE_CONTENT;
				break;
			case Constants.SAVE:
				fieldName = ((org.json.JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_KEY).toString();
				break;
			default:
				fieldName = "自定义字段名";
			}
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_NAME, fieldName);
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_TYPE,
					((org.json.JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_NAME));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_XPATH,
					((org.json.JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_XPATH));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_KEY,
					((org.json.JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_KEY));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_REG,
					((org.json.JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_REG));
			fieldJsonObj.put(JsonKeys.ZCD_FIELD_VALUE,
					((org.json.JSONObject) jsonArray.get(k)).get(JsonKeys.ZCD_FIELD_VALUE));
			details.put(fieldJsonObj);
		}
		return details;
	}
}
