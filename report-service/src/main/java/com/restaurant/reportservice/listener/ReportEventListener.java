package com.restaurant.reportservice.listener;

import com.restaurant.reportservice.event.OrderPlacedEvent;
import com.restaurant.reportservice.event.OrderReadyEvent;
import com.restaurant.reportservice.service.OrderEventProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Stub implementation - to be implemented in GREEN phase
 */
@Component
@RequiredArgsConstructor
public class ReportEventListener {
    private final OrderEventProcessingService orderEventProcessingService;

    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void handleOrderReadyEvent(OrderReadyEvent event) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
