/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import org.junit.Test;

import com.ustcinfo.ptp.yunting.model.Dbwriteinfo;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class DbwriteInofImplTest extends Basejunit{

	private DbwriteInofImpl DbwriteInofImpl = new DbwriteInofImpl();
	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.DbwriteInofImpl#store(com.ustcinfo.ptp.yunting.model.Dbwriteinfo)}.
	 */
	@Test
	public void testStore() {
		Dbwriteinfo dbwriteinfo =  new Dbwriteinfo("123", 345+"", "www.baidu.com", "1", "2", "", "0", "wqwqe");
		dbwriteinfo.setInfoSourceType(2);
		DbwriteInofImpl.store(dbwriteinfo);
	}

}
