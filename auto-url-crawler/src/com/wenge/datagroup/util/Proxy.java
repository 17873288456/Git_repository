package com.wenge.datagroup.util;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.stfl.Socks5ServerListMode;
import com.stfl.misc.Config;
import com.stfl.network.proxy.IProxy.GROUP;
import com.wenge.datagroup.common.SysConstants;

@SuppressWarnings("deprecation")
public class Proxy implements Runnable{
	private static final Logger LOG = Logger.getLogger(Proxy.class);
	private static final String url="https://www.facebook.com/";
	private int localPro ;
	private String localIP = getIp();
	public String getLocalIP() {
		return localIP;
	}
	public int getLocalPro() {
		return localPro;
	}
	static{
		String fileName = "config/gui-config.json";
		// 启动代理
		File configfile = new File(fileName);
		JSONObject config_json;
		try {
			config_json = JSON.parseObject(Files.toString(configfile, Charsets.UTF_8));
			/** 装载配置，启动代理 **/
			Socks5ServerListMode.startSocks5(config_json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setProxy(){
		Config config = Socks5ServerListMode.getRandomSocksWithGroup(GROUP.GROUP_OUT);
		String proxyIP=config.getRemoteIpAddress();
		int prot = config.getRemotePort();
		localPro = config.getLocalPort();
		while(true){
//			localIP="127.0.0.1";
			LOG.info("proxyIP="+proxyIP);
			LOG.info("prot="+prot);
			LOG.info("localIP="+localIP);
			LOG.info("localPro="+localPro);
			String data = JsoupCrawler.downloadByProxy(url, localIP, localPro);
			if (data!=null) {
				try {
					data=null;
					LOG.info(">>>>>>>>> 代理【"+proxyIP+":"+prot+"】可用！,睡眠"+SysConstants.PROSLEEP+" 分");
					Thread.sleep(SysConstants.PROSLEEP*1000*60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				LOG.info(">>>>>>>>> 代理【"+proxyIP+":"+prot+"】失效！重新获取");
				config = Socks5ServerListMode.getRandomSocksWithGroup(GROUP.GROUP_OUT);
				proxyIP=config.getRemoteIpAddress();
				prot = config.getRemotePort();
				localPro = config.getLocalPort();
			}
		}
	}
	@Override
	public void run() {
		setProxy();
	}
	/**
	 * 获取本机ip
	 * @return
	 */
	public String getIp(){
		String ipStr = "";
		try{
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements()){
				NetworkInterface netInterface = allNetInterfaces.nextElement();
//				System.out.println(netInterface.getName());
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()){
					ip = addresses.nextElement();
					if (ip != null && ip instanceof Inet4Address){
						ipStr = ip.getHostAddress();
//						System.out.println("本机的IP = " + ip.getHostAddress());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return ipStr;
	}
	private String getIp2(){
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostAddress = addr.getHostAddress();
			return hostAddress;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) throws UnknownHostException {
		Proxy p = new Proxy();
		p.run();
	}
}
