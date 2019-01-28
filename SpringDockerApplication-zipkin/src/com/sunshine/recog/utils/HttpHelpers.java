package com.sunshine.recog.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import sun.net.www.ParseUtil;
import sun.net.www.protocol.ftp.FtpURLConnection;

/**
 * 获取请求流
 * 
 * @author OY
 */
public abstract class HttpHelpers {

	private static final String KEY = "file.encoding";

	private static byte[] EMPTY_BYTE = new byte[0];
	
	/**
	 * 使用FtpURLConnectio,本地设置不起作用
	 * 需要在JVM Argument设置-Dfile.encoding=UTF-8
	 * Myeclipse/eclipse修改jdk
	 */
	private static final String ENCODING = "UTF-8";

	public static InputStream getInputStream(String url) throws Exception{
		URLConnection con = null;
		HttpURLConnection httpCon = null;
		try {
			//System.out.println(System.getProperty(KEY)+"<<<<<Y>>>>"+url);
			URL _url = new URL(url);
			String _temp = url.toLowerCase();
			if(_temp.startsWith("http")){
				con = _url.openConnection();
				con.setConnectTimeout(4000);
				con.setUseCaches(false);// 不缓存
				con.setDefaultUseCaches(false);
				httpCon = (HttpURLConnection) con;
				httpCon.setInstanceFollowRedirects(true);
				//httpCon.setRequestProperty("Accept-Charset", "utf-8");
				if (httpCon.getResponseCode() >= 300) {
					System.out.println("URL:" + url
							+ ",HTTP Request is not success, Response code is "
							+ httpCon.getResponseCode());
				} else {
					return httpCon.getInputStream();
				}
			} else if(_temp.startsWith("ftp")){
				con = _url.openConnection();
				con.setConnectTimeout(4000);
				con.setUseCaches(false);// 不缓存
				con.setDefaultUseCaches(false);
				FtpURLConnection ftpCon = (FtpURLConnection)con;
				return ftpCon.getInputStream();
//				return FtpTools.getInputStream(_url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	public static byte[] getInputStream(String url, String type) throws Exception{
		URLConnection con = null;
		HttpURLConnection httpCon = null;
		try {
			//System.out.println(System.getProperty(KEY)+"<<<<<Y>>>>"+url);
			URL _url = new URL(url);
			String _temp = url.toLowerCase();
			if(_temp.startsWith("http")){
				con = _url.openConnection();
				con.setConnectTimeout(4000);
				con.setUseCaches(false);// 不缓存
				con.setDefaultUseCaches(false);
				httpCon = (HttpURLConnection) con;
				httpCon.setInstanceFollowRedirects(true);
				//httpCon.setRequestProperty("Accept-Charset", "utf-8");
				if (httpCon.getResponseCode() >= 300) {
					System.out.println("URL:" + url
							+ ",HTTP Request is not success, Response code is "
							+ httpCon.getResponseCode());
				} else {
					return FtpTools.transferToByteArray(httpCon.getInputStream());
				}
			} else if(_temp.startsWith("ftp")){
				return FtpTools.getInputStream(_url, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return EMPTY_BYTE;
	}
	
	public static void cd(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (path.indexOf('/') == -1) {
            return;
        }
        StringTokenizer token = new StringTokenizer(path, "/");
        while (token.hasMoreTokens()) {
            System.out.println(ParseUtil.decode(token.nextToken()));
        }
    }
	
	public static void decodePath(String path) {
        int i = path.indexOf(";type=");
        String type = "";
        String filename=null;
        String pathname =null;
        String fullpath = null;
        if (i >= 0) {
            String s1 = path.substring(i + 6, path.length());
            if ("i".equalsIgnoreCase(s1)) {
                type = "i";
            }
            if ("a".equalsIgnoreCase(s1)) {
                type = "s";
            }
            if ("d".equalsIgnoreCase(s1)) {
                type = "d";
            }
            path = path.substring(0, i);
        }
        if (path != null && path.length() > 1 &&
                path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path == null || path.length() == 0) {
            path = "./";
        }
        if (!path.endsWith("/")) {
            i = path.lastIndexOf('/');
            if (i > 0) {
                filename = path.substring(i + 1, path.length());
                filename = ParseUtil.decode(filename);
                pathname = path.substring(0, i);
            } else {
                filename = ParseUtil.decode(path);
                pathname = null;
            }
        } else {
            pathname = path.substring(0, path.length() - 1);
            filename = null;
        }
        if (pathname != null) {
            fullpath = pathname + "/" + (filename != null ? filename : "");
        } else {
            fullpath = filename;
        }
        System.out.println("Type:" + type + ",pathname:" + pathname + ",filename:" + filename + ",fullpath:" + fullpath);
    }
	
	
	public static void getInfo(URL url) {
        String host = url.getHost();
        int port = url.getPort();
        String user= null ;
        String password = null;
        String userInfo = url.getUserInfo();

        if (userInfo != null) { // get the user and password
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                user = ParseUtil.decode(userInfo);
                password = null;
            } else {
                user = ParseUtil.decode(userInfo.substring(0, delimiter++));
                password = ParseUtil.decode(userInfo.substring(delimiter));
            }
        }
        System.out.println("user-info:"+user + ":" + password + "@" + host + ":" + port);
    }
	
	
	public static URL getFtpStr(URL _url) throws Exception{
		String authority = _url.getAuthority();
		String path = _url.getPath();
		System.out.println(authority + "__" + path);
		if(path.startsWith("/"))
			path = path.substring(1, path.length());
		String pathEncodeStr = sun.net.www.ParseUtil.encodePath(path);
		StringBuffer sb = new StringBuffer("ftp://");
		sb.append(authority);
		sb.append("/");
		sb.append(pathEncodeStr);
		System.out.println("path encode: "+sb.toString());
		return new URL(sb.toString());
	}
	
	
	public static String getUtf8Url(String url){
		char[] chars=url.toCharArray();
		StringBuilder  sb=new StringBuilder();
		final int charCount=chars.length;
		for (int i = 0; i < charCount; i++) {
			byte[] b=(""+chars[i]).getBytes();
			if(b.length==1){
				sb.append(chars[i]);
			}else{
				try {
					sb.append(URLEncoder.encode(String.valueOf(chars[i]), "UTF-8"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		
		//  本地测试
		try {
			//InputStream input = getInputStream("ftp://ygnet:123456@192.168.110.189:21/test/tp/沙漠.jpg");
			//InputStream input = getInputStream("ftp://test:123456@10.40.56.190/1.jpg");
			//InputStream input = getInputStream("ftp://test:123456@10.40.56.190/中文.jpg");
//			byte[] bys = new byte[1024*5];
//			int rb =0;
//			System.out.println("可读："+input.available());
//			OutputStream ii = new FileOutputStream("D:\\1.jpg");
//			while((rb= input.read(bys))!=-1){
//				ii.write(bys,0,rb);
//			}
//			input.close();
			
			byte[] bys = getInputStream("http://img1.3lian.com/img2008/09/015/996Turbo19.jpg", null);
//			byte[] bys = getInputStream("ftp://ygnet:123456@192.168.110.132:21/test/tp/Chrysanthemum.jpg", null);
//			byte[] bys = getInputStream("ftp://ygnet:123456@192.168.110.132:21/test/tp/沙漠.jpg", null);
//			byte[] bys = getInputStream("ftp://ygnet:123456@192.168.110.132:21/test/图片/Koala.jpg", null);
//			byte[] bys = getInputStream("ftp://ygnet:123456@192.168.110.132:21/test/图片/企鹅.jpg", null);
			OutputStream ii = new FileOutputStream("D:\\1.jpg");
			ii.write(bys);
			ii.flush();
			ii.close();
			System.out.println("图片下载完成");
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*// 是否可构建图片
		try {
			InputStream input = getInputStream("http://10.143.73.55:8305/20160105/14519730000380.jpg");
			ImageInputStream iinput = ImageIO.createImageInputStream(input);
			if (iinput != null) {
				Iterator<ImageReader> it = ImageIO.getImageReaders(iinput);
				ImageReader ir = it.next();
				System.out.print(ir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
}
