package com.cdac.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.dto.response.OwnershipQuestionResponse;
import com.cdac.security.SwaggerConfig;
import com.cdac.service.OwnershipQuestionService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/items/{itemId}/ownership-questions")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Ownership Questions",
        description = "APIs for retrieving ownership verification questions"
)

@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class OwnershipQuestionController {

    private final OwnershipQuestionService ownershipQuestionService;

    @Operation(
            summary = "Get ownership questions for an item",
            description = """
                    Returns all ownership verification questions
                    associated with a FOUND item.
                    Questions are returned in display order.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Questions retrieved successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = OwnershipQuestionResponse.class))
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Item is not a found item"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found"
            )
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<OwnershipQuestionResponse>> getOwnershipQuestions(
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                ownershipQuestionService.getQuestionsByItem(itemId));
    }
}