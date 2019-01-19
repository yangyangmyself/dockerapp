package com.sunshine.recog.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author oyyl
 * @since 2017/11/29
 * Ftp download tools
 *
 */
public abstract class FtpTools {

	// 默认端口
	private static final int DEFAULT_PORT = 21;
	
	// 文件路径编码方式
	private static String DEFAULT_FTP_ENCODE = "iso-8859-1";
	
	// FTP服务器的编码方式
	private static String DEFAULT_ENCODE_OS_UTF8 = "UTF-8";
	
	private static byte[] EMPTY_BYTE = new byte[0];
	
	private static Logger logger = LoggerFactory.getLogger(FtpTools.class);
	
	public FTPClientConfig getFTPClientConfig(){
		FTPClientConfig config = new FTPClientConfig();
		config.setServerLanguageCode("zh");
		return config;
	}
	
	public static FTPClient getClient(String host, String user, String pass, int port){
		FTPClient ftpClient = null;
		ftpClient = new FTPClient();
		try {
			if(port < 0)
				ftpClient.connect(host, DEFAULT_PORT);
			else
				ftpClient.connect(host, port);
			int defaultTimeout = 3 * 1000; // 超时配置
			ftpClient.setControlKeepAliveTimeout(defaultTimeout);
			ftpClient.setControlKeepAliveReplyTimeout(defaultTimeout);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				logger.error("FTP server refused connection.");
			}
		} catch (IOException e) {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
			logger.error("Could not connect to server.");
			e.printStackTrace();
		}
		try
        {
            if (!ftpClient.login(user, pass)){
                ftpClient.logout();
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // Use passive mode as default because most of us are
            // behind firewalls these days.
            //ftpClient.enterLocalActiveMode();
            ftpClient.enterLocalPassiveMode();
            ftpClient.setUseEPSVwithIPv4(false);
        }catch(FTPConnectionClosedException e){
        	logger.error("Server closed connection.");
        }catch (IOException e){
        	logger.error("Exception {}",e.getMessage());
		} finally {
			// do nothing
		}
		return ftpClient;
	}
	
	public static InputStream getInputStream(URL url){
		FTPClient ftpClient = null;
		InputStream inputStream = null;
		String filename = null;
		String pathname = null;
		String fullpath = null;
		String host = url.getHost();
        int port = url.getPort();
        String user= "" ;
        String pass = "";
        String userInfo = url.getUserInfo();
        String path = url.getPath();
        if (userInfo != null) { // get the user and password
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                user = userInfo;
                pass = null;
            } else {
                user = userInfo.substring(0, delimiter++);
                pass = userInfo.substring(delimiter);
            }
        }
		try {
			ftpClient = getClient(host, user, pass, port);
			if(!path.endsWith("/")){
				int i = path.lastIndexOf('/');
				if(i>0){
					filename=path.substring(i+1,path.length());
					pathname=path.substring(0,i);
				}else{
					filename=path;
					pathname=null;
				}
			} else {
				pathname=path.substring(0,path.length()-1);
				filename=null;
			}
			if(pathname!=null){
				fullpath=pathname+"/"+(filename!=null?filename:"");
			}else{
				fullpath = filename;
			}
			
			// 动态获取服务器编码
			String dEncode = "UTF-8";//getEncode(ftpClient.getSystemType());
			ftpClient.setControlEncoding(dEncode);
			
			logger.debug("Y<<<<fullpath>>>>" + fullpath+ "<<<filename>>>" + filename);
			String _filename = new String(filename.getBytes(dEncode),DEFAULT_FTP_ENCODE);
			long a = System.currentTimeMillis();
			StringBuffer sb = new StringBuffer();
	        StringTokenizer token = new StringTokenizer(pathname, "/");
	        while (token.hasMoreTokens()) {
	        	String fold = token.nextToken();
	        	String encodeFold = new String(fold.getBytes(dEncode),DEFAULT_FTP_ENCODE);
	        	boolean flag = ftpClient.changeWorkingDirectory(encodeFold);
	        	if(!flag){
	        		logger.error("目录：["+ fold + "]不存在!");
	        		logger.error("指定的目录不存在或ftp无法打开，路径为："+pathname);
	        		return null;
	        	}
	        	sb.append(encodeFold);
	        	if(token.hasMoreTokens()){
	        		sb.append("/");
				}else{
					sb.append("/");
					sb.append(_filename);
	        	}
	        }
	        long b = System.currentTimeMillis();
	        logger.debug(dEncode+"<<<<fullpath>>>>" + sb.toString());
			inputStream=ftpClient.retrieveFileStream(_filename);
			long c = System.currentTimeMillis();
			logger.debug("切换工作目录-"+(b-a) + " 获取远程流-" + (c-b));
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
		}
		return inputStream;
	}
	
	public static byte[] getInputStream(URL url, String type){
		FTPClient ftpClient = null;
		InputStream inputStream = null;
		String filename = null;
		String pathname = null;
		String fullpath = null;
		String host = url.getHost();
        int port = url.getPort();
        String user= "" ;
        String pass = "";
        String userInfo = url.getUserInfo();
        String path = url.getPath();
        if (userInfo != null) { // get the user and password
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                user = userInfo;
                pass = null;
            } else {
                user = userInfo.substring(0, delimiter++);
                pass = userInfo.substring(delimiter);
            }
        }
		try {
			ftpClient = getClient(host, user, pass, port);
			if(!path.endsWith("/")){
				int i = path.lastIndexOf('/');
				if(i>0){
					filename=path.substring(i+1,path.length());
					pathname=path.substring(0,i);
				}else{
					filename=path;
					pathname=null;
				}
			} else {
				pathname=path.substring(0,path.length()-1);
				filename=null;
			}
			if(pathname!=null){
				fullpath=pathname+"/"+(filename!=null?filename:"");
			}else{
				fullpath = filename;
			}
			
			// 动态获取服务器编码
			String dEncode = "UTF-8";//getEncode(ftpClient.getSystemType());
			ftpClient.setControlEncoding(dEncode);
			
			logger.debug("Y<<<<fullpath>>>>" + fullpath+ "<<<filename>>>" + filename);
			String _filename = new String(filename.getBytes(dEncode),DEFAULT_FTP_ENCODE);
			long a = System.currentTimeMillis();
			StringBuffer sb = new StringBuffer();
	        StringTokenizer token = new StringTokenizer(pathname, "/");
	        while (token.hasMoreTokens()) {
	        	String fold = token.nextToken();
	        	String encodeFold = new String(fold.getBytes(dEncode),DEFAULT_FTP_ENCODE);
	        	boolean flag = ftpClient.changeWorkingDirectory(encodeFold);
	        	if(!flag){
	        		logger.error("目录：["+ fold + "]不存在!");
	        		logger.error("指定的目录不存在或ftp无法打开，路径为："+pathname);
	        		return null;
	        	}
	        	sb.append(encodeFold);
	        	if(token.hasMoreTokens()){
	        		sb.append("/");
				}else{
					sb.append("/");
					sb.append(_filename);
	        	}
	        }
	        long b = System.currentTimeMillis();
	        logger.debug(dEncode+"<<<<fullpath>>>>" + sb.toString());
			inputStream=ftpClient.retrieveFileStream(_filename);
			long c = System.currentTimeMillis();
			logger.debug("切换工作目录-"+(b-a) + " 获取远程流-" + (c-b));
			if(inputStream == null)
				return EMPTY_BYTE;
			return transferToByteArray(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
		}
		return EMPTY_BYTE;
	}
	
	public static byte[] transferToByteArray(InputStream input){
		BufferedInputStream bi = null;
		ByteArrayOutputStream barray = null;
		try {
			byte[] buffer = new byte[1024];
			bi = new BufferedInputStream(input); 
			barray = new ByteArrayOutputStream();
			int len = 0;
			while((len=bi.read(buffer))!=-1){
				barray.write(buffer, 0, len);
			}
			barray.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(barray != null)
					barray.close();
				if(bi != null)
					bi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return barray.toByteArray();
	}
	
	public static boolean cd(FTPClient ftpClient, String path, String encode) throws Exception {
        if (path == null || path.isEmpty()) {
        	return true;
        }
        if (path.indexOf('/') == -1) {
        	ftpClient.changeWorkingDirectory(path);
            return true;
        }
        StringTokenizer token = new StringTokenizer(path, "/");
        while (token.hasMoreTokens()) {
        	String fold = token.nextToken();
        	boolean flag = ftpClient.changeWorkingDirectory(new String(fold.getBytes(encode),DEFAULT_FTP_ENCODE));
        	if(!flag){
        		logger.error("目录：["+ fold + "]不存在!");
        		return false;
        	}
        }
        return true;
    }
	
	private static String getEncode(String str){
		int pos = str.toLowerCase().indexOf("windows");
		if(pos != -1){
			return "GBK";
		}
		return DEFAULT_ENCODE_OS_UTF8;
	}
	
	public static void main(String[] args) {
		// 本地测试
		try {
			InputStream input = getInputStream(new URL("ftp://veh_ealpass0002:vehrealpass@10.147.3.232/0002/430666000051/20171129/16/20171129165112046-湘D5T078.jpg"));
			//InputStream input =getInputStream(new URL("ftp://test:123456@10.40.56.190/0011/2017/20171028150503045-湘D9LX39.jpg"));
			// InputStream input =
			// getInputStream("ftp://test:123456@10.40.56.190/中文.jpg");
			byte[] bys = new byte[1024 * 5];
			int rb = 0;
			System.out.println("可读："+input.available());
			OutputStream ii = new FileOutputStream("D:\\1.jpg");
			while ((rb = input.read(bys)) != -1) {
				ii.write(bys, 0, rb);
			}
			input.close();
			ii.flush();
			ii.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
