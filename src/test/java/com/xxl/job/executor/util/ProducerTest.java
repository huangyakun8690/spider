/**
 * 
 */
package com.xxl.job.executor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class ProducerTest extends Basejunit{

	/**
	 * Test method for {@link com.xxl.job.executor.util.Producer#sendMessage(org.json.JSONObject, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSendMessage() {
		JSONObject jsonObject = new JSONObject();
		try {
			Producer.sendMessage(jsonObject, "test", "tag_1", "we", "asdasd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link com.xxl.job.executor.util.Producer#sendMessageList(java.util.List, java.lang.String)}.
	 */
	@Test
	public void testSendMessageList() {
		
		try {
			List<Map<String,Object>> messageList = new ArrayList<>();
			Map<String,Object> map= new HashMap<>();
			map.put("topicName", "sad");
			map.put("tag", "sadw");
			map.put("keys", "sad1");
			map.put("jsonObject", "sad2d");
			messageList.add(map);
			Producer.sendMessageList(messageList, "asds");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
