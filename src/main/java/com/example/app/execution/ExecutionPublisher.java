package com.example.app.execution;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
// take a message and put it into RabbitMQ.
public class ExecutionPublisher {

    private final AmqpTemplate amqpTemplate;
    // Spring's abstraction for sending messages to RabbitMQ

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void publish(ExecutionMessage message) {
        amqpTemplate.convertAndSend(exchange, routingKey, message);
    }
}