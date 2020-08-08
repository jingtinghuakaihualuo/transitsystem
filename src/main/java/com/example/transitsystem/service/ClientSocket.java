package com.example.transitsystem.service;

import com.example.transitsystem.base.SocketApiRespnose;
import com.example.transitsystem.controller.DelongServerSocket;
import com.example.transitsystem.utils.SocketPachageUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * socket 具体处理类
 */

@Data
@Slf4j
@Component
public class ClientSocket implements Runnable {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Map<Integer, SocketApiRespnose> message = new ConcurrentHashMap<Integer, SocketApiRespnose>();
    private Gson gson = new GsonBuilder().create();

    private static EquipmentManagerService equipmentManagerService;

    @Autowired
    public void setEquipmentManagerService(EquipmentManagerService equipmentManagerService) {
        this.equipmentManagerService = equipmentManagerService;
    }

    /**
     * 注册socket到map里
     *
     * @param socket
     * @return
     */
    public static ClientSocket register(Socket socket) {
        ClientSocket client = new ClientSocket();
        try {
            client.setSocket(socket);
            socket.setKeepAlive(true);
            socket.setSoTimeout(180000);
            client.setInputStream(new DataInputStream(socket.getInputStream()));
            client.setOutputStream(new DataOutputStream(socket.getOutputStream()));
            return client;
        } catch (IOException e) {
            try {
                client.logout();
            } catch (IOException e2) {
                log.error("关闭输入输出异常,e={}" + e2.getMessage());
            }
        }
        return null;
    }

    /**
     * 发送数据
     *
     * @param str
     */
    public void send(String str) {
        try {
                byte[] sendBytes = SocketPachageUtil.builderSendBytes(str);
                outputStream.write(sendBytes);
        } catch (IOException e) {
            log.error("发送数据异常，e={}", e.getMessage());
        }
    }

    /**
     * 发送数据
     *
     * @param bytes
     */
    public void send(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            log.error("发送数据异常，e={}", e.getMessage());
        }
    }

    /**
     * 接收数据
     *
     * @return
     * @throws IOException
     */
    public String receive() {
        try {
            byte[] head = new byte[4];
            int index = 0;
            byte[] bytes = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int b = 0;
            //先循环读头
            while ((b = inputStream.read()) >= 0 ) {
                if (index < SocketPachageUtil.HEADLENGTH -1 ) {
                    head[index] = (byte)b;
                    index++;
                } else if (index == SocketPachageUtil.HEADLENGTH -1) {
                    head[index] = (byte)b;

                    //处理长度
                    int bodyLen = SocketPachageUtil.bytes2Int(head,true);
                    log.info("##########server:主体包长度为 bodyLen=" + bodyLen);

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
                    log.info("#############server 请求数据为:" + sb.toString());

                    //处理
                    try {
                        String retString = apiManager(sb.toString());
                        if (!StringUtils.isEmpty(retString)) {
                            send(retString);
                        }
                    } finally {
                        //重置数据
                        index = 0;
                        sb.setLength(0);
                    }

                }
            }
//            byte[] bytes = new byte[1024];
//            int len =inputStream.read(bytes);
//            String info = new String(bytes, 0, len, "utf-8");
//            log.info(info);
//            //处理
//            String retString = apiManager(info);
//            if (!StringUtils.isEmpty(retString)) {
//                log.debug("retString = {}", retString);
//                send(retString);
//            }
        } catch (SocketException e) {
            log.error("连接出现错误,将要关闭连接。e={}", e);
            try{
                logout();
            }catch (Exception ee){}
        } catch (IOException e) {
            log.error("发生错误,e={}", e);
        }
        return null;
    }

    /**
     * 登出操作, 关闭各种流
     */
    public void logout() throws IOException {

        log.info("当前在线用户" + DelongServerSocket.tokenMappingclient.size());
        try {
            socket.shutdownOutput();
            socket.shutdownInput();
            //移除client
            Long tokenId = DelongServerSocket.clientMappingToken.get(this);
            DelongServerSocket.clientMappingToken.remove(this);
            DelongServerSocket.tokenMappingclient.remove(tokenId);
            //变更状态为下线
            equipmentManagerService.updateEquipmentStatus(tokenId);
        } catch (IOException e) {
            throw new SocketException("关闭输入输出异常,e=" + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new SocketException("关闭socket异常,e=" + e.getMessage());
            }
        }
    }

    /**
     * 发送数据包, 判断数据连接状态
     *
     * @return
     */
    public boolean isSocketClosed() {
        try {
            socket.sendUrgentData(1);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public void run() {
        // 每过5秒连接一次客户端

        try {
            while (true) {
                TimeUnit.SECONDS.sleep(2);
                receive();
            }
        } catch (InterruptedException e) {
            log.info("exception, e={}", e);
        } catch (Exception e) {
            try {
                logout();
            } catch (Exception e1) {

            }
        } finally {
            try {
                logout();
            } catch (Exception e) {
                log.info("logout exception, e={}", e);
            }
        }


    }

    @Override
    public String toString() {
        return "Client{" +
                "socket=" + socket +
                ", inputStream=" + inputStream +
                ", outputStream=" + outputStream +
                '}';
    }

    public String apiManager(String reqStr) {
        String retStr = null;
        log.debug("ClientSocket:apiManager()。 reqStr={}", reqStr);
        try {
            JsonObject jsonObject = gson.fromJson(reqStr, JsonObject.class);
            JsonElement api = jsonObject.get("api");
            //请求数据
            if(api == null) {
                Integer respNo = jsonObject.get("respNo").getAsInt();
                if (respNo != null) {
                    SocketApiRespnose socketApiRespnose = message.get(respNo);
                    synchronized (socketApiRespnose) {
                        message.put(respNo, gson.fromJson(reqStr, SocketApiRespnose.class));
                        socketApiRespnose.notify();
                    }
                }
            } else {
                String apiStr = api.getAsString();
                //客户端发起请求
                switch (apiStr) {
                    case "/register":
                        retStr = equipmentManagerService.register(this, reqStr);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error("ClientSocket:apiManager(). request param error,e={}", e);
            return retStr;
        }
        return retStr;
    }

}
