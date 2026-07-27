package com.cdac.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.cdac.dto.response.ItemMatchResponse;
import com.cdac.dto.response.ItemMatchSummaryResponse;
import com.cdac.security.SwaggerConfig;
import com.cdac.service.ItemMatchService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Item Matching",
        description = "APIs for viewing and managing automatically generated matches."
)
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ItemMatchController {
	
    private final ItemMatchService itemMatchService;

    @Operation(
            summary = "Get matches for a lost item",
            description = "Returns all potential found item matches."
    )
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/lost-item/{lostItemId}")
    public ResponseEntity<List<ItemMatchSummaryResponse>> getMatchesForLostItem(
            @Parameter(description = "Lost Item ID")
            @PathVariable Long lostItemId) {

        return ResponseEntity.ok(
                itemMatchService.getMatchesForLostItem(lostItemId));
    }

    @Operation(
            summary = "Get matches for a found item",
            description = "Returns all potential lost item matches."
    )
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/found-item/{foundItemId}")
    public ResponseEntity<List<ItemMatchSummaryResponse>> getMatchesForFoundItem(
            @Parameter(description = "Found Item ID")
            @PathVariable Long foundItemId) {

        return ResponseEntity.ok(
                itemMatchService.getMatchesForFoundItem(foundItemId));
    }

    @Operation(
            summary = "Get match details",
            description = "Returns complete information about a specific match."
    )
    @ApiResponse(responseCode = "200", description = "Match retrieved successfully")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{matchId}")
    public ResponseEntity<ItemMatchResponse> getMatchById(
            @Parameter(description = "Match ID")
            @PathVariable Long matchId) {

        return ResponseEntity.ok(
                itemMatchService.getMatchById(matchId));
    }

    @Operation(
            summary = "Confirm match",
            description = "Confirms an automatically generated match."
    )
    @ApiResponse(responseCode = "200", description = "Match confirmed successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{matchId}/confirm")
    public ResponseEntity<String> confirmMatch(
            @Parameter(description = "Match ID")
            @PathVariable Long matchId) {

        itemMatchService.confirmMatch(matchId);

        return ResponseEntity.ok(
                "Match confirmed successfully.");
    }

    @Operation(
            summary = "Reject match",
            description = "Rejects an automatically generated match."
    )
    @ApiResponse(responseCode = "200", description = "Match rejected successfully")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{matchId}/reject")
    public ResponseEntity<String> rejectMatch(
            @Parameter(description = "Match ID")
            @PathVariable Long matchId) {

        itemMatchService.rejectMatch(matchId);

        return ResponseEntity.ok(
                "Match rejected successfully.");
    }
}