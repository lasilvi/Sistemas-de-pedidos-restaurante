package com.restaurant.reportservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.restaurant.reportservice.dto.ReportResponseDTO;
import java.time.LocalDate;

/**
 * Stub implementation - to be implemented in GREEN phase
 */
@RestController
@RequestMapping("/reports")
public class ReportController {
    
    @GetMapping
    public ReportResponseDTO getReport(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
