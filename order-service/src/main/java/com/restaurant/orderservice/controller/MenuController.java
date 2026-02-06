package com.restaurant.orderservice.controller;

import com.restaurant.orderservice.dto.ErrorResponse;
import com.restaurant.orderservice.dto.ProductResponse;
import com.restaurant.orderservice.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Menu", description = "Menu management endpoints for retrieving active products")
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
    @Operation(
            summary = "Get active menu products",
            description = "Retrieves all active products available for ordering. " +
                    "Only products with isActive=true are returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved active products",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)),
                            examples = @ExampleObject(
                                    name = "Active Products",
                                    value = """
                                            [
                                              {
                                                "id": 1,
                                                "name": "Pizza Margherita",
                                                "description": "Pizza clásica con tomate, mozzarella y albahaca"
                                              },
                                              {
                                                "id": 2,
                                                "name": "Hamburguesa Clásica",
                                                "description": "Hamburguesa de carne con lechuga, tomate y queso"
                                              },
                                              {
                                                "id": 3,
                                                "name": "Ensalada César",
                                                "description": "Ensalada fresca con pollo, parmesano y aderezo César"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service unavailable - Database is not accessible",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Service Unavailable",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00",
                                              "status": 503,
                                              "error": "Service Unavailable",
                                              "message": "Database service is temporarily unavailable"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<List<ProductResponse>> getMenu() {
        List<ProductResponse> activeProducts = menuService.getActiveProducts();
        return ResponseEntity.ok(activeProducts);
    }
}
