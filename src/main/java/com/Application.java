package com;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

<<<<<<< HEAD
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
<<<<<<< HEAD
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ustcinfo.ptp.yunting.service.ISpiderService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.ustcinfo.tpc.framework.core.util.GetSystemValue;
import com.xxl.job.executor.processor.detail.DetailProcessThreadNew2;
import com.xxl.job.executor.processor.list.NaviThreadMq;
import com.xxl.job.executor.scheduler.TaskSchedulerThread;
import com.xxl.job.executor.util.DequeWriteOut;
import com.xxl.job.executor.util.DequeWritedb;
import com.xxl.job.executor.util.Dequepal;
import com.xxl.job.executor.util.LoggerBean;
import com.xxl.job.executor.util.LoggerUtils;
import com.xxl.job.executor.util.ObjectPase;
import com.xxl.job.executor.util.SpringContextUtil;
import com.xxl.job.executor.util.StringHas;

@SpringBootApplication
@EnableScheduling
public class Application {


	@Value("${redisHost}")
	private static String redisHost;
	@Value("${file.path}")
	private static String filePath;


	private static int tcount = 1;
	private static int func = 0;// 0-列表和详情线程都启动，1-只启动详情线程，2-只启动列表线程
	private static String desc = "";
	//private static int Strategy = 0;// 策略编码
	private static String types = "";
	private static int classif = 1;

	private static int baseLocation;

	
	private static int batchSizeLimit;

	private static final int DEFAULT_BATCH_SIZE = 50;
	
	private static final int DEFAULT_BASE_LOCATION = 0;
	
	/**
	 * 输入参数： 文件名、 线程数、 详情/导航 、描述、策略编码、信源大类、爬虫分类
	 * 文件路径、线程数、详情1/导航2/调度3、描述、策略编码、信源大类、信源类型、基地编号、单批次导航数据条数
	 * @param args
	 */
	public static void main(String[] args) {
		
		ApplicationContext appContext = SpringApplication.run(Application.class,args);
		SpringContextUtil.setApplicationContext(appContext);
		ObjectPase.setGetLoggBean("programe start ... params[Frist-file store path,Second-thread count,Third-task type<0-列表和详情线程都启动，1-只启动详情线程，2-只启动列表线程，3-启动调度任务>", Application.class.getName(), "info");
		if (args.length < 7) {
			ObjectPase.setGetLoggBean("异常退出，请输入文件输出路径！", Application.class.getName(), JsonKeys.LOG_LEVEL_INFO);
			System.exit(1);
		}
		for(int i=0;i<args.length-1;i++) {
			args[i]=args[i+1];
		}
		try {		
			tcount = Integer.parseInt(args[1]);
			func = Integer.parseInt(args[2]);
			desc = args[3];
			
			types = args[5];
			classif = Integer.parseInt(args[6]);
		} catch (Exception e) {
			ObjectPase.setGetLoggBean(AllErrorMessage.getExceptionStackTrace(e), Application.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
		}

		try {
			baseLocation = Integer.parseInt(args[7]);
		} catch (Exception e) {
			
			ObjectPase.setGetLoggBean("解析基地编码异常，将采用默认值0-中心基地），详细异常信息如下：" + AllErrorMessage.getExceptionStackTrace(e), Application.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
			baseLocation = DEFAULT_BASE_LOCATION;
			
		}
		
		try {
			batchSizeLimit = Integer.parseInt(args[8]);
		} catch (Exception e) {			
			ObjectPase.setGetLoggBean("解析单批次数据条数异常，将采用默认值50，详细异常信息如下：" + AllErrorMessage.getExceptionStackTrace(e), Application.class.getName(), JsonKeys.LOG_LEVEL_ERROR);
			batchSizeLimit = DEFAULT_BATCH_SIZE;
		
		}

	
		String path = args[0];
		
		Thread t = new Thread(new DequeWriteOut(path));
		t.start();

		Thread tdb = new Thread(new DequeWritedb(path));
		tdb.start();

		ExecutorService servicestite = Executors.newFixedThreadPool(tcount);
		servicestite.execute(new Dequepal(desc,classif+""));
		// 商机进程

		runProcess(tcount, appContext);
		
		
	}
	
	private static void  runProcess(int tcount,ApplicationContext appContext) {
		/**
		 * 为了适应采集型男提升，将架构进行拆分，列表和详情页分开处理
		 */
	
		if (func == 0 || func == 1) {	
			printLog("开始扫描详情表,开始时间为" + StringHas.getDateNowStr(),"info");
			ExecutorService service = Executors.newFixedThreadPool(tcount);
			for (int i = 0; i < tcount; i++) {
				service.execute(new DetailProcessThreadNew2("acq_details", classif, types));

			}
		}
		
		try {
			Thread.sleep(3000);
			
			if (func == 0 || func == 2) {
				printLog("开始扫描导航表,开始时间为" + StringHas.getDateNowStr(),"info");
				ExecutorService servicelist = Executors.newFixedThreadPool(tcount);
				for (int i = 0; i < tcount; i++) {
					servicelist.execute(new NaviThreadMq("acq_navi", classif, types));
				}
			}
			
			if (func == 3 ) {
				printLog("即将启动导航任务调度线程,开始时间为" + StringHas.getDateNowStr(),"info");
				ExecutorService serviceList = Executors.newFixedThreadPool(tcount);
				for (int i = 0; i < tcount; i++) {
					serviceList.execute(new TaskSchedulerThread(appContext.getBean(ISpiderService.class), baseLocation, classif, types, batchSizeLimit));
				}
			}

		} catch (Exception e) {
			printLog("程序出错" +AllErrorMessage.getExceptionStackTrace(e),"error");
		}
	}
	
	public static void printLog(String msg,String logType) {
		LoggerBean loggerBean = new LoggerBean();
		loggerBean.setMsg(msg);
		loggerBean.setLevel(logType);
		String dockId=GetSystemValue.getDockerId();
		loggerBean.setDockerId(dockId);
		loggerBean.setServerIp(GetSystemValue.getIp());
		LoggerUtils.info(loggerBean);
	}
	
	public static int getBaseLocation() {
		return baseLocation;
	}

	public static String getFilePath() {
		return filePath;
	}
=======

import com.ustcinfo.ptp.yunting.dao.AcqDeatilsDaoImpl;
import com.ustcinfo.ptp.yunting.service.AcqPageRuleService;
import com.ustcinfo.ptp.yunting.service.CollectionSiteService;
import com.ustcinfo.ptp.yunting.service.CollectionStrategyService;
import com.ustcinfo.ptp.yunting.service.ISpiderService;
import com.ustcinfo.ptp.yunting.service.InfoEntityService;
import com.ustcinfo.ptp.yunting.support.AllErrorMessage;
import com.xxl.job.executor.processor.detail.DetailProcessThread;
import com.xxl.job.executor.processor.list.ListItratorThread;
import com.xxl.job.executor.util.DequeOuts;
import com.xxl.job.executor.util.DequeWrite;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.YuntingRedisScheduler;

@SpringBootApplication

public class Application {
  

	@Value("${redisHost}")
	private static  String redisHost;
	
	public static String veriCodePath;
	
	@Value("${file.path}")
	private static  String filePath;

	public static YuntingRedisScheduler redisScheduler;
	
	public static DequeOuts DequeOut;
	private static int T_COUNT = 1 ;
	private static int FUNC = 0 ;//0-列表和详情线程都启动，1-只启动详情线程，2-只启动列表线程
	
	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(Application.class);
		logger.info("programe start ... params[Frist-file store path,Second-thread count,Third-task type<0-列表和详情线程都启动，1-只启动详情线程，2-只启动列表线程>") ;
		if(args.length<1){
			logger.info("异常退出，请输入文件输出路径！");
			System.exit(1);
		}else if(args.length==2){
			try{
				T_COUNT = Integer.parseInt(args[1]) ;
			}catch(Exception e){
				e.printStackTrace();
				logger.info(AllErrorMessage.getExceptionStackTrace(e));
			}
		}else if(args.length==3){
			try{
				FUNC = Integer.parseInt(args[2]) ;
			}catch(Exception e){
				logger.info(AllErrorMessage.getExceptionStackTrace(e));
				e.printStackTrace();
			}
		}
		
		
        ApplicationContext appContext = SpringApplication.run(Application.class, args);
//        Runtime.getRuntime().addShutdownHook( new Thread( new ShutdownHook()));  //得到Runtime的引用并注册关闭钩子  
	    //商家进程
        String path=args[0];
        Thread t = new Thread(new DequeWrite(path));  
        t.start();  
        //商机进程
        
        /**
         * 为了适应采集型男提升，将架构进行拆分，列表和详情页分开处理
         */
        if(FUNC==0 || FUNC==1){
        	logger.info("开始扫描详情表,开始时间为"+StringHas.getDateNowStr());
	        ExecutorService service = Executors.newFixedThreadPool(T_COUNT);  
	        for(int i=0;i<T_COUNT;i++){
	        	service.execute(new DetailProcessThread(appContext.getBean(ISpiderService.class),
	        			appContext.getBean(AcqPageRuleService.class),
	        			appContext.getBean(CollectionStrategyService.class),
	        			appContext.getBean(CollectionSiteService.class),
	        			appContext.getBean(InfoEntityService.class)));
	        	
	        }
        }
        if(FUNC==0 || FUNC==2){
        	logger.info("开始扫描导航表,开始时间为"+StringHas.getDateNowStr());
	        ExecutorService service_list = Executors.newFixedThreadPool(T_COUNT);  
	        for(int i=0;i<T_COUNT;i++){
	        	service_list.execute(new ListItratorThread(
	        			appContext.getBean(CollectionStrategyService.class),
	        			appContext.getBean(ISpiderService.class),
	        			appContext.getBean(AcqPageRuleService.class),
	        			appContext.getBean(CollectionSiteService.class),
	        			appContext.getBean(InfoEntityService.class),
	        			appContext.getBean(AcqDeatilsDaoImpl.class)));
	        }
        }
        
	}
	
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
	
	
	
	

}
<<<<<<< HEAD
=======

>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
