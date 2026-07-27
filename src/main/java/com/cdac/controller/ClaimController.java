package com.cdac.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.dto.request.SubmitClaimRequest;
import com.cdac.dto.response.ClaimResponse;
import com.cdac.dto.response.ClaimSummaryResponse;
import com.cdac.security.SwaggerConfig;
import com.cdac.service.ClaimService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Claim Management",
        description = "APIs for submitting, viewing and withdrawing ownership claims."
)
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ClaimController {

    private final ClaimService claimService;

    @Operation(
            summary = "Submit ownership claim",
            description = "Submits a claim for a matched lost/found item."
    )
    @ApiResponse(responseCode = "201", description = "Claim submitted successfully")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody SubmitClaimRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.submitClaim(request));
    }

    @Operation(
            summary = "Get claim details",
            description = "Returns complete information about a claim."
    )
    @ApiResponse(responseCode = "200", description = "Claim retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @Parameter(description = "Claim ID")
            @PathVariable Long claimId) {

        return ResponseEntity.ok(
                claimService.getClaimById(claimId));
    }

    @Operation(
            summary = "Get my claims",
            description = "Returns all claims submitted by the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Claims retrieved successfully")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-claims")
    public ResponseEntity<List<ClaimSummaryResponse>> getMyClaims() {

        return ResponseEntity.ok(
                claimService.getMyClaims());
    }

    @Operation(
            summary = "Get claims by item",
            description = "Returns all claims submitted for a specific item."
    )
    @ApiResponse(responseCode = "200", description = "Claims retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ClaimSummaryResponse>> getClaimsByItem(
            @Parameter(description = "Item ID")
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                claimService.getClaimsByItem(itemId));
    }

    @Operation(
            summary = "Get claims by match",
            description = "Returns all claims submitted for a specific match."
    )
    @ApiResponse(responseCode = "200", description = "Claims retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<ClaimSummaryResponse>> getClaimsByMatch(
            @Parameter(description = "Match ID")
            @PathVariable Long matchId) {

        return ResponseEntity.ok(
                claimService.getClaimsByMatch(matchId));
    }

    @Operation(
            summary = "Withdraw claim",
            description = "Withdraws a previously submitted ownership claim."
    )
    @ApiResponse(responseCode = "200", description = "Claim withdrawn successfully")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{claimId}/withdraw")
    public ResponseEntity<String> withdrawClaim(
            @Parameter(description = "Claim ID")
            @PathVariable Long claimId) {

        claimService.withdrawClaim(claimId);

        return ResponseEntity.ok(
                "Claim withdrawn successfully.");
    }

}