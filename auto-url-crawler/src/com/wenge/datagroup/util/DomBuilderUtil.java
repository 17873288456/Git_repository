package com.wenge.datagroup.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.xpath.XPathAPI;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.Connection.Method;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * @author Crunchify.com
 *
 */

public class DomBuilderUtil {

	private DocumentFragment domtree;

	/**
	 * 构建Dom树对象
	 * 
	 * @param html
	 *            网页内容字节数组
	 * @param charset
	 *            网页编码，如为空，则默认为utf8
	 * @return 网页Dom树对象
	 */
	public boolean domBuild(InputStream io, String charset) {

		if (io == null) {
			return false;
		}
		charset = (charset == null || "".equals(charset)) ? "utf-8" : charset;
		InputSource source = null;
		DOMFragmentParser parser = null;
		InputStreamReader isr = null;
		try {
			source = new InputSource();
			isr = new InputStreamReader(io, charset);
			source.setCharacterStream(isr);
			parser = new DOMFragmentParser();
			domtree = new HTMLDocumentImpl().createDocumentFragment();
			parser.parse(source, domtree);
			return true;
		} catch (Exception e) {
			domtree = null;
			e.printStackTrace();
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public String getNextPageURL(String html, String xpath, String baseURL) {
		
		String nextURL = null;
		boolean succ = domBuild(new ByteArrayInputStream(html.getBytes()), "UTF-8");
		Node selectNode = null;
		try {
			xpath = "//UL[@class='mainListPagination']/LI[last()-1]/A";
			selectNode = XPathAPI.selectSingleNode(domtree, xpath);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		if (selectNode != null) {
			NamedNodeMap attributes = selectNode.getAttributes();
			if (attributes != null && attributes.getLength() > 0) {
				Node namedItem = attributes.getNamedItem("href");
				if (namedItem != null)
					nextURL = namedItem.getNodeValue();
			}
		}
		if (nextURL != null) {
			if (nextURL.startsWith("./")) {
				nextURL = baseURL.substring(0, baseURL.lastIndexOf("/")) +nextURL.substring(1);
			} else if (nextURL.startsWith("//")) {
				nextURL = baseURL.substring(0, baseURL.indexOf("/")) + nextURL;
			} else if (nextURL.startsWith("/")) {
				if (null != StringUtils.extrator(baseURL, "^(?:https?://)?[\\w]{1,}(?:\\.?[\\w]{1,})+")) {
					nextURL = StringUtils.extrator(baseURL, "^(?:https?://)?[\\w]{1,}(?:\\.?[\\w]{1,})+")
							+ nextURL;
				}
			}else if (nextURL.startsWith("?")) {
				int indexOf = baseURL.indexOf("?");
				if(indexOf!=-1){
					nextURL = baseURL.substring(0,indexOf)+nextURL;
				}else{
					if(baseURL.endsWith("/")){
						baseURL = baseURL.substring(0,baseURL.length()-1);
					}
					nextURL = baseURL+nextURL;
				}
			}
		}
		return nextURL;
	}

	public static void main(String[] args) throws Exception {
		String url = "http://www.quanzhou.gov.cn/zfb/xxgk/zfxxgkzl/ghjh/fzgh/";
		String html = Jsoup.connect(url).get().html();
		Elements select = Jsoup.parse(html).select("#pages_pg_2 > table > tbody > tr > td:nth-child(2) > div > span:nth-child(6)");
		System.out.println(select);
		DomBuilderUtil domUtil = new DomBuilderUtil();
		boolean sucess = domUtil.domBuild(new ByteArrayInputStream(html.getBytes()), "UTF-8");
		if (!sucess) {
			System.out.println(sucess);
		}
		// //Li[last()]
		Node selectNode = XPathAPI.selectSingleNode(domUtil.domtree,"//DIV[@id='pages_pg_2']//SPAN[6]/A");
		System.out.println(selectNode);
//		System.out.println("selectNode"+selectNode.getAttributes().getNamedItem("class"));
//		System.out.println(selectNode.getChildNodes().getLength());
//		System.out.println(selectNode.getAttributes().getNamedItem("href").getNodeValue());
//		NamedNodeMap attributes = selectNode.getAttributes();
//		for (int i = 0; i < attributes.getLength(); i++) {
//
//			Node item = attributes.item(i);
//			System.out.println(item);
//		}

	}

	private static String downloadPage(String url, int isProxy) {
		try {
			Connection.Response res = null;
			Connection connect = Jsoup.connect(url);
			if (isProxy == 1) {
				java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
						new InetSocketAddress("127.0.0.1", 0));
				connect.proxy(proxy);
			} else if (isProxy == 2) {
				java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
						new InetSocketAddress("127.0.0.1", 0));
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
			org.jsoup.nodes.Document parse = Jsoup.parse(bya, encoding, url);
			return parse.html();
		} catch (Exception e) {
		}
		return null;
	}
}