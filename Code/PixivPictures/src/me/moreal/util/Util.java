package me.moreal.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.net.ssl.HttpsURLConnection;

public class Util {
	public static boolean downloadImage(String url, String folder) {
		int index = 0;
		while(url.indexOf("/",index) != -1)
			index = url.indexOf("/",index);
		
		return downloadImage(url, folder, url.substring(index), null);
	}
	
	public static boolean downloadImage(String url, String folder, String name, String Cookies) {
			System.out.println("[+] Download ImageURL:"+url);
			
			HttpsURLConnection conn;
			try {
				conn = (HttpsURLConnection) new URL(url).openConnection();
				conn.addRequestProperty("Referer", url);
				conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");

				File f = new File(folder + name);
				
				if(!f.exists())
					f.createNewFile();
				else return false;
				
				FileImageOutputStream fios = new FileImageOutputStream(f); 

				BufferedImage image = ImageIO.read(conn.getInputStream());
				
				ImageIO.write(image, name.substring(name.length()-3, name.length()), f);
				
				fios.close();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				System.out.println("[!!] " + folder + name);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return true;
	}
}
