package com.restaurant.orderservice.infrastructure.messaging;

import com.restaurant.orderservice.domain.event.OrderReadyDomainEvent;
import com.restaurant.orderservice.exception.EventPublicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitOrderReadyEventPublisherTest {

    private RabbitTemplate rabbitTemplate;
    private RabbitOrderReadyEventPublisher publisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        publisher = new RabbitOrderReadyEventPublisher(rabbitTemplate, new OrderReadyEventMessageMapper());
        ReflectionTestUtils.setField(publisher, "exchangeName", "restaurant.exchange");
        ReflectionTestUtils.setField(publisher, "orderReadyRoutingKey", "order.ready");
    }

    @Test
    void publish_whenRabbitTemplateFails_throwsEventPublicationException() {
        OrderReadyDomainEvent event = sampleDomainEvent();
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq("restaurant.exchange"), eq("order.ready"), any(), any(MessagePostProcessor.class));

        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(EventPublicationException.class)
                .hasMessageContaining("Unable to publish order.ready event");
    }

    @Test
    void publish_whenRabbitTemplateSucceeds_sendsVersionedMessage() {
        OrderReadyDomainEvent event = sampleDomainEvent();

        publisher.publish(event);

        ArgumentCaptor<OrderReadyEventMessage> messageCaptor = ArgumentCaptor.forClass(OrderReadyEventMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq("restaurant.exchange"),
                eq("order.ready"),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        OrderReadyEventMessage sent = messageCaptor.getValue();
        assertThat(sent.getEventType()).isEqualTo(OrderReadyDomainEvent.EVENT_TYPE);
        assertThat(sent.getEventVersion()).isEqualTo(OrderReadyDomainEvent.CURRENT_VERSION);
        assertThat(sent.getPayload()).isNotNull();
        assertThat(sent.getPayload().getOrderId()).isEqualTo(event.getOrderId());
    }

    private OrderReadyDomainEvent sampleDomainEvent() {
        return OrderReadyDomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrderReadyDomainEvent.EVENT_TYPE)
                .eventVersion(OrderReadyDomainEvent.CURRENT_VERSION)
                .occurredAt(LocalDateTime.now())
                .orderId(UUID.randomUUID())
                .status("READY")
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
