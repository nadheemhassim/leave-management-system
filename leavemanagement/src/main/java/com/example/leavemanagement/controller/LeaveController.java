package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.service.LeaveBalanceService;
import com.example.leavemanagement.service.LeaveRequestService;
import com.example.leavemanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> createLeaveRequest(@RequestBody LeaveRequest leaveRequest,
                                                @RequestParam Long employeeId) {
        try {
            User employee = userService.getUserById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            LeaveRequest created = leaveRequestService.createLeaveRequest(leaveRequest, employee);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getEmployeeRequests(@PathVariable Long employeeId) {
        User employee = userService.getUserById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return ResponseEntity.ok(leaveRequestService.getEmployeeLeaveRequests(employee));
    }

    // Universal approve endpoint - works for managers, admins, and MD
    @PutMapping("/requests/{requestId}/approve")
    public ResponseEntity<?> approveLeaveRequest(@PathVariable Long requestId,
                                                 @RequestParam Long approverId) {
        try {
            User approver = userService.getUserById(approverId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has permission to approve
            if (!hasApprovalPermission(approver)) {
                return ResponseEntity.status(403).body(Map.of("error", "You don't have permission to approve leave requests"));
            }

            LeaveRequest approved = leaveRequestService.approveLeaveRequest(requestId, approver);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Universal reject endpoint - works for managers, admins, and MD
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectLeaveRequest(@PathVariable Long requestId,
                                                @RequestParam Long approverId,
                                                @RequestBody Map<String, String> body) {
        try {
            User approver = userService.getUserById(approverId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has permission to reject
            if (!hasApprovalPermission(approver)) {
                return ResponseEntity.status(403).body(Map.of("error", "You don't have permission to reject leave requests"));
            }

            String reason = body.get("rejectionReason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rejection reason is required"));
            }

            LeaveRequest rejected = leaveRequestService.rejectLeaveRequest(requestId, approver, reason);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balances/{employeeId}")
    public ResponseEntity<List<Map<String, Object>>> getLeaveBalances(@PathVariable Long employeeId) {
        User employee = userService.getUserById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<LeaveBalance> balances = leaveBalanceService.getUserLeaveBalances(employee);

        List<Map<String, Object>> response = balances.stream().map(balance -> {
            Map<String, Object> balanceMap = new HashMap<>();
            balanceMap.put("id", balance.getId());
            balanceMap.put("totalDays", balance.getTotalDays());
            balanceMap.put("usedDays", balance.getUsedDays());
            balanceMap.put("availableDays", balance.getAvailableDays());
            balanceMap.put("year", balance.getYear());

            // Add leave type information
            Map<String, Object> leaveTypeMap = new HashMap<>();
            leaveTypeMap.put("id", balance.getLeaveType().getId());
            leaveTypeMap.put("name", balance.getLeaveType().getName());
            leaveTypeMap.put("description", balance.getLeaveType().getDescription());
            leaveTypeMap.put("accrualRate", balance.getLeaveType().getAccrualRate());
            leaveTypeMap.put("maxCarryover", balance.getLeaveType().getMaxCarryover());
            leaveTypeMap.put("requiresDocumentation", balance.getLeaveType().getRequiresDocumentation());
            leaveTypeMap.put("isActive", balance.getLeaveType().getIsActive());

            balanceMap.put("leaveType", leaveTypeMap);

            return balanceMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/balances/add")
    public ResponseEntity<?> addLeaveBalance(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long leaveTypeId = Long.valueOf(request.get("leaveTypeId").toString());
            java.math.BigDecimal daysToAdd = new java.math.BigDecimal(request.get("daysToAdd").toString());

            leaveBalanceService.addLeaveBalance(userId, leaveTypeId, daysToAdd);
            return ResponseEntity.ok(Map.of("message", "Leave balance updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all requests for a manager
    @GetMapping("/requests/manager/{managerId}/all")
    public ResponseEntity<List<LeaveRequest>> getAllRequestsForManager(@PathVariable Long managerId) {
        try {
            User manager = userService.getUserById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            List<LeaveRequest> requests = leaveRequestService.getAllRequestsForManager(manager);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get approved requests for a manager
    @GetMapping("/requests/manager/{managerId}/approved")
    public ResponseEntity<List<LeaveRequest>> getApprovedRequestsForManager(@PathVariable Long managerId) {
        try {
            User manager = userService.getUserById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            List<LeaveRequest> requests = leaveRequestService.getApprovedRequestsForManager(manager);
            System.out.println("Approved requests found: " + requests.size());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get rejected requests for a manager
    @GetMapping("/requests/manager/{managerId}/rejected")
    public ResponseEntity<List<LeaveRequest>> getRejectedRequestsForManager(@PathVariable Long managerId) {
        try {
            User manager = userService.getUserById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            List<LeaveRequest> requests = leaveRequestService.getRejectedRequestsForManager(manager);
            System.out.println("Rejected requests found: " + requests.size());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get all pending requests (for Admin and Managing Director)
    @GetMapping("/requests/pending/all")
    public ResponseEntity<List<LeaveRequest>> getAllPendingRequests() {
        try {
            List<LeaveRequest> pending = leaveRequestService.getAllPendingRequests();
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get pending requests for a specific manager
    @GetMapping("/requests/manager/{managerId}/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingRequestsForManager(@PathVariable Long managerId) {
        try {
            User manager = userService.getUserById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            List<LeaveRequest> requests = leaveRequestService.getPendingRequestsForManager(manager);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get all requests (for Admin and Managing Director)
    @GetMapping("/requests/all")
    public ResponseEntity<List<LeaveRequest>> getAllRequests() {
        try {
            List<LeaveRequest> allRequests = leaveRequestService.getAllRequests();
            return ResponseEntity.ok(allRequests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get approved requests (for Admin and Managing Director)
    @GetMapping("/requests/approved")
    public ResponseEntity<List<LeaveRequest>> getAllApprovedRequests() {
        try {
            List<LeaveRequest> approved = leaveRequestService.getAllApprovedRequests();
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get rejected requests (for Admin and Managing Director)
    @GetMapping("/requests/rejected")
    public ResponseEntity<List<LeaveRequest>> getAllRejectedRequests() {
        try {
            List<LeaveRequest> rejected = leaveRequestService.getAllRejectedRequests();
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get requests by status (generic)
    @GetMapping("/requests/status/{status}")
    public ResponseEntity<List<LeaveRequest>> getRequestsByStatus(@PathVariable String status) {
        try {
            LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
            List<LeaveRequest> requests = leaveRequestService.getRequestsByStatus(leaveStatus);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Check if user has approval permission
    private boolean hasApprovalPermission(User user) {
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.MANAGING_DIRECTOR || role == Role.MANAGER;
    }

}
