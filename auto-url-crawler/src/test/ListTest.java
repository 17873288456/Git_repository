package test;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.wenge.datagroup.imp.CrawlerList;
import com.wenge.datagroup.model.CrawlerListUrl;

public class ListTest {
	
	public static void main(String[] args) throws InterruptedException {
		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
//		InitContext.initDatasource();
//		String proxyIP = "127.0.0.1";
//		int port = -1;
//		Proxy pro = new Proxy();
//		new Thread(pro).start();
//		Thread.sleep(20000);
//		port = pro.getLocalPro();

		String listUrl = "http://elinformadorweb.com.mx/2018/08/";
		listUrl = "http://spanish.china.org.cn/node_7196111.htm";
		listUrl = "https://www.anntw.com/col/editorial-meeting/";
		listUrl = "http://www.fukuishimbun.co.jp/category/news-fukui";
		listUrl = "http://www.fukuishimbun.co.jp/category/news-fukui/%E3%83%9E%E3%83%A9%E3%82%BD%E3%83%B3";
		listUrl = "http://www.fukuishimbun.co.jp/category/news-fukui";
		listUrl = "http://jp.eastday.com/node2/home/n3092/n3883/index.html";
		listUrl = "http://jp.eastday.com/node2/home/latest/xzj/gn/index.html";
		listUrl = "http://www.jms.gov.cn/html/index/list/lb00010005.html";
		CrawlerListUrl crawlerListUrl = new CrawlerListUrl();
		crawlerListUrl.setId(81714);
		crawlerListUrl.setHomeUrlId(365);
		crawlerListUrl.setChannelName("新华专栏");
		crawlerListUrl.setListUrl(listUrl);
		crawlerListUrl.setStatus(2);
		crawlerListUrl.setIsProxy(0);
		crawlerListUrl.setIsRender(1);
		crawlerListUrl.setRuleId(2);
		String nextPageXpath = "//*[@id=\"laypage_0\"]/A[last()]";
		crawlerListUrl.setNextPageXpath(nextPageXpath);
		 String clickXpath = "";
		 crawlerListUrl.setClickXpath(clickXpath);
		 String nextPageRule = "http://tv.cctv.com/lm/xwlb/day/#yyyy#MM#dd.shtml";
		crawlerListUrl.setNextPageRule(nextPageRule);
		CrawlerList l = new CrawlerList(crawlerListUrl, null, 0);
//		CrawlerList l = new CrawlerList(crawlerListUrl, proxyIP, port);
		l.run();
	}

}
