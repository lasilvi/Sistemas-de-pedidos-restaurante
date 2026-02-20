package com.restaurant.orderservice.infrastructure.messaging;

import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderReadyEventMessageMapper.
 * 
 * Verifies that domain event fields are correctly mapped
 * to the transport contract message.
 */
class OrderReadyEventMessageMapperTest {

    private OrderReadyEventMessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderReadyEventMessageMapper();
    }

    @Test
    void shouldMapAllFieldsFromDomainEventToMessage() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        OrderReadyDomainEvent domainEvent = OrderReadyDomainEvent.builder()
                .eventId(eventId)
                .eventType(OrderReadyDomainEvent.EVENT_TYPE)
                .eventVersion(OrderReadyDomainEvent.CURRENT_VERSION)
                .occurredAt(occurredAt)
                .orderId(orderId)
                .status("READY")
                .updatedAt(updatedAt)
                .build();

        // Act
        OrderReadyEventMessage message = mapper.toMessage(domainEvent);

        // Assert
        assertThat(message.getEventId()).isEqualTo(eventId);
        assertThat(message.getEventType()).isEqualTo("order.ready");
        assertThat(message.getEventVersion()).isEqualTo(1);
        assertThat(message.getOccurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void shouldMapPayloadWithOrderIdStatusAndUpdatedAt() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.now();

        OrderReadyDomainEvent domainEvent = OrderReadyDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderReadyDomainEvent.EVENT_TYPE)
                .eventVersion(OrderReadyDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(orderId)
                .status("READY")
                .updatedAt(updatedAt)
                .build();

        // Act
        OrderReadyEventMessage message = mapper.toMessage(domainEvent);

        // Assert
        assertThat(message.getPayload()).isNotNull();
        assertThat(message.getPayload().getOrderId()).isEqualTo(orderId);
        assertThat(message.getPayload().getStatus()).isEqualTo("READY");
        assertThat(message.getPayload().getUpdatedAt()).isEqualTo(updatedAt);
    }
}
