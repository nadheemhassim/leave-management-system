package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.InternLeave;
import com.example.leavemanagement.model.User;
import com.example.leavemanagement.service.InternLeaveService;
import com.example.leavemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intern-leave")
@CrossOrigin(origins = "http://localhost:3000")
public class InternLeaveController {


    @Autowired
    private InternLeaveService internLeaveService;

    private UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> requestInternLeave(@RequestBody InternLeave leaveRequest,
                                                @RequestParam Long userId,
                                                @RequestParam boolean isPaid) {
        try {
            InternLeave created = internLeaveService.requestInternLeave(leaveRequest, userId, isPaid);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/remaining-paid/{userId}")
    public ResponseEntity<?> getRemainingPaidLeave(@PathVariable Long userId,
                                                   @RequestParam int year,
                                                   @RequestParam int month) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            BigDecimal remaining = internLeaveService.getRemainingPaidLeaveForMonth(user, year, month);
            return ResponseEntity.ok(Map.of("remainingDays", remaining));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report/{userId}")
    public ResponseEntity<?> getInternLeaveReport(@PathVariable Long userId,
                                                  @RequestParam int year) {
        try {
            var report = internLeaveService.getInternLeaveReport(userId, year);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reports/all")
    public ResponseEntity<?> getAllInternReports(@RequestParam int year) {
        try {
            // Get all interns
            List<User> interns = userService.getInterns();
            List<Map<String, Object>> reports = new ArrayList<>();

            for (User intern : interns) {
                var report = internLeaveService.getInternLeaveReport(intern.getId(), year);
                Map<String, Object> internReport = new HashMap<>();
                internReport.put("employeeId", intern.getId());
                internReport.put("employeeName", intern.getFullName());
                internReport.put("department", intern.getDepartment());
                internReport.put("totalPaidDays", report.totalPaidDays);
                internReport.put("totalUnpaidDays", report.totalUnpaidDays);
                internReport.put("monthlyReports", report.monthlyReports);
                reports.add(internReport);
            }

            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
