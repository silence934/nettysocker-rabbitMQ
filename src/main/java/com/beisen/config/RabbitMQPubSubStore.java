package com.beisen.config;

import com.beisen.service.RabbitMQService;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author : silence
 * @Date: 2021-01-22 21:04
 * @Description :
 */
@Slf4j
public class RabbitMQPubSubStore implements PubSubStore {

    private MessageConverter messageConverter = new SimpleMessageConverter();
    private final RabbitTemplate rabbitPub;
    private final RabbitMQService rabbitMQService;
    private final Long nodeId;

    private final ConcurrentMap<String, Queue<String>> map = PlatformDependent.newConcurrentHashMap();

    public RabbitMQPubSubStore(RabbitTemplate rabbitPub, RabbitMQService rabbitMQService, Long nodeId) {
        this.rabbitPub = rabbitPub;
        this.rabbitMQService = rabbitMQService;
        this.nodeId = nodeId;
    }

    @Override
    public void publish(PubSubType type, PubSubMessage msg) {
        msg.setNodeId(nodeId);
        if (type == PubSubType.DISPATCH) {
            rabbitPub.convertSendAndReceive(type.toString(), "", msg);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PubSubMessage> void subscribe(PubSubType type, PubSubListener<T> listener, Class<T> clazz) {
        String name = type.toString();

        Exchange exchange = rabbitMQService.createExchange(ExchangeTypes.FANOUT, false, name);
        org.springframework.amqp.core.Queue queue = rabbitMQService.createQueue();
        rabbitMQService.queueBindExchange(queue, exchange);

        rabbitMQService.queueAddListener(queue, message -> {
            T t = (T) messageConverter.fromMessage(message);
            if (!nodeId.equals(t.getNodeId())) {
                listener.onMessage(t);
            }
        });

        Queue<String> list = map.get(name);
        if (list == null) {
            list = new ConcurrentLinkedQueue<>();
            Queue<String> oldList = map.putIfAbsent(name, list);
            if (oldList != null) {
                list = oldList;
            }
        }
        list.add(queue.getName());
    }

    @Override
    public void unsubscribe(PubSubType type) {
        String name = type.toString();
        Queue<String> queueNames = map.remove(name);
        for (String queueName : queueNames) {
            rabbitMQService.deleteQueueAndRemoveListener(queueName);
        }
    }

    @Override
    public void shutdown() {

    }

    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

}
