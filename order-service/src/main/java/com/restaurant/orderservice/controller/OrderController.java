package com.restaurant.orderservice.controller;

import com.restaurant.orderservice.dto.CreateOrderRequest;
import com.restaurant.orderservice.dto.OrderResponse;
import com.restaurant.orderservice.dto.UpdateStatusRequest;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for order operations.
 * 
 * Provides endpoints for creating, retrieving, filtering, and updating orders.
 * Handles order management operations for restaurant staff through a REST API.
 * 
 * Validates Requirements: 2.1, 4.1, 5.1, 6.1
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Constructor for OrderController.
     * 
     * @param orderService Service for order operations
     */
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * POST /orders endpoint to create a new order.
     * 
     * Creates a new order with the specified table ID and items.
     * Validates that all products exist and are active before creating the order.
     * Publishes an order.placed event to RabbitMQ after successful creation.
     * 
     * @param request CreateOrderRequest containing tableId and list of items
     * @return ResponseEntity with 201 Created status and OrderResponse
     * 
     * Validates Requirements:
     * - 2.1: Order Service exposes POST /orders endpoint accepting tableId and items
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
    
    /**
     * GET /orders/{id} endpoint to retrieve an order by its ID.
     * 
     * Returns complete order details including all items, status, and timestamps.
     * 
     * @param id UUID of the order to retrieve
     * @return ResponseEntity with 200 OK status and OrderResponse
     * 
     * Validates Requirements:
     * - 4.1: Order Service exposes GET /orders/{id} endpoint
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") UUID id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(orderResponse);
    }
    
    /**
     * GET /orders endpoint to retrieve orders, optionally filtered by status.
     * 
     * If status parameter is provided, returns only orders with that status.
     * If status parameter is omitted, returns all orders.
     * 
     * @param status Optional OrderStatus to filter by (can be null)
     * @return ResponseEntity with 200 OK status and list of OrderResponse
     * 
     * Validates Requirements:
     * - 5.1: Order Service exposes GET /orders with optional status parameter
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(name = "status", required = false) List<OrderStatus> status) {
        List<OrderResponse> orders = orderService.getOrders(status);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * PATCH /orders/{id}/status endpoint to update the status of an order.
     * 
     * Updates the order status and automatically updates the updatedAt timestamp.
     * 
     * @param id UUID of the order to update
     * @param request UpdateStatusRequest containing the new status
     * @return ResponseEntity with 200 OK status and OrderResponse
     * 
     * Validates Requirements:
     * - 6.1: Order Service exposes PATCH /orders/{id}/status endpoint
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        OrderResponse orderResponse = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(orderResponse);
    }
}
