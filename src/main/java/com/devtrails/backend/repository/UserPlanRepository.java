package com.devtrails.backend.repository;

import com.devtrails.backend.model.UserPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserPlanRepository extends JpaRepository<UserPlan, Long> {
    Optional<UserPlan> findByUserIdAndActiveTrue(Long userId);
    List<UserPlan> findByUserId(Long userId);
    boolean existsByUserIdAndActiveTrue(Long userId);
}