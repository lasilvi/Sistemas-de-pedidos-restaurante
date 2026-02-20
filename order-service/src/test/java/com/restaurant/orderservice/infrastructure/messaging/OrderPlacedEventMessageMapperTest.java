package com.restaurant.orderservice.infrastructure.messaging;

import com.restaurant.orderservice.domain.event.OrderPlacedDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderPlacedEventMessageMapper.
 * 
 * Verifies that enriched domain event fields (price, productName)
 * are correctly mapped to the transport contract.
 */
class OrderPlacedEventMessageMapperTest {

    private OrderPlacedEventMessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderPlacedEventMessageMapper();
    }

    @Test
    void shouldMapPriceAndProductNameToPayload() {
        // Arrange
        OrderPlacedDomainEvent domainEvent = OrderPlacedDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderPlacedDomainEvent.EVENT_TYPE)
                .eventVersion(OrderPlacedDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .tableId(5)
                .createdAt(LocalDateTime.now())
                .items(List.of(
                        OrderPlacedDomainEvent.OrderItemData.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(new BigDecimal("10.50"))
                                .productName("Pizza Margherita")
                                .build(),
                        OrderPlacedDomainEvent.OrderItemData.builder()
                                .productId(2L)
                                .quantity(1)
                                .price(new BigDecimal("5.00"))
                                .productName("Coca Cola")
                                .build()
                ))
                .build();

        // Act
        OrderPlacedEventMessage message = mapper.toMessage(domainEvent);

        // Assert — payload items
        List<OrderPlacedEventMessage.OrderItemPayload> payloadItems = message.getPayload().getItems();
        assertThat(payloadItems).hasSize(2);

        assertThat(payloadItems.get(0).getProductId()).isEqualTo(1L);
        assertThat(payloadItems.get(0).getQuantity()).isEqualTo(2);
        assertThat(payloadItems.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(payloadItems.get(0).getProductName()).isEqualTo("Pizza Margherita");

        assertThat(payloadItems.get(1).getProductId()).isEqualTo(2L);
        assertThat(payloadItems.get(1).getQuantity()).isEqualTo(1);
        assertThat(payloadItems.get(1).getPrice()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(payloadItems.get(1).getProductName()).isEqualTo("Coca Cola");

        // Assert — flat legacy items also carry enriched fields
        List<OrderPlacedEventMessage.OrderItemPayload> flatItems = message.getItems();
        assertThat(flatItems).hasSize(2);
        assertThat(flatItems.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(flatItems.get(0).getProductName()).isEqualTo("Pizza Margherita");
    }

    @Test
    void shouldMapBasicFieldsWithoutEnrichedData() {
        // Arrange
        OrderPlacedDomainEvent domainEvent = OrderPlacedDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderPlacedDomainEvent.EVENT_TYPE)
                .eventVersion(OrderPlacedDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .tableId(3)
                .createdAt(LocalDateTime.now())
                .items(List.of(
                        OrderPlacedDomainEvent.OrderItemData.builder()
                                .productId(1L)
                                .quantity(1)
                                .build()
                ))
                .build();

        // Act
        OrderPlacedEventMessage message = mapper.toMessage(domainEvent);

        // Assert
        List<OrderPlacedEventMessage.OrderItemPayload> payloadItems = message.getPayload().getItems();
        assertThat(payloadItems).hasSize(1);
        assertThat(payloadItems.get(0).getProductId()).isEqualTo(1L);
        assertThat(payloadItems.get(0).getQuantity()).isEqualTo(1);
        assertThat(payloadItems.get(0).getPrice()).isNull();
        assertThat(payloadItems.get(0).getProductName()).isNull();
    }
}
