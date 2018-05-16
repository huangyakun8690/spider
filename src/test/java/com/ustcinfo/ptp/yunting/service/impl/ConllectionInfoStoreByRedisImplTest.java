/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import org.json.JSONObject;
import org.junit.Test;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.service.ICollectionInfoStore;

import base.Basejunit;

/**
 * @author huangyakun
 *
 */
public class ConllectionInfoStoreByRedisImplTest extends Basejunit {
private ICollectionInfoStore ICollectionInfoStore = new ConllectionInfoStoreByRedisImpl();
	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.ConllectionInfoStoreByRedisImpl#store(com.ustcinfo.ptp.yunting.model.InfoEntity, com.ustcinfo.ptp.yunting.model.CollectionSite, org.json.JSONObject)}.
	 * @throws Exception 
	 */
	@Test
	public void testStoreInfoEntityCollectionSiteJSONObject() throws Exception {
		ICollectionInfoStore.store(null, null,null);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.ConllectionInfoStoreByRedisImpl#store(org.json.JSONObject, com.ustcinfo.ptp.yunting.model.CollectionSite)}.
	 */
	@Test
	public void testStoreJSONObjectCollectionSite() {
		CollectionSite collectionSite= new CollectionSite();
		collectionSite.setId(271l);
		collectionSite.setSiteUrl("wwww.baidu.com");
		collectionSite.setInfoSourceType(1);
		collectionSite.setCategoryId(271l);
		JSONObject pageJsonObj = new JSONObject();
		pageJsonObj.append("ext1", "asdsad");
		pageJsonObj.append("url", "wwww.baidu.com");
		ICollectionInfoStore.store(pageJsonObj, collectionSite);
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.ConllectionInfoStoreByRedisImpl#store(org.json.JSONObject, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testStoreJSONObjectString() throws Exception {
		ICollectionInfoStore.store(null, "");
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.ConllectionInfoStoreByRedisImpl#getType(int)}.
	 */
	@Test
	public void testGetType() {
		new ConllectionInfoStoreByRedisImpl().getType(1);
		new ConllectionInfoStoreByRedisImpl().getType(2);
		new ConllectionInfoStoreByRedisImpl().getType(3);
		new ConllectionInfoStoreByRedisImpl().getType(4);
		new ConllectionInfoStoreByRedisImpl().getType(5);
		new ConllectionInfoStoreByRedisImpl().getType(6);
		new ConllectionInfoStoreByRedisImpl().getType(7);
		new ConllectionInfoStoreByRedisImpl().getType(8);
		new ConllectionInfoStoreByRedisImpl().getType(9);
		new ConllectionInfoStoreByRedisImpl().getType(10);
		new ConllectionInfoStoreByRedisImpl().getType(11);
		
	}

}
