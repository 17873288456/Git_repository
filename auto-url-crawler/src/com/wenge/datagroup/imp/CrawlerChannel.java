package com.wenge.datagroup.imp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.wenge.datagroup.common.BaseTreeGrid;
import com.wenge.datagroup.common.ChannelCompute;
import com.wenge.datagroup.common.CommonTasks;
import com.wenge.datagroup.common.InitContext;
import com.wenge.datagroup.common.SysConstants;
import com.wenge.datagroup.main.CrawlerRmiClient;
import com.wenge.datagroup.model.CrawlerHomeUrl;
import com.wenge.datagroup.model.CrawlerListUrl;
import com.wenge.datagroup.util.BrowserEngine;
import com.wenge.datagroup.util.MyURLUtils;
import com.wenge.datagroup.util.Proxy;
import com.wenge.datagroup.util.StringSimilar;
import com.wenge.datagroup.util.TimerUtil;
import com.wenge.datagroup.util.URLUtils;

public class CrawlerChannel implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(CrawlerChannel.class);
	public static final double THRESHOLD = 0.4;
	private static StringSimilar stringSimilar = new StringSimilar();
	private ArrayList<String> urlError = new ArrayList<String>(); // 错误的url
	private BrowserEngine browserEngine ;
	private WebDriver driver;
	private CrawlerHomeUrl crawlerHomeUrl;
	private List<LanguageProfile> languageProfiles ;
	private ChannelCompute channelCompute;
	//记录该站点语言
	private String languageCode;
	private String proxy;
	private int port;
	private String proxyIP;
	public CrawlerChannel(CrawlerHomeUrl crawlerHomeUrl,String proxyTP,int port){
		this.port = port;
		this.crawlerHomeUrl = crawlerHomeUrl;
		if(proxyTP==null||proxyTP.isEmpty()){
			this.proxyIP="192.168.6.1";
			this.port=1984;
		}else{
			this.proxyIP = proxyTP;
		}
		 try {
			languageProfiles = new LanguageProfileReader().readAllBuiltIn();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 channelCompute = new ChannelCompute();
	}

	@Override
	public void run() {
		startTask(crawlerHomeUrl.getIsRender() == 1);
		
	}
	private void initBrower(){
		try {
			browserEngine = new BrowserEngine();
			browserEngine.initConfigData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Integer isProxy = crawlerHomeUrl.getIsProxy();
		if(isProxy==1){
			//境内代理
			
		}else if(isProxy==2){
			//境外代理
			if(port>0){
				proxy = "--proxy-server=http://"+proxyIP+":"+port;
			}
		}
		driver = browserEngine.getBrowser(proxy,crawlerHomeUrl.getHomeUrl());
	}
	public void startTask(boolean isRender) {
		try {
			if(isRender){ //是否js渲染、0不是、1是  
				initBrower();
			}
			try {
				List<BaseTreeGrid> list = new ArrayList<BaseTreeGrid>();
				// Map<url,name>
				Map<String, String> channelNameMap = new HashMap<>();
				List<String> resultList = addURL(crawlerHomeUrl.getHomeUrl(), list, channelNameMap);  //list是所有频道URL
				Set<String> setList = new HashSet<>(resultList);
				List<String> res = new LinkedList<>(setList);
				logger.info("开始分簇....");
				JSONArray jsonArray = new JSONArray();
				// 核心匹配
				int categaryIndex = 0;
				while (res.size() > 0) {
					List<String> core = core(res);
					if (core != null && core.size() > 0) {
						JSONObject json = new JSONObject();

						JSONArray parseArray = JSONArray.parseArray(JSON.toJSONString(core));
						json.put("" + categaryIndex++, parseArray);
						jsonArray.add(json);
					}
				}
				for (Object json : jsonArray) {
					JSONObject obj = (JSONObject) json;
					Set<String> keySet = obj.keySet();
					for (String string : keySet) {
						JSONArray urlArray = obj.getJSONArray(string);
						for (Object object : urlArray) {
							CrawlerListUrl crawlerListUrl = new CrawlerListUrl();
							crawlerListUrl.setClusterId(Integer.valueOf(string));
							crawlerListUrl.setInsertTime(new Date());
							crawlerListUrl.setIsProxy(crawlerHomeUrl.getIsProxy());
							crawlerListUrl.setStatus(1);
							crawlerListUrl.setListUrl(object.toString());
							crawlerListUrl.setChannelName(channelNameMap.get(object.toString()));
							crawlerListUrl.setHomeUrlId(crawlerHomeUrl.getId());
							List<CrawlerListUrl> find = CrawlerListUrl.dao.find(
									"select * from crawler_list_url where ( list_url = '" + object.toString()
											+ "' or list_url = '" + object.toString()+"/' or list_url='"+object.toString().replace("http", "https")+"' "
													+ " or list_url='"+object.toString().replace("http", "https")+"/') and home_url_id = " + crawlerHomeUrl.getId());
							if (find != null && find.size()>0) {
								logger.info("已存在---list_url = " + object.toString() + " and home_url_id = "
										+ crawlerHomeUrl.getId());
							} else {
								boolean save = crawlerListUrl.save();
								if (save) {
									logger.info("SUCCESS---list_url = " + object.toString()
											+ " and home_url_id = " + crawlerHomeUrl.getId());
								}
							}
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			crawlerHomeUrl.setStatus(2);
			boolean update = crawlerHomeUrl.update();
			if(update){
				logger.info("URL:"+crawlerHomeUrl.getHomeUrl()+"更新状态为已采集成功!");
			}else{
				logger.info("URL:"+crawlerHomeUrl.getHomeUrl()+"更新状态为已采集失败!");
			}
			CrawlerRmiClient.rmiService.updateCursorChannal(crawlerHomeUrl.getId()+"");
			logger.info("URL："+crawlerHomeUrl.getHomeUrl()+" 采集完成!");
			TimerUtil.sleep_second(3);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			proxy = null;
			try {
				if(browserEngine!=null){
					browserEngine.tearDown();
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Add a URL to waiting list.
	 *
	 * @param url
	 * @throws IOException
	 */
	public List<String> addURL(String url, List<BaseTreeGrid> list, Map<String, String> channelNameMap) {
		try {
			String host = URLUtils.getHost(url);
			logger.info("Host: "+host);
			BaseTreeGrid root = new BaseTreeGrid();
			root.setId(url);
			list.add(root);
			ArrayList<String> urlList = new ArrayList<String>();
			ArrayList<String> child1UrlList = new ArrayList<String>();
			logger.info("一级URL: "+url+" 开始遍历获取");
			getAllUrl(url, urlList, child1UrlList, host, channelNameMap, true);
			if (child1UrlList != null && child1UrlList.size() > 0) {
				for (String child1_url : child1UrlList) {
					logger.info("二级URL: "+child1_url+" 开始遍历获取");
					ArrayList<String> child2UrlList = new ArrayList<String>();
					getAllUrl(child1_url, urlList, child2UrlList, host, channelNameMap, true);
					BaseTreeGrid child1 = new BaseTreeGrid();
					if(child2UrlList.contains(child1_url)){
						child1.setId(child1_url);
						child1.setParentId(root.getId());
						list.add(child1);
					}
					if (child2UrlList != null && child2UrlList.size() > 0) {
						for (String child2_url : child2UrlList) {
							logger.info("三级URL: "+child2_url+" 开始验证");
							getAllUrl(child2_url, urlList, null, host, channelNameMap, false);
							if(channelNameMap.containsKey(child2_url)){
								BaseTreeGrid child2 = new BaseTreeGrid();
								child2.setId(child2_url);
								child2.setParentId(child1.getId());
								list.add(child2);
							}
							// ArrayList<String> child3UrlList = new ArrayList<String>();
							// getAllUrl(child2_url, urlList, child3UrlList, host, channelNameMap);
							// if (child3UrlList != null && child3UrlList.size() > 0) {
							// for (String child3_url : child3UrlList) {
							// BaseTreeGrid child3 = new BaseTreeGrid();
							// child3.setId(child3_url);
							// child3.setParentId(child2.getId());
							// list.add(child3);
							// }
							// }
						}
					}else{
						logger.info("二级URL: "+child1_url+" 没有获取到子URL");
					}
				}
			}else{
				logger.info("一级URL: "+url+" 没有获取到子URL");
			}
			/*List<BaseTreeGrid> newList = new ArrayList<>();
			for(BaseTreeGrid obj:list){
				String id = obj.getId();
				boolean tmp = true;
				if(channelNameMap.containsKey(id)){
					for(BaseTreeGrid obj2:newList){
						if(obj2.getId().equals(id)){
							tmp = false;
							break;
						}
					}
					if(tmp)
						newList.add(obj);
				}
			}*/
			
			Set<BaseTreeGrid> treeSet = new HashSet<BaseTreeGrid>();
			for (BaseTreeGrid tree1 : list) {
				for (BaseTreeGrid tree2 : list) {
					if (tree2.parentId == tree1.id) {
						if (tree1.getChildren() == null) {
							tree1.setChildren(new ArrayList<BaseTreeGrid>());
						}
						tree1.getChildren().add(tree2);
						treeSet.add(tree1);
					}
				}
			}
			List<String> resultList = new ArrayList<String>();
			if(treeSet.size()<1){
				for (BaseTreeGrid tree1 : list) {
					String id = tree1.getId();
					if(!resultList.contains(id)){
						resultList.add(id);
					}
				}
				return resultList;
			}
			List<BaseTreeGrid> treeList = new ArrayList<>(treeSet);
			Collections.sort(treeList, new Comparator<BaseTreeGrid>() {
				@Override
				public int compare(BaseTreeGrid o1, BaseTreeGrid o2) {
					List<BaseTreeGrid> children2 = o2.getChildren();
					List<BaseTreeGrid> children1 = o1.getChildren();
					if(children2==null||children1==null)
						return 0;
					return children2.size() - children1.size();
				}
			});
			for (BaseTreeGrid baseTreeGrid : treeList) {
				List<BaseTreeGrid> children = baseTreeGrid.getChildren();
				if (children.size() >= 5) {
					resultList.add(baseTreeGrid.getId());
					logger.info(baseTreeGrid.getId() + ":" + children.size());
				}
				resultList.add(baseTreeGrid.getId());
				for (BaseTreeGrid channal : children) {
					resultList.add(channal.getId());
				}
			}
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private boolean filterURL(String url){
		if(url==null||url.isEmpty())
			return true;
		url = url.toLowerCase();
		if (url.endsWith(".mp4") || url.endsWith(".swf") || url.endsWith(".css")|| url.endsWith(".js")
				|| url.endsWith(".ico") || url.contains("detail")|| url.endsWith(".jpg")|| url.endsWith(".png")
				|| url.endsWith(".pdf")|| url.endsWith(".json")|| url.endsWith(".jpeg")) {
			return true;
		}
		return false;
	}
	public void getAllUrl(String url, ArrayList<String> urlList, ArrayList<String> childUrlList, String host,
			  Map<String, String> channelNameMap,  boolean isContinuous) {
		try {
			if(!filterURL(url)){
				if(!urlList.contains(url)){
					logger.info("开始采集:" + url);
				}else{
					logger.info("已采集过:" + url);
					return;
				}
			}else{
				logger.info("非法URL,过滤:" + url);
				return;
			}
			int count = 0;
			Document document = null;
			String title = null;
			while (count < 2) {
				if(browserEngine!=null){
					logger.info("URL:"+url+", 使用Selenium解析");
					try {
						try {
//						StopLoadPage loadPage = new StopLoadPage(driver,url, 30);
							long start_time = System.currentTimeMillis();
//						new Thread(loadPage).start();;
//							driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
							driver.get(url);
							CommonTasks.waitForPageLoad(driver, 20);
//						loadPage.isLoad = true;
							long end_time = System.currentTimeMillis();
							logger.info("url--"+url+"加载完成,耗时:"+((double)(end_time-start_time))/1000+"秒");
						} catch (UnhandledAlertException e) {
							driver.switchTo().alert().accept();
							logger.error("进入UnhandledAlertException异常");
						}
						String currentUrl = driver.getCurrentUrl();
						if(!URLUtils.getHost(currentUrl).equals(host)){
							logger.info("URL:"+url+"重定向至"+currentUrl+",放弃");
							if(channelNameMap!=null && channelNameMap.containsKey(url)){
								logger.info("URL:"+url+" channelNameMap Remove "+currentUrl);
								channelNameMap.remove(url);
								if(childUrlList!=null && childUrlList.contains(url)){
									logger.info("URL:"+url+" childUrlList Remove "+currentUrl);
									childUrlList.remove(url);
								}
							}
							return;
						}
						if(!url.equals(currentUrl)){
							logger.info("URL:"+url+"重定向至"+currentUrl);
							if(!isContinuous) {
								if(channelNameMap!=null && channelNameMap.containsKey(url)){
									logger.info("URL:"+url+" channelNameMap Remove "+currentUrl);
									channelNameMap.remove(url);
									if(childUrlList!=null && childUrlList.contains(url)){
										logger.info("URL:"+url+" childUrlList Remove "+currentUrl);
										childUrlList.remove(url);
									}
								}
							}
							url = currentUrl;
						}
						String pageSource = driver.getPageSource();
						document = Jsoup.parse(pageSource);
						title = driver.getTitle();
					} catch (Exception e) {
						e.printStackTrace();
						// 若捕捉异常,关掉这个浏览器,重新生成一个
						try {
							browserEngine.tearDown();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						try {
							browserEngine = new BrowserEngine();
							browserEngine.initConfigData();
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						driver = browserEngine.getBrowser(proxy,url);
						Thread.sleep(1000L);
					}
				}else{
					logger.info("URL:"+url+", 使用Jsoup解析");
					Integer isProxy = crawlerHomeUrl.getIsProxy();
					document = downloadPage(url, isProxy);
				}
				if (document != null && null!=document.getElementsByTag("body") && !document.getElementsByTag("body").text().trim().equals("")) {
					logger.info("解析document完成");
					document.select("script,pre,noscript,style,iframe,*[class*=footer],*[id*=footer],*[style*=display:none]").remove();
					break;
				}
				count++;
			}
			urlList.add(url);
			if (document != null) {
				if(title==null || title.isEmpty()){
					title = document.title();
				}
				if(languageCode==null){
					String text = document.text();
					languageCode= getLanguage(text);
				}
				if (null == channelNameMap.get(url) || ("").equals(channelNameMap.get(url).trim())) {
					boolean tmp = false;
					JSONObject json = channelCompute.isList(document, url);
					if(json.getBooleanValue("result")){
						Elements links = document.select("[href]");
						if(links!=null&&links.size()>9){
							logger.info("URL:"+url+" 是List,添加");
							channelNameMap.put(url, title);
						}else{
							logger.info("URL:"+url+"URL过少, 不是List");
							return;
						}
					}else{
						if(title!=null&&!title.isEmpty()){
							if(languageCode!=null&&languageCode.contains("zh")){
								if(title.split("_|-")[0].length()<=10 && !title.contains("密码") && !title.contains("登陆")){
									tmp = true;
								}
							}else{
								if(title.split("\\s+|\\|").length<=5 && !title.toLowerCase().contains("login") ){
									tmp = true;
								}
							}
						}
						if(tmp){
							if(json.getIntValue("textLength")>=SysConstants.nodeTextWords){
								logger.info("URL:"+url+" 不是频道!");
							}else{
								logger.info("URL:"+url+" 是List,添加");
								channelNameMap.put(url, title);
							}
						}else{
							logger.info("URL:"+url+" 不是频道!");
							if(channelNameMap.containsKey(url)){
								logger.info("URL:"+url+"从map中删除");
								channelNameMap.remove(url);
							}
						}
					}
					
				}else{
					logger.info("URL:"+url+"已处理过");
				}
				if(isContinuous){
					Elements links = document.select("[href]");// 比如a元素
					document.setBaseUri(url);
					logger.info("url---" + url + "--href有--" + links.size());
					for (Iterator<Element> iter = links.iterator(); iter.hasNext();) {
						Element e = iter.next();
						String channel_name = e.text();
						String href = e.absUrl("href");
						if (href == null)
							continue;
						if (href.contains("#")) {
							href = href.substring(0, href.indexOf("#"));
						}
						if(href==null||href.replaceAll("\\s+", "").isEmpty()){
							continue;
						}
						if(filterURL(href)||!URLUtils.isValidUrl(href)){
							logger.info("过滤无效URL: "+href);
							continue;
						}
						if(!URLUtils.getHost(href).equals(host)){
							logger.info("不是此站点URL: "+href);
							continue;
						}
						String tmpHref = href;
						if(tmpHref.endsWith("/")){
							tmpHref = tmpHref.substring(0,tmpHref.length()-1);
						}
						String[] arr = tmpHref.split("/");
						
						if(!arr[arr.length-1].contains("index")){
							if(languageCode!=null&&languageCode.contains("zh")){
								if(channel_name!=null&&!channel_name.isEmpty()){
									if(channel_name.split("_|-")[0].length()>10){
										logger.info("过滤超过10字符URL:"+href);
										continue;
									}
								}
//							}else if(la!=null&&la.contains("en")){ //英文URL过滤
							}else { //英文URL过滤
								String[] split = tmpHref.split("/");
								if(split[split.length-1].split("-").length>4){
									logger.info("过滤(-)超过5URL:"+href);
									continue;
								}
							}
							String res="(/([a-zA-Z_-]+)?([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12]"
									+ "[0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-8]))))(_)?([0-9a-zA-Z]+)?(.(html|shtml|htm|asp|php))?";
							Pattern p = Pattern.compile(res);
							Matcher m = p.matcher(href);
							if(m.find()){
								logger.info("正则过滤URL:"+href);
								continue;
							}
							res = "([2][0-9]{3}([_-])?([0-9]{1,2})([_/])?([0-9]{1,2})?)";
							p = Pattern.compile(res);
							m = p.matcher(href);
							if(m.find()){
								logger.info("正则过滤URL:"+href);
								continue;
							}
							
						}
						if (!urlList.contains(href)&&!childUrlList.contains(href)) {
							logger.info("URL:"+href+" 遍历完成,放入list");
							childUrlList.add(href);
							channelNameMap.put(href, channel_name);
						}
					}
				}else{
					JSONObject json = channelCompute.isList(document, url);
					if(!json.getBooleanValue("result")){
						logger.info("URL:"+url+"不是List,删除");
						if(channelNameMap!=null && channelNameMap.containsKey(url)){
							channelNameMap.remove(url);
						}
						if(childUrlList!=null && childUrlList.contains(url)){
							childUrlList.remove(url);
						}
					}
				}
				
			}else{
				logger.info(url + "Document为空");
			}
		} catch (Exception e) {
			urlError.add(url);
			logger.info(url + "下载失败");
			e.printStackTrace();
		}
	}
	public static List<String> core(List<String> templateURLList) {
		List<String> niceGoodList = new ArrayList<String>();
		// 获取第一个新闻作为匹配
		String tempUrl = templateURLList.get(0);
		niceGoodList.add(tempUrl);
		templateURLList.remove(0);
		String host = MyURLUtils.getFullHost(tempUrl);
		Iterator<String> iterator = templateURLList.iterator();
		while (iterator.hasNext()) {
			String next_tempUrl = iterator.next();

			// logger.info((indexI+1)+"/"+templateURLList.size());

			double stringSimilarValue = stringSimilar.getStringSimilar(tempUrl.replace(host, ""),
					next_tempUrl.replace(host, ""));
			if (1 - stringSimilarValue / tempUrl.length() > THRESHOLD) {
				niceGoodList.add(next_tempUrl);
				iterator.remove();
			}
		}
		return niceGoodList;
	}
	private Document downloadPage(String url, int isProxy) {
		try {
			Connection.Response res = null;
			Connection connect = Jsoup.connect(url);
			if (isProxy == 1) {
				java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
						new InetSocketAddress(proxyIP, port));
				connect.proxy(proxy);
			} else if (isProxy == 2) {
				java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
						new InetSocketAddress(proxyIP, port));
				connect.proxy(proxy);
			}
			res = connect.method(Method.GET).timeout(30 * 1000).followRedirects(true)
					.header("Content-Language", "en-US").header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3")
					.header("Accept-Encoding", "gzip, deflate")
					.userAgent(
							"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
					.ignoreContentType(true).execute();
			CharsetDetector detector = new CharsetDetector();
			detector.setText(res.bodyAsBytes());
			CharsetMatch match = detector.detect();
			String encoding = match.getName();
			ByteArrayInputStream bya = new ByteArrayInputStream(res.bodyAsBytes());
			Document parse = Jsoup.parse(bya, encoding, url);
			logger.info("URL:" + url + ", 使用Jsoup下载完成");
			return parse;
		} catch (Exception e) {
		}
		return null;
	}
	public String getLanguage(String str) {
		try{
			LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
			        .withProfiles(languageProfiles)
			        .build();
			TextObjectFactory textObjectFactory = CommonTextObjectFactories.forIndexingCleanText();
			
			com.google.common.base.Optional<LdLocale> lang = null;
			int count = 1;
			while(count <= 5) {
				try{
					TextObject textObject = textObjectFactory.forText(str);				
					lang = languageDetector.detect(textObject);
					
					textObject=null;
					languageDetector=null;
					textObjectFactory=null;
					
					if(lang.isPresent()) {
						String language_str = lang.get().getLanguage();
						return language_str;
					} else {
						return null;
					}
					
				} catch(Exception e) {
				}
				count++;
			}
			return null;
		}catch(Exception e) {
			return null;
		}
	}
}