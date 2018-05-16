package com.xxl.job.executor.util;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

import com.xxl.job.core.log.XxlJobFileAppender;

/**
 * @author li.lingyue@ustcinfo.com
 * @date 2018-01-14
 * @version 1.0
 */
public class LogUtil {
	
    //private static Logger logger = LoggerFactory.getLogger("xxl-job logger");
    

    
    private static final String FQCN = LogUtil.class.getName();  

    
    /**
     * append log
     *
     * @param appendLog
     */
    public static void trace(String appendLog) {
    	log(appendLog,Level.TRACE);
        
    }
    
    /**
     * append log
     *
     * @param appendLog
     */
    public static void debug(String appendLog) {
    	log(appendLog,Level.DEBUG);
        
    }
    
    
    
    /**
     * append log
     *
     * @param appendLog
     */
    public static void info(String appendLog) {
    	log(appendLog,Level.INFO);
       
    }
    
    /**
     * append log
     *
     * @param appendLog
     */
    public static void warn(String appendLog) {
    	log(appendLog,Level.WARN);
    }
    
    /**
     * append log
     *
     * @param appendLog
     */
    public static void error(String appendLog) {
    	log(appendLog,Level.ERROR);
       
    }
    
    
    

    /**
     * append log with pattern
     *
     * @
     *
     * @param appendLogPattern  like "aaa {0} bbb {1} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static void log(String appendLogPattern,Level level, Object ... appendLogArguments) {
        String appendLog = MessageFormat.format(appendLogPattern, appendLogArguments);
        log(appendLog,level);
    }
    
    /**
     * append log with pattern
     *
     * @
     *
     * @param appendLogPattern  like "aaa {0} bbb {1} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static void trace(String appendLogPattern, Object ... appendLogArguments) {
    	log(appendLogPattern,Level.TRACE, appendLogArguments);
    }
    
    
    /**
     * append log with pattern
     *
     * @
     *
     * @param appendLogPattern  like "aaa {0} bbb {1} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static void info(String appendLogPattern, Object ... appendLogArguments) {
    	log(appendLogPattern,Level.INFO, appendLogArguments);
    }
    
    /**
     * append log with pattern
     *
     * @
     *
     * @param appendLogPattern  like "aaa {0} bbb {1} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static void warn(String appendLogPattern, Object ... appendLogArguments) {
    	log(appendLogPattern,Level.WARN, appendLogArguments);
    }
    
    /**
     * append log with pattern
     *
     * @
     *
     * @param appendLogPattern  like "aaa {0} bbb {1} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static void error(String appendLogPattern, Object ... appendLogArguments) {
    	log(appendLogPattern,Level.ERROR, appendLogArguments);
    }
    
    
    /**
     * append log with pattern
     *
     * @
     *
     * @param appendLogPattern  like "aaa {0} bbb {1} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static void debug(String appendLogPattern, Object ... appendLogArguments) {
    	log(appendLogPattern,Level.DEBUG, appendLogArguments);
    }
    
    /**
     * append log
     *
     * @param appendLog
     */
    public static void log(String appendLog, Level level) {
    	SimpleDateFormat xxlJobLoggerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // logFileName
        String logFileName = XxlJobFileAppender.contextHolder.get();
        if (logFileName==null || logFileName.trim().length()==0) {
            return;
        }

        // "yyyy-MM-dd HH:mm:ss [ClassName]-[MethodName]-[LineNumber]-[ThreadName] log";
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        StackTraceElement callInfo = stackTraceElements[1];

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(xxlJobLoggerFormat.format(new Date())).append(" ")
            .append("["+ callInfo.getClassName() +"]").append("-")
            .append("["+ callInfo.getMethodName() +"]").append("-")
            .append("["+ callInfo.getLineNumber() +"]").append("-")
            .append("["+ Thread.currentThread().getName() +"]").append(" ")
            .append(appendLog!=null?appendLog:"");
        String formatAppendLog = stringBuffer.toString();
        
        // appendlog
        XxlJobFileAppender.appendLog(logFileName, formatAppendLog);
//        logger.warn("[{}]: {}", logFileName, formatAppendLog);
        
        //lly180113
        //添加本地日志打印，区分日志级别
        LocationAwareLogger locallogger = (LocationAwareLogger) LoggerFactory.getLogger(callInfo.getClass());
        locallogger.log(null, FQCN, level.toInt(), appendLog, null, null);
    }

}
