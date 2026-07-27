package com.cdac.service;

import java.util.List;

import com.cdac.dto.response.NotificationResponse;
import com.cdac.entity.Claim;
import com.cdac.entity.Item;
import com.cdac.entity.ItemMatch;
import com.cdac.entity.User;
import com.cdac.enums.NotificationType;

public interface NotificationService {

    // User APIs
    List<NotificationResponse> getMyNotifications();

    List<NotificationResponse> getUnreadNotifications();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    long getUnreadNotificationCount();

    // Internal APIs (used by other modules)
    void createNotification(User user, NotificationType type, String message, Item item,
            ItemMatch itemMatch, Claim claim);
}