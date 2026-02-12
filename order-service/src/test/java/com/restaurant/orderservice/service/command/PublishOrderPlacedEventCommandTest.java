package com.restaurant.orderservice.service.command;

import com.restaurant.orderservice.event.OrderPlacedEvent;
import com.restaurant.orderservice.service.OrderEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PublishOrderPlacedEventCommandTest {

    @Test
    void execute_delegatesToOrderEventPublisher() {
        OrderEventPublisher orderEventPublisher = mock(OrderEventPublisher.class);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                8,
                Collections.emptyList(),
                LocalDateTime.now()
        );
        PublishOrderPlacedEventCommand command = new PublishOrderPlacedEventCommand(orderEventPublisher, event);

        command.execute();

        verify(orderEventPublisher).publishOrderPlacedEvent(event);
    }
}
