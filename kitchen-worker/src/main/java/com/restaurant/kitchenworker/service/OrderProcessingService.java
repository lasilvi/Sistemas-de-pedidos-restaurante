package com.restaurant.kitchenworker.service;

import com.restaurant.kitchenworker.entity.Order;
import com.restaurant.kitchenworker.enums.OrderStatus;
import com.restaurant.kitchenworker.event.OrderPlacedEvent;
import com.restaurant.kitchenworker.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service responsible for processing order events received from RabbitMQ.
 * 
 * This service handles the business logic for updating order status when
 * an order placed event is received. It updates the order status to IN_PREPARATION
 * and handles error cases gracefully.
 * 
 * Validates Requirements: 7.2, 7.3, 7.4, 7.5, 7.6
 */
@Service
@Slf4j
public class OrderProcessingService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Processes an order placed event by updating the order status to IN_PREPARATION.
     * 
     * This method:
     * 1. Retrieves the order from the database using the orderId from the event
     * 2. If the order doesn't exist, logs an error and returns gracefully (no exception thrown)
     * 3. If the order exists, updates its status to IN_PREPARATION
     * 4. Saves the updated order (updatedAt is automatically updated by @PreUpdate)
     * 5. Logs successful processing information
     * 
     * Error handling:
     * - If the order is not found, logs an error and returns without throwing an exception
     *   to prevent message reprocessing (Requirement 7.6)
     * - If any other exception occurs, logs the error and re-throws it to trigger
     *   the retry mechanism (Requirement 7.7)
     * 
     * @param event The OrderPlacedEvent containing order details
     * @throws Exception if an error occurs during processing (to trigger retry)
     * 
     * Validates Requirements:
     * - 7.2: Deserialize event JSON payload
     * - 7.3: Log order details including orderId and tableId
     * - 7.4: Update order status to IN_PREPARATION
     * - 7.5: Update updatedAt timestamp
     * - 7.6: Handle non-existent orders gracefully without throwing exception
     */
    @Transactional
    public void processOrder(OrderPlacedEvent event) {
        try {
            log.info("Processing order event: orderId={}, tableId={}", 
                event.getOrderId(), event.getTableId());
            
            // Retrieve the order from the database
            Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
            
            Order order;
            if (orderOpt.isEmpty()) {
                // Order doesn't exist in kitchen-worker database, create it
                log.info("Order not found in kitchen-worker database, creating new record: orderId={}", 
                    event.getOrderId());
                order = new Order();
                order.setId(event.getOrderId());
                order.setTableId(event.getTableId());
                order.setStatus(OrderStatus.PENDING);
                order.setCreatedAt(event.getCreatedAt());
                order.setUpdatedAt(event.getCreatedAt());
            } else {
                order = orderOpt.get();
            }
            
            // Update order status to IN_PREPARATION
            order.setStatus(OrderStatus.IN_PREPARATION);
            
            // Save the order (updatedAt is automatically updated by @PreUpdate)
            orderRepository.save(order);
            
            // Log successful processing
            log.info("Order processed successfully: orderId={}, tableId={}, newStatus={}", 
                event.getOrderId(), event.getTableId(), OrderStatus.IN_PREPARATION);
                
        } catch (Exception ex) {
            // Log the error with full context
            log.error("Error processing order event: orderId={}, tableId={}, error={}", 
                event.getOrderId(), event.getTableId(), ex.getMessage(), ex);
            
            // Re-throw the exception to trigger the retry mechanism
            // This allows RabbitMQ to retry the message processing
            throw ex;
        }
    }
}
