package com.xxl.job.executor.util;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import com.ustcinfo.ptp.yunting.service.IAcqDataOutput;

public class DequeWriteOut implements Runnable {

	public final AtomicInteger ai = new AtomicInteger(1);

	private String path = "";
	public DequeWriteOut(String path) {
		super();
		this.path = path;
	
	}
	

	public DequeWriteOut() {
		super();
		
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			ServiceLoader<IAcqDataOutput> loader = ServiceLoader.load(IAcqDataOutput.class);
			Iterator<IAcqDataOutput> outServices = loader.iterator();
			while(outServices.hasNext()){
				IAcqDataOutput service =  outServices.next();
				service.write(path);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}
	}
	
	
}
