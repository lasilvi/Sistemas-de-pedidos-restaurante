package com.restaurant.orderservice.service;

import com.restaurant.orderservice.event.OrderPlacedEvent;
import com.restaurant.orderservice.exception.EventPublicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderEventPublisherTest {

    private RabbitTemplate rabbitTemplate;
    private OrderEventPublisher orderEventPublisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        orderEventPublisher = new OrderEventPublisher(rabbitTemplate);
        ReflectionTestUtils.setField(orderEventPublisher, "exchangeName", "restaurant.exchange");
        ReflectionTestUtils.setField(orderEventPublisher, "orderPlacedRoutingKey", "order.placed");
    }

    @Test
    void publishOrderPlacedEvent_whenRabbitTemplateFails_throwsEventPublicationException() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                10,
                Collections.emptyList(),
                LocalDateTime.now()
        );
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend("restaurant.exchange", "order.placed", event);

        assertThatThrownBy(() -> orderEventPublisher.publishOrderPlacedEvent(event))
                .isInstanceOf(EventPublicationException.class)
                .hasMessageContaining("Unable to publish order.placed event");
    }

    @Test
    void publishOrderPlacedEvent_whenRabbitTemplateSucceeds_sendsMessage() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                7,
                Collections.emptyList(),
                LocalDateTime.now()
        );

        orderEventPublisher.publishOrderPlacedEvent(event);

        verify(rabbitTemplate).convertAndSend("restaurant.exchange", "order.placed", event);
    }
}
