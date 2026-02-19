package com.restaurant.reportservice.domain.service;

import com.restaurant.reportservice.domain.model.DateRange;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stub implementation - to be implemented in GREEN phase
 */
public class DateRangeFilter {
    public DateRange validateAndCreate(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean isWithinRange(LocalDateTime timestamp, DateRange range) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
