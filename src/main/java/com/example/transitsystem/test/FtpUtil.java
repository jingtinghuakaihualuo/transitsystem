package com.example.transitsystem.test;

import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpProtocolException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class FtpUtil {
    FtpClient ftpClient;

    /***
     * 连接ftp
     * @param url  必须是  192.168.8.1  否则提示异常
     * @param port
     * @param username
     * @param password
     * @return
     */
    public static FtpClient connectFTP(String url, int port, String username, String password) {
        //创建ftp
        FtpClient ftp = null;
        try {
            //创建地址
            SocketAddress addr = new InetSocketAddress(url, port);
            //连接
            ftp = FtpClient.create();
            ftp.connect(addr);
            //登陆
            ftp.login(username, password.toCharArray());
            ftp.setBinaryType();
        } catch (FtpProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftp;
    }

    public static List<String> download(String ftpFile, FtpClient ftp) throws Exception{
        List<String> list = new ArrayList<String>();
        String str = "";
        InputStream is = null;
//        FileOutputStream fos = new FileOutputStream(new File("E:\\test\\tmp\\rtl8188eu-master.zip"));
        FileOutputStream fos = new FileOutputStream(new File("/home/yao/test.txt"));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        BufferedInputStream bis = null;
        byte[] bytes = new byte[1024 * 100];
        int len = 0;
        try {
            // 获取ftp上的文件
            is = ftp.getFileStream(ftpFile);
            bis = new BufferedInputStream(is);
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes,0, len);
            }
        }catch (FtpProtocolException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
            bis.close();
            bos.close();
            fos.close();
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        FtpClient ftp = connectFTP("120.79.11.80",21,"uftp","123456");
        List<String> list = download("/home/uftp/test.txt",ftp);
        for(int i=0;i<list.size();i++){
            System.out.println("list "+ i + " :"+list.get(i));
        }
        try {
            ftp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
