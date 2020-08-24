package com.example.transitsystem.test;


import com.example.transitsystem.utils.SocketPachageUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class ChatClient {
    private static Gson gson = new GsonBuilder().create();

    public static void main(String[] args) throws Exception {

        String host = "120.79.11.80";
//        String host = "127.0.0.1";
        double a = 1.0;
        int port = 10086;
        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        socket.setOOBInline(true);
        // 建立连接后获得输出流
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());

        startListen(outputStream, inputStream);

        String jsonStr = "{       \n" +
                "        \"api\" : \"/register\",\n" +
                "\t\"reqNo\" : 100,\n" +
                "\t\"reqDate\" : 1234567890,\n" +
                "\t\"data\" : {\n" +
                "\t\t\"sno\" : \"1110000103111203051007\",\n" +
                "\t\t\"mac\" : \"c4:3a:35:f8:06:98\"\n" +
                "\t}\n" +
                "}";

        String heartBeatStr = "{       \n" +
                "        \"api\" : \"heartBeat\",\n" +
                "\t\"reqNo\" : 100,\n" +
                "\t\"reqDate\" : 1234567890,\n" +
                "\t\"data\" : {\n" +
                "\t}\n" +
                "}";

        //生成包头，前四位为主题包长度
        byte[] sendBytes = SocketPachageUtil.builderSendBytes(jsonStr);
        outputStream.write(sendBytes);
        outputStream.flush();

        while (true) {
            sendBytes = SocketPachageUtil.builderSendBytes(heartBeatStr);
            outputStream.write(sendBytes);
            outputStream.flush();
            TimeUnit.SECONDS.sleep(20);
        }

    }

    private static void startListen(OutputStream outputStream, InputStream inputStream) {
        Thread thread = new Thread(new Runnable() {



            @Override
            public void run() {
                try {
                    byte[] head = new byte[4];
                    int index = 0;
                    byte[] bytes = new byte[1024];
                    StringBuilder sb = new StringBuilder();
                    int b = 0;
                    //先循环读头
                    while ((b = inputStream.read()) >= 0) {
                        if (index < SocketPachageUtil.HEADLENGTH - 1) {
                            head[index] = (byte) b;
                            index++;
                        } else if (index == SocketPachageUtil.HEADLENGTH - 1) {
                            head[index] = (byte) b;

                            //处理长度
                            int bodyLen = SocketPachageUtil.bytes2Int(head, true);
                            System.out.println("##############client : 主体包长度为 bodyLen=" + bodyLen);

                            byte[] body = new byte[bodyLen];
                            //循环读主体部分数据，直到读完已知长度为止
                            index = 0;//置为0，从新循环读主体数据
                            while (index + bytes.length < bodyLen) {
                                inputStream.read(bytes);
                                sb.append(new String(bytes, 0, bytes.length));
                                index = index + bytes.length;
                            }
                            if (bodyLen - index > 0) {
                                inputStream.read(bytes);
                                sb.append(new String(bytes, 0, bodyLen - index));
                            }
                            //数据读取完毕   解析数据
                            System.out.println("############client:" + sb.toString());


                            try {
                                //处理数据部分start
                                //test return
//                    String s = "{\"code\" : \"0000\", \"msg\" : \"success!\", \"respNo\" : 100}";
//                                SocketApiRequest request = gson.fromJson(sb.toString(), SocketApiRequest.class);
//                                SocketApiRespnose respnose = new SocketApiRespnose(ResultEnum.SUCCESS, request.getReqNo()== null ? 100 : request.getReqNo());
//                                String retStr = gson.toJson(respnose);
//                                outputStream.write(SocketPachageUtil.builderSendBytes(retStr));
                            } finally {
                                //重置数据
                                index = 0;
                                sb.setLength(0);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}
