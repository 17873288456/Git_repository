package com.wenge.datagroup.util;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopLoadPage implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(StopLoadPage.class);
	WebDriver driver = null;
	public boolean isLoad;
	int sec = 0;
	String url;

	public StopLoadPage(WebDriver driver, String url, int sec) {
		this.driver = driver;
		this.sec = sec;
		this.url = url;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(sec * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!isLoad) {
			logger.warn("URL：" + url + "超时,停止加载");
			// ((JavascriptExecutor) driver).executeScript("window.Stop();");
//			Actions action = new Actions(driver);
//			action.sendKeys(Keys.ESCAPE).build().perform();
			try {
				Robot robot = new Robot();
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(!isLoad){
				logger.warn("URL:"+url+" 超时, 刷新页面");
				driver.navigate().refresh();
				isLoad = true;
			}
		}

	}
}