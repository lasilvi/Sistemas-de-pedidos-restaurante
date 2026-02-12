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
import com.restaurant.orderservice.repository.ProductRepository;
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
 * Provides business logic for creating, retrieving, filtering, and updating orders.
 * Handles validation of order data, product availability, and event publishing.
 * 
 * Validates Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 3.1, 4.1, 4.2, 5.1, 5.2, 6.2
 */
@Service
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderCommandExecutor orderCommandExecutor;
    
    /**
     * Constructor for OrderService.
     * 
     * @param orderRepository Repository for accessing order data
     * @param productRepository Repository for accessing product data
     * @param orderEventPublisher Service for publishing order events to RabbitMQ
     */
    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       ProductRepository productRepository,
                       OrderEventPublisher orderEventPublisher,
                       OrderCommandExecutor orderCommandExecutor) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.orderCommandExecutor = orderCommandExecutor;
    }
    
    /**
     * Creates a new order with the specified items for a table.
     * 
     * This method performs the following operations:
     * 1. Validates that all referenced products exist and are active
     * 2. Validates that tableId is valid and items list is not empty
     * 3. Creates an Order entity with status PENDING
     * 4. Creates associated OrderItem entities
     * 5. Persists the order to the database
     * 6. Publishes an OrderPlacedEvent to RabbitMQ
     * 7. Returns the created order as an OrderResponse
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
        
        // Validate tableId (additional validation beyond @Valid annotation)
        if (request.getTableId() == null || request.getTableId() <= 0) {
            throw new InvalidOrderException("Table ID must be a positive integer");
        }
        
        // Validate items list is not empty
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }
        
        // Validate that all products exist and are active
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            
            if (!product.getIsActive()) {
                throw new ProductNotFoundException(itemRequest.getProductId());
            }
        }
        
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
        
        // Build and publish OrderPlacedEvent
        OrderPlacedEvent event = buildOrderPlacedEvent(savedOrder);
        orderCommandExecutor.execute(new PublishOrderPlacedEventCommand(orderEventPublisher, event));
        
        // Map to OrderResponse and return
        return mapToOrderResponse(savedOrder);
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
        
        return mapToOrderResponse(order);
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
        
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
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
        
        return mapToOrderResponse(updatedOrder);
    }
    
    /**
     * Builds an OrderPlacedEvent from an Order entity.
     * 
     * @param order The Order entity to convert to an event
     * @return OrderPlacedEvent ready to be published to RabbitMQ
     */
    private OrderPlacedEvent buildOrderPlacedEvent(Order order) {
        List<OrderPlacedEvent.OrderItemEventData> eventItems = order.getItems().stream()
                .map(item -> new OrderPlacedEvent.OrderItemEventData(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
        
        return new OrderPlacedEvent(
                order.getId(),
                order.getTableId(),
                eventItems,
                order.getCreatedAt()
        );
    }
    
    /**
     * Maps an Order entity to an OrderResponse DTO.
     * 
     * @param order The Order entity to map
     * @return OrderResponse DTO with complete order information
     */
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .tableId(order.getTableId())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    /**
     * Maps an OrderItem entity to an OrderItemResponse DTO.
     * 
     * @param orderItem The OrderItem entity to map
     * @return OrderItemResponse DTO with order item information
     */
    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .note(orderItem.getNote())
                .build();
    }
}
