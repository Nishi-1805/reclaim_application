package com.cdac.dto.response;

import java.time.LocalDateTime;

import com.cdac.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long notificationId;

    private NotificationType notificationType;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
}