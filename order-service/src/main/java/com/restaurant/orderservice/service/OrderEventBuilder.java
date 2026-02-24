package com.restaurant.orderservice.service;

import com.restaurant.orderservice.domain.event.OrderPlacedDomainEvent;
import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.entity.Product;
import com.restaurant.orderservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builder for order-related domain events.
 * 
 * Single Responsibility: Constructs domain event objects from order entities.
 * Separated from OrderService to follow SRP and facilitate event schema evolution.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventBuilder {
    
    private final ProductRepository productRepository;
    
    /**
     * Builds an OrderPlacedDomainEvent from an Order entity.
     * Enriches each item with product name and price from the catalog.
     * 
     * @param order The Order entity to convert to an event
     * @return OrderPlacedDomainEvent ready to be published through the output port
     */
    public OrderPlacedDomainEvent buildOrderPlacedEvent(Order order) {
        log.debug("Building OrderPlacedDomainEvent for order {}", order.getId());
        
        // Batch-fetch products for enrichment
        List<Long> productIds = order.getItems().stream()
                .map(item -> item.getProductId())
                .collect(Collectors.toList());
        
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        
        List<OrderPlacedDomainEvent.OrderItemData> eventItems = order.getItems().stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    return OrderPlacedDomainEvent.OrderItemData.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(product != null ? product.getPrice() : BigDecimal.ZERO)
                            .productName(product != null ? product.getName() : null)
                            .build();
                })
                .collect(Collectors.toList());
        
        return OrderPlacedDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderPlacedDomainEvent.EVENT_TYPE)
                .eventVersion(OrderPlacedDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(order.getId())
                .tableId(order.getTableId())
                .items(eventItems)
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Builds an OrderReadyDomainEvent from an Order entity.
     * 
     * @param order The Order entity to convert to an event
     * @return OrderReadyDomainEvent ready to be published through the output port
     */
    public OrderReadyDomainEvent buildOrderReadyEvent(Order order) {
        log.debug("Building OrderReadyDomainEvent for order {}", order.getId());
        
        return OrderReadyDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderReadyDomainEvent.EVENT_TYPE)
                .eventVersion(OrderReadyDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(order.getId())
                .status(order.getStatus().name())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
