package com.wenge.datagroup.util;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupCrawler {
	public static String downloadByProxy(String url,String proxyIP,int proxyPort){
		
		Document doc = null;
		try {
			doc = Jsoup.connect(url).proxy(proxyIP, proxyPort)
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
			        .header("Accept-Encoding", "gzip, deflate, br")
			        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.6")
			        .header("Cache-Control", "private")
			        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36")
			        .timeout(30 * 1000).post();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(doc!=null){
			return doc.html();
		}
		return null;
	}
	public static String download(String url,String charset){
		Document doc = null;
		try {
			doc = Jsoup.connect(url)
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
			        .header("Accept-Encoding", "gzip, deflate, br")
			        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.6")
			        .header("Cache-Control", "private")
			        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36")
			        .header("Cookie", "MUID=3AAF290E82246E8E39E722EA86246888; SRCHD=AF=Z9FD1; SRCHUID=V=2&GUID=CA8CF832D8754B0D8A5290A7EEA11C1F&dmnchg=1; _FP=hta=on; BFBN=gRCbIADdQZTwz-SSX-AhOt8W1u1MgdBIYsEtgURVdnEGfQ; MUIDB=3AAF290E82246E8E39E722EA86246888; _ITAB=STAB=TR; ANON=A=26BB8339C168E3EE658813F7FFFFFFFF&E=153b&W=1; NAP=V=1.9&E=14e1&C=zpVOw2eEOtQ46qkqZk7GsZNBDW1evREFVWQOjm4-1n-g1RrV8UPbaQ&W=1; RMS=A=gUACEACAACAQAAAAI; AcaBatchCitePop=CLOSE=1; _RwBf=s=70&o=18; WLS=C=&N=; SRCHUSR=DOB=20180518&T=1527072931000; ClarityID=5ef88bda75424de7bd67b1d15c40d56b; ENSEARCHZOSTATUS=STATUS=0; ENSEARCH=TIPBUBBLE=1&BENVER=0; _EDGE_CD=m=en-us&u=en-us; SNRHOP=I=&TS=; _UR=MC=1; ipv6=hit=1527389472192&t=4; _EDGE_S=mkt=en-us&ui=en-us&SID=079DB7E7ADCE68221F68BC10ACE069A4; ULC=P=CDEE|68:6&H=CDEE|1:1&T=CDEE|91:7:22; _BINGNEWS=SW=880&MSW=1903; SRCHHPGUSR=CW=1920&CH=974&DPR=1&UTC=480&NEWWND=0&NRSLT=-1&SRCHLANG=&AS=1&ADLT=DEMOTE&NNT=1&HIS=1&HAP=0&WTS=63662997222; _SS=SID=079DB7E7ADCE68221F68BC10ACE069A4&HV=1527403349&bIm=179338&R=-1")
			        .header("Referer", "https://www.bing.com/")
			        .timeout(30 * 1000).post();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(doc!=null){
			return doc.html();
		}
		return null;
	}
	public static void main(String[] args) {
		String url = "https://www.bing.com/news/search?q=Turtle+Beach+Site";
		JsoupCrawler.downloadByProxy(url, "127.0.0.1", 1080);
	}

}
