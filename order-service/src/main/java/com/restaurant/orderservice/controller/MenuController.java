package com.restaurant.orderservice.controller;

import com.restaurant.orderservice.dto.ProductResponse;
import com.restaurant.orderservice.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for menu operations.
 * 
 * Provides endpoints for retrieving menu information, specifically active products
 * that can be ordered by restaurant staff.
 * 
 * Validates Requirements: 1.1
 */
@RestController
@RequestMapping("/menu")
public class MenuController {
    
    private final MenuService menuService;
    
    /**
     * Constructor for MenuController.
     * 
     * @param menuService Service for menu operations
     */
    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    /**
     * GET /menu endpoint to retrieve active products.
     * 
     * Returns a list of all active products available for ordering.
     * Products are filtered to include only those with isActive = true.
     * 
     * @return ResponseEntity with 200 OK status and list of ProductResponse
     * 
     * Validates Requirements:
     * - 1.1: Order Service exposes GET /menu endpoint that returns active products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getMenu() {
        List<ProductResponse> activeProducts = menuService.getActiveProducts();
        return ResponseEntity.ok(activeProducts);
    }
}
