package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.User;
import com.example.leavemanagement.service.AuthService;
import com.example.leavemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        System.out.println("Login attempt - Username: " + username); // Debug log

        User user = authService.login(username, password);

        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());
            response.put("email", user.getEmail());
            response.put("department", user.getDepartment());

            System.out.println("Login successful: " + username); // Debug log
            return ResponseEntity.ok(response);
        } else {
            System.out.println("Login failed: " + username); // Debug log
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }
}
