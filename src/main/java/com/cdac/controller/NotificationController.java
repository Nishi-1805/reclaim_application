package com.cdac.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.dto.response.NotificationResponse;
import com.cdac.security.SwaggerConfig;
import com.cdac.service.NotificationService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Notification",
        description = "APIs for Notifications of Item matches and Claims"
)
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class NotificationController {

    private final NotificationService notificationService;

    
    @Operation(
            summary = "Get all notifications",
            description = "Returns all notifications for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {

        return ResponseEntity.ok(
                notificationService.getMyNotifications());
    }

    
    @Operation(
            summary = "Get unread notifications",
            description = "Returns only unread notifications of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread notifications fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications());
    }

    
    @Operation(
            summary = "Get unread notification count",
            description = "Returns the total number of unread notifications for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread notification count fetched successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount() {

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationCount());
    }

    
    @Operation(
            summary = "Mark notification as read",
            description = "Marks the specified notification as read."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification marked as read"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long notificationId) {

        notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(
                "Notification marked as read.");
    }

    
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks every unread notification of the authenticated user as read."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "All notifications marked as read"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/read-all")
    public ResponseEntity<String> markAllAsRead() {

        notificationService.markAllAsRead();

        return ResponseEntity.ok(
                "All notifications marked as read.");
    }
}