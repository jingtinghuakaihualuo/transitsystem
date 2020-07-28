package com.example.transitsystem.controller;

import com.example.transitsystem.service.ClientSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 长链接服务端
 * 用于客户端连接通讯
 */

@Slf4j
@Data
@Component
public class DelongServerSocket {

    @Value("${socket.port}")
    private Integer port;
    private boolean started;
    private ServerSocket ss;
    public static ConcurrentHashMap<Long, ClientSocket> tokenMappingclient = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<ClientSocket, Long> clientMappingToken = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new DelongServerSocket().start(null);
    }


    public void start(Integer port) {
        try {
            ss = new ServerSocket(port == null ? this.port : port);
            started = true;
            System.out.println("端口已开启,占用10086端口号....");
        } catch (Exception e) {
            System.out.println("端口使用中....");
            System.out.println("请关掉相关程序并重新运行服务器！");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            while (started) {
                Socket socket = ss.accept();
                socket.setKeepAlive(true);
                log.info("socket == null ? =" + (socket == null));
                ClientSocket register = ClientSocket.register(socket);
                System.out.println("a client connected!");
                if (register != null) {
                    executorService.submit(register);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
