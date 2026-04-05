package com.devtrails.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_plans")
public class UserPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;

    public UserPlan() {}

    public UserPlan(Long userId, Plan plan, boolean active) {
        this.userId = userId;
        this.plan = plan;
        this.active = active;
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(7);
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Plan getPlan() { return plan; }
    public Long getPlanId() { return plan != null ? plan.getId() : null; }
    public boolean isActive() { return active; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public void setActive(boolean active) { this.active = active; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}