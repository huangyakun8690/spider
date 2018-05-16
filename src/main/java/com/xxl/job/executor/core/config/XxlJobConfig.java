package com.xxl.job.executor.core.config;

<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Value;
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

<<<<<<< HEAD
=======
import com.xxl.job.core.executor.XxlJobExecutor;

>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

@Configuration


@ImportResource("classpath:META-INF/spring/framework-context.xml")
@ComponentScan(basePackages = "com.xxl.job.executor.service.jobhandler")
public class XxlJobConfig {
<<<<<<< HEAD
   
=======
    private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

    @Value("${xxl.job.admin.addresses}")
    private String addresses;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logpath;

<<<<<<< HEAD
/*
=======

>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobExecutor xxlJobExecutor() {
        logger.error("------------ xxlJobExecutor -----------");
        XxlJobExecutor xxlJobExecutor = new XxlJobExecutor();
        xxlJobExecutor.setIp(ip);
        xxlJobExecutor.setPort(port);
        xxlJobExecutor.setAppName(appname);
        xxlJobExecutor.setAdminAddresses(addresses);
        xxlJobExecutor.setLogPath(logpath);
        return xxlJobExecutor;
    }
<<<<<<< HEAD
    */
    
=======
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

}