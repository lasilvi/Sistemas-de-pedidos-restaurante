package com.restaurant.kitchenworker.listener;

import com.restaurant.kitchenworker.event.OrderPlacedEvent;
import com.restaurant.kitchenworker.service.OrderProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener that consumes order placed events from the message queue.
 * 
 * This component listens to the configured RabbitMQ queue and delegates
 * event processing to the OrderProcessingService. It acts as the entry point
 * for asynchronous order processing in the Kitchen Worker service.
 * 
 * The listener is configured to:
 * - Listen to the queue specified in application.yml (rabbitmq.queue.name)
 * - Automatically deserialize JSON messages to OrderPlacedEvent objects
 * - Acknowledge messages after successful processing
 * - Retry failed messages according to the configured retry policy
 * - Route messages to the Dead Letter Queue after max retry attempts
 * 
 * Validates Requirements: 7.1, 7.2
 */
@Component
@Slf4j
public class OrderEventListener {
    
    @Autowired
    private OrderProcessingService orderProcessingService;
    
    /**
     * Handles incoming order placed events from RabbitMQ.
     * 
     * This method is automatically invoked by Spring AMQP when a message
     * arrives in the configured queue. The message is automatically deserialized
     * from JSON to an OrderPlacedEvent object using the configured MessageConverter.
     * 
     * Processing flow:
     * 1. Receive and deserialize the OrderPlacedEvent from the queue
     * 2. Log the received event for monitoring and debugging
     * 3. Delegate processing to OrderProcessingService
     * 4. If processing succeeds, the message is acknowledged
     * 5. If processing fails, the exception triggers the retry mechanism
     * 
     * Error handling:
     * - Exceptions thrown by OrderProcessingService will trigger message retry
     * - After max retry attempts, the message is routed to the Dead Letter Queue
     * - Messages with non-existent orders are acknowledged without retry
     * 
     * @param event The OrderPlacedEvent deserialized from the queue message
     * 
     * Validates Requirements:
     * - 7.1: Listen to the "order.placed" queue bound to the topic exchange
     * - 7.2: Deserialize JSON payload to OrderPlacedEvent
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received order placed event from queue: orderId={}, tableId={}", 
            event.getOrderId(), event.getTableId());
        
        // Delegate processing to the service layer
        orderProcessingService.processOrder(event);
    }
}
