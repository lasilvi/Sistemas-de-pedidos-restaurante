package com.restaurant.orderservice.service;

import com.restaurant.orderservice.dto.*;
import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.entity.OrderItem;
import com.restaurant.orderservice.entity.Product;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.event.OrderPlacedEvent;
import com.restaurant.orderservice.exception.InvalidOrderException;
import com.restaurant.orderservice.exception.OrderNotFoundException;
import com.restaurant.orderservice.exception.ProductNotFoundException;
import com.restaurant.orderservice.repository.OrderRepository;
import com.restaurant.orderservice.service.command.OrderCommandExecutor;
import com.restaurant.orderservice.service.command.PublishOrderPlacedEventCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing order operations.
 * 
 * Refactored to follow Single Responsibility Principle (SRP).
 * This service now focuses solely on orchestration, delegating to specialized components:
 * - OrderValidator: Business rule validation
 * - OrderMapper: Entity-DTO mapping (with N+1 optimization)
 * - OrderEventBuilder: Event construction
 * - OrderEventPublisher: Event publishing
 * 
 * Validates Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 3.1, 4.1, 4.2, 5.1, 5.2, 6.2
 */
@Service
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final OrderMapper orderMapper;
    private final OrderEventBuilder orderEventBuilder;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderCommandExecutor orderCommandExecutor;
    
    /**
     * Constructor for OrderService.
     * 
     * @param orderRepository Repository for accessing order data
     * @param orderValidator Validator for order business rules
     * @param orderMapper Mapper for entity-DTO conversions
     * @param orderEventBuilder Builder for order events
     * @param orderEventPublisher Service for publishing order events to RabbitMQ
     */
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       OrderValidator orderValidator,
                       OrderMapper orderMapper,
                       OrderEventBuilder orderEventBuilder,
                       OrderEventPublisher orderEventPublisher,
                       OrderCommandExecutor orderCommandExecutor) {
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
        this.orderMapper = orderMapper;
        this.orderEventBuilder = orderEventBuilder;
        this.orderEventPublisher = orderEventPublisher;
        this.orderCommandExecutor = orderCommandExecutor;
    }
    
    /**
     * Creates a new order with the specified items for a table.
     * 
     * Orchestrates the order creation process by delegating to specialized components.
     * 
     * @param request CreateOrderRequest containing tableId and list of items
     * @return OrderResponse with the created order details
     * @throws ProductNotFoundException if any product does not exist or is inactive
     * @throws InvalidOrderException if tableId is invalid or items list is empty
     * 
     * Validates Requirements:
     * - 2.1: Order Service exposes POST /orders endpoint accepting tableId and items
     * - 2.2: Validates that all productIds exist and are active
     * - 2.3: Persists order with status PENDING in PostgreSQL
     * - 2.4: Generates unique UUID as order identifier
     * - 2.5: Automatically sets createdAt and updatedAt timestamps
     * - 2.6: Rejects order if productId doesn't exist or is inactive
     * - 2.7: Rejects order if tableId is missing or invalid
     * - 2.8: Rejects order if items list is empty
     * - 3.1: Publishes "order.placed" event to RabbitMQ after successful creation
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for table {}", request.getTableId());
        
        // Delegate validation to OrderValidator
        orderValidator.validateCreateOrderRequest(request);
        
        // Create Order entity
        Order order = new Order();
        order.setTableId(request.getTableId());
        order.setStatus(OrderStatus.PENDING);
        
        // Create OrderItem entities and associate with order
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductId(itemRequest.getProductId());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setNote(itemRequest.getNote());
                    return orderItem;
                })
                .collect(Collectors.toList());
        
        order.setItems(orderItems);
        
        // Save order to database (timestamps are set automatically by @PrePersist)
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created successfully: orderId={}, tableId={}, itemCount={}", 
                savedOrder.getId(), savedOrder.getTableId(), savedOrder.getItems().size());
        
        // Delegate event building and publishing via command
        OrderPlacedEvent event = orderEventBuilder.buildOrderPlacedEvent(savedOrder);
        orderCommandExecutor.execute(new PublishOrderPlacedEventCommand(orderEventPublisher, event));
        
        // Delegate mapping to OrderMapper
        return orderMapper.mapToOrderResponse(savedOrder);
    }
    
    /**
     * Retrieves an order by its unique identifier.
     * 
     * @param orderId UUID of the order to retrieve
     * @return OrderResponse with complete order details
     * @throws OrderNotFoundException if the order does not exist
     * 
     * Validates Requirements:
     * - 4.1: Order Service exposes GET /orders/{id} endpoint
     * - 4.2: Returns complete order with all items, status, and timestamps
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        log.info("Retrieving order by id: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Delegate mapping to OrderMapper
        return orderMapper.mapToOrderResponse(order);
    }
    
    /**
     * Retrieves orders, optionally filtered by status.
     * 
     * If status is null, returns all orders.
     * If status is provided, returns only orders with that status.
     * 
     * @param status Optional OrderStatus to filter by (can be null)
     * @return List of OrderResponse matching the filter criteria
     * 
     * Validates Requirements:
     * - 5.1: Order Service exposes GET /orders with optional status parameter
     * - 5.2: Returns only orders matching the specified status when provided
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(List<OrderStatus> status) {
        log.info("Retrieving orders with status filter: {}", status);
        
        List<Order> orders;
        if (status == null || status.isEmpty()) {
            // Return all orders
            orders = orderRepository.findAll();
        } else {
            // Return orders filtered by any of the provided statuses
            orders = orderRepository.findByStatusIn(status);
        }
        
        // Delegate mapping to OrderMapper (optimized for batch)
        return orderMapper.mapToOrderResponseList(orders);
    }
    
    /**
     * Updates the status of an existing order.
     * 
     * The updatedAt timestamp is automatically updated by the @PreUpdate callback.
     * 
     * @param orderId UUID of the order to update
     * @param newStatus New status to set for the order
     * @return OrderResponse with updated order details
     * @throws OrderNotFoundException if the order does not exist
     * 
     * Validates Requirements:
     * - 6.2: Updates order status and updatedAt timestamp
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        log.info("Updating order status: orderId={}, newStatus={}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.setStatus(newStatus);
        // updatedAt is automatically updated by @PreUpdate
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order status updated successfully: orderId={}, status={}", 
                updatedOrder.getId(), updatedOrder.getStatus());
        
        // Delegate mapping to OrderMapper
        return orderMapper.mapToOrderResponse(updatedOrder);
    }
}
