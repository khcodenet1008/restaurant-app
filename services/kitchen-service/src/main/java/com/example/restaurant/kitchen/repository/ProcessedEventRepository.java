package com.example.restaurant.kitchen.repository;

import com.example.restaurant.kitchen.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
