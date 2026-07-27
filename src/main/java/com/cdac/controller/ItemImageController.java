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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.dto.request.ItemImageRequest;
import com.cdac.dto.response.ItemImageResponse;
import com.cdac.service.ItemImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/items/{itemId}/images")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Item Images",
        description = "APIs for managing images associated with lost and found items"
)
public class ItemImageController {

    private final ItemImageService itemImageService;

    @Operation(
            summary = "Get images for an item",
            description = "Retrieves all images associated with the specified item in display order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item images retrieved successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<ItemImageResponse>> getImagesByItem(
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                itemImageService.getImagesByItem(itemId));
    }

    @Operation(
            summary = "Add image to an item",
            description = "Adds a new image to the specified item. Only the owner of an open "
            		+ "item can add images. Maximum 3 images are allowed per item."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Image added successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or maximum image limit exceeded",
                    content = @Content(
                            schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "403",
                    description = "You are not authorized to modify this item",
                    content = @Content(
                            schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ItemImageResponse> addImageToItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemImageRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemImageService.addImageToItem(itemId, request));
    }

    @Operation(
            summary = "Delete an item image",
            description = "Deletes an image from the specified item. Only the owner of an open"
            		+ " item can delete images."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image deleted successfully"),
            @ApiResponse(
                    responseCode = "403",
                    description = "You are not authorized to modify this item",
                    content = @Content(
                            schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item or image not found",
                    content = @Content(
                            schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<String> deleteImage(
            @PathVariable Long itemId,
            @PathVariable Long imageId) {

        itemImageService.deleteImage(itemId, imageId);

        return ResponseEntity.ok("Item image deleted successfully.");
    }
}