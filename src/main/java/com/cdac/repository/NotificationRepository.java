package com.cdac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Notification;
import com.cdac.entity.User;
import com.cdac.enums.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);

    long countByUserAndIsReadFalse(User user);

    List<Notification> findByUserAndNotificationTypeOrderByCreatedAtDesc(User user, NotificationType notificationType);

    Optional<Notification> findByIdAndUser(Long id, User user);
}