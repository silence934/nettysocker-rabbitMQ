package com.beisen.controller;

import com.beisen.config.CustomBroadcastOperations;
import com.beisen.entity.Message;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : silence
 * @Date: 2020-11-12 10:06
 * @Description :
 */
@Slf4j
@Component
public class NettySocketEvent {

    @Autowired
    private CustomBroadcastOperations customBroadcastOperations;


    public static final Map<String, List<SocketIOClient>> CLIENT_MAP = new ConcurrentHashMap<>();

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String mac = client.getHandshakeData().getSingleUrlParam("mac");

        List<SocketIOClient> clients = CLIENT_MAP.computeIfAbsent(mac, k -> new ArrayList<>());
        clients.add(client);

        client.sendEvent("messageevent", "嗨," + mac + " 你好! 咱们已建立连接");
        log.info("客户端:" + client.getSessionId() + "已连接,mac=" + mac);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String mac = client.getHandshakeData().getSingleUrlParam("mac");
        List<SocketIOClient> clients = CLIENT_MAP.get(mac);
        clients.remove(client);
        log.info("客户端:" + client.getSessionId() + "断开连接");
    }

    @OnEvent(value = "sendEvent")
    public void receiveMessage(Message message) {
        customBroadcastOperations.sendEvent("messageevent", message);
    }

}
