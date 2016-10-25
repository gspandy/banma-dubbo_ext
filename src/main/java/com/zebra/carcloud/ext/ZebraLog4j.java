package com.zebra.carcloud.ext;

import com.zebra.xconfig.client.XConfig;
import com.zebra.xconfig.client.XKeyObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 日志辅助类，依赖xconfig，自动生成DailyRollingFileAppender，使用此方法将不需要再自己添加fileappender
 * 可以主动修改root日志级别
 * file path每次重启时候生效
 * Created by ying on 16/8/31.
 */
public class ZebraLog4j {
    private static Logger logger = LoggerFactory.getLogger(ZebraLog4j.class);

    private static boolean isInit = false;
    private static int CHECK_INTERVAL_MILL = 1000*10;

    /**
     *
     * @param levelKey 日志等级key
     * @param filePathKey 日志文件路径
     */
    public static void init(final String levelKey, final String filePathKey){
        if(!isInit) {
            synchronized (ZebraLog4j.class) {
                if(!isInit) {
                    new Thread(){
                        @Override
                        public void run() {
                            while(true){
                                try {
                                    if(StringUtils.isNotBlank(XConfig.getValue(levelKey))){
                                        logger.debug("==========>check logLevel");
                                        initLogLevel(levelKey);
                                        break;
                                    }
                                    Thread.sleep(CHECK_INTERVAL_MILL);
                                } catch (InterruptedException e) {
                                    logger.error(e.getMessage(),e);
                                }
                            }
                        }
                    }.start();

                    new Thread(){
                        @Override
                        public void run() {
                            while(true){
                                try {
                                    if(StringUtils.isNotBlank(XConfig.getValue(filePathKey))){
                                        logger.debug("==========>check logFileAppender");
                                        initLogFileAppender(filePathKey);
                                        break;
                                    }
                                    Thread.sleep(CHECK_INTERVAL_MILL);
                                } catch (InterruptedException e) {
                                    logger.error(e.getMessage(),e);
                                }
                            }
                        }
                    }.start();

                    isInit = true;
                }
            }
        }
    }

    private static void initLogFileAppender(String filePathKey){
        String filePath = XConfig.getValue(filePathKey);

        Layout layout = new PatternLayout("[%-5p] %d{HH:mm:ss.SSS} [%t] (%F:%L) - %m%n");
        if (StringUtils.isBlank(filePath)) {
            logger.error("无法获取到filePath，fileAppender将无法初始化");
        }

        Appender appender = null;
        try {
            appender = new DailyRollingFileAppender(layout, filePath, "'.'yyyy-MM-dd");
            appender.setName("zebraDailyRollingAppender");

//            org.apache.log4j.Logger myLogger = org.apache.log4j.Logger.getLogger("com.zebra");
//            myLogger.setAdditivity(false);
//            myLogger.addAppender(appender);
//            myLogger.setLevel(LogManager.getRootLogger().getLevel());

            LogManager.getRootLogger().addAppender(appender);

            logger.debug("==========>logFileAppender is add,filePath is {}", filePath);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private static void initLogLevel(final String logLevelKey){
        String logLevel = XConfig.getValue(logLevelKey);

        if (StringUtils.isBlank(logLevel)) {
            logger.warn("无法获取到logLevel，默认将会使用error级别。");
        }

        logger.debug("==========>logLevel is {}",logLevel);

        LogManager.getRootLogger().setLevel(Level.toLevel(logLevel, Level.ERROR));

        XConfig.addObserver(new XKeyObserver() {
            @Override
            public String getKey() {
                return logLevelKey;
            }

            @Override
            public void change(String value) {
                Level level = Level.toLevel(value, Level.ERROR);
                logger.debug("==========>logLevel is {}",level);
                LogManager.getRootLogger().setLevel(level);
            }
        });
    }
}
