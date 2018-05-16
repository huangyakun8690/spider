<<<<<<< HEAD
package com.xxl.job.executor.nextpage;



public class NextPageFactory {

	/**
	 * 下一页的生成工厂类
	 * @param type
	 * @return
	 */
	public static NextPageGenerator getNextPageGenerator(int type)
	{
		if(type==1){
			return new CharactorMatchNextPageGenerator();
		}
		if(type==2){
			return new NumMatchNextPageGenerator();
		}
		else{
			return new RulePageGenerator();
		}
	}

}
=======
package com.xxl.job.executor.nextpage;

import com.xxl.job.core.log.XxlJobLogger;
import org.openqa.selenium.WebDriver;

public class NextPageFactory {

	/**
	 * 下一页的生成工厂类
	 * @param type
	 * @return
	 */
	public static NextPageGenerator getNextPageGenerator(int type)
	{
		if(type==1){
			return new CharactorMatchNextPageGenerator();
		}
		if(type==2){
			return new NumMatchNextPageGenerator();
		}
		else{
			return new RulePageGenerator();
		}
	}

}
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
