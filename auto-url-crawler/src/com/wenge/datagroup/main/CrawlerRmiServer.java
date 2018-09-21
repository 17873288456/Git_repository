package com.wenge.datagroup.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.wenge.datagroup.common.InitContext;
import com.wenge.datagroup.common.SysConstants;
import com.wenge.datagroup.model.CrawlerHomeUrl;
import com.wenge.datagroup.model.CrawlerListUrl;
import com.wenge.datagroup.service.RmiService;
import com.wenge.datagroup.util.Proxy;

public class CrawlerRmiServer extends UnicastRemoteObject implements RmiService {

    private static final long serialVersionUID = 6225843067552710429L;
    
    private static Logger LOG = Logger.getLogger(CrawlerRmiServer.class);
    private static LinkedBlockingQueue<CrawlerHomeUrl> queueChannal = new LinkedBlockingQueue<>(SysConstants.queueChannalSize);
    private static LinkedBlockingQueue<CrawlerListUrl> queueList = new LinkedBlockingQueue<>(SysConstants.queueListSize);
    private static RmiService rmiService;
    private Proxy proxy = new Proxy();
    private ChannalDao channalDao = new ChannalDao();
    private ListDao listDao = new ListDao();
    private String text;
    private String text2;

    protected CrawlerRmiServer() throws RemoteException {
        super();
        if(SysConstants.proxyIP==null){
        	new Thread(proxy).start();
        }
        try {
        	text = FileUtils.readFileToString(new File("cursor/channal.properties"), "UTF-8");
        	text2 = FileUtils.readFileToString(new File("cursor/list.properties"), "UTF-8");
        } catch (IOException e) {
        	e.printStackTrace();
        }
        new Thread(channalDao).start();
        new Thread(listDao).start();
    }

    public static void init() {
    	LOG.info("init server dataSource start!");
    	InitContext.initDatasource();
        LOG.info("init rmi service start!");
        try {
            rmiService = new CrawlerRmiServer();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(new File("config/config.properties"));
            Properties pro = new Properties();
            pro.load(inStream);
            System.setProperty("java.rmi.server.hostname", pro.getProperty("server.ip"));
            Registry createRegistry = LocateRegistry.createRegistry(Integer.parseInt(pro.getProperty("server.port").trim()));
            createRegistry.rebind(pro.getProperty("server.name"), rmiService);
            
            LOG.info(pro.getProperty("server.name") + "---->" + pro.getProperty("server.ip") + ":" + Integer.parseInt(pro.getProperty("server.port").trim()) + ",ready!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (inStream != null)
                    inStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        LOG.info("init rmi service end!");
    }
    @Override
    public CrawlerHomeUrl getHomeUrl() throws RemoteException {
    	CrawlerHomeUrl data = null;
    	synchronized (lock) {
			LOG.info("--从队列poll channal数据--");
			data = queueChannal.poll();
			String url=null;
			if(data!=null){
				String text = data.getId()+"\r\n";
				try {
					FileUtils.writeStringToFile(new File("cursor/channal.properties"), text, "UTF-8", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				data.setStatus(3);//设置为正在采集
				boolean update = data.update();
				if(update)
					LOG.info(data.getHomeUrl()+"更新为正在采集");
				else
					LOG.info(data.getHomeUrl()+"更新正在采集失败");
				url=data.getHomeUrl();
			}
			LOG.info("--从队列poll 到 channal 数据,URL:"+url+" 剩余:"+queueChannal.size());
    	}
    	return data;
    }
    @Override
    public CrawlerListUrl getListUrl() throws RemoteException {
    	CrawlerListUrl data = null;
    	synchronized (lock2) {
    		LOG.info("--从队列poll list数据--");
    		data = queueList.poll();
    		String url=null;
			if(data!=null){
				data.setStatus(4);//设置为正在采集
				boolean update = data.update();
				String text = data.getId()+"\r\n";
				try {
					FileUtils.writeStringToFile(new File("cursor/list.properties"), text, "UTF-8", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(update)
					LOG.info(data.getListUrl()+"更新为正在采集");
				else
					LOG.info(data.getListUrl()+"更新正在采集失败");
				url=data.getListUrl();
			}
    		LOG.info("--从队列poll 到 list 数据,URL:"+url+" 剩余:"+queueList.size());
    	}
    	return data;
    }
    @Override
    public void updateCursorChannal(String id) throws RemoteException {
    	synchronized (lock) {
			String text;
			try {
				text = FileUtils.readFileToString(new File("cursor/channal.properties"), "UTF-8");
				String[] split = text.split("\r\n");
				StringBuffer sb = new StringBuffer();
				for(String str:split){
					if(!str.equals(id+"")){
						sb.append(str).append("\r\n");
					}
				}
				FileUtils.writeStringToFile(new File("cursor/channal.properties"), sb.toString(), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    @Override
    public void updateCursorList(String id) throws RemoteException {
    	synchronized (lock2) {
    		String text;
    		try {
    			text = FileUtils.readFileToString(new File("cursor/list.properties"), "UTF-8");
    			String[] split = text.split("\r\n");
				StringBuffer sb = new StringBuffer();
				for(String str:split){
					if(!str.equals(id+"")){
						sb.append(str).append("\r\n");
					}
				}
    			FileUtils.writeStringToFile(new File("cursor/list.properties"), sb.toString(), "UTF-8");
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    @Override
    public int getProxyPort() throws RemoteException {
    	synchronized (lock) {
    		int port=0;
    		port = proxy.getLocalPro();
    		return port;
    	}
    }
    @Override
	public String getProxyIP() throws RemoteException {
    	synchronized (lock) {
    		return proxy.getLocalIP();
    	}
	}
    public static void main(String[] args) {
    	PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
        init();
	}
    private class ChannalDao extends Thread{
    	@Override
    	public void run() {
    		while(true){
    			if(queueChannal.isEmpty()){
    				LOG.info("Channal 队列没数据了,进行查询~");
    				selectHomeDao();
    				LOG.info("Channal 查询完毕,当前数据队列大小:"+ queueChannal.size());
    			}
				LOG.info("Channal 队列还有数据:"+ queueChannal.size()+" 休眠"+SysConstants.select_sleep_time+"分钟");
				try {
					Thread.sleep(1000 * 60 * SysConstants.select_sleep_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    	private void selectHomeDao(){
    		String sql = SysConstants.CHANNAL_SQL;
    		if(text!=null&&!text.isEmpty()){
    			text = text.replaceAll("\r\n", ",");
    			if(text.endsWith(","))
    				text = text.substring(0, text.length()-1);
            	sql = "select * from crawler_home_url where id in("+text+") order by id asc";
            	LOG.info("查询正在采集任务ID,SQL: "+sql);
            }
    		List<CrawlerHomeUrl> homeUrlList = CrawlerHomeUrl.dao.find(sql);
			for(CrawlerHomeUrl obj:homeUrlList){
				boolean offer = queueChannal.offer(obj);
				if(offer){
					LOG.info(obj.getHomeUrl()+"加入Channal采集队列");
				}else
					LOG.info(obj.getHomeUrl()+"未加入Channal采集队列");
			}
			text = null;
        }
    }
    private class ListDao extends Thread{
    	private long id;
    	@Override
    	public void run() {
    		try {
				String str = FileUtils.readFileToString(new File("cursor/id.list"), "UTF-8");
				if(str!=null&&!str.trim().isEmpty()){
					id = Long.parseLong(str);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    		while(true){
    			if(queueList.isEmpty()){
    				LOG.info("List 队列没数据了,进行查询~");
    				selectListDao();
    				LOG.info("List 查询完毕,当前数据队列大小:"+ queueList.size());
    			}
				LOG.info("List 队列还有数据:"+ queueList.size()+" 休眠"+SysConstants.select_sleep_time+"分钟");
				try {
					Thread.sleep(1000 * 60 * SysConstants.select_sleep_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    	private void selectListDao(){
    		String sql = SysConstants.LIST_SQL;
    		if(text2!=null&&!text2.isEmpty()){
    			text2 = text2.replaceAll("\r\n", ",");
    			if(text2.endsWith(","))
    				text2 = text2.substring(0, text2.length()-1);
            	sql = "select * from crawler_list_url where id in("+text2+")  order by id asc";
            	LOG.info("查询正在采集任务ID,SQL: "+sql);
            }
    		sql = sql.replaceAll("\\[ID\\]", id+"");
    		List<CrawlerListUrl> listUrlList = CrawlerListUrl.dao.find(sql);
    		if(listUrlList==null || listUrlList.size()<1){
    			id = 0;
    			try {
					FileUtils.writeStringToFile(new File("cursor/id.list"), id+"", "UTF-8", false);
				} catch (IOException e) {
					e.printStackTrace();
				}
    			return;
    		}
			for(CrawlerListUrl obj:listUrlList){
				boolean offer = queueList.offer(obj);
				if(offer){
					try {
						FileUtils.writeStringToFile(new File("cursor/id.list"), id+"", "UTF-8", false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					LOG.info(obj.getListUrl()+"加入List采集队列");
				}else
					LOG.info(obj.getListUrl()+"未加入List采集队列");
			}
			text2 = null;
    	}
    }
	

}
