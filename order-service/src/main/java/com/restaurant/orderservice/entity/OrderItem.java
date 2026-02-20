package com.restaurant.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an item within an order.
 * 
 * Each order item references a product by its ID and specifies the quantity ordered.
 * Optional notes can be added for special instructions (e.g., "no onions").
 * 
 * Validates Requirements: 2.1, 9.1
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItem {
    
    /**
     * Unique identifier for the order item.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    /**
     * Reference to the parent order.
     * Configured with LAZY loading for performance.
     * Cannot be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    /**
     * ID of the product being ordered.
     * Stored as a foreign key reference to the products table.
     * Cannot be null.
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    /**
     * Quantity of the product being ordered.
     * Must be a positive integer.
     * Cannot be null.
     */
    @Column(nullable = false)
    private Integer quantity;
    
    /**
     * Optional notes or special instructions for this item.
     * Stored as TEXT to allow longer notes.
     * Can be null.
     */
    @Column(columnDefinition = "TEXT")
    private String note;
}
