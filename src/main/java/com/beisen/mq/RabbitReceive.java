package com.beisen.mq;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author : silence
 * @Date: 2021-01-22 19:12
 * @Description :
 */
@Component
public class RabbitReceive {


    @Value("server.port")
    private String port;

    @Value("server.address")
    private String address;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue-2"),
            exchange = @Exchange(name = "exchange-1", type = ExchangeTypes.FANOUT)
    ))
    @RabbitHandler
    public void onMessage(Object message) {
        System.err.println("-----------------------");
        System.err.println("消费消息:" + message);
    }


}
