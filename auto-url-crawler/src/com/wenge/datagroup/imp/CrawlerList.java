package com.wenge.datagroup.imp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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
import com.wenge.datagroup.common.CommonTasks;
import com.wenge.datagroup.common.InitContext;
import com.wenge.datagroup.common.SysConstants;
import com.wenge.datagroup.main.CrawlerRmiClient;
import com.wenge.datagroup.model.CrawlerDetailUrl;
import com.wenge.datagroup.model.CrawlerListUrl;
import com.wenge.datagroup.util.BrowserEngine;
import com.wenge.datagroup.util.DomBuilderUtil;
import com.wenge.datagroup.util.MyURLUtils;
import com.wenge.datagroup.util.Proxy;
import com.wenge.datagroup.util.StringSimilar;
import com.wenge.datagroup.util.TimerUtil;
import com.wenge.datagroup.util.URLUtils;


public class CrawlerList implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(CrawlerList.class);
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final Calendar myDate = Calendar.getInstance();
	private static final double THRESHOLD = 0.4;
	private static StringSimilar stringSimilar = new StringSimilar();
	private List<LanguageProfile> languageProfiles ;
	private WebDriver driver;
	private BrowserEngine browserEngine;
	private CrawlerListUrl crawlerListUrl;
	private String proxy;
	private int port;
	private String proxyIP;
	private int PAGENUM = SysConstants.PAGENUM_AFTER;

	public CrawlerList(CrawlerListUrl crawlerListUrl, String proxyIP, int port) {
		this.crawlerListUrl = crawlerListUrl;
		this.port = port;
		if (proxyIP == null || proxyIP.isEmpty()) {
			this.proxyIP = "192.168.6.1";
			this.port = 1984;
		} else {
			this.proxyIP = proxyIP;
		}
		try {
			languageProfiles = new LanguageProfileReader().readAllBuiltIn();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (this.crawlerListUrl.getStatus().intValue() == 2) {
			PAGENUM = SysConstants.PAGENUM_BEFORE;
		}
	}

	@Override
	public void run() {
		startTask(crawlerListUrl.getIsRender() == 1);
	}

	private void initBrowser() {
		Integer isProxy = crawlerListUrl.getIsProxy();
		if (isProxy == 1) {
			// 境内代理

		} else if (isProxy == 2) {
			// 境外代理
			if (port > 0) {
				proxy = "--proxy-server=http://" + proxyIP + ":" + port;
			}
		}
		try {
			browserEngine = new BrowserEngine();
			browserEngine.initConfigData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		driver = browserEngine.getBrowser(proxy, crawlerListUrl.getListUrl());
	}


	@SuppressWarnings("deprecation")
	public void startTask(boolean isRender) {
		String language = null;
		try {
			if (isRender) {
				initBrowser();
			}
			try {
				int pageNum = 0;
				String url = crawlerListUrl.getListUrl();
				boolean hasNext = true;
				boolean isContinue = true;
				String nextPageURL = url;
				boolean isNext = true;
//				int rep_page_count = 0;
				while (isContinue || nextPageURL != null) {
					if (!isNext || pageNum >= PAGENUM) {
						logger.info("当前是第" + pageNum + "页,不再采集下一页");
						break;
					}
					pageNum++;
					logger.info("当前是第" + pageNum + "页");
					String host = URLUtils.getHost(url);
					int count = 0;
					Document document = null;
					Map<String, String> templateURLMap = new HashMap<>();
					List<String> templateURLList = new ArrayList<String>();
					while (count < 3) {
						if (browserEngine != null) {
							if (!hasNext) {
								nextPageURL = driver.getCurrentUrl();
								logger.info("CurrentUrl:" + nextPageURL);
							}
							logger.info("URL:" + nextPageURL + ", 使用Selenium解析");
							try {
								if (hasNext) {
									try {
										logger.info("开始下载---" + nextPageURL);
										driver.get(nextPageURL);
										CommonTasks.waitForPageLoad(driver, 20);
										// driver.manage().timeouts().implicitlyWait(30,
										// TimeUnit.SECONDS);
										logger.info("url---" + nextPageURL + "加载完成");
										driver.manage().window().maximize();
									} catch (UnhandledAlertException e) {
										// TODO: handle exception
										driver.switchTo().alert().accept();
										logger.error("进入UnhandledAlertException异常");
									}

									// 下一页
									Integer ruleId = crawlerListUrl.getRuleId();
									switch (ruleId) {
									case 1:
										// 鼠标翻滚,判断什么时候到底
										Long firstHeight = 0L;
										Long secondHeight = 0L;
										int heightEqualCount = 0;
										int scrollCount = 0;
										while (heightEqualCount < 3) {
											if (scrollCount >= PAGENUM * 5) {
												logger.info("鼠标翻滚" + scrollCount + "次,不再翻滚下一页");
												break;
											}
											firstHeight = (Long) ((JavascriptExecutor) driver)
													.executeScript("return document.documentElement.scrollTop");
											logger.info("翻滚前高度:" + firstHeight);
											((JavascriptExecutor) driver).executeScript(
													"document.documentElement.scrollTop=" + (firstHeight + 1000)); // 将页面滚动条拖到底部
											// ((JavascriptExecutor)
											// driver).executeScript("window.scrollTo(0,
											// "+(firstHeight+10000)+")"); //
											// 将页面滚动条拖到底部
											logger.info("鼠标翻滚,第" + (scrollCount + 1) + "页---" + nextPageURL);
											Thread.sleep(2000);
											secondHeight = (Long) ((JavascriptExecutor) driver)
													.executeScript("return document.documentElement.scrollTop");
											if (firstHeight.equals(secondHeight)) {
												if (firstHeight.equals(secondHeight)) {
													int countF = 3;
													while (countF > 0) {
														((JavascriptExecutor) driver)
																.executeScript("document.documentElement.scrollTop="
																		+ (secondHeight / 3));
														Thread.sleep(1000);
														((JavascriptExecutor) driver).executeScript(
																"document.documentElement.scrollTop=" + (secondHeight));
														Thread.sleep(1000);
														countF--;
													}
													secondHeight = (Long) ((JavascriptExecutor) driver)
															.executeScript("return document.documentElement.scrollTop");
													if (firstHeight.equals(secondHeight)) {
														heightEqualCount++;
														logger.warn("第" + heightEqualCount + "次滚动条拖动高度恒定");
													}
												}
											}
											scrollCount++;
										}
										isNext = false;
										break;
									case 3:
										// 点击
										String clickXpath = crawlerListUrl.getClickXpath();
										if (clickXpath != null) {
											if (clickXpath.contains("\"")) {
												clickXpath = clickXpath.replaceAll("\"", "'");
											}
											WebElement nextEle = null;
											try {
												nextEle = driver.findElement(By.xpath(clickXpath));
											} catch (NoSuchElementException e) {
												logger.error("下一页已不存在");
												nextEle = null;
											}
											int clickCount = 0;
											while (null != nextEle) {
												if (clickCount >= PAGENUM) {
													logger.info("鼠标点击" + PAGENUM + "次,不再点击下一页");
													break;
												}
												if (nextEle.getAttribute("style").contains("none")
														|| nextEle.getAttribute("style").contains("hidden")
														|| nextEle.getText().contains("收起")
														|| nextEle.getText().contains("首页")) {
													break;
												}
												try{
													nextEle.click();
													logger.info("点击下一页---" + nextPageURL);
												}catch(Exception e){
													e.printStackTrace();
													((JavascriptExecutor)driver).executeScript("arguments[0].click();", nextEle);
													logger.info("Method2 点击下一页---" + nextPageURL);
												}
												Thread.sleep(5000);
												try {
													nextEle = driver.findElement(By.xpath(clickXpath));
												} catch (NoSuchElementException e) {
													logger.error("下一页已不存在");
													nextEle = null;
												}
												clickCount++;
											}
										}
										isNext = false;
										break;
									default:
										break;
									}
								}
								String pageSource = driver.getPageSource();
								document = Jsoup.parse(pageSource);
							} catch (Exception e) {
								logger.info("url--" + nextPageURL + "下载失败", e);
								count++;
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
								driver = browserEngine.getBrowser(proxy, nextPageURL);
								Thread.sleep(1000L);
							}
						} else {
							logger.info("URL:" + nextPageURL + ", 使用Jsoup解析");
							Integer isProxy = crawlerListUrl.getIsProxy();
							document = downloadPage(nextPageURL, isProxy);
						}
						if (document != null && null != document.getElementsByTag("body")
								&& !document.getElementsByTag("body").text().trim().equals("")) {
							logger.info("URL:" + nextPageURL + ",解析成功!");
							document.select(
									"script,pre,noscript,style,iframe,*[class*=footer],*[id*=footer],*[style*=display:none]")
									.remove();
							if(document.select("iframe")!=null || document.select("iframe").size()>0 ){
								WebElement findElement = driver.findElement(By.id("left_iframe"));
								if(findElement!=null){
									driver.switchTo().frame(findElement);
								}
								String pageSource = driver.getPageSource();
								document = Jsoup.parse(pageSource);
							}
							break;
						}
						count++;
					}
					if (document != null) {
						if(language==null){
							language = getLanguage(document.text());
						}
						Elements select = document.select("a");
						document.setBaseUri(nextPageURL);
						logger.info("URL:" + nextPageURL + " 获取到href: " + select.size());
						for (Element element : select) {
							String text = element.text();
							boolean isValidURL = false;
							if(language!=null&&language.contains("zh")){
								if (text.length() >=4) {
									// 有效URL
									isValidURL = true;
								}
							} else {
								text = text.replaceAll("-", " ");
								if (text.split(" ").length >=3) {
									// 有效URL
									isValidURL = true;
								}
							}
							if (isValidURL) {
								String title = element.text();
								String href = element.absUrl("href");
								if (href.contains("#")) {
									href = href.substring(0, href.indexOf("#"));
								}
								href = href.trim();
								String hrefTmp = href;
								if (href.endsWith("/")) {
									hrefTmp = href.substring(0, href.length() - 1);
								}
								if (!URLUtils.isValidUrl(hrefTmp)) {
									logger.info("无效URL:" + href);
									continue;
								}
								if(href.endsWith("index")){
									logger.info("无效URL:" + href);
									continue;
								}
								if (!URLUtils.getHost(href).equals(host)) {
									logger.info("非此网站:" + href);
									continue;
								}
								if (templateURLList.contains(href)) {
									logger.info("已添加过:" + href);
									continue;
								}
								logger.info("采集到新闻URL:" + href);
								templateURLMap.put(href, title);
								templateURLList.add(href);
							}
						}

						JSONArray jsonArray = new JSONArray();
						// 核心匹配
						logger.info("开始分簇···");
						int totalLinks = 0;
						int categaryIndex = 0;
						while (templateURLList.size() > 0) {
							List<String> core = core(templateURLList);
							if (core != null && core.size() > 0) {
								JSONObject json = new JSONObject();
								JSONArray parseArray = JSONArray.parseArray(JSON.toJSONString(core));
								int size = parseArray.size();
//								if (size >= 2) {
									totalLinks += size;
//								}
								json.put("" + categaryIndex++, parseArray);
								jsonArray.add(json);
							}
						}
//						boolean isNext2 = true;
						logger.info("分簇后arr数据:" + jsonArray.size() + ", totalLinks:" + totalLinks);
						for (Object json : jsonArray) {
							JSONObject obj = (JSONObject) json;
							Set<String> keySet = obj.keySet();
//							if (!isNext2) {
//								logger.info("已经连续三分之一重复数据,不在处理,break");
//								break;
//							}
							for (String string : keySet) {
								int urlSize = obj.getJSONArray(string).size();
//								if (urlSize >= 2) {
									JSONArray listArray = obj.getJSONArray(string);
									int isExistCount = 0;
//									if (!isNext2) {
//										logger.info("已经连续三分之一(" + isExistCount + ")条重复数据,不在处理,break");
//										break;
//									}
									for (Object object : listArray) {
										try {
											// 连续二分之一已存在,则不再处理
//											if (isExistCount >= totalLinks / 3) {
//												logger.info("已经连续三分之一(" + isExistCount + ")条重复数据,不在处理,最后一条url为--"
//														+ object.toString());
//												isNext2 = false;
//												rep_page_count++;
//												break;
//											}
											CrawlerDetailUrl crawlerDetailUrl = new CrawlerDetailUrl();
											crawlerDetailUrl.setClusterId(Integer.valueOf(string));
											crawlerDetailUrl.setDetailUrl(object.toString());
											crawlerDetailUrl.setTitle(templateURLMap.get(object.toString()));
											crawlerDetailUrl.setPageNum(pageNum);
											crawlerDetailUrl.setInsertTime(new Date());
											crawlerDetailUrl.setStatus(2);
											crawlerDetailUrl.setListUrlId(crawlerListUrl.getId());
											List<CrawlerDetailUrl> find = CrawlerDetailUrl.dao
													.find("select * from crawler_detail_url where detail_url = '"
															+ object.toString() + "'" + " or detail_url = '"
															+ object.toString().replace("http", "https") + "'"
															+ " or detail_url = '"
															+ object.toString().replace("https", "http") + "'"
															+ " or detail_url = '"
															+ object.toString().replace("https", "http") + "/'"
															+ " or detail_url = '"
															+ object.toString().replace("http", "https") + "/'");
											if (find != null && find.size() > 0) {
												logger.info("已存在---detail_url = " + object.toString()
														+ "---list_url_id = " + crawlerListUrl.getId());
												isExistCount++;
											} else {
												boolean save = crawlerDetailUrl.save();
												if (save) {
													logger.info("SUCCESS---detail_url = " + object.toString()
															+ " and list_url_id = " + crawlerListUrl.getId());
													isExistCount = 0;
												}
											}
										} catch (Exception e) {
											logger.error("detail_url = " + object.toString() + " and list_url_id = "
													+ crawlerListUrl.getId() + "保存数据库error", e);
											continue;
										}
									}
//								} else {
//									logger.info("同一个簇URL小于6条,跳过:" + obj.getJSONArray(string));
//								}
							}
						}
//						if (isNext2) {
//							rep_page_count = 0;
//						}
//						if (rep_page_count >= 3) {
//							logger.info("已经连续3页三分之一数据是重复数据,不在处理,break");
//							isNext = false;
//						}
						// 下一页
						if (isNext) {
							Integer ruleId = crawlerListUrl.getRuleId();
							switch (ruleId) {
							case 0:
								// 没有下一页
								isContinue = false;
								hasNext = false;
								nextPageURL = null;
								break;
							case 2:
								// xpath
								logger.info("下一页xpath---" + nextPageURL);
								String nextPageXpath = crawlerListUrl.getNextPageXpath();
								if (nextPageXpath != null) {
									if (nextPageXpath.contains("\"")) {
										nextPageXpath = nextPageXpath.replaceAll("\"", "'");
									}
//									nextPageXpath = nextPageXpath.toLowerCase();
									if (isRender) {
										WebElement nextEle = null;
										try {
											nextEle = driver.findElement(By.xpath(nextPageXpath));
											logger.info("点击翻页  " + nextPageURL);
											nextEle.click();
											hasNext = false;
											Thread.sleep(5000);
										} catch (WebDriverException e) {
											e.printStackTrace();
											try{
												((JavascriptExecutor)driver).executeScript("arguments[0].click();", nextEle);
												logger.info("Method2 点击下一页---" + nextPageURL);
											}catch(Exception e2){
												e2.printStackTrace();
												logger.error("下一页翻页未获取到");
												isContinue = false;
												hasNext = false;
												nextEle = null;
												nextPageURL = null;
											}
										}
									} else {
										// Jsoup翻页
										DomBuilderUtil domUtil = new DomBuilderUtil();
										nextPageURL = domUtil.getNextPageURL(document.html(), nextPageXpath, url);
										if(nextPageURL==null || nextPageURL.contains("javascript") || nextPageURL.equals("#")){
											nextPageURL = null;
											isContinue = false;
											logger.info(nextPageURL + "未获取到下一页元素");
										}
									}
								} else {
									logger.info(nextPageURL + " 未获取到下一页Xpath");
								}
								break;
							case 4:// 规则翻页
								String nextPageRule = crawlerListUrl.getNextPageRule();
								if (nextPageRule != null) {
									myDate.setTime(new Date());
									int indexOf = nextPageRule.indexOf("[");
									String page1 = nextPageRule;
									if (indexOf != -1) {
										page1 = nextPageRule.substring(0, indexOf)
												+ nextPageRule.substring(nextPageRule.lastIndexOf("]") + 1);
									}
									page1 = replaceRule(page1);
									nextPageRule = nextPageRule.replaceAll("\\[", "").replaceAll("\\]", "");
									if (pageNum == 1) {
										nextPageURL = page1;
									} else {
										nextPageURL = replaceRule(nextPageRule).replaceAll("<p>", pageNum + "");
									}
								} else {
									nextPageURL = null;
								}
								break;
							default:
								break;
							}
						}
					} else {
						logger.error("URL:" + nextPageURL + " 解析失败!");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			crawlerListUrl.setStatus(3);
			crawlerListUrl.setLastCrawlerTime(new Date());
			boolean update = crawlerListUrl.update();
			if (update) {
				logger.info("更新List:" + crawlerListUrl.getListUrl() + "成功");
			} else {
				logger.info("更新List:" + crawlerListUrl.getListUrl() + "失败");
			}
			CrawlerRmiClient.rmiService.updateCursorList(crawlerListUrl.getId() + "");
			logger.info("URL:" + crawlerListUrl.getListUrl() + " 采集完成!");
			TimerUtil.sleep_second(3);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (browserEngine != null) {
					browserEngine.tearDown();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 根据Unicode编码完美的判断中文汉字和符号
	private boolean isChinese(String str) {
		char[] cs = str.toCharArray();
		for (char c : cs) {
			Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
			if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
					|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
					|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
					|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
					|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
				return true;
			}
		}
		return false;
	}

	private String replaceRule(String page1) {
		if (page1 == null)
			return page1;
		if (page1.contains("#yyyy") && page1.contains("#MM") && page1.contains("#dd")) {
			page1 = page1.replaceAll("#yyyy", String.valueOf(myDate.get(Calendar.YEAR)));
			page1 = page1.replaceAll("#MM", String.valueOf(myDate.get(Calendar.MONTH) + 1));
			page1 = page1.replaceAll("#dd", String.valueOf(myDate.get(Calendar.DAY_OF_MONTH)));
		}
		if (page1.contains("#yyyy") && page1.contains("#MM") && !page1.contains("#dd")) {
			page1 = page1.replaceAll("#yyyy", String.valueOf(myDate.get(Calendar.YEAR)));
			page1 = page1.replaceAll("#MM", String.valueOf(myDate.get(Calendar.MONTH) + 1));
		} 
		if (page1.contains("#yyyy") && !page1.contains("#M") && !page1.contains("#d")) {
			page1 = page1.replaceAll("#yyyy", String.valueOf(myDate.get(Calendar.YEAR)));
		}
		if (page1.contains("#yyyy") && page1.contains("#M") && !page1.contains("#d")) {
			page1 = page1.replaceAll("#yyyy", String.valueOf(myDate.get(Calendar.YEAR)));
			page1 = page1.replaceAll("#M", String.valueOf(myDate.get(Calendar.MONTH) + 1));
		} 
		if (page1.contains("#yyyy") && page1.contains("#M") && page1.contains("#d")) {
			page1 = page1.replaceAll("#yyyy", String.valueOf(myDate.get(Calendar.YEAR)));
			page1 = page1.replaceAll("#M", String.valueOf(myDate.get(Calendar.MONTH) + 1));
			page1 = page1.replaceAll("#d", String.valueOf(myDate.get(Calendar.DAY_OF_MONTH)));
		} 
		return page1;
	}

	public List<String> core(List<String> templateURLList) {
		List<String> niceGoodList = new ArrayList<String>();
		// 获取第一个新闻作为匹配
		String tempUrl = templateURLList.get(0);
		niceGoodList.add(tempUrl);
		templateURLList.remove(0);
		String host = MyURLUtils.getFullHost(tempUrl);
		Iterator<String> iterator = templateURLList.iterator();
		while (iterator.hasNext()) {
			String next_tempUrl = iterator.next();

			// System.out.println((indexI+1)+"/"+templateURLList.size());

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
	private Set<String> listSet = new HashSet<>();
	private void loadAllChannel(){
		Integer homeUrlId = crawlerListUrl.getHomeUrlId();
		List<CrawlerListUrl> find = CrawlerListUrl.dao.find("select list_url from crawler_list_url where status!=0 and home_url_id="+homeUrlId);
		for(CrawlerListUrl list:find){
			listSet.add(list.getListUrl());
		}
	}
}