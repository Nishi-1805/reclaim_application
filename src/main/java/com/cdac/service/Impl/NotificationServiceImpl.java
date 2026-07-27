package com.cdac.service.Impl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.dto.response.NotificationResponse;
import com.cdac.entity.Claim;
import com.cdac.entity.Item;
import com.cdac.entity.ItemMatch;
import com.cdac.entity.Notification;
import com.cdac.entity.User;
import com.cdac.enums.NotificationType;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.NotificationRepository;
import com.cdac.repository.UserRepository;
import com.cdac.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void createNotification(User user, NotificationType type,
    		String message, Item item, ItemMatch itemMatch, Claim claim) {
    	 Notification notification = Notification.builder()
    			  .user(user)
    		        .notificationType(type)
    		        .title(getNotificationTitle(type))
    		        .message(message)
    		        .relatedItem(item)
    		        .relatedMatch(itemMatch)
    		        .relatedClaim(claim)
    		        .isRead(false)
    		        .build();

    	    notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::convertToNotificationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .findByUserAndIsReadOrderByCreatedAtDesc(
                        currentUser,
                        false)
                .stream()
                .map(this::convertToNotificationResponse)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {

        User currentUser = getCurrentUser();

        Notification notification =
                notificationRepository
                        .findByIdAndUser(notificationId, currentUser)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found."));

        if (!notification.getIsRead()) {

            notification.setIsRead(true);

            notificationRepository.save(notification);
        }
    }

    @Override
    public void markAllAsRead() {

        User currentUser = getCurrentUser();

        List<Notification> notifications =
                notificationRepository
                        .findByUserAndIsReadOrderByCreatedAtDesc(
                                currentUser,
                                false);

        notifications.forEach(notification ->
                notification.setIsRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .countByUserAndIsReadFalse(currentUser);
    }

    // Helper methods below
    
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));
    }
    
    private NotificationResponse convertToNotificationResponse( Notification notification) {

        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    
    private String getNotificationTitle(NotificationType notificationType) {

        return switch (notificationType) {

            case MATCH_FOUND -> "Potential Match Found";

            case CLAIM_SUBMITTED -> "New Claim Submitted";

            case MATCH_APPROVED -> "Match Approved";

            case CLAIM_REJECTED -> "Claim Rejected";

            case CLAIM_WITHDRAWN -> "Claim Withdrawn";

            case ITEM_CLOSED -> "Item Closed";

            case ITEM_CANCELLED -> "Item Cancelled";
            
            case  MATCH_REJECTED -> "Match is rejected";
        };
}
}
