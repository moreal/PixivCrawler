package me.moreal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.stream.FileImageOutputStream;
import javax.net.ssl.HttpsURLConnection;

public class Util {
	public static boolean downloadImage(String url, String folder) {
		int index = 0;
		while(url.indexOf("/",index) != -1)
			index = url.indexOf("/",index);
		
		return downloadImage(url, folder, url.substring(index));
	}
	
	public static boolean downloadImage(String url, String folder, String name) {
			System.out.println("URL:"+url);
			System.out.println("Folder:"+folder);
			System.out.println("Name:"+name);
			HttpsURLConnection conn;
			try {
				conn = (HttpsURLConnection) new URL(url).openConnection();
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				br.readLine();
				byte[] b = new byte[1024*1024];
				conn.getInputStream().read(b);
				String s = new String(b);
				
				s.substring(s.indexOf("Content: "), s.length());
				File f = new File(folder + name);
				System.out.println("PATH:"+f.getAbsolutePath());
				if(!f.exists())
				{
					f.createNewFile();
					System.out.println("There is no file, so I created file");
				}
				
				FileImageOutputStream fios = new FileImageOutputStream(f);
				
				fios.write(s.getBytes());
				fios.flush();
				
				fios.close();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return true;
		
	}
}
