package com.restaurant.kitchenworker.service;

import com.restaurant.kitchenworker.entity.Order;
import com.restaurant.kitchenworker.enums.OrderStatus;
import com.restaurant.kitchenworker.event.OrderPlacedEvent;
import com.restaurant.kitchenworker.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderProcessingService.
 * 
 * Tests verify that the service correctly processes order events,
 * handles non-existent orders gracefully, and updates order status
 * and timestamps as expected.
 * 
 * Validates Requirements: 7.4, 7.5, 7.6
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderProcessingService orderProcessingService;
    
    private UUID orderId;
    private OrderPlacedEvent event;
    private Order order;
    
    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        
        // Create a sample event
        event = new OrderPlacedEvent();
        event.setOrderId(orderId);
        event.setTableId(5);
        event.setItems(new ArrayList<>());
        event.setCreatedAt(LocalDateTime.now());
        
        // Create a sample order
        order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
    }
    
    /**
     * Test: processOrder with existing orderId updates status to IN_PREPARATION
     * 
     * Validates Requirements: 7.4
     */
    @Test
    void processOrder_WithExistingOrderId_UpdatesStatusToInPreparation() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        orderProcessingService.processOrder(event);
        
        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
    }
    
    /**
     * Test: processOrder with non-existent orderId does not throw exception
     * 
     * Validates Requirements: 7.6
     */
    @Test
    void processOrder_WithNonExistentOrderId_DoesNotThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        // Act & Assert - should not throw exception
        orderProcessingService.processOrder(event);
        
        // Verify that findById was called but save was not
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    /**
     * Test: processOrder updates the order in the database
     * 
     * Validates Requirements: 7.5
     */
    @Test
    void processOrder_SavesUpdatedOrder() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        orderProcessingService.processOrder(event);
        
        // Assert
        verify(orderRepository).save(order);
    }
    
    /**
     * Test: processOrder with database exception re-throws exception
     * 
     * This test verifies that exceptions during processing are re-thrown
     * to trigger the retry mechanism.
     */
    @Test
    void processOrder_WithDatabaseException_ReThrowsException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        assertThatThrownBy(() -> orderProcessingService.processOrder(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
        
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    /**
     * Test: processOrder with save exception re-throws exception
     * 
     * This test verifies that exceptions during save are re-thrown
     * to trigger the retry mechanism.
     */
    @Test
    void processOrder_WithSaveException_ReThrowsException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Save failed"));
        
        // Act & Assert
        assertThatThrownBy(() -> orderProcessingService.processOrder(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Save failed");
        
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }
}
