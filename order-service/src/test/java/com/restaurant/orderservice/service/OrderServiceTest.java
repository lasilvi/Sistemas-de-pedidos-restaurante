package com.restaurant.orderservice.service;

import com.restaurant.orderservice.dto.*;
import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.entity.OrderItem;
import com.restaurant.orderservice.entity.Product;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.event.OrderPlacedEvent;
import com.restaurant.orderservice.exception.InvalidOrderException;
import com.restaurant.orderservice.exception.OrderNotFoundException;
import com.restaurant.orderservice.exception.EventPublicationException;
import com.restaurant.orderservice.exception.ProductNotFoundException;
import com.restaurant.orderservice.repository.OrderRepository;
import com.restaurant.orderservice.repository.ProductRepository;
import com.restaurant.orderservice.service.command.OrderCommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 * 
 * Tests the core business logic for order creation, retrieval, filtering, and status updates.
 * Uses Mockito to mock repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private OrderCommandExecutor orderCommandExecutor;
    
    @Mock
    private OrderValidator orderValidator;
    
    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private OrderEventBuilder orderEventBuilder;
    
    @InjectMocks
    private OrderService orderService;
    
    private Product activeProduct;
    private Product inactiveProduct;
    
    @BeforeEach
    void setUp() {
        activeProduct = new Product();
        activeProduct.setId(1L);
        activeProduct.setName("Pizza");
        activeProduct.setDescription("Delicious pizza");
        activeProduct.setIsActive(true);
        
        inactiveProduct = new Product();
        inactiveProduct.setId(2L);
        inactiveProduct.setName("Old Burger");
        inactiveProduct.setDescription("Discontinued burger");
        inactiveProduct.setIsActive(false);
    }
    
    @Test
    void createOrder_withValidData_createsOrderWithPendingStatus() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2, "No onions");
        CreateOrderRequest request = new CreateOrderRequest(5, List.of(itemRequest));
        
        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setTableId(5);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setCreatedAt(LocalDateTime.now());
        savedOrder.setUpdatedAt(LocalDateTime.now());
        
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(savedOrder);
        orderItem.setProductId(1L);
        orderItem.setQuantity(2);
        orderItem.setNote("No onions");
        savedOrder.setItems(List.of(orderItem));
        
        OrderResponse expectedResponse = OrderResponse.builder()
                .id(savedOrder.getId())
                .tableId(5)
                .status(OrderStatus.PENDING)
                .items(List.of(OrderItemResponse.builder()
                        .id(1L)
                        .productId(1L)
                        .productName("Pizza")
                        .quantity(2)
                        .note("No onions")
                        .build()))
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .build();
        
        // Mock the new dependencies
        doNothing().when(orderValidator).validateCreateOrderRequest(request);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderEventBuilder.buildOrderPlacedEvent(savedOrder)).thenReturn(mock(OrderPlacedEvent.class));
        when(orderMapper.mapToOrderResponse(savedOrder)).thenReturn(expectedResponse);
        
        // Act
        OrderResponse response = orderService.createOrder(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTableId()).isEqualTo(5);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        
        verify(orderValidator).validateCreateOrderRequest(request);
        verify(orderRepository).save(any(Order.class));
        verify(orderCommandExecutor).execute(any());
        verify(orderEventBuilder).buildOrderPlacedEvent(savedOrder);
        verify(orderCommandExecutor).execute(any());
        verify(orderMapper).mapToOrderResponse(savedOrder);
    }

    @Test
    void createOrder_whenEventPublicationFails_propagatesException() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2, "No onions");
        CreateOrderRequest request = new CreateOrderRequest(5, List.of(itemRequest));

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setTableId(5);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setCreatedAt(LocalDateTime.now());
        savedOrder.setUpdatedAt(LocalDateTime.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(savedOrder);
        orderItem.setProductId(1L);
        orderItem.setQuantity(2);
        orderItem.setNote("No onions");
        savedOrder.setItems(List.of(orderItem));

        doNothing().when(orderValidator).validateCreateOrderRequest(request);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderEventBuilder.buildOrderPlacedEvent(savedOrder)).thenReturn(mock(OrderPlacedEvent.class));
        doThrow(new EventPublicationException("Broker unavailable", new RuntimeException("broker down")))
                .when(orderCommandExecutor).execute(any());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(EventPublicationException.class)
                .hasMessageContaining("Broker unavailable");

        verify(orderValidator).validateCreateOrderRequest(request);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventBuilder).buildOrderPlacedEvent(savedOrder);
        verify(orderCommandExecutor).execute(any());
    }
    
    @Test
    void createOrder_withNonExistentProduct_throwsProductNotFoundException() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(999L, 1, null);
        CreateOrderRequest request = new CreateOrderRequest(5, List.of(itemRequest));
        
        doThrow(new ProductNotFoundException(999L))
                .when(orderValidator).validateCreateOrderRequest(request);
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
        
        verify(orderValidator).validateCreateOrderRequest(request);
        verify(orderRepository, never()).save(any());
        verify(orderCommandExecutor, never()).execute(any());
    }
    
    @Test
    void createOrder_withInactiveProduct_throwsProductNotFoundException() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(2L, 1, null);
        CreateOrderRequest request = new CreateOrderRequest(5, List.of(itemRequest));
        
        doThrow(new ProductNotFoundException(2L))
                .when(orderValidator).validateCreateOrderRequest(request);
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 2");
        
        verify(orderValidator).validateCreateOrderRequest(request);
        verify(orderRepository, never()).save(any());
        verify(orderCommandExecutor, never()).execute(any());
    }
    
    @Test
    void createOrder_withInvalidTableId_throwsInvalidOrderException() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 1, null);
        CreateOrderRequest request = new CreateOrderRequest(0, List.of(itemRequest));
        
        doThrow(new InvalidOrderException("Table ID must be a positive integer"))
                .when(orderValidator).validateCreateOrderRequest(request);
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Table ID must be a positive integer");
        
        verify(orderValidator).validateCreateOrderRequest(request);
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    void createOrder_withEmptyItems_throwsInvalidOrderException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(5, Collections.emptyList());
        
        doThrow(new InvalidOrderException("Order must contain at least one item"))
                .when(orderValidator).validateCreateOrderRequest(request);
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Order must contain at least one item");
        
        verify(orderValidator).validateCreateOrderRequest(request);
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    void getOrderById_withValidId_returnsOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        
        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .tableId(5)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.mapToOrderResponse(order)).thenReturn(expectedResponse);
        
        // Act
        OrderResponse response = orderService.getOrderById(orderId);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
        assertThat(response.getTableId()).isEqualTo(5);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        
        verify(orderMapper).mapToOrderResponse(order);
    }
    
    @Test
    void getOrderById_withNonExistentId_throwsOrderNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: " + orderId);
    }
    
    @Test
    void getOrders_withoutStatusFilter_returnsAllOrders() {
        // Arrange
        Order order1 = createTestOrder(OrderStatus.PENDING);
        Order order2 = createTestOrder(OrderStatus.IN_PREPARATION);
        
        List<OrderResponse> expectedResponses = List.of(
                OrderResponse.builder().id(order1.getId()).tableId(5).status(OrderStatus.PENDING).items(new ArrayList<>()).build(),
                OrderResponse.builder().id(order2.getId()).tableId(5).status(OrderStatus.IN_PREPARATION).items(new ArrayList<>()).build()
        );
        
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));
        when(orderMapper.mapToOrderResponseList(List.of(order1, order2))).thenReturn(expectedResponses);
        
        // Act
        List<OrderResponse> responses = orderService.getOrders(null);
        
        // Assert
        assertThat(responses).hasSize(2);
        verify(orderRepository).findAll();
        verify(orderRepository, never()).findByStatusIn(any());
        verify(orderMapper).mapToOrderResponseList(List.of(order1, order2));
    }
    
    @Test
    void getOrders_withStatusFilter_returnsFilteredOrders() {
        // Arrange
        Order order1 = createTestOrder(OrderStatus.PENDING);
        Order order2 = createTestOrder(OrderStatus.PENDING);
        
        List<OrderResponse> expectedResponses = List.of(
                OrderResponse.builder().id(order1.getId()).tableId(5).status(OrderStatus.PENDING).items(new ArrayList<>()).build(),
                OrderResponse.builder().id(order2.getId()).tableId(5).status(OrderStatus.PENDING).items(new ArrayList<>()).build()
        );
        
        when(orderRepository.findByStatusIn(List.of(OrderStatus.PENDING)))
                .thenReturn(List.of(order1, order2));
        when(orderMapper.mapToOrderResponseList(List.of(order1, order2))).thenReturn(expectedResponses);
        
        // Act
        List<OrderResponse> responses = orderService.getOrders(List.of(OrderStatus.PENDING));
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses).allMatch(r -> r.getStatus() == OrderStatus.PENDING);
        verify(orderRepository).findByStatusIn(List.of(OrderStatus.PENDING));
        verify(orderRepository, never()).findAll();
        verify(orderMapper).mapToOrderResponseList(List.of(order1, order2));
    }
    
    @Test
    void updateOrderStatus_withValidData_updatesStatusSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = createTestOrder(OrderStatus.PENDING);
        order.setId(orderId);
        
        Order updatedOrder = createTestOrder(OrderStatus.IN_PREPARATION);
        updatedOrder.setId(orderId);
        
        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .tableId(5)
                .status(OrderStatus.IN_PREPARATION)
                .items(new ArrayList<>())
                .build();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.mapToOrderResponse(updatedOrder)).thenReturn(expectedResponse);
        
        // Act
        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.IN_PREPARATION);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
        
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).mapToOrderResponse(updatedOrder);
    }
    
    @Test
    void updateOrderStatus_withNonExistentOrder_throwsOrderNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.READY))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: " + orderId);
        
        verify(orderRepository, never()).save(any());
    }
    
    private Order createTestOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setTableId(5);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        return order;
    }
}
