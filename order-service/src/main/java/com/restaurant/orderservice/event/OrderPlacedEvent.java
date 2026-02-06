package com.restaurant.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event published when an order is successfully placed.
 * This event is sent to RabbitMQ for asynchronous processing by the Kitchen Worker.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier of the order
     */
    private UUID orderId;
    
    /**
     * Table number where the order was placed
     */
    private Integer tableId;
    
    /**
     * List of items in the order
     */
    private List<OrderItemEventData> items;
    
    /**
     * Timestamp when the order was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Inner class representing an order item in the event payload.
     * Contains minimal information needed for order processing.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEventData implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * ID of the product being ordered
         */
        private Long productId;
        
        /**
         * Quantity of the product
         */
        private Integer quantity;
    }
}
