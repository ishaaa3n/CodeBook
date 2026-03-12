package com.example.app.config;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.execution}")
    private String executionQueue;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Bean
    public Queue executionQueue() {
        return new Queue(executionQueue, true);
        // The true means durable. If RabbitMQ restarts, the queue survives. 
        // If false, the queue disappears on restart and all pending messages are lost.

    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
        // Creates the exchange with  configured name.
        // Direct exchange means one routing key maps to exactly one queue — simple and predictable.

    
    }
    @Bean
    public Binding binding(Queue executionQueue, DirectExchange exchange) {
        return BindingBuilder
                .bind(executionQueue)
                .to(exchange)
                .with(routingKey);
// message arrives at exchange with key "execution.routing.key"
//       ↓
// binding says "this key goes to execution.queue"
//       ↓
// message lands in execution.queue
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
        // RabbitMQ sends messages as bytes. 
        //  Java objects need to be converted to bytes before
        // sending and back to Java objects after receiving.

    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    // This is the main tool use to send messages to RabbitMQ. 

    }
}
