package me.moreal.main;

public class Main {
	public static void main(String args[])
	{
		PixivParser parser = new PixivParser("www.pixiv.net", 61487000);
		parser.run();
		/*
		try {
			SSLSocket s = (SSLSocket) SSLSocketFactory.getDefault().createSocket("www.pixiv.net",80);
			//s.connect(new Socket("www.pixiv.net",80).getRemoteSocketAddress(), 200);
			s.getOutputStream().write("Server Hello".getBytes());
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String str;
			while((str = br.readLine()) != null)
				System.out.println(str);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
