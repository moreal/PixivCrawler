package me.moreal.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

import me.moreal.util.Config;
import me.moreal.util.Util;

public class PixivParser extends Thread {
	private static ArrayList<String> banned = new ArrayList<String>();
	private String url = null;

	//private HttpSocket sock = null;
	private String page = null;

	private BufferedReader br = null;
	private HttpsURLConnection conn = null;

	public PixivParser() {
		
		String[] arr = {"乳","エロ"};
		
		for (String s : arr) 
			banned.add(s);
	
	}

	public void run() {
		while (true) {
			try {
				url = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + ++Config.illust_id;
				
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
				
				if (getViews() < Config.leastView) {
					continue;
				}
				
				if (getGoodPoint() < Config.leastGood) {
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
				
				String dirname = "D:/TESTT/" + String.format("%s - %s (%s)/",getAuthor(), getAuthorId(), getFollowersPoint(getAuthorId()));
				File dir = new File(dirname);
				
				if (!dir.exists())
					dir.mkdirs();
				
				String filename = getTitle() + "." + getFormat(image_url);
				filename = filename.replaceAll("\\||\"|\\\\|:|\\?|>|<|/", "_");
				
				System.out.println("[*] filename : " + filename);
				
				Util.downloadImage(image_url, dirname, filename, null);
				
			} catch (FileNotFoundException e) {
				
			} catch (IOException e) {
			}
		}
	}
	
	private int getViews() {
		int start = page.indexOf("<li class=\"info\"><span>閲覧数</span><span class=\"views\">"); // <li class=\"info\"><span>[^<]+
		int end = page.indexOf("</span>",start+54);
		
		if (start == -1 || end == -1)
			return -1;

		try {
			return Integer.parseInt(page.substring(start+54,end));
		} catch (Exception e) {
			System.out.println(page);
			return -1;
		}
	}
	
	private int getGoodPoint() {
		int start = page.indexOf("<li class=\"info\"><span>いいね！</span><span class=\"views\">"); // <li class=\"info\"><span>[^<]+
		int end = page.indexOf("</span>",start+54);
		
		if (start == -1 || end == -1)
			return -1;
		
		return Integer.parseInt(page.substring(start+54,end));
	}
	
	private int getFollowersPoint(String id) {
		String url = "https://www.pixiv.net/member.php?id=" + id;
		
		try {
			conn = (HttpsURLConnection) new URL(url).openConnection();
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String p = "";

			while (br.ready())
				p += br.readLine();
			
			String fmt = String.format("data-title=\"showBookmarkRegister\" data-user-id=\"%s\">", id);
			
			int start = p.indexOf(fmt);
			int end = p.indexOf("</a>",start+fmt.length());
			
			try {
				return Integer.parseInt(p.substring(start+fmt.length(), end));
			} catch (Exception e) {
				System.out.println(url);
				System.out.println(p);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	private String getAuthor() {
		String fmt = String.format("<h2 class=\"name\"><a href=\"member.php?id=%s\">", getAuthorId());
		int start = page.indexOf(fmt);
		int end = page.indexOf("</a>", start + fmt.length());
		
		if (start == -1 || end == -1)
			return "No Author";
		
		return page.substring(start + fmt.length(), end).replaceAll("\"", "");
	}
	
	private String getAuthorId() {
		int start = page.indexOf("<a href=\"member.php?id=");
		int end= page.indexOf(">", start + 23);
		
		if (start == -1 || end == -1)
			return "No AuthorId";
		
		return page.substring(start + 23, end).replaceAll("\"", "");
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
		int end = page.indexOf("</title>", start+7);

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
	
	private String getFormat(String name) {
		return name.substring(name.length()-3, name.length());
	}
}