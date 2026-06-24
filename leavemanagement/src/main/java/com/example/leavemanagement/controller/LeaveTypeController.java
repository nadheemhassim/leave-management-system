package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-types")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveTypeController {
    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeaveType>> getActiveLeaveTypes() {
        return ResponseEntity.ok(leaveTypeRepository.findByIsActiveTrue());
    }

    @PostMapping
    public ResponseEntity<?> createLeaveType(@RequestBody LeaveType leaveType) {
        try {
            LeaveType saved = leaveTypeRepository.save(leaveType);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeaveType(@PathVariable Long id, @RequestBody LeaveType leaveType) {
        try {
            LeaveType existing = leaveTypeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Leave type not found"));

            existing.setName(leaveType.getName());
            existing.setDescription(leaveType.getDescription());
            existing.setAccrualRate(leaveType.getAccrualRate());
            existing.setMaxCarryover(leaveType.getMaxCarryover());
            existing.setRequiresDocumentation(leaveType.getRequiresDocumentation());
            existing.setIsActive(leaveType.getIsActive());

            return ResponseEntity.ok(leaveTypeRepository.save(existing));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeaveType(@PathVariable Long id) {
        try {
            leaveTypeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Leave type deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
