/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.junit.Test;

/**
 * @author huangyakun
 *
 */
public class RocketMqConsumerImplTest {
    
	private RocketMqConsumerImpl rocketMqConsumerImpl =new RocketMqConsumerImpl();
	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.RocketMqConsumerImpl#setInitialState(int)}.
	 */
	@Test
	public void testSetInitialState() {
		RocketMqConsumerImpl.setInitialState(0);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.RocketMqConsumerImpl#consumer(java.lang.String)}.
	 */
	@Test
	public void testConsumer() {
		rocketMqConsumerImpl.consumer("ceshi");
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.RocketMqConsumerImpl#getconsumer(java.lang.String, org.apache.rocketmq.client.consumer.DefaultMQPushConsumer)}.
	 */
	@Test
	public void testGetconsumer() {
		rocketMqConsumerImpl.getconsumer("test", new DefaultMQPushConsumer());
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.RocketMqConsumerImpl#getMessage(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetMessage() {
		rocketMqConsumerImpl.getMessage("sad", "test");
	}

}
