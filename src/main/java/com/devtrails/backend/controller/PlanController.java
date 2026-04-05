package com.devtrails.backend.controller;

import com.devtrails.backend.dto.CreatePlanRequest;
import com.devtrails.backend.model.Plan;
import com.devtrails.backend.model.UserPlan;
import com.devtrails.backend.service.PlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // Get all 3 GigShield plans
    @GetMapping("/plans")
    public List<Plan> getPlans() {
        return planService.getAllPlans();
    }

    // Get active plan for a user
    @GetMapping("/plans/active/{userId}")
    public ResponseEntity<?> getActivePlan(@PathVariable Long userId) {
        UserPlan userPlan = planService.getUserPlan(userId);

        if (userPlan != null && userPlan.isActive()) {
            Plan plan = userPlan.getPlan();
            Map<String, Object> response = new HashMap<>();
            response.put("id", userPlan.getId());
            response.put("planId", plan.getId());
            response.put("name", plan.getName());
            response.put("tier", plan.getTier());
            response.put("premium", plan.getPremium());
            response.put("coverage", plan.getCoverage());
            response.put("description", plan.getDescription());
            response.put("aqiThreshold", plan.getAqiThreshold());
            response.put("rainfallThreshold", plan.getRainfallThreshold());
            response.put("startDate", userPlan.getStartDate());
            response.put("endDate", userPlan.getEndDate());
            response.put("isActive", true);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.noContent().build(); // 204 = no active plan
    }

    // Manual plan activation from frontend
    @PostMapping("/plans/activate")
    public ResponseEntity<Map<String, String>> activatePlan(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long planId = Long.valueOf(request.get("planId").toString());
        String message = planService.activatePlan(userId, planId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // AI auto-assign plan based on risk
    @PostMapping("/plans/auto-assign")
    public ResponseEntity<Map<String, String>> autoAssign(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String riskLevel = request.getOrDefault("riskLevel", "Moderate").toString();
        int activeDays = Integer.parseInt(request.getOrDefault("activeDays", "7").toString());

        String message = planService.autoAssignPlan(userId, riskLevel, activeDays);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // Parametric payout check (override-friendly for testing)
    @PostMapping("/plans/check-payout")
    public ResponseEntity<?> checkPayout(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        double aqi = Double.parseDouble(request.getOrDefault("aqi", "0").toString());
        double rainfall = Double.parseDouble(request.getOrDefault("rainfallMmHr", "0").toString());

        Map<String, Object> result = planService.calculatePayout(userId, aqi, rainfall);
        return ResponseEntity.ok(result);
    }

    // Backward compat
    @PostMapping("/activate-plan")
    public ResponseEntity<Map<String, String>> activatePlanOld(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long planId = Long.valueOf(request.get("planId").toString());
        String message = planService.activatePlan(userId, planId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/create-plan")
    public Map<String, String> createPlan(@RequestBody CreatePlanRequest request) {
        String message = planService.createPlan(
            request.name, request.premium, request.coverage, request.email
        );
        return Map.of("message", message);
    }
}