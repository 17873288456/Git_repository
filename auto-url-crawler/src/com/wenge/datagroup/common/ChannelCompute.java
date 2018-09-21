package com.wenge.datagroup.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;


public class ChannelCompute {
	
	class CountInfo {
        int textCount = 0;
        int linkTextCount = 0;
        int tagCount = 0;
        int linkTagCount = 0;
        double density = 0;
        double densitySum = 0;
        double score = 0;
        int pCount = 0;
        ArrayList<Integer> leafList = new ArrayList<Integer>();

    }
	public static void main(String[] args) throws IOException {
		String URL = "https://ironna.jp/article/627";
//		URL = "http://www.xinhua.jp/category/%E7%A6%8F%E5%B3%B6%E7%9C%8C/";
//		URL = "http://news.163.com/18/0806/08/DOGUQLBR0001899N.html";
//		URL = "http://www.national-assembly.org.kh";
//		URL = "http://www.cts.com.tw/";
//		URL = "http://www.focac.org/fra/zjfz_2/ssxw/t1581615.htm";
		
//		URL = "http://www.sahafah24.net/link69.html";
//		URL = "http://elinformadorweb.com.mx/2018/08/page/4/";
//		URL = "https://xx.rednet.cn/content/2018/08/06/316918.html";
//		URL = "https://xx.rednet.cn/channel/270.html";
//		URL = "https://yz.rednet.cn/content/2018/08/05/316830.html";
//		URL = "http://elinformadorweb.com.mx/category/cultura-y-entre/page/10/";
//		URL = "http://elinformadorweb.com.mx/comments/feed/";
//		URL="http://elinformadorweb.com.mx/china-jiangsu-loto/";
//		URL="http://elinformadorweb.com.mx/economia/";
		URL="https://www.toutiao.com/ch/news_hot/";
		Document document = Jsoup.connect(URL).ignoreContentType(true).proxy("192.168.6.1", 1984).get();
//		String html = FileUtils.readFileToString(new File("my.html"), "UTF-8");
//		Document document = Jsoup.parse(html);
		document.select("script,noscript,style,iframe,*[class*=footer],*[id*=footer],*[style*=display:none]").remove();
		ChannelCompute c = new ChannelCompute();
		c.isList(document, URL);
		
	}
	public  JSONObject isList(Document document, String URL){
		HashMap<Element, CountInfo> infoMap = new HashMap<Element, CountInfo>();
		try{
			JSONObject json = new JSONObject();
			boolean result = false;
			Element node=null;
			try {
				node = getContentElement(document,infoMap);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
//		
			if(node !=null && (node instanceof Element)){
//				System.out.println("文本密集区域 class: "+node.attr("class"));
//				System.out.println("文本密集区域 name: "+node.attr("name"));
//				System.out.println("文本密集区域 id: "+node.attr("id"));
//				System.out.println("---------------------");
				int textWords = 0;
				Element e = (Element) node;
				textWords = e.text().replaceAll("\\s+", "").length();
				System.out.println("文本: "+textWords);
				json.put("class", node.attr("class"));
				json.put("name", node.attr("name"));
				json.put("id", node.attr("id"));
				json.put("textLength", textWords);
				
				if(textWords < SysConstants.nodeTextWords){
					if(e.hasParent()){
						Element parent = e.parent();
						if(parent.hasParent()){
							Element parent2 = parent.parent();
							if(getLinks(parent2)>=SysConstants.linksCount){
								result = true;
								System.out.println(URL+" 此URL可能为列表页");
							}else{
								System.out.println(URL+" 此URL可能为新闻页");
							}
						}else{
							if(getLinks(parent)>=SysConstants.linksCount+10){
								result = true;
								System.out.println(URL+" 此URL可能为列表页");
							}else{
								System.out.println(URL+" 此URL可能为新闻页");
							}
							
						}
					}else{
						if(getLinks(document) <= SysConstants.linksCount){
							System.out.println(URL+" 此URL可能为新闻页");
						}else{
							result = true;
							System.out.println(URL+" 此URL可能为列表页");
						}
					}
					/*if(getLinks(document) <= SysConstants.linksCount){
						System.out.println(URL+" 此URL可能为新闻页");
					}else{
						result = true;
						System.out.println(URL+" 此URL可能为列表页");
					}*/
				}else{
					if(getLinks(node) <= SysConstants.linksCount){
						System.out.println(URL+" 此URL可能为新闻页");
					}else{
						result = true;
						System.out.println(URL+" 此URL可能为列表页");
					}
				}
				
			}else{
				Element body = document.body();
				if(body!=null && body.childNodeSize()>1){
					result = true;
					System.out.println(URL+" 未找到文本密集区! 此URL可能为列表页");
				}else{
					System.out.println(URL+" 未找到文本密集区! 此URL可能为新闻页");
				}
			}
			json.put("result", result);
			return json;
		}finally{
			infoMap.clear();
			infoMap = null;
		}
	}
	private  int getLinks(Node node){
		int count=0;
		if(node == null){
			return count;
		}
		if(node instanceof Comment) {
			return count;
		}
		if(node instanceof Element) {
			Element e = (Element) node;
			Elements select = e.select("[href]");
			for(Element u:select){
				String attr = u.attr("href");
				if(attr==null||attr.isEmpty()||attr.equalsIgnoreCase("#")||attr.contains("javascript")
						||attr.endsWith(".js")||attr.endsWith(".css")){
					continue;
				}
				count++;
			}
		}
		System.out.println("links:"+count);
		return count;
	}
	public  Element getContentElement(Document doc,HashMap<Element, CountInfo> infoMap) throws Exception {
        computeInfo(doc.body(), infoMap);
        double maxScore = 0;
        Element content = null;
        for (Map.Entry<Element, CountInfo> entry : infoMap.entrySet()) {
            Element tag = entry.getKey();
            if (tag.tagName().equals("a") || tag == doc.body()) {
                continue;
            }
            double score = computeScore(tag, infoMap);
            if (score > maxScore) {
                maxScore = score;
                content = tag;
            }
        }
        return content;
    }
	protected  CountInfo computeInfo(Node node, HashMap<Element, CountInfo> infoMap) {
        if (node instanceof Element) {
            Element tag = (Element) node;

            CountInfo countInfo = new CountInfo();
            for (Node childNode : tag.childNodes()) {
                CountInfo childCountInfo = computeInfo(childNode, infoMap);
                countInfo.textCount += childCountInfo.textCount;
                countInfo.linkTextCount += childCountInfo.linkTextCount;
                countInfo.tagCount += childCountInfo.tagCount;
                countInfo.linkTagCount += childCountInfo.linkTagCount;
                countInfo.leafList.addAll(childCountInfo.leafList);
                countInfo.densitySum += childCountInfo.density;
                countInfo.pCount += childCountInfo.pCount;
            }
            countInfo.tagCount++;
            String tagName = tag.tagName();
            if (tagName.equals("a")) {
                countInfo.linkTextCount = countInfo.textCount;
                countInfo.linkTagCount++;
            } else if (tagName.equals("p")) {
                countInfo.pCount++;
            }

            int pureLen = countInfo.textCount - countInfo.linkTextCount;
            int len = countInfo.tagCount - countInfo.linkTagCount;
            if (pureLen == 0 || len == 0) {
                countInfo.density = 0;
            } else {
                countInfo.density = (pureLen + 0.0) / len;
            }

            infoMap.put(tag, countInfo);

            return countInfo;
        } else if (node instanceof TextNode) {
            TextNode tn = (TextNode) node;
            CountInfo countInfo = new CountInfo();
            String text = tn.text();
            int len = text.length();
            countInfo.textCount = len;
            countInfo.leafList.add(len);
            return countInfo;
        } else {
            return new CountInfo();
        }
    }
	protected  double computeScore(Element tag, HashMap<Element, CountInfo> infoMap) {
        CountInfo countInfo = infoMap.get(tag);
        double var = Math.sqrt(computeVar(countInfo.leafList) + 1);
        double score = Math.log(var) * countInfo.densitySum * Math.log(countInfo.textCount - countInfo.linkTextCount + 1) * Math.log10(countInfo.pCount + 2);
        return score;
    }
	protected  double computeVar(ArrayList<Integer> data) {
        if (data.size() == 0) {
            return 0;
        }
        if (data.size() == 1) {
            return data.get(0) / 2;
        }
        double sum = 0;
        for (Integer i : data) {
            sum += i;
        }
        double ave = sum / data.size();
        sum = 0;
        for (Integer i : data) {
            sum += (i - ave) * (i - ave);
        }
        sum = sum / data.size();
        return sum;
    }
}
