package test;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.wenge.datagroup.common.InitContext;
import com.wenge.datagroup.imp.CrawlerChannel;
import com.wenge.datagroup.model.CrawlerHomeUrl;
import com.wenge.datagroup.util.Proxy;

public class ChannelTest {
	
	public static void main(String[] args) throws InterruptedException {
		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
		InitContext.initDatasource();
		Proxy pro = new Proxy();
		new Thread(pro).start();
		int port = pro.getLocalPro();
		Thread.sleep(20000);
		CrawlerHomeUrl m = new CrawlerHomeUrl();
		m.setId(587);
		m.setSiteName("福岛民友");
		m.setHomeUrl("http://www.minyu-net.com/");
		m.setStatus(2);
		m.setIsProxy(0);
		m.setIsLogin(0);
		m.setIsRender(1);
		CrawlerChannel c = new CrawlerChannel(m, "127.0.0.1", port);
		c.startTask(m.getIsRender()==1);
//		c.initBrower();
//		String host = URLUtils.getHost(url);
//		Map<String, String> channelNameMap = new HashMap<>();
//		ArrayList<String> urlList = new ArrayList<>();
//		ArrayList<String> childUrlList = new ArrayList<>();
//		c.getAllUrl(url, urlList, childUrlList, host, channelNameMap, true);
		
	}

}
