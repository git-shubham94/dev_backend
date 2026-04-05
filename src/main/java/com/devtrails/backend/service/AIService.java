package com.devtrails.backend.service;

import com.devtrails.backend.repository.UserPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final PlanService planService;
    private final String AI_URL = "http://localhost:8000/evaluate";

    private static final Map<String, double[]> ZONE_COORDS = Map.of(
        "Zone_A_Bangalore", new double[]{12.9716, 77.5946},
        "Zone_B_Mumbai",    new double[]{19.0760, 72.8777},
        "Zone_C_Delhi",     new double[]{28.7041, 77.1025},
        "Zone_D_Hyderabad", new double[]{17.3850, 78.4867},
        "Zone_E_Chennai",   new double[]{13.0827, 80.2707}
    );

    public AIService(PlanService planService) {
        this.restTemplate = new RestTemplate();
        this.planService = planService;
    }

    public Map<String, Object> evaluateWorkerRisk(Map<String, Object> workerData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(workerData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(AI_URL, request, Map.class);
            if (response.getBody() != null) {
                return response.getBody();
            }
            return getDefaultResponse();

        } catch (Exception e) {
            System.err.println("⚠️ AI Service unavailable, using defaults: " + e.getMessage());
            return getDefaultResponse();
        }
    }

    // ✅ Evaluate + auto-assign plan based on AI result
    public Map<String, Object> evaluateAndAssignPlan(Long userId, int numActiveDays, String zone) {
        String resolvedZone = (zone != null && !zone.isEmpty()) ? zone : "Zone_B_Mumbai";
        double[] coords = ZONE_COORDS.getOrDefault(resolvedZone, new double[]{19.0760, 72.8777});

        Map<String, Object> workerData = new HashMap<>();
        workerData.put("worker_id", userId.toString());
        workerData.put("zone", resolvedZone);
        workerData.put("gps_lat", coords[0]);
        workerData.put("gps_lon", coords[1]);
        workerData.put("daily_earnings_inr", 800.0);
        workerData.put("weekly_earnings_inr", 4500.0);
        workerData.put("num_deliveries", numActiveDays * 3);
        workerData.put("active_hours", 6.0);
        workerData.put("gps_speed_variance", 1.2);
        workerData.put("location_jump_km", 0.0);

        Map<String, Object> aiResult = evaluateWorkerRisk(workerData);
        String riskLevel = (String) aiResult.getOrDefault("risk_level", "Moderate");

        System.out.println("🤖 AI Risk for userId " + userId + ": " + riskLevel);

        // Auto-assign plan based on AI risk
        String planMsg = planService.autoAssignPlan(userId, riskLevel, numActiveDays);
        aiResult.put("planAssigned", planMsg);

        return aiResult;
    }

    public Map<String, Object> checkPaymentFraud(String userId, Double amount, String zone) {
        String resolvedZone = (zone != null && !zone.isEmpty()) ? zone : "Zone_B_Mumbai";
        double[] coords = ZONE_COORDS.getOrDefault(resolvedZone, new double[]{19.0760, 72.8777});

        Map<String, Object> request = new HashMap<>();
        request.put("worker_id", userId);
        request.put("zone", resolvedZone);
        request.put("gps_lat", coords[0]);
        request.put("gps_lon", coords[1]);
        request.put("daily_earnings_inr", 800.0);
        request.put("weekly_earnings_inr", 4500.0);
        request.put("num_deliveries", 15);
        request.put("active_hours", 6.0);
        request.put("gps_speed_variance", 1.2);
        request.put("location_jump_km", 0.0);

        return evaluateWorkerRisk(request);
    }

    private Map<String, Object> getDefaultResponse() {
        Map<String, Object> def = new HashMap<>();
        def.put("status", "PROCESSED");
        def.put("risk_level", "Moderate");
        def.put("weekly_premium_inr", 35.0);
        def.put("fraud_check", "Normal");
        def.put("risk_score", 50);
        def.put("payout", Map.of("amount", 0, "triggered", false));
        def.put("live_conditions", Map.of(
            "rainfall_mm_hr", 25.0,
            "aqi", 200,
            "temperature_c", 30.0,
            "humidity_pct", 65
        ));
        return def;
    }
}