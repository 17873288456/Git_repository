package com.wenge.datagroup.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.wenge.datagroup.common.InitContext;
import com.wenge.datagroup.common.SysConstants;
import com.wenge.datagroup.imp.CrawlerChannel;
import com.wenge.datagroup.imp.CrawlerList;
import com.wenge.datagroup.model.CrawlerHomeUrl;
import com.wenge.datagroup.model.CrawlerListUrl;
import com.wenge.datagroup.service.RmiService;


public class CrawlerRmiClient {

    public static RmiService rmiService;
    private static Logger logger = Logger.getLogger(CrawlerRmiClient.class);
    private static int proxyPort;
    private static String proxyIP = "192.168.6.1";
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(SysConstants.threadPoolSize);

    /**
     * 初始化rmi服务
     */
    private static void init() {
    	logger.info("init client dataSource start!");
    	InitContext.initDatasource();
        FileInputStream inStream = null;
        try {
            // 加载rmi配置文件
            inStream = new FileInputStream(new File("config/config.properties"));
            Properties pro = new Properties();
            pro.load(inStream);

            String loopup = "//" + pro.getProperty("server.ip") + ":"
                    + Integer.parseInt(pro.getProperty("server.port").trim())
                    + "/" + pro.getProperty("server.name");
            logger.info(loopup);
            try {
                rmiService = (RmiService) Naming.lookup(loopup);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe.getMessage());
            System.exit(0);
        } finally {
            try {
                if (inStream != null)
                    inStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    public static void start(String type) {
        while (true) {
            try {
            	if(SysConstants.proxyIP!=null){
            		proxyIP = SysConstants.proxyIP.split(":")[0];
            		proxyPort = Integer.parseInt(SysConstants.proxyIP.split(":")[1]);
            	}else{
            		proxyPort = rmiService.getProxyPort();
            		proxyIP = rmiService.getProxyIP();
            	}
            	CrawlerHomeUrl channalData=null;
            	CrawlerListUrl listData=null;
            	int threadCount = ((ThreadPoolExecutor)fixedThreadPool).getActiveCount();
            	if(threadCount==SysConstants.threadPoolSize){
            		logger.info("-->>>> "+type+" 线程阻塞等待"+SysConstants.WITE_SLEEP+"分钟");
        			Thread.sleep(1000*60*SysConstants.WITE_SLEEP);
            	}
            	if(type.contains("channal")){
            		channalData = rmiService.getHomeUrl();
            		if (channalData != null) {
            			CrawlerChannel crawlerChannal = new CrawlerChannel(channalData,proxyIP,proxyPort);
        				fixedThreadPool.execute(crawlerChannal);
        				logger.info(type+" 活动线程:"+threadCount);
            		}
            		if(channalData==null){
            			logger.info("-->>>> channal 暂无数据,休息"+SysConstants.SLEEP+"分钟");
            			Thread.sleep(1000*60*SysConstants.SLEEP);
            		}
            	}else if(type.contains("list")){
            		listData = rmiService.getListUrl();
            		if (listData != null) {
            			CrawlerList crawlerList = new CrawlerList(listData,proxyIP,proxyPort);
        				fixedThreadPool.execute(crawlerList);
        				logger.info(type+" 活动线程:"+threadCount);
            		}
            		if(listData==null){
            			logger.info("-->>>> list 暂无数据,休息"+SysConstants.SLEEP+"分钟");
            			Thread.sleep(1000*60*SysConstants.SLEEP);
            		}
            	}
            } catch (RemoteException e1) {
                logger.info("get data exception", e1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
    	PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    	if(args==null||args.length!=1){
    		logger.error("Parameter error!{ Parameter is (list or channal)}");
    		System.exit(0);
    	}
    	init();
    	start(args[0]);
	}
}

