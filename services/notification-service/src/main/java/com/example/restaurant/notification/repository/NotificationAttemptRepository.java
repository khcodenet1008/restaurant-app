package com.example.restaurant.notification.repository;

import com.example.restaurant.notification.domain.NotificationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAttemptRepository extends JpaRepository<NotificationAttempt, String> {
}
