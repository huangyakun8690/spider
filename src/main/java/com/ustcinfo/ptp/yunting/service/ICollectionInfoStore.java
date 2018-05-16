<<<<<<< HEAD
package com.ustcinfo.ptp.yunting.service;

import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;

public interface ICollectionInfoStore {
	
	public void store(InfoEntity infoEntity,CollectionSite collectionSite,JSONObject pageJsonObj) throws Exception  ;
	
	public void store(JSONObject pageJsonObj,CollectionSite collectionSite);
	public void store(JSONObject pageJsonObj,String url) throws Exception;

	
}
=======
package com.ustcinfo.ptp.yunting.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.ustcinfo.ptp.yunting.model.CollectionSite;
import com.ustcinfo.ptp.yunting.model.InfoEntity;

import us.codecraft.webmagic.Page;

public interface ICollectionInfoStore {
	
	public void store(InfoEntity infoEntity,CollectionSite collectionSite,JSONObject pageJsonObj) throws JSONException  ;

	
}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
