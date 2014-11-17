package com.vteba.protostuff;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

/**
 * 启动Netty Server
 * @author yinlei
 * @since 2014-5-2
 */
public class Startup {
	private static final Logger LOGGER = LoggerFactory.getLogger(Startup.class);
	
	public static void main(String[] args) {
		initLogger();
		
		LOGGER.info(Arrays.toString(args));
		LOGGER.info("开始加载启动Netty Server的Spring配置文件。");
		
		String configLocation = "classpath:application-netty-protostuff.xml";
		ClassPathXmlApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext(configLocation);
			Server server = context.getBean(Server.class);
			server.start();
		} catch (Exception e) {
			LOGGER.error("启动Netty Server守护线程出错。", e);
		} finally {
			context.close();
		}
	}
	
	/**
	 * 加载log4j的日志
	 */
	public static void initLogger() {
		try {
			Log4jConfigurer.initLogging("classpath:log4j.xml");
		} catch (Exception e) {
			throw new IllegalStateException("没有找到log4j配置文件。", e);
		}
	}
}
