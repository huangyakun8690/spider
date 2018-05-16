/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import org.junit.Test;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class RocketMqProducerImplTest extends Basejunit{
	private RocketMqProducerImpl RocketMqProducerImpl = new RocketMqProducerImpl();

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.RocketMqProducerImpl#createProducter(java.lang.String)}.
	 */
	@Test
	public void testCreateProducter() {
		RocketMqProducerImpl.createProducter("test");
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.RocketMqProducerImpl#getcreateProducter(java.lang.String)}.
	 */
	@Test
	public void testGetcreateProducter() {
		RocketMqProducerImpl.getcreateProducter("test");
	}

}
