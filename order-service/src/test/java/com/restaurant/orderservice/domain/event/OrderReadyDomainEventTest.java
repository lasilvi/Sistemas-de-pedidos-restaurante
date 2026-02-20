package com.restaurant.orderservice.domain.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderReadyDomainEvent.
 * 
 * Verifies that the event can be built with all fields,
 * and that static constants are correct.
 */
class OrderReadyDomainEventTest {

    @Test
    void shouldBuildEventWithAllFields() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // Act
        OrderReadyDomainEvent event = OrderReadyDomainEvent.builder()
                .eventId(eventId)
                .eventType(OrderReadyDomainEvent.EVENT_TYPE)
                .eventVersion(OrderReadyDomainEvent.CURRENT_VERSION)
                .occurredAt(occurredAt)
                .orderId(orderId)
                .status("READY")
                .updatedAt(updatedAt)
                .build();

        // Assert
        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo("order.ready");
        assertThat(event.getEventVersion()).isEqualTo(1);
        assertThat(event.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getStatus()).isEqualTo("READY");
        assertThat(event.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void currentVersion_shouldBeOne() {
        assertThat(OrderReadyDomainEvent.CURRENT_VERSION).isEqualTo(1);
    }

    @Test
    void eventType_shouldBeOrderReady() {
        assertThat(OrderReadyDomainEvent.EVENT_TYPE).isEqualTo("order.ready");
    }
}
