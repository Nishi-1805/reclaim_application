package com.cdac.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.dto.request.CreateItemRequest;
import com.cdac.dto.request.UpdateItemRequest;
import com.cdac.dto.response.ItemResponse;
import com.cdac.enums.ItemType;
import com.cdac.security.SwaggerConfig;
import com.cdac.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Item Management",
        description = "APIs for reporting, updating, viewing and cancelling lost/found items."
)
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ItemController {

    private final ItemService itemService;

    @Operation(
            summary = "Create a new lost/found item",
            description = "Creates a new lost or found item for the currently authenticated user."
    )
    @ApiResponse(responseCode = "201", description = "Item created successfully")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody CreateItemRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createItem(request));
    }

    @Operation(
            summary = "Get item by ID",
            description = "Returns complete details of a specific item."
    )
    @ApiResponse(responseCode = "200", description = "Item retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponse> getItemById(@Parameter(description = "Item ID")
            @PathVariable Long itemId) {

        return ResponseEntity.ok(itemService.getItemById(itemId));
    }

    @Operation(
            summary = "Get all items",
            description = "Returns all reported lost and found items."
    )
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {

        return ResponseEntity.ok(itemService.getAllItems());
    }

    @Operation(
            summary = "Get my items",
            description = "Returns all items reported by the logged-in user."
    )
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-items")
    public ResponseEntity<List<ItemResponse>> getMyItems() {

        return ResponseEntity.ok(itemService.getMyItems());
    }

    @Operation(
            summary = "Update an item",
            description = "Updates an existing item reported by the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Item updated successfully")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "Item ID")
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request) {

        return ResponseEntity.ok(
                itemService.updateItem(itemId, request));
    }
    
    @Operation(
            summary = "Get items by type",
            description = "Returns all LOST or FOUND items."
    )
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/type/{itemType}")
    public ResponseEntity<List<ItemResponse>> getItemsByType(
            @Parameter(description = "LOST or FOUND")
            @PathVariable ItemType itemType) {

        return ResponseEntity.ok(
                itemService.getItemsByType(itemType));
    }

    @Operation(
            summary = "Cancel an item",
            description = "Cancels a previously reported item."
    )
    @ApiResponse(responseCode = "200", description = "Item cancelled successfully")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteItem(
            @Parameter(description = "Item ID")
            @PathVariable Long itemId) {

        itemService.deleteItem(itemId);

        return ResponseEntity.ok("Item cancelled successfully.");
    }
}