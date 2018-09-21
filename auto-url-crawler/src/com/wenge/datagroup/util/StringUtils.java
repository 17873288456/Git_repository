package com.wenge.datagroup.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {
	private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

	public static boolean isNotEmpty(String str) {
		return (str != null) && (str.length() > 0);
	}

	public static boolean isEmpty(String str) {
		return (str == null) || (str.length() == 0);
	}

	public static String escapeSolrQueryChars(String input) {
		StringBuffer sb = new StringBuffer();

		String regex = "[+\\-&|!(){}\\[\\]^\"~?:(\\)]";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			matcher.appendReplacement(sb, "\\\\" + matcher.group());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 用正则抽取
	 * 
	 * @author QianHang
	 * @date 2018年4月4日
	 */
	public static String extrator(String str, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			sb.append(m.group().trim());
		}
		return sb.toString();
	}

	public static String handelSplit(String string) {
		if (string.contains(":")) {
			String[] split = string.split(":");
			if (split.length > 1) {
				string = "";
				for (int i = 1; i < split.length; i++)
					string = string + split[i];
			} else {
				string = "";
			}
		}
		if (string.contains("：")) {
			String[] split = string.split("：");
			if (split.length > 1) {
				string = "";
				for (int i = 1; i < split.length; i++)
					string = string + split[i];
			} else {
				string = "";
			}
		}
		return string;
	}

	public static String jsoupParse(String title) {
		return trimAll(Jsoup.parse(title).text());
	}

	public static String trimAll(String str) {
		char[] value = str.toCharArray();
		int len = value.length;
		if (len > 0) {
			int st = 0;
			char[] val = value;

			while ((st >= len) && (val[st] == ' ') || (val[st] == ' ') || (val[st] == '　')) {
				st++;
			}

			while ((st < len) && ((val[(len - 1)] == ' ') || (val[(len - 1)] == ' ') || (val[(len - 1)] == '　'))) {
				len--;
			}
			return (st > 0) || (len < value.length) ? str.substring(st, len) : str;
		}
		return "";
	}

	/**
	 * 匹配起始和结束位置之间的内容
	 * 
	 * @param content
	 * @param start
	 * @param end
	 * @return
	 */
	public static String regMatcher(String content, String start, String end) {
		return regMatcher(content, start, end, true);
	}

	/**
	 * 匹配起始和结束位置之间的内容，是否贪婪匹配，默认为否
	 * 
	 * @param content
	 * @param start
	 * @param end
	 * @param is
	 * @return
	 */
	public static String regMatcher(String content, String start, String end, boolean is) {
		String mat = null;
		if (is) {
			mat = start + "([\\s\\S]+?)(\\s)?" + end;
		} else {
			mat = start + "([\\s\\S]+)(\\s)?" + end;
		}
		Pattern p = Pattern.compile(mat);
		Matcher m = p.matcher(content);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	/**
	 * 匹配起始和结束位置之间的内容，是否贪婪匹配，默认为否
	 * 
	 * @param content
	 * @param start
	 * @param is
	 * @return
	 */
	public static String regMatcher(String content, String start, boolean is) {
		String mat = null;
		if (is) {
			mat = start + "([\\s\\S]+?)(\\s)?";
		} else {
			mat = start + "([\\s\\S]+)(\\s)?";
		}
		Pattern p = Pattern.compile(mat);
		Matcher m = p.matcher(content);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	/**
	 * 从url中获取参数值
	 * 
	 * @author QianHang
	 * @date 2018年4月4日
	 */
	public static String getParamFromUrl(String url, String param) {
		try {
			
		if (url.contains(param + "=")) {
			String[] split = url.split(param + "=");
			if (split.length > 1) {
				String[] split2 = split[1].split("&");
				return split2[0];
			}
		}
		} catch (Exception e) {
			logger.error("getParamFromUrl has error",e);
		}
		return null;
	}

	public static void main(String[] args) {
		String extrator = StringUtils.extrator("http://www.sinmeng.com/html/201807/10/1030809.html", "^(?:https?://)?[\\w]{1,}(?:\\.?[\\w]{1,})+");
		System.out.println(extrator);
	}

}