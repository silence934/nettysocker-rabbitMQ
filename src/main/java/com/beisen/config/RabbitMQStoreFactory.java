package com.beisen.config;

import com.beisen.service.RabbitMQService;
import com.beisen.utils.SpringContextUtil;
import com.corundumstudio.socketio.handler.AuthorizeHandler;
import com.corundumstudio.socketio.namespace.NamespacesHub;
import com.corundumstudio.socketio.protocol.JsonSupport;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.MemoryStore;
import com.corundumstudio.socketio.store.Store;
import com.corundumstudio.socketio.store.pubsub.BaseStoreFactory;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import io.netty.util.internal.PlatformDependent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * @Author : silence
 * @Date: 2021-01-22 21:00
 * @Description :
 */
public class RabbitMQStoreFactory extends BaseStoreFactory {

    private final PubSubStore pubSubStore;

    public RabbitMQStoreFactory(RabbitTemplate rabbitPub, RabbitMQService rabbitMQService) {
        this.pubSubStore = new RabbitMQPubSubStore(rabbitPub, rabbitMQService, getNodeId());
    }


    @Override
    public void init(NamespacesHub namespacesHub, AuthorizeHandler authorizeHandler, JsonSupport jsonSupport) {
        //只对 DISPATCH 消息进行集群共享
        pubSubStore.subscribe(PubSubType.DISPATCH,
                data -> {
                    Packet packet = data.getPacket();
                    SpringContextUtil.getBeanByClass(CustomBroadcastOperations.class).sendNotDispatch(packet);
                },
                DispatchMessage.class);
    }

    @Override
    public PubSubStore pubSubStore() {
        return pubSubStore;
    }

    @Override
    public <K, V> Map<K, V> createMap(String name) {
        return PlatformDependent.newConcurrentHashMap();
    }

    @Override
    public Store createStore(UUID sessionId) {
        return new MemoryStore();
    }

    @Override
    public void shutdown() {

    }
}
