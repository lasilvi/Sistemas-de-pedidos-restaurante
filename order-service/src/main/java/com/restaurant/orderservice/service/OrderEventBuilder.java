package com.restaurant.orderservice.service;

import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder for order-related events.
 * 
 * Single Responsibility: Constructs event objects from domain entities.
 * Separated from OrderService to follow SRP and facilitate event schema evolution.
 */
@Component
@Slf4j
public class OrderEventBuilder {
    
    /**
     * Builds an OrderPlacedEvent from an Order entity.
     * 
     * @param order The Order entity to convert to an event
     * @return OrderPlacedEvent ready to be published to RabbitMQ
     */
    public OrderPlacedEvent buildOrderPlacedEvent(Order order) {
        log.debug("Building OrderPlacedEvent for order {}", order.getId());
        
        List<OrderPlacedEvent.OrderItemEventData> eventItems = order.getItems().stream()
                .map(item -> new OrderPlacedEvent.OrderItemEventData(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
        
        return new OrderPlacedEvent(
                order.getId(),
                order.getTableId(),
                eventItems,
                order.getCreatedAt()
        );
    }
}
