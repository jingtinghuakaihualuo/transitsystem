package com.example.transitsystem.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        double a = 1.0;
        int port = 10086;
        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        socket.setOOBInline(true);
        // 建立连接后获得输出流
        DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream());
        DataInputStream inputStream = new DataInputStream( socket.getInputStream());
        String jsonStr = "{       \n" +
                "        \"api\" : \"/register\",\n" +
                "\t\"reqNo\" : 100,\n" +
                "\t\"reqDate\" : 1234567890,\n" +
                "\t\"data\" : {\n" +
                "\t\t\"sno\" : \"123456789\",\n" +
                "\t\t\"mac\" : \"AA-BB-CC-DD\"\n" +
                "\t}\n" +
                "}";

        outputStream.write(jsonStr.getBytes());

        StringBuilder sb = new StringBuilder();
        byte[] bytes = new byte[1024 * 100];
        int len = 0;
        try {
            while ((len = inputStream.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len, "utf-8"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        JsonObject jsonObject = new GsonBuilder().create().fromJson(sb.toString(), JsonObject.class);
        System.out.println("return data from server :" + jsonObject.toString());
        String url = "/home/yao/test/tmp/tomcat/";
        try {
            if (jsonObject.get("respNo") == null) {    //图片是由服务端推送的  没有respNo
                jsonObject = (JsonObject) jsonObject.get("data");
                if(jsonObject.get("headPicName") != null) {
                    String filePicBase64 = jsonObject.get("headPic").getAsString();
                    String headfileName = jsonObject.get("headPicName").getAsString();
                    //写文件
                    wirteFile(headfileName, filePicBase64, url);
                }
                JsonArray jsonArray = jsonObject.get("files").getAsJsonArray();
                if (jsonArray.size() > 0) {
                    for (int i = 0 ; i < jsonArray.size(); i++) {
                        JsonObject subObject = jsonArray.get(i).getAsJsonObject();
                        String fileName = subObject.get("fileName").getAsString();
                        String fileBase64 = subObject.get("file").getAsString();
                        //写文件
                        wirteFile(fileName, fileBase64, url);
                    }
                }
            }
        } catch (Exception e) {

        }


        //通过shutdownOutput高速服务器已经发送完数据，后续只能接受数据

//        InputStream inputStreams = new FileInputStream("/home/yao/test/test.txt");
//        byte[] bytes = new byte[inputStreams.available()];
//        inputStreams.read(bytes);
//        DataInputStream inputStream1 = new DataInputStream(socket.getInputStream());
//        byte[] bytess = new byte[1024];
//        while (bytes.length > 0){
//            bytess = new byte[1024];
//            inputStream1.read(bytess);
//            String info = new String(bytess, "utf-8");
//            System.out.println(info);
//        }
    }

    private static void wirteFile(String fileName, String fileBase64, String url) {
        File file = new File(url + fileName);

        try {
            Files.write(Paths.get(url+fileName), Base64.getDecoder().decode(fileBase64), StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
