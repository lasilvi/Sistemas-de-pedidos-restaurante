package com.restaurant.orderservice.service;

import com.restaurant.orderservice.event.OrderPlacedEvent;
import com.restaurant.orderservice.exception.EventPublicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing order events to RabbitMQ.
 * Handles the asynchronous communication between Order Service and Kitchen Worker.
 */
@Service
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing-key.order-placed}")
    private String orderPlacedRoutingKey;

    /**
     * Constructor for dependency injection.
     * 
     * @param rabbitTemplate Spring AMQP template for sending messages to RabbitMQ
     */
    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes an order placed event to RabbitMQ.
     * If publishing fails, the error is logged and a runtime exception is thrown
     * so the caller can trigger transaction rollback.
     * 
     * @param event The OrderPlacedEvent containing order details
     */
    public void publishOrderPlacedEvent(OrderPlacedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, orderPlacedRoutingKey, event);
            log.info("Successfully published order.placed event: orderId={}, tableId={}", 
                    event.getOrderId(), event.getTableId());
        } catch (Exception ex) {
            log.error("Failed to publish order.placed event: orderId={}, tableId={}, error={}", 
                    event.getOrderId(), event.getTableId(), ex.getMessage(), ex);
            throw new EventPublicationException(
                    String.format("Unable to publish order.placed event for orderId=%s", event.getOrderId()),
                    ex
            );
        }
    }
}
