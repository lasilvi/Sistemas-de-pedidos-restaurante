package com.restaurant.reportservice.service;

import com.restaurant.reportservice.domain.service.ReportAggregationService;
import com.restaurant.reportservice.domain.service.DateRangeFilter;
import com.restaurant.reportservice.repository.OrderReportRepository;
import com.restaurant.reportservice.dto.ReportResponseDTO;
import java.time.LocalDate;

/**
 * Stub implementation - to be implemented in GREEN phase
 */
public class ReportService {
    private final OrderReportRepository orderReportRepository;
    private final ReportAggregationService aggregationService;
    private final DateRangeFilter dateRangeFilter;

    public ReportService(OrderReportRepository orderReportRepository,
                        ReportAggregationService aggregationService,
                        DateRangeFilter dateRangeFilter) {
        this.orderReportRepository = orderReportRepository;
        this.aggregationService = aggregationService;
        this.dateRangeFilter = dateRangeFilter;
    }

    public ReportResponseDTO generateReport(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
