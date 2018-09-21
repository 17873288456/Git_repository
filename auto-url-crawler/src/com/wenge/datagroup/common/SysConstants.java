package com.wenge.datagroup.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qianhang
 * @date 2017年9月27日
 */
public class SysConstants {
	private static final Logger logger = LoggerFactory.getLogger(SysConstants.class);

	public static final String CONFIG_FILE = "config/config.properties";
	public static final String LOG4J_FILE = "config/log4j.properties";
	// 数据库地址
	public static String DBURL = null;
	// 数据库IP
	public static String DBIP = null;
	// 数据库名称
	public static String DBNAME = null;
	// 数据库用户名
	public static String USER = null;
	// 数据库密码
	public static String PASSWORD = null;
	// 数据库mapping地址
	public static String MAPPINGKIT = null;
	// 数据库没有新任务时的休眠时间 单位分钟
	public static Integer SLEEP = null;
	// 代理
	public static Integer PROSLEEP = null;
	// 只采集多少页的数据 -- 针对未采集过的
	public static Integer PAGENUM_BEFORE = null;
	// 只采集多少页的数据 -- 针对未已采集过的
	public static Integer PAGENUM_AFTER = null;
	// 采集初始URL
	public static String ZOLPHONEURL = null;
	// Kafka 地址
	public static String KAFKAADDRESS = null;
	//查询数据库channal sql
	public static String CHANNAL_SQL = null;
	//查询数据库LIST sql
	public static String LIST_SQL = null;
	//数据查询 Channal队列大小
	public static int queueChannalSize = 100;
	//数据查询 List队列大小
	public static int queueListSize = 100;
	//数据查询睡眠时间
	public static long select_sleep_time = 100;
	//线程阻塞等待时间
	public static long WITE_SLEEP = 100;
	//Thread pool size
	public static int threadPoolSize = 5;
	
	//判断网页文本密集区域文本数量阈值,若小于此阈值,列表页可能行比较大
	public static int nodeTextWords = 1000;
	//网页中链接最低阈值
	public static int linksCount = 10;
	
	//代理
	public static String proxyIP;

	static {
		init();
	}

	/**
	 * 初始化参数
	 * 
	 * @author QianHang
	 * @date 2017年9月27日
	 */
	private static void init() {

		Properties prop = getProperties(CONFIG_FILE);
		DBURL = prop.getProperty("dbURL");
		DBIP = prop.getProperty("dbIP");
		DBNAME = prop.getProperty("dbName");
		USER = prop.getProperty("user");
		PASSWORD = prop.getProperty("password");
		MAPPINGKIT = prop.getProperty("mappingKit");
		SLEEP = Integer.parseInt(prop.getProperty("sleep"));
		PROSLEEP = Integer.parseInt(prop.getProperty("proxy_sleep"));
		PAGENUM_BEFORE = Integer.parseInt(prop.getProperty("pageNum_before"));
		PAGENUM_AFTER = Integer.parseInt(prop.getProperty("pageNum_after"));
		queueChannalSize = Integer.parseInt(prop.getProperty("queue_channal_size"));
		queueListSize = Integer.parseInt(prop.getProperty("queue_list_size"));
		threadPoolSize = Integer.parseInt(prop.getProperty("threadPoolSize"));
		linksCount = Integer.parseInt(prop.getProperty("linksCount"));
		nodeTextWords = Integer.parseInt(prop.getProperty("nodeTextWords"));
		
		select_sleep_time = Long.parseLong(prop.getProperty("select_sleepTime"));
		WITE_SLEEP = Long.parseLong(prop.getProperty("wait_sleep"));
		KAFKAADDRESS = prop.getProperty("kafkaAddress");
		CHANNAL_SQL = prop.getProperty("channal_sql");
		LIST_SQL = prop.getProperty("list_sql");
		proxyIP = prop.getProperty("proxyIP");
	}

	public static Properties getProperties(String fileName) {
		Properties prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream(fileName);
			prop.load(fis);
			fis.close();
		} catch (IOException e) {
			logger.error("==========>>>文件---" + fileName + "解析失败");
			return null;
		}
		return prop;
	}
}
