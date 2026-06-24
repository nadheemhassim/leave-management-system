package com.example.leavemanagement.controller;

import com.example.leavemanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/unused-leave")
    public ResponseEntity<Map<String, Object>> getUnusedLeaveReport() {
        return ResponseEntity.ok(reportService.generateUnusedLeaveReport());
    }

    @GetMapping("/leave-summary")
    public ResponseEntity<Map<String, Object>> getLeaveSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateLeaveSummaryReport(startDate, endDate));
    }

    @GetMapping("/department-stats")
    public ResponseEntity<Map<String, Object>> getDepartmentReport() {
        return ResponseEntity.ok(reportService.generateDepartmentLeaveReport());
    }

}
