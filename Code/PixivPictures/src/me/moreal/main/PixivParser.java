package me.moreal.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import me.moreal.network.HttpSocket;
import me.moreal.util.Util;

public class PixivParser extends Thread {
	private static String[] banned = {"乳","エロ"};
	private int illust_id = 0;
	private String url = null;

	private HttpSocket sock = null;
	private String page = null;

	private BufferedReader br = null;
	private HttpsURLConnection conn = null;

	public PixivParser(String url, int startnum) {
		this.illust_id = startnum;
		this.url = url;
		// sock = new HttpSocket(this.url, "/member_illust.php"); //
		// ?mode=medium&illust_id=61838494
	}

	public PixivParser(String url) {
		this(url, 0);
	}

	public void run() {
		while (true) {
			try {
				url = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + illust_id;
				
				conn = (HttpsURLConnection) new URL(url).openConnection();
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				page = "";

				while (br.ready())
					page += br.readLine();
				
				String image_url = getImageLink();

				if (image_url == null) {
					continue;
				}
				
				if (isBad()) {
					continue;
				}
				
				if (getViews() < 1000) {
					//System.out.println("[+] It is too lower view point.. : " + getGoodPoint());
					continue;
				}
				
				if (getGoodPoint() < 1000) {
					//System.out.println("[+] It is too lower good point.. : " + getGoodPoint());
					continue;
				}
				
				System.out.println("");
				System.out.println("[*] Tried URL : " + url);

				try {
					HttpsURLConnection c = (HttpsURLConnection) new URL(image_url).openConnection();
					c.addRequestProperty("Referer", url);
					c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
					
					c.getInputStream().read(new byte[2]);
				} catch (FileNotFoundException e) {
					image_url = image_url.replace(".jpg", ".png");
				}
				
				System.out.println("[*] ImageLink is " + image_url);
				System.out.println("[*] ImageFileName is " + toFileName(image_url));
				
				Util.downloadImage(image_url, "D:/TEST/", toFileName(image_url), null);
				
				System.out.println("");
			} catch (FileNotFoundException e) {
				
			} catch (IOException e) {
			} finally {
				++illust_id;
			}
		}
	}
	
	private int getViews() {
		int start = page.indexOf("<li class=\"info\"><span>閲覧数</span><span class=\"views\">"); // <li class=\"info\"><span>[^<]+
		int end = page.indexOf("</span>",start+54);
		
		if (start == -1 || end == -1)
			return -1;

		return Integer.parseInt(page.substring(start+54,end));
	}
	
	private int getGoodPoint() {
		int start = page.indexOf("<li class=\"info\"><span>いいね！</span><span class=\"views\">"); // <li class=\"info\"><span>[^<]+
		int end = page.indexOf("</span>",start+54);
		
		if (start == -1 || end == -1)
			return -1;
		
		return Integer.parseInt(page.substring(start+54,end));
	}

	private String getAuthor() {
		int start = page.indexOf("<div class=\"userdata\"><h1 class=\"title\">");
		int end = page.indexOf("</h1>",start + 40);
		
		if (start == -1 || end == -1)
			return "No Author";
		
		return page.substring(start + 40, end);	
	}

	private String getImageLink() {
		int start = page.indexOf("data-title=\"registerImage\"><img src=\"");
		int end = page.indexOf("\" alt=", start);
		
		if (start == -1)
			return null;
		
		return page.substring(start + 37, end).replaceAll("_master[0-9]+", "").replaceAll("c/[a-zA-Z0-9]+/img-master",
				"img-original");
	}

	private String getTitle() {
		int start = page.indexOf("<title>");
		int end = page.indexOf("</title>", start);

		return page.substring(start + 7, end);
	}
	
	private String toFileName(String link) {
		int i = link.length() - 1;
		String s = "";
		
		while (i >= 0 && link.charAt(i) != '/') {
			s = link.charAt(i) + s;
			--i;
		}
		
		return s;
	}
	
	private boolean isBad() {
		for (String s : banned)
			if(page.indexOf(s) != -1)
				return true;
		
		return false;
	}
}