package com.beisen.service.impl;

import com.beisen.service.RabbitMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : silence
 * @Date: 2021-01-23 20:23
 * @Description :
 */
@Slf4j
@Service
public class RabbitMQServiceImpl implements RabbitMQService {

    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Map<String, DirectMessageListenerContainer> CONTAINER_MAP = new ConcurrentHashMap<>(8);


    @Override
    public Exchange createExchange(String type, Boolean durable, String name) {
        Exchange exchange = null;
        switch (type) {
            case ExchangeTypes.FANOUT:
                exchange = ExchangeBuilder.fanoutExchange(name).durable(durable).build();
                break;
            case ExchangeTypes.TOPIC:
                exchange = ExchangeBuilder.topicExchange(name).durable(durable).build();
                break;
            case ExchangeTypes.HEADERS:
                exchange = ExchangeBuilder.headersExchange(name).durable(durable).build();
                break;
            case ExchangeTypes.DIRECT:
                exchange = ExchangeBuilder.directExchange(name).durable(durable).build();
                break;
            default:
                break;
        }
        amqpAdmin.declareExchange(exchange);
        log.info("声明交换机【{}】", name);
        return exchange;
    }

    @Override
    public Queue createQueue() {
        Queue queue = amqpAdmin.declareQueue();
        assert queue != null;
        log.info("声明队列【{}】", queue.getName());
        return queue;
    }

    @Override
    public Binding queueBindExchange(Queue queue, Exchange exchange) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with("").noargs();
        amqpAdmin.declareBinding(binding);
        log.info("绑定队列【{}】到交换机【{}】", queue.getName(), exchange.getName());
        return binding;
    }

    @Override
    public void queueAddListener(Queue queue, MessageListener listener) {
        DirectMessageListenerContainer container = new DirectMessageListenerContainer(rabbitTemplate.getConnectionFactory());
        container.setQueueNames(queue.getName());
        container.setExposeListenerChannel(true);
        container.setPrefetchCount(1);
        container.setConsumersPerQueue(1);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setMessageListener(listener);
        container.start();
        CONTAINER_MAP.put(queue.getName(), container);
    }

    @Override
    public void deleteQueueAndRemoveListener(String queueName) {
        DirectMessageListenerContainer container = CONTAINER_MAP.get(queueName);
        if (container != null) {
            container.stop();
            container.destroy();
            CONTAINER_MAP.remove(queueName);
        }
        log.info("停止监听队列{}", queueName);
        amqpAdmin.deleteQueue(queueName);
        log.info("成功删除队列{}", queueName);
    }

}
