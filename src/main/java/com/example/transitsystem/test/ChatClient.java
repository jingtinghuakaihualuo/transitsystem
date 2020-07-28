package com.example.transitsystem.test;

import java.io.*;
import java.net.Socket;

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

        while (true) {
            outputStream.write(jsonStr.getBytes());
            try {
                Thread.currentThread().sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

}
