package com.restaurant.orderservice.repository;

import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Order entity operations.
 * 
 * Provides database access methods for Order entities, including
 * standard CRUD operations and custom query methods for filtering orders.
 * 
 * Validates Requirements: 4.1, 5.1, 6.1
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    /**
     * Finds all orders with the specified status.
     * 
     * This method is used to filter orders by their current status,
     * allowing the restaurant staff to view orders at specific stages
     * of preparation (PENDING, IN_PREPARATION, or READY).
     * 
     * @param status The order status to filter by
     * @return List of orders matching the specified status. Returns empty list if no orders match.
     * 
     * Validates Requirements: 5.1, 5.2, 5.4
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds all orders with any of the specified statuses.
     *
     * This method allows filtering by multiple statuses in a single query,
     * which is useful for kitchen views that need PENDING, IN_PREPARATION,
     * and READY orders together.
     *
     * @param statuses List of order statuses to include
     * @return List of orders matching any of the specified statuses.
     */
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}
