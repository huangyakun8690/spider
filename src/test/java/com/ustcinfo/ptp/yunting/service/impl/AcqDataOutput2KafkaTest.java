/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.junit.Test;

import com.xxl.job.executor.util.DequeOuts;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */

public class AcqDataOutput2KafkaTest extends Basejunit{

	private AcqDataOutput2Kafka acqDataOutput2Kafka = new AcqDataOutput2Kafka();
	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.AcqDataOutput2Kafka#write(java.lang.String)}.
	 */
	@Test(timeout = 10000*1)
	public void testWrite() {
		BlockingDeque<Map<String, Object>> OutPropers=  new LinkedBlockingDeque<>();
		for(int i=0;i<101;i++) {
			Map<String, Object> map= new HashMap<>();
			
			 map.put("outFormat", "2");
			 if(i%3==0) {
				 map.put("line", "|id|qwewqe\n");
				 map.put("type", "sadsda");
			 }else {
				 map.put("type", "32324435");
			 }
			
			 OutPropers.add(map);
			 DequeOuts.setOutPropers(OutPropers);
		}
		acqDataOutput2Kafka.write("D:\\123");
	}

}
