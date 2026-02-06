package com.restaurant.orderservice.controller;

import com.restaurant.orderservice.dto.ProductResponse;
import com.restaurant.orderservice.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MenuController.
 * 
 * Tests the REST endpoint for retrieving active menu products.
 * 
 * Validates Requirements: 1.1
 */
@ExtendWith(MockitoExtension.class)
class MenuControllerTest {
    
    @Mock
    private MenuService menuService;
    
    @InjectMocks
    private MenuController menuController;
    
    private List<ProductResponse> sampleProducts;
    
    @BeforeEach
    void setUp() {
        sampleProducts = Arrays.asList(
            ProductResponse.builder()
                .id(1L)
                .name("Pizza Margherita")
                .description("Pizza clásica con tomate, mozzarella y albahaca")
                .build(),
            ProductResponse.builder()
                .id(2L)
                .name("Hamburguesa Clásica")
                .description("Hamburguesa de carne con lechuga, tomate y queso")
                .build(),
            ProductResponse.builder()
                .id(3L)
                .name("Ensalada César")
                .description("Ensalada fresca con pollo, parmesano y aderezo César")
                .build()
        );
    }
    
    /**
     * Test: GET /menu returns 200 OK with list of active products.
     * 
     * Validates Requirements:
     * - 1.1: Order Service exposes GET /menu endpoint that returns active products
     */
    @Test
    void getMenu_ReturnsActiveProducts() {
        // Arrange
        when(menuService.getActiveProducts()).thenReturn(sampleProducts);
        
        // Act
        ResponseEntity<List<ProductResponse>> response = menuController.getMenu();
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()).isEqualTo(sampleProducts);
    }
    
    /**
     * Test: GET /menu returns empty list when no active products exist.
     * 
     * Validates Requirements:
     * - 1.4: When a product does not exist in database, Order Service returns empty list without error
     */
    @Test
    void getMenu_ReturnsEmptyList_WhenNoActiveProducts() {
        // Arrange
        when(menuService.getActiveProducts()).thenReturn(Arrays.asList());
        
        // Act
        ResponseEntity<List<ProductResponse>> response = menuController.getMenu();
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }
}
