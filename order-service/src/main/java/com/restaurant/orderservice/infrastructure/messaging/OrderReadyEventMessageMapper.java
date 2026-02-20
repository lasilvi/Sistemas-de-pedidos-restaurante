package com.restaurant.orderservice.infrastructure.messaging;

import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import org.springframework.stereotype.Component;

/**
 * Maps domain events to transport contract messages.
 */
@Component
public class OrderReadyEventMessageMapper {

    public OrderReadyEventMessage toMessage(OrderReadyDomainEvent domainEvent) {
        OrderReadyEventMessage.Payload payload = OrderReadyEventMessage.Payload.builder()
                .orderId(domainEvent.getOrderId())
                .status(domainEvent.getStatus())
                .updatedAt(domainEvent.getUpdatedAt())
                .build();

        return OrderReadyEventMessage.builder()
                .eventId(domainEvent.getEventId())
                .eventType(domainEvent.getEventType())
                .eventVersion(domainEvent.getEventVersion())
                .occurredAt(domainEvent.getOccurredAt())
                .payload(payload)
                .build();
    }
}
