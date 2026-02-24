package com.restaurant.orderservice.service.command;

import com.restaurant.orderservice.application.port.out.OrderReadyEventPublisherPort;
import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PublishOrderReadyEventCommandTest {

    @Test
    void execute_delegatesToOrderReadyEventPublisherPort() {
        OrderReadyEventPublisherPort publisherPort = mock(OrderReadyEventPublisherPort.class);
        OrderReadyDomainEvent event = OrderReadyDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderReadyDomainEvent.EVENT_TYPE)
                .eventVersion(OrderReadyDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .status("READY")
                .updatedAt(LocalDateTime.now())
                .build();
        PublishOrderReadyEventCommand command = new PublishOrderReadyEventCommand(publisherPort, event);

        command.execute();

        verify(publisherPort).publish(event);
    }
}
