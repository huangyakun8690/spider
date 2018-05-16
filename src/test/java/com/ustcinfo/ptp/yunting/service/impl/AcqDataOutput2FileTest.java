/**
 * 
 */
package com.ustcinfo.ptp.yunting.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.junit.Test;

import com.ustcinfo.ptp.yunting.service.IAcqDataOutput;
import com.xxl.job.executor.util.DequeOuts;

/**
 * @author huangyakun
 *
 */
public class AcqDataOutput2FileTest {
private IAcqDataOutput iAcqDataOutput = new AcqDataOutput2File();
	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.AcqDataOutput2File#write(java.lang.String)}.
	 * @throws InterruptedException 
	 */
	@Test(timeout = 10000*5)
	public void testWrite() throws InterruptedException {
		BlockingDeque<Map<String, Object>> OutPropers=  new LinkedBlockingDeque<>();
		for(int i=0;i<101;i++) {
			Map<String, Object> map= new HashMap<>();
			 map.put("line", "qwewqe\n");
			 map.put("outFormat", "2");
			 if(i%3==0) {
				 map.put("type", "sadsda");
			 }else {
				 map.put("type", "32324435");
			 }
			
			 OutPropers.add(map);
			 DequeOuts.setOutPropers(OutPropers);
		}
		 
		 iAcqDataOutput.write("D:\\123");
		
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.AcqDataOutput2File#renameFile(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testRenameFile() {
		AcqDataOutput2File output2File = new AcqDataOutput2File();
		output2File.renameFile("D:\\123\\2\\20180515161842_29993_sadsa_192.168.135.45_1_1.log", "D:\\123\\2\\20180515161842_29993_sadsa_192.168.135.45_1_1.out");
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.AcqDataOutput2File#getFileName()}.
	 */
	@Test
	public void testGetFileName() {
		AcqDataOutput2File.getFileName();
	}

	/**
	 * Test method for {@link com.ustcinfo.ptp.yunting.service.impl.AcqDataOutput2File#setFileName(java.lang.String)}.
	 */
	@Test
	public void testSetFileName() {
		
		AcqDataOutput2File.setFileName("D:\\123");
	}
	
	@Test
	public void setFor() throws InterruptedException {
		AcqDataOutput2File.setFor(true);
	}


}
