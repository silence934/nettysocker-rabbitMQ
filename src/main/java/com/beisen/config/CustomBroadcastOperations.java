package com.beisen.config;

import com.beisen.controller.NettySocketEvent;
import com.beisen.entity.Message;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.namespace.Namespace;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.StoreFactory;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author : silence
 * @Date: 2021-01-22 11:03
 * @Description :
 */
@Service
public class CustomBroadcastOperations extends BroadcastOperations {


    private final Iterable<SocketIOClient> clients;
    private final StoreFactory storeFactory;

    public CustomBroadcastOperations(SocketIOServer socketIoServer) {
        super(socketIoServer.getAllClients(), socketIoServer.getConfiguration().getStoreFactory());
        clients = socketIoServer.getAllClients();
        storeFactory = socketIoServer.getConfiguration().getStoreFactory();
    }


    @Override
    public void send(Packet packet) {
        sendNotDispatch(packet);
        dispatchV2(packet);
    }

    public void sendNotDispatch(Packet packet) {
        Object ob = packet.getData();
        if (ob instanceof List) {
            List<?> list = (List<?>) ob;
            for (Object o : list) {
                if (o instanceof Message) {
                    Message message = (Message) o;
                    List<SocketIOClient> clients = NettySocketEvent.CLIENT_MAP.get(message.getTo());
                    if (!CollectionUtils.isEmpty(clients)) {
                        for (SocketIOClient client : clients) {
                            client.sendEvent(packet.getName(), message.toString());
                        }
                    }
                }
            }
        }
    }


    private void dispatchV2(Packet packet) {
        Map<String, Set<String>> namespaceRooms = new HashMap<>();
        for (SocketIOClient client : clients) {
            Namespace namespace = (Namespace) client.getNamespace();
            Set<String> rooms = namespace.getRooms(client);

            Set<String> roomsList = namespaceRooms.computeIfAbsent(namespace.getName(), k -> new HashSet<>());
            roomsList.addAll(rooms);
        }
        for (Map.Entry<String, Set<String>> entry : namespaceRooms.entrySet()) {
            for (String room : entry.getValue()) {
                storeFactory.pubSubStore().publish(PubSubType.DISPATCH, new DispatchMessage(room, packet, entry.getKey()));
            }
        }
    }
}
