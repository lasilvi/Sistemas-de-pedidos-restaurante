package com.restaurant.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Order Service.
 * Configures the topic exchange, queues, bindings, and message converter for order event publishing.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.order-placed}")
    private String orderPlacedRoutingKey;

    private static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    private static final String ORDER_PLACED_DLQ = "order.placed.dlq";

    /**
     * Declares the topic exchange for order events.
     * Topic exchanges route messages to queues based on routing key patterns.
     * 
     * @return TopicExchange configured as durable
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Declares the main queue for order.placed events.
     * This queue will receive messages published with the "order.placed" routing key.
     * 
     * @return Queue configured as durable
     */
    @Bean
    public Queue orderPlacedQueue() {
        return new Queue(ORDER_PLACED_QUEUE, true);
    }

    /**
     * Declares the Dead Letter Queue for failed order.placed messages.
     * Messages that fail processing after retries will be routed here for manual inspection.
     * 
     * @return Queue configured as durable
     */
    @Bean
    public Queue orderPlacedDLQ() {
        return new Queue(ORDER_PLACED_DLQ, true);
    }

    /**
     * Binds the order.placed queue to the order exchange with the specified routing key.
     * Messages published to the exchange with routing key "order.placed" will be routed to this queue.
     * 
     * @return Binding between queue and exchange
     */
    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with(orderPlacedRoutingKey);
    }

    /**
     * Configures the message converter to use Jackson for JSON serialization/deserialization.
     * This allows automatic conversion of Java objects to JSON when publishing messages.
     * 
     * @return MessageConverter configured for JSON
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
