package com.restaurant.orderservice.domain.event;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderPlacedDomainEvent.
 * 
 * Verifies that OrderItemData supports the enriched fields (price, productName)
 * needed for revenue calculation in report-service.
 */
class OrderPlacedDomainEventTest {

    @Test
    void orderItemData_shouldExposeEnrichedFields_priceAndProductName() {
        // Arrange & Act
        OrderPlacedDomainEvent.OrderItemData item = OrderPlacedDomainEvent.OrderItemData.builder()
                .productId(42L)
                .quantity(3)
                .price(new BigDecimal("12.50"))
                .productName("Pizza Margherita")
                .build();

        // Assert
        assertThat(item.getProductId()).isEqualTo(42L);
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(item.getProductName()).isEqualTo("Pizza Margherita");
    }

    @Test
    void orderItemData_enrichedFieldsDefaultToNull_whenNotSet() {
        // Arrange & Act
        OrderPlacedDomainEvent.OrderItemData item = OrderPlacedDomainEvent.OrderItemData.builder()
                .productId(10L)
                .quantity(1)
                .build();

        // Assert
        assertThat(item.getProductId()).isEqualTo(10L);
        assertThat(item.getQuantity()).isEqualTo(1);
        assertThat(item.getPrice()).isNull();
        assertThat(item.getProductName()).isNull();
    }
}
