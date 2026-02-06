package com.restaurant.orderservice.service;

import com.restaurant.orderservice.dto.ProductResponse;
import com.restaurant.orderservice.entity.Product;
import com.restaurant.orderservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MenuService.
 * 
 * Tests the business logic for retrieving active products from the menu.
 * 
 * Validates Requirements: 1.1, 1.2, 1.3, 1.4
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private MenuService menuService;
    
    private Product activeProduct1;
    private Product activeProduct2;
    private Product inactiveProduct;
    
    @BeforeEach
    void setUp() {
        activeProduct1 = new Product(1L, "Pizza Margherita", "Classic pizza with tomato and mozzarella", true);
        activeProduct2 = new Product(2L, "Hamburguesa Clásica", "Beef burger with lettuce and cheese", true);
        inactiveProduct = new Product(3L, "Ensalada César", "Fresh salad with chicken", false);
    }
    
    /**
     * Test that getActiveProducts returns only products with isActive = true.
     * 
     * Validates Requirements: 1.3
     */
    @Test
    void getActiveProducts_shouldReturnOnlyActiveProducts() {
        // Arrange
        List<Product> activeProducts = Arrays.asList(activeProduct1, activeProduct2);
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);
        
        // Act
        List<ProductResponse> result = menuService.getActiveProducts();
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductResponse::getId)
                .containsExactly(1L, 2L);
        assertThat(result).extracting(ProductResponse::getName)
                .containsExactly("Pizza Margherita", "Hamburguesa Clásica");
    }
    
    /**
     * Test that getActiveProducts returns empty list when no active products exist.
     * 
     * Validates Requirements: 1.4
     */
    @Test
    void getActiveProducts_shouldReturnEmptyListWhenNoActiveProducts() {
        // Arrange
        when(productRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        
        // Act
        List<ProductResponse> result = menuService.getActiveProducts();
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    /**
     * Test that getActiveProducts correctly maps Product entities to ProductResponse DTOs.
     * 
     * Validates Requirements: 1.1, 1.2
     */
    @Test
    void getActiveProducts_shouldMapProductToProductResponse() {
        // Arrange
        List<Product> activeProducts = Collections.singletonList(activeProduct1);
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);
        
        // Act
        List<ProductResponse> result = menuService.getActiveProducts();
        
        // Assert
        assertThat(result).hasSize(1);
        ProductResponse response = result.get(0);
        assertThat(response.getId()).isEqualTo(activeProduct1.getId());
        assertThat(response.getName()).isEqualTo(activeProduct1.getName());
        assertThat(response.getDescription()).isEqualTo(activeProduct1.getDescription());
    }
    
    /**
     * Test that getActiveProducts returns all fields (id, name, description) for each product.
     * 
     * Validates Requirements: 1.2
     */
    @Test
    void getActiveProducts_shouldReturnAllProductFields() {
        // Arrange
        List<Product> activeProducts = Arrays.asList(activeProduct1, activeProduct2);
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);
        
        // Act
        List<ProductResponse> result = menuService.getActiveProducts();
        
        // Assert
        assertThat(result).hasSize(2);
        result.forEach(productResponse -> {
            assertThat(productResponse.getId()).isNotNull();
            assertThat(productResponse.getName()).isNotNull();
            assertThat(productResponse.getDescription()).isNotNull();
        });
    }
}
