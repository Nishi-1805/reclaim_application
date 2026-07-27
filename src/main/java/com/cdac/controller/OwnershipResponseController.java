package com.cdac.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.dto.response.OwnershipAnswerResponse;
import com.cdac.security.SwaggerConfig;
import com.cdac.service.OwnershipResponseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims/{claimId}/ownership-responses")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Ownership Responses",
        description = "APIs for retrieving ownership verification responses"
)

@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class OwnershipResponseController {

    private final OwnershipResponseService ownershipResponseService;

    @Operation(
            summary = "Get ownership responses for an item",
            description = """
                    Returns all ownership verification responses
                    associated with a FOUND item.
                    Responsess are returned in display order.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Responses retrieved successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = OwnershipAnswerResponse.class))
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
    public ResponseEntity<List<OwnershipAnswerResponse>> getResponsesByClaim(
            @PathVariable Long claimId) {

        return ResponseEntity.ok(
                ownershipResponseService.getResponsesByClaim(claimId));
    }
}