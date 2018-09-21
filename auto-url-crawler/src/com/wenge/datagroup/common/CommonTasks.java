package com.wenge.datagroup.common;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.common.base.Function;

public class CommonTasks {
	WebDriver driver;

	/**
	 * This method is for waiting until page is ready if document.readyState =
	 * complete?
	 *
	 * @param driver
	 */
	public static void waitForPageLoad(WebDriver driver, long timeOut) {
		Function<WebDriver, Boolean> waitFn = new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, timeOut);
		wait.until(waitFn);
	}

}
