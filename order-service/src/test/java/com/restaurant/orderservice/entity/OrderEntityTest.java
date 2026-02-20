package com.restaurant.orderservice.entity;

import com.restaurant.orderservice.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Order and OrderItem entity identity, toString safety,
 * and equals/hashCode correctness with bidirectional JPA references.
 * 
 * Validates fix for OS-C01: @Data on bidirectional entities caused StackOverflowError.
 */
@DisplayName("Order & OrderItem — entity identity and toString safety")
class OrderEntityTest {

    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setTableId(5);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        item = new OrderItem();
        item.setId(1L);
        item.setOrder(order);
        item.setProductId(100L);
        item.setQuantity(2);
        item.setNote("No onions");

        order.getItems().add(item);
    }

    @Test
    @DisplayName("Order.toString() should not cause StackOverflowError with bidirectional reference")
    void orderToStringShouldNotOverflow() {
        assertDoesNotThrow(() -> order.toString());
    }

    @Test
    @DisplayName("OrderItem.toString() should not cause StackOverflowError with bidirectional reference")
    void orderItemToStringShouldNotOverflow() {
        assertDoesNotThrow(() -> item.toString());
    }

    @Test
    @DisplayName("Order.hashCode() should not cause StackOverflowError")
    void orderHashCodeShouldNotOverflow() {
        assertDoesNotThrow(() -> order.hashCode());
    }

    @Test
    @DisplayName("OrderItem.hashCode() should not cause StackOverflowError")
    void orderItemHashCodeShouldNotOverflow() {
        assertDoesNotThrow(() -> item.hashCode());
    }

    @Test
    @DisplayName("Order.equals() should not cause StackOverflowError")
    void orderEqualsShouldNotOverflow() {
        Order other = new Order();
        other.setId(order.getId());
        other.setItems(order.getItems());
        assertDoesNotThrow(() -> order.equals(other));
    }

    @Test
    @DisplayName("OrderItem.equals() should not cause StackOverflowError")
    void orderItemEqualsShouldNotOverflow() {
        OrderItem otherItem = new OrderItem();
        otherItem.setId(item.getId());
        otherItem.setOrder(order);
        assertDoesNotThrow(() -> item.equals(otherItem));
    }

    @Test
    @DisplayName("Orders with same id should be equal (identity-based equals)")
    void ordersWithSameIdShouldBeEqual() {
        UUID sharedId = UUID.randomUUID();
        Order a = new Order();
        a.setId(sharedId);
        Order b = new Order();
        b.setId(sharedId);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Orders with different id should not be equal")
    void ordersWithDifferentIdShouldNotBeEqual() {
        Order a = new Order();
        a.setId(UUID.randomUUID());
        Order b = new Order();
        b.setId(UUID.randomUUID());
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("OrderItems with same id should be equal (identity-based equals)")
    void orderItemsWithSameIdShouldBeEqual() {
        OrderItem a = new OrderItem();
        a.setId(42L);
        OrderItem b = new OrderItem();
        b.setId(42L);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("OrderItems with different id should not be equal")
    void orderItemsWithDifferentIdShouldNotBeEqual() {
        OrderItem a = new OrderItem();
        a.setId(1L);
        OrderItem b = new OrderItem();
        b.setId(2L);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Order can be added to HashSet without StackOverflowError")
    void orderCanBeAddedToHashSet() {
        Set<Order> set = new HashSet<>();
        assertDoesNotThrow(() -> set.add(order));
        assertTrue(set.contains(order));
    }

    @Test
    @DisplayName("Order.toString() should not contain items collection to prevent cycles")
    void orderToStringShouldExcludeItems() {
        String str = order.toString();
        assertNotNull(str);
        // toString should not contain the items list representation
        assertFalse(str.contains("OrderItem"), "Order.toString() should exclude items to prevent cycles");
    }

    @Test
    @DisplayName("OrderItem.toString() should not contain order back-reference to prevent cycles")
    void orderItemToStringShouldExcludeOrder() {
        String str = item.toString();
        assertNotNull(str);
        // toString should not contain the full Order representation
        assertFalse(str.contains("tableId"), "OrderItem.toString() should exclude order to prevent cycles");
    }
}
