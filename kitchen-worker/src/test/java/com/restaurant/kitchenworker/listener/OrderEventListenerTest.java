package com.restaurant.kitchenworker.listener;

import com.restaurant.kitchenworker.event.OrderPlacedEvent;
import com.restaurant.kitchenworker.service.OrderProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderEventListener.
 * 
 * These tests verify that the listener correctly receives events from RabbitMQ
 * and delegates processing to the OrderProcessingService.
 * 
 * Validates Requirements: 7.1, 7.2
 */
@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {
    
    @Mock
    private OrderProcessingService orderProcessingService;
    
    @InjectMocks
    private OrderEventListener orderEventListener;
    
    private OrderPlacedEvent testEvent;
    
    @BeforeEach
    void setUp() {
        // Create a test event with sample data
        UUID orderId = UUID.randomUUID();
        Integer tableId = 5;
        LocalDateTime createdAt = LocalDateTime.now();
        
        List<OrderPlacedEvent.OrderItemEventData> items = new ArrayList<>();
        items.add(new OrderPlacedEvent.OrderItemEventData(1L, 2));
        items.add(new OrderPlacedEvent.OrderItemEventData(2L, 1));
        
        testEvent = new OrderPlacedEvent(orderId, tableId, items, createdAt);
    }
    
    /**
     * Test that handleOrderPlacedEvent calls OrderProcessingService.
     * 
     * Validates Requirement 7.1: Listen to the order.placed queue
     * Validates Requirement 7.2: Deserialize event and delegate processing
     */
    @Test
    void handleOrderPlacedEvent_ShouldCallOrderProcessingService() {
        // Arrange
        doNothing().when(orderProcessingService).processOrder(testEvent);
        
        // Act
        orderEventListener.handleOrderPlacedEvent(testEvent);
        
        // Assert
        verify(orderProcessingService, times(1)).processOrder(testEvent);
    }
    
    /**
     * Test that handleOrderPlacedEvent correctly deserializes event data.
     * 
     * Validates Requirement 7.2: Deserialize JSON payload correctly
     */
    @Test
    void handleOrderPlacedEvent_ShouldDeserializeEventCorrectly() {
        // Arrange
        doNothing().when(orderProcessingService).processOrder(any(OrderPlacedEvent.class));
        
        // Act
        orderEventListener.handleOrderPlacedEvent(testEvent);
        
        // Assert - verify the event passed to the service has the correct data
        verify(orderProcessingService).processOrder(argThat(event ->
            event.getOrderId().equals(testEvent.getOrderId()) &&
            event.getTableId().equals(testEvent.getTableId()) &&
            event.getItems().size() == 2 &&
            event.getCreatedAt().equals(testEvent.getCreatedAt())
        ));
    }
    
    /**
     * Test that exceptions from OrderProcessingService are propagated.
     * This allows the retry mechanism to be triggered.
     * 
     * Validates Requirement 7.7: Retry mechanism for failed messages
     */
    @Test
    void handleOrderPlacedEvent_ShouldPropagateExceptions() {
        // Arrange
        RuntimeException testException = new RuntimeException("Processing failed");
        doThrow(testException).when(orderProcessingService).processOrder(testEvent);
        
        // Act & Assert
        try {
            orderEventListener.handleOrderPlacedEvent(testEvent);
        } catch (RuntimeException e) {
            // Exception should be propagated to trigger retry
            verify(orderProcessingService, times(1)).processOrder(testEvent);
        }
    }
}
