package com.beisen.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;

/**
 * @Author : silence
 * @Date: 2021-01-23 20:23
 * @Description :
 */
public interface RabbitMQService {
    Exchange createExchange(String type, Boolean durable, String name);

    Queue createQueue();

    Binding queueBindExchange(Queue queue, Exchange exchange);

    void queueAddListener(Queue queue, MessageListener listener);

    void deleteQueueAndRemoveListener(String queueName);
}
