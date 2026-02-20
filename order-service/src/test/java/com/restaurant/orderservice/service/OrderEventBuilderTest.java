package com.restaurant.orderservice.service;

import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.entity.OrderItem;
import com.restaurant.orderservice.entity.Product;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.domain.event.OrderPlacedDomainEvent;
import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import com.restaurant.orderservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OrderEventBuilder.
 * 
 * Tests event construction from domain entities.
 */
@ExtendWith(MockitoExtension.class)
class OrderEventBuilderTest {
    
    @Mock
    private ProductRepository productRepository;
    
    private OrderEventBuilder orderEventBuilder;
    
    @BeforeEach
    void setUp() {
        orderEventBuilder = new OrderEventBuilder(productRepository);
    }
    
    @Test
    void buildOrderPlacedEvent_withSingleItem_createsCorrectEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        Product product10 = new Product(10L, "Burger", "Juicy burger", true);
        product10.setPrice(new BigDecimal("8.99"));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product10));
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(createdAt);
        
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(10L);
        item.setQuantity(2);
        item.setNote("No onions");
        
        order.setItems(List.of(item));
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getTableId()).isEqualTo(5);
        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
        assertThat(event.getItems()).hasSize(1);
        
        OrderPlacedDomainEvent.OrderItemData eventItem = event.getItems().get(0);
        assertThat(eventItem.getProductId()).isEqualTo(10L);
        assertThat(eventItem.getQuantity()).isEqualTo(2);
    }
    
    @Test
    void buildOrderPlacedEvent_withMultipleItems_includesAllItems() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        when(productRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(3);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductId(10L);
        item1.setQuantity(2);
        
        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductId(20L);
        item2.setQuantity(1);
        
        OrderItem item3 = new OrderItem();
        item3.setId(3L);
        item3.setProductId(30L);
        item3.setQuantity(3);
        
        order.setItems(List.of(item1, item2, item3));
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event.getItems()).hasSize(3);
        assertThat(event.getItems().get(0).getProductId()).isEqualTo(10L);
        assertThat(event.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(event.getItems().get(1).getProductId()).isEqualTo(20L);
        assertThat(event.getItems().get(1).getQuantity()).isEqualTo(1);
        assertThat(event.getItems().get(2).getProductId()).isEqualTo(30L);
        assertThat(event.getItems().get(2).getQuantity()).isEqualTo(3);
    }
    
    @Test
    void buildOrderPlacedEvent_withEmptyItems_createsEventWithEmptyItemsList() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        when(productRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getItems()).isEmpty();
    }
    
    @Test
    void buildOrderPlacedEvent_doesNotIncludeItemNotes() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        when(productRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(10L);
        item.setQuantity(2);
        item.setNote("This note should not be in the event");
        
        order.setItems(List.of(item));
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        OrderPlacedDomainEvent.OrderItemData eventItem = event.getItems().get(0);
        assertThat(eventItem.getProductId()).isEqualTo(10L);
        assertThat(eventItem.getQuantity()).isEqualTo(2);
        // OrderItemEventData only has productId and quantity, no note field
    }
    
    @Test
    void buildOrderPlacedEvent_preservesOrderMetadata() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime specificTime = LocalDateTime.of(2026, 2, 12, 10, 30, 0);
        
        when(productRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(7);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(specificTime);
        order.setItems(new ArrayList<>());
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getTableId()).isEqualTo(7);
        assertThat(event.getCreatedAt()).isEqualTo(specificTime);
    }
    
    @Test
    void buildOrderPlacedEvent_withLargeQuantities_handlesCorrectly() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        when(productRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(10L);
        item.setQuantity(100);
        
        order.setItems(List.of(item));
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event.getItems().get(0).getQuantity()).isEqualTo(100);
    }
    
    @Test
    void buildOrderPlacedEvent_maintainsItemOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        when(productRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        List<OrderItem> items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            OrderItem item = new OrderItem();
            item.setId((long) i);
            item.setProductId((long) (i * 10));
            item.setQuantity(i);
            items.add(item);
        }
        
        order.setItems(items);
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event.getItems()).hasSize(5);
        for (int i = 0; i < 5; i++) {
            assertThat(event.getItems().get(i).getProductId()).isEqualTo((long) ((i + 1) * 10));
            assertThat(event.getItems().get(i).getQuantity()).isEqualTo(i + 1);
        }
    }
    
    @Test
    void shouldEnrichItemsWithProductNameAndPrice() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        Product product1 = new Product(10L, "Pizza Margherita", "Classic pizza", true);
        product1.setPrice(new BigDecimal("12.50"));
        Product product2 = new Product(20L, "Coca Cola", "Refreshing drink", true);
        product2.setPrice(new BigDecimal("3.00"));
        
        when(productRepository.findAllById(List.of(10L, 20L)))
                .thenReturn(List.of(product1, product2));
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductId(10L);
        item1.setQuantity(2);
        
        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductId(20L);
        item2.setQuantity(1);
        
        order.setItems(List.of(item1, item2));
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event.getItems()).hasSize(2);
        
        OrderPlacedDomainEvent.OrderItemData eventItem1 = event.getItems().get(0);
        assertThat(eventItem1.getProductId()).isEqualTo(10L);
        assertThat(eventItem1.getQuantity()).isEqualTo(2);
        assertThat(eventItem1.getPrice()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(eventItem1.getProductName()).isEqualTo("Pizza Margherita");
        
        OrderPlacedDomainEvent.OrderItemData eventItem2 = event.getItems().get(1);
        assertThat(eventItem2.getProductId()).isEqualTo(20L);
        assertThat(eventItem2.getQuantity()).isEqualTo(1);
        assertThat(eventItem2.getPrice()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(eventItem2.getProductName()).isEqualTo("Coca Cola");
    }
    
    @Test
    void shouldHandleMissingProductGracefully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        
        // ProductRepository returns empty — product not found
        when(productRepository.findAllById(List.of(99L)))
                .thenReturn(Collections.emptyList());
        
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(3);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(99L);
        item.setQuantity(1);
        
        order.setItems(List.of(item));
        
        // Act
        OrderPlacedDomainEvent event = orderEventBuilder.buildOrderPlacedEvent(order);
        
        // Assert
        assertThat(event.getItems()).hasSize(1);
        OrderPlacedDomainEvent.OrderItemData eventItem = event.getItems().get(0);
        assertThat(eventItem.getProductId()).isEqualTo(99L);
        assertThat(eventItem.getQuantity()).isEqualTo(1);
        assertThat(eventItem.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(eventItem.getProductName()).isNull();
    }

    @Test
    void buildOrderReadyEvent_createsCorrectEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.of(2026, 2, 20, 12, 0, 0);

        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.READY);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(updatedAt);
        order.setItems(new ArrayList<>());

        // Act
        OrderReadyDomainEvent event = orderEventBuilder.buildOrderReadyEvent(order);

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo("order.ready");
        assertThat(event.getEventVersion()).isEqualTo(1);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getStatus()).isEqualTo("READY");
        assertThat(event.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
