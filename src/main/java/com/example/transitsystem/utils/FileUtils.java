package com.example.transitsystem.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.Scanner;

public class FileUtils {

    /**
     * @return
     * @Description: 根据图片地址转换为base64编码字符串
     * @Author:
     * @CreateTime:
     */
    public static String getImageStr(String imgFile) {
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 加密
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    /**
     * @param imgStr base64编码字符串
     * @param path   图片路径-具体到文件
     * @return
     * @Description: 将base64编码字符串转换为图片
     * @Author:
     * @CreateTime:
     */
    public static boolean generateImage(String imgStr, String path) {
        if (imgStr == null) {
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // 解密
            byte[] b = decoder.decodeBuffer(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return
     * @Description: 根据图片转换为base64编码字符串
     * @Author:
     * @CreateTime:
     */
    public static String getImageStr(InputStream inputStream) {
        byte[] data = null;
        try {
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 加密
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    /**
     * @return
     * @Description: 根据图片转换为base64编码字符串
     * @Author:
     * @CreateTime:
     */
    public static String getImageStr(byte[] bytes) {
        // 加密
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes);
    }

    /**
     * 大文件
     * 通过文件的流读取byte数组
     *
     * @param inputStream
     * @return
     */
    public static String getFileByteArray(InputStream inputStream) {
        Scanner sc = null;
        StringBuilder sb = new StringBuilder();
        try {
            sc = new Scanner(inputStream, "utf-8");
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }

            return getImageStr(sb.toString().getBytes("utf-8"));
        } catch (Exception e) {

        }
        return null;
    }
}
