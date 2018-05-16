package com.ustcinfo.ptp.yunting.service.impl;

import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;

import com.ustcinfo.ptp.yunting.service.IAcqDataOutput;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.SpringContextUtil;

public class AcqDataOutput2Kafka implements IAcqDataOutput {
	
	private long num = 0;

	@Override
	public void write(String path) {
            boolean loop=true; 		    
 
			while(loop){
				try {
					Map<String, Object> map = DequeOuts.getOutPropers().poll();
					if (map == null || map.size() < 0) {
						if (num < 10) {
							num++;
						}
						Thread.sleep(500l * num);
						continue;
					}
					num=0;
					String line = (map.get("line") != null ? map.get("line").toString() : "");
					if (line.indexOf("|id|") != -1) {
						line = line.replace("|id|", "|");
					}
					KafkaTemplate<String, String> kafkaTemplate = (KafkaTemplate<String, String>) SpringContextUtil.getBean(KafkaTemplate.class);
					kafkaTemplate.send("DATA_TOPIC",line);
				} catch (Exception e) {
					Thread.currentThread().interrupt();
					
				}
				
			}
		
	}

}
