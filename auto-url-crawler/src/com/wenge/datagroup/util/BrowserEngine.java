package com.wenge.datagroup.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wenge.datagroup.imp.CrawlerChannel;

public class BrowserEngine {
	private static final Logger logger = LoggerFactory.getLogger(CrawlerChannel.class);
	public String browserName;
	public String serverURL;
	public WebDriver driver;
	public String systemName;

	public void initConfigData() throws IOException {

		Properties p = new Properties();
		// 加载配置文件
		InputStream ips = new FileInputStream("config/config.properties");
		p.load(ips);

		logger.info("Start to select browser name from properties file");
		browserName = p.getProperty("browserName");
		systemName = p.getProperty("systemName");
		logger.info("Your had select test browser type is: " + browserName);
		serverURL = p.getProperty("URL");
		logger.info("The test server URL is: " + serverURL);
		ips.close();
	}
	private ChromeDriverService service ;

	public WebDriver getBrowser(String proxy,String url) {

		if (browserName.equalsIgnoreCase("Firefox")) {
			System.setProperty("webdriver.firefox.bin", "E:\\Life_Software\\Mozilla Firefox\\firefox.exe");
			System.setProperty("webdriver.gecko.driver", "Tools/geckodriver32-0.20.1.exe");
			driver = createFireFoxDriver();

			logger.info("Launching Firefox ...");

		} else if (browserName.equalsIgnoreCase("Chrome")) {
			if (systemName.equals("Linux")) {
				System.setProperty("webdriver.chrome.driver", "Tools/chromedriver_linux");
				service = new ChromeDriverService.Builder().build();
				try {
					service.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (systemName.equals("Windows")){
				System.setProperty("webdriver.chrome.driver", "Tools/chromedriver-2.40.exe");
				service = new ChromeDriverService.Builder().build();
				try {
					service.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			ChromeOptions chromeOptions = new ChromeOptions();
			// 设置为 headless 模式 （必须）
			
			chromeOptions.addArguments("--ignore-certificate-errors");
			
			Map<String, Object> prefs = new HashMap<String, Object>();
			//禁止加载图片
	        prefs.put("profile.managed_default_content_settings.images", 2);
	        chromeOptions.setExperimentalOption("prefs", prefs);
	        //使用无头模式
			chromeOptions.addArguments("--headless");
			chromeOptions.addArguments("--no-sandbox");
			// 设置浏览器窗口打开大小 （非必须）
//			chromeOptions.addArguments("--window-size=1920,1080");
//			chromeOptions.addArguments("--proxy-server=http://192.168.10.77:23480");
			if(proxy!=null&&!proxy.isEmpty()){
				logger.info("URL:"+url+" 使用代理访问:"+proxy);
				chromeOptions.addArguments(proxy);
			}
			 driver = new ChromeDriver(chromeOptions);

//			driver = new ChromeDriver();
			logger.info("Launching Chrome ...");

		} else if (browserName.equalsIgnoreCase("IE")) {

			System.setProperty("webdriver.ie.driver", ".\\Tools\\IEDriverServer.exe");
			driver = new InternetExplorerDriver();
			logger.info("Launching IE ...");
		}

		// driver.get(serverURL);
		// logger.info("Open URL: " + serverURL);
		// driver.manage().window().maximize();
		// logger.info("Maximize browser...");
		// callWait(5);
		return driver;
	}

	public void crawlerUrl(String url) {
		driver.get(serverURL);
		logger.info("Open URL: " + serverURL);
		driver.manage().window().maximize();
		logger.info("Maximize browser...");
		callWait(5);
	}

	/*
	 * 关闭浏览器并退出方法
	 */

	public void tearDown() throws InterruptedException {

		logger.info("Closing browser...");
		if(driver!=null){
			driver.quit();
		}
		if(service != null){
			// 关闭 ChromeDriver 接口
			service.stop();
		}
		Thread.sleep(3000);
	}

	/*
	 * 隐式时间等待方法
	 */
	public void callWait(int time) {

		driver.manage().timeouts().implicitlyWait(time, TimeUnit.SECONDS);
		logger.info("Wait for " + time + " seconds.");
	}

	/*
	 * createFireFox Driver
	 * 
	 * @Param: null
	 * 
	 * @return: WebDriver
	 */

	private WebDriver createFireFoxDriver() {

		WebDriver driver = null;
		FirefoxProfile firefoxProfile = new FirefoxProfile();

		firefoxProfile.setPreference("prefs.converted-to-utf8", true);
		// set download folder to default folder: TestDownload
		firefoxProfile.setPreference("browser.download.folderList", 2);
		firefoxProfile.setPreference("browser.download.dir", ".\\TestDownload");

		firefoxProfile.setPreference("network.proxy.type", 1);
		firefoxProfile.setPreference("network.proxy.http", "192.168.10.77");
		firefoxProfile.setPreference("network.proxy.http_port", 23480);
		firefoxProfile.setPreference("network.proxy.ssl", "192.168.10.77");
		firefoxProfile.setPreference("network.proxy.ssl_port", 23480);
		                             
		try {
			driver = new FirefoxDriver(firefoxProfile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.error("Failed to initilize the Firefox driver");
		}
		return driver;
	}
}
