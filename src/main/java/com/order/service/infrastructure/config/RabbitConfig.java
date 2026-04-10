package com.order.service.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbitmq")
public class RabbitConfig {

    @Value("${rabbitmq.queue.orders:orders.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.orders:orders.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.orders:orders.routing.key}")
    private String routingKey;

    @Bean
    public Queue ordersQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue ordersQueue, TopicExchange ordersExchange) {
        return BindingBuilder
                .bind(ordersQueue)
                .to(ordersExchange)
                .with(routingKey);
    }
}
