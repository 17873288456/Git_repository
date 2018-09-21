package com.wenge.datagroup.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyURLUtils {
	public static boolean isValidUrl(String url) {
		String regEx = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-" + "Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
				+ "2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}" + "[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
				+ "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-" + "4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
				+ "-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/" + "[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$\\=~_\\-@]*)*$";

		return url.matches(regEx);
	}

	public static String getSecondHost(String url) {
		String regStr="[0-9a-zA-Z]+((\\.com)|(\\.cn)|(\\.org)|(\\.net)|(\\.edu)|(\\.com.cn)|(\\.xyz)|(\\.xin)|(\\.club)|(\\.shop)|(\\.site)|(\\.wang)" +
				"|(\\.top)|(\\.win)|(\\.online)|(\\.tech)|(\\.store)|(\\.bid)|(\\.cc)|(\\.ren)|(\\.lol)|(\\.pro)|(\\.red)|(\\.kim)|(\\.space)|(\\.link)|(\\.click)|(\\.news)|(\\.news)|(\\.ltd)|(\\.website)" +
				"|(\\.biz)|(\\.help)|(\\.mom)|(\\.work)|(\\.date)|(\\.loan)|(\\.mobi)|(\\.live)|(\\.studio)|(\\.info)|(\\.pics)|(\\.photo)|(\\.trade)|(\\.vc)|(\\.party)|(\\.game)|(\\.rocks)|(\\.band)" +
				"|(\\.gift)|(\\.wiki)|(\\.design)|(\\.software)|(\\.social)|(\\.lawyer)|(\\.engineer)|(\\.org)|(\\.net.cn)|(\\.org.cn)|(\\.gov.cn)|(\\.name)|(\\.tv)|(\\.me)|(\\.asia)|(\\.co)|(\\.press)|(\\.video)|(\\.market)" +
				"|(\\.games)|(\\.science)|(\\.中国)|(\\.公司)|(\\.网络)|(\\.pub)" +
				"|(\\.la)|(\\.auction)|(\\.email)|(\\.sex)|(\\.sexy)|(\\.one)|(\\.host)|(\\.rent)|(\\.fans)|(\\.cn.com)|(\\.life)|(\\.cool)|(\\.run)" +
				"|(\\.gold)|(\\.rip)|(\\.ceo)|(\\.sale)|(\\.hk)|(\\.io)|(\\.gg)|(\\.tm)|(\\.com.hk)|(\\.gs)|(\\.us))";
		Pattern p = Pattern.compile(regStr);  
		Matcher m = p.matcher(url); 
		String domain = "";
		//获取一级域名
		while(m.find()){
			domain = m.group();
		}
		return domain;
	}
	public static void main(String[] args) {
		System.out.println(getFullHost("http://www.xt.rednet.cnar.ae/"));
	}
	public static String getFullHost(String url) {
		if (url == null || url.trim().length() == 0) {
			System.out.println("[getHost] request params null");
			return null;
		}
		int doubleslash = url.indexOf("//");
		if (-1 == doubleslash) {
			doubleslash = -2;
		}
		if ((doubleslash + 2) > url.length()) {
			System.out.println("[getHost] error " + url);
			return null;
		}
		String temp = url.substring(doubleslash + 2);
		if (temp == null || temp.trim().length() == 0) {
			System.out.println("[getHost] error " + url);
			return null;
		}
		int firstsingleslash = temp.indexOf("/");
		if (-1 == firstsingleslash) {
			firstsingleslash = temp.length();
		}

		String result = null;
		if (firstsingleslash > 0) {
			result = temp.substring(0, firstsingleslash);
		}
		return result;
	}
}
