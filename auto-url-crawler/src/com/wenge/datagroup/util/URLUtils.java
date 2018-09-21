package com.wenge.datagroup.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {
	public static void main2(String[] args) {
		String href = "http://fy.iciba.com/b.png";
		String hrefTmp = "https://theworldnews.net/source-news/New York Post";
		System.out.println(isValidUrl(hrefTmp));
		
		String regex = "^([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[fF][tT][pP]:/*)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)$";
		
		regex = "^((https|http|ftp)?://)"
		        + "?(([0-9a-z_!~*\'().&=+$%-]+: )?[0-9a-z_!~*\'().&=+$%-]+@)?" //ftp的user@ 
		        + "(([0-9]{1,3}.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184 
		        + "|" // 允许IP和DOMAIN（域名） 
		        + "([0-9a-z_!~*\'()-]+.)*" // 域名- www. 
		        + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]." // 二级域名 
		        + "[a-z]{2,6})" // first level domain- .com or .museum 
		        + "(:[0-9]{1,4})?" // 端口- :80 
		        + "((/?)|" // a slash isn't required if there is no file name 
		        + "(/[0-9a-z_!~*\'().;?:@&=+$,%#-]+)+/?)$";
		System.out.println(href.matches(regex));
		
		System.out.println(isValidUrl("https://theworldnews.net/source-news/Українські"));
		System.out.println("Українські".matches(".*"));
	}
	public static void main(String[] args) {
		String url="theworldnews.net/source-news/a.js";
		url="javascrpt();";
		url="https://www.digitimes.com.tw/tech/dt/dtpage_cold.asp";
		long a = System.nanoTime();
		System.out.println(isValidUrl(url));
		long b = System.nanoTime();
		System.out.println(b-a);
	}
	public static boolean isValidUrl(String url) {
//		String regEx = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
//				+ "2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
//				+ "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
//				+ "-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$\\=~_\\-@]*)*$";
		
//		String regEx = "^((https|http|ftp)?://)"
//		        + "?(([0-9A-Za-z_*.&=+$%-]+: )?[0-9a-z_!~*\'().&=+$%-]+@)?" //ftp的user@ 
//		        + "(([0-9]{1,3}.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184 
//		        + "|" // 允许IP和DOMAIN（域名） 
//		        + "([0-9a-z_!~*\'()-]+.)*" // 域名- www. 
//		        + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]." // 二级域名 
//		        + "[a-z]{2,6})" // first level domain- .com or .museum 
//		        + "(:[0-9]{1,4})?" // 端口- :80 
//		        + "((/?)|"
//		        + "(/[0-9a-zA-Z\\s_!~*\'().;?:@&=+$,%#-]+)+/?.*)$";
		
//		String regEx ="^http[s]?:\\/\\/(([0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-z_!~*\'()-]+\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\.[a-z]{2,6})(:[0-9]{1,4})?((\/\?)|(\/[0-9a-zA-Z_!~\*\'\(\)\.;\?:@&=\+\$,%#-\/]*)?)$/";
		String regEx ="^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+([A-Za-z]+)[/\\?\\:]?.*$";
		
		return url.matches(regEx);
	}

	public static boolean isEndWithPointSth(String url) {
		String regEx = ".+\\.[a-z]+$";

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
	public static String getHost(String srcUrl) {
		// 对srcUrl进行处理得到主机名
		String url = null;
		try {
			if (srcUrl == null) {
				return null;
			}
			url = srcUrl.trim().toLowerCase().replace(" ", "");
			if (url.startsWith("http://www.")) {
				url = url.replace("http://www.", "");
			}
			if (url.startsWith("http://")) {
				url = url.replace("http://", "");
			}
			if (url.startsWith("https://www.")) {
				url = url.replace("https://www.", "");
			}
			if (url.startsWith("https://")) {
				url = url.replace("https://", "");
			}
			if (url.startsWith("/")) {
				url = url.replaceFirst("/", "");
			}
			if (url.contains("/")) {
				url = url.substring(0, url.indexOf("/"));
			}
			if (url.contains(":")) {
				url = url.substring(0, url.indexOf(":"));
			}
			String[] urls = url.split("\\.");
			String first = "aero,arts,biz,com,coop,edu,firm,gov,idv,info,int,mil,museum,name,net,nom,org,pro,rec,store,web,xxx";
			String second = "aa,at,au,be,ca,cn,dk,fr,it,jp,nl,nz,uk,za,ad,ae,af,ag,ai,al,an,ao,aq,"
					+ "ar,as,at,au,aw,az,ba,bb,bd,be,bf,bg,bh,bi,bj,bm,bn,bo,br,bs,bt,bv,bw,"
					+ "by,bz,ca,cc,cf,cg,ch,ci,ck ,cl,cm,cn,co,cq,cr,cu,cv,cx,cy,cz,de,dj,dk,dm,"
					+ "do,dz,ec,ee,eg,eh,es,et,eu,ev,fi,fj,fk,fm,fo,fr,ga,gb,gd,ge,gf,gh,gi,gl,gm,gn,"
					+ "gp,gr,gt,gu,gw,gy,hk,hm,hn,hr,ht,hu,id,ie,il,in,io,iq,ir,is,it,jm,jo,jp,ke,"
					+ "kg,kh,ki,km,kn,kp,kr,kw,ky,kz,la,lb,lc,li,lk,lr,ls,lt,lu,lv,ly,ma,mc,md,me,mg,mh,"
					+ "ml,mm,mn,mo,mp,mq,mr,ms,mt,mv,mw,mx,my,mz,na,nc,ne,nf,ng,ni,nl,no,np,nr,nt,nu,"
					+ "nz,om,pa,pe,pf,pg,ph,pk,pl,pm,pn,pr,pt,pw,py,qa,re,ro,ru,rw,sa,sb,sc,sd,se,sg,"
					+ "sh,si,sj,sk,sl,sm,sn,so,sr,st,su,sy,sz,tc,td,tf,tg,th,tj,tk,tm,tn,to,tp,tr,tt,"
					+ "tv,tw,tz,ua,ug,uk,us,uy,va,vc,ve,vg,vn,vu,wf,ws,ye,yu,za,zm,zr,zw";
			if (first.contains(urls[urls.length - 1])) {
				return urls[urls.length - 2] + "." + urls[urls.length - 1];
			}
			if (second.contains(urls[urls.length - 1])) {
				if (urls.length > 2 && first.contains(urls[urls.length - 2])) {
					return urls[urls.length - 3] + "." + urls[urls.length - 2] + "." + urls[urls.length - 1];
				} else {
					return urls[urls.length - 2] + "." + urls[urls.length - 1];
				}
			}
		} catch (Exception e) {
			return null;
		}
		return url;
	}
	public static String getHost2(String url) {
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
