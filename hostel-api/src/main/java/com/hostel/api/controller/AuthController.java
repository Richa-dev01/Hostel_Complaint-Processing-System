package com.hostel.api.controller;

import com.hostel.api.dto.LoginRequest;
import com.hostel.api.dto.RegisterRequest;
import com.hostel.api.service.AuthService;
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
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of("message", "Registration successful. Please log in."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/student/login")
    public ResponseEntity<Map<String, String>> studentLogin(@RequestBody LoginRequest request) {
        return authService.loginStudent(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid email or password.")));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody LoginRequest request) {
        return authService.loginAdmin(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid email or password.")));
    }
}
