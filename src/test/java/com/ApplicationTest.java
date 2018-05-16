/**
 * 
 */
package com;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.xxl.job.executor.util.SpringContextUtil;

/**
 * @author huangyakun
 *
 */
public class ApplicationTest {

	@Test
	public void test() {
		
	}
	public static void main(String[] args) throws Exception {
		ApplicationContext appContext=SpringApplication.run(ApplicationTest.class, args);
		SpringContextUtil.setApplicationContext(appContext);
	}


}
