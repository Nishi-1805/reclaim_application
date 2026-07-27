package com.cdac.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.cdac.dto.request.ChangePasswordRequest;
import com.cdac.dto.request.UpdateProfileRequest;
import com.cdac.dto.request.UpdateUserStatusRequest;
import com.cdac.dto.response.UserDashboardResponse;
import com.cdac.dto.response.UserProfileResponse;
import com.cdac.dto.response.UserSummaryResponse;
import com.cdac.enums.AccountStatus;
import com.cdac.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management",
description = "APIs for user profile management, dashboard, password management "
		+ "and admin user operations")
public class UserController {

    private final UserService userService;

    /**
     * Logged-in user's profile
     */
    @Operation(
            summary = "Get logged-in user profile",
            description = "Returns the profile details of the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {

        return ResponseEntity.ok(
                userService.getMyProfile());
    }

    /**
     * Update logged-in user's profile
     */
    @Operation(
            summary = "Update logged-in user profile",
            description = "Updates the full name and phone number of the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateProfile(request));
    }

    /**
     * Soft delete own account
     */
    @Operation(
            summary = "Deactivate own account",
            description = "Soft deletes (deactivates) the currently authenticated user's account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/me")
    public ResponseEntity<String> deactivateMyAccount() {

        userService.deactivateMyAccount();

        return ResponseEntity.ok(
                "Account deactivated successfully.");
    }
    /**
     * Change password of own account
     */
    @Operation(
            summary = "Change password",
            description = "Changes the password of the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/me/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok(
                "Password changed successfully.");
    }
    
    @Operation(
            summary = "Get user dashboard",
            description = "Returns dashboard statistics including total items,"
            		+ " lost items, found items and claim counts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/me/dashboard")
    public ResponseEntity<UserDashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                userService.getDashboard());
    }

    /**
     * Admin - View all users
     */
    @Operation(
            summary = "Get all users",
            description = "Returns the list of all registered users. Accessible only to administrators."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers());
    }

    /**
     * Admin - View users by account status
     */
    @Operation(
            summary = "Get users by account status",
            description = "Returns all users having the specified account status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid account status"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserSummaryResponse>> getUsersByStatus(

            @Parameter(description = "Account status (ACTIVE, INACTIVE, DEACTIVATED)", required = true)
            @PathVariable AccountStatus status) {

        return ResponseEntity.ok(
                userService.getUsersByStatus(status));
    }

    /**
     * Admin - Activate or Suspend user
     */
    @Operation(
            summary = "Update user account status",
            description = "Allows an administrator to activate, suspend or deactivate a user account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserProfileResponse> updateUserStatus(

            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,

            @Valid @RequestBody UpdateUserStatusRequest request) {

        return ResponseEntity.ok(
                userService.updateUserStatus(userId, request));
    }
    
    @Operation(
            summary = "Get user by ID",
            description = "Returns complete profile details of a specific user. Accessible only to administrators."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(

            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserById(userId));
    }
}