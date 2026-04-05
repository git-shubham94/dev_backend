package com.devtrails.backend.controller;

import com.devtrails.backend.dto.LoginRequest;
import com.devtrails.backend.dto.RegisterRequest;
import com.devtrails.backend.model.User;
import com.devtrails.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        System.out.println("📝 Registration request for: " + request.phone);

        // Let AuthService handle ALL normalization — no regex here
        User user = new User(
            request.name,
            request.email,
            request.password,
            request.phone
        );

        String result = authService.register(user);

        // Return proper HTTP status based on result
        if (result.equals("User registered successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        System.out.println("🔐 Login request received");
        System.out.println("   Identifier: " + request.phone);

        Map<String, Object> response = authService.login(request.phone, request.password);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}