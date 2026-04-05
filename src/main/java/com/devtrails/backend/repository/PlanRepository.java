package com.devtrails.backend.repository;

import com.devtrails.backend.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(String name);
    Optional<Plan> findByTier(String tier);
}