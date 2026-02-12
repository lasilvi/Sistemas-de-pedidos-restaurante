package com.restaurant.orderservice.service.command;

import com.restaurant.orderservice.event.OrderPlacedEvent;
import com.restaurant.orderservice.service.OrderEventPublisher;

/**
 * Concrete command that publishes an order.placed event.
 */
public class PublishOrderPlacedEventCommand implements OrderCommand {

    private final OrderEventPublisher orderEventPublisher;
    private final OrderPlacedEvent event;

    public PublishOrderPlacedEventCommand(OrderEventPublisher orderEventPublisher, OrderPlacedEvent event) {
        this.orderEventPublisher = orderEventPublisher;
        this.event = event;
    }

    @Override
    public void execute() {
        orderEventPublisher.publishOrderPlacedEvent(event);
    }
}
