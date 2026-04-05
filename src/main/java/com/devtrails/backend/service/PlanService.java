package com.devtrails.backend.service;

import com.devtrails.backend.model.Plan;
import com.devtrails.backend.model.UserPlan;
import com.devtrails.backend.repository.PlanRepository;
import com.devtrails.backend.repository.UserPlanRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final UserPlanRepository userPlanRepository;

    public PlanService(PlanRepository planRepository, UserPlanRepository userPlanRepository) {
        this.planRepository = planRepository;
        this.userPlanRepository = userPlanRepository;
    }

    // ✅ Auto-seed the 3 official GigShield plans on startup
    @PostConstruct
    public void seedPlans() {
        if (planRepository.count() == 0) {
            System.out.println("🌱 Seeding GigShield plans...");
            planRepository.saveAll(List.of(
                new Plan("Basic Shield",    20, 1000),
                new Plan("Standard Shield", 35, 2000),
                new Plan("Premium Shield",  50, 3500)
            ));
            System.out.println("✅ Plans seeded: Basic Shield ₹20, Standard Shield ₹35, Premium Shield ₹50");
        }
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Plan getPlanById(Long planId) {
        return planRepository.findById(planId).orElse(null);
    }

    // ✅ AI-driven auto plan assignment based on project rules
    @Transactional
    public String autoAssignPlan(Long userId, String riskLevel, int numActiveDays) {
        String planName;

        // Project rule: < 5 active days = lower tier regardless of risk
        if (numActiveDays < 5) {
            planName = "Basic Shield";
        } else if ("High".equalsIgnoreCase(riskLevel)) {
            planName = "Premium Shield";
        } else if ("Moderate".equalsIgnoreCase(riskLevel)) {
            planName = "Standard Shield";
        } else {
            planName = "Basic Shield";
        }

        Plan plan = planRepository.findByName(planName).orElse(null);
        if (plan == null) {
            System.out.println("❌ Plan not found: " + planName);
            return "Plan not found";
        }

        // Deactivate existing plan
        Optional<UserPlan> existingOpt = userPlanRepository.findByUserIdAndActiveTrue(userId);
        existingOpt.ifPresent(existing -> {
            existing.setActive(false);
            userPlanRepository.save(existing);
        });

        // Assign new plan
        UserPlan userPlan = new UserPlan(userId, plan, true);
        userPlanRepository.save(userPlan);

        System.out.println("✅ Auto-assigned [" + planName + "] to userId: " + userId);
        return "Plan auto-assigned: " + planName;
    }

    // ✅ Manual plan activation (for frontend plan selection)
    @Transactional
    public String activatePlan(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId).orElse(null);
        if (plan == null) {
            return "Plan not found";
        }

        // Deactivate existing
        Optional<UserPlan> existingOpt = userPlanRepository.findByUserIdAndActiveTrue(userId);
        existingOpt.ifPresent(existing -> {
            existing.setActive(false);
            userPlanRepository.save(existing);
        });

        // Assign new
        UserPlan userPlan = new UserPlan(userId, plan, true);
        userPlanRepository.save(userPlan);

        System.out.println("✅ Plan [" + plan.getName() + "] activated for userId: " + userId);
        return "Plan activated successfully";
    }

    // ✅ Get user's active plan
    public UserPlan getUserPlan(Long userId) {
        return userPlanRepository.findByUserIdAndActiveTrue(userId).orElse(null);
    }

    // ✅ Calculate payout based on project's parametric rules
    public Map<String, Object> calculatePayout(Long userId, double aqi, double rainfallMmHr) {
        UserPlan userPlan = getUserPlan(userId);

        if (userPlan == null) {
            return Map.of(
                "triggered", false,
                "reason", "No active plan found",
                "amount", 0
            );
        }

        Plan plan = userPlan.getPlan();
        boolean aqiTriggered = aqi > plan.getAqiThreshold();
        boolean rainTriggered = rainfallMmHr > plan.getRainfallThreshold();
        boolean triggered = aqiTriggered || rainTriggered;

        if (!triggered) {
            return Map.of(
                "triggered", false,
                "reason", "Conditions normal — no trigger",
                "amount", 0,
                "plan", plan.getName()
            );
        }

        // Project rule: 30% payout per trigger day, capped at coverage
        double payoutPercent = 0.30;
        if (aqi > 350 || rainfallMmHr > 60) payoutPercent = 0.50; // severe conditions
        int payoutAmount = (int) Math.min(plan.getCoverage() * payoutPercent, plan.getCoverage());

        String reason = aqiTriggered
            ? "AQI " + (int) aqi + " exceeded threshold of " + plan.getAqiThreshold()
            : "Rainfall " + rainfallMmHr + "mm/hr exceeded threshold of " + plan.getRainfallThreshold() + "mm/hr";

        return Map.of(
            "triggered", true,
            "reason", reason,
            "amount", payoutAmount,
            "plan", plan.getName(),
            "coverage", plan.getCoverage(),
            "aqi", aqi,
            "rainfall", rainfallMmHr
        );
    }

    @Transactional
    public String createPlan(String name, int premium, int coverage, String email) {
        if (!email.equals("admin@devtrails.com")) {
            return "Access denied";
        }
        Plan plan = new Plan(name, premium, coverage);
        planRepository.save(plan);
        return "Plan created successfully";
    }
}