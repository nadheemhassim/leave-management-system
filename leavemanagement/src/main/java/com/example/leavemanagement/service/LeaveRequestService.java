package com.example.leavemanagement.service;

import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.DocumentRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.repository.LeaveTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest, User employee) {
        // Calculate total days
        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        leaveRequest.setTotalDays(BigDecimal.valueOf(days));

        // Check if employee has enough balance
        LeaveType leaveType = leaveTypeRepository.findById(leaveRequest.getLeaveType().getId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        List<LeaveBalance> balances = leaveBalanceService.getUserLeaveBalances(employee);
        LeaveBalance balance = balances.stream()
                .filter(b -> b.getLeaveType().getId().equals(leaveType.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No balance found for this leave type"));

        if (balance.getAvailableDays().compareTo(leaveRequest.getTotalDays()) < 0) {
            throw new RuntimeException("Insufficient leave balance");
        }

        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getEmployeeLeaveRequests(User employee) {
        return leaveRequestRepository.findByEmployee(employee);
    }

    public List<LeaveRequest> getPendingRequestsForManager(User manager) {
        return leaveRequestRepository.findByManagerAndStatus(manager, LeaveStatus.PENDING);
    }

    public List<LeaveRequest> getAllPendingRequests() {
        return leaveRequestRepository.findByStatusAndStartDateAfter(LeaveStatus.PENDING, LocalDate.now().minusDays(30));
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(Long requestId, User manager, String rejectionReason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        request.setManager(manager);
        request.setStatus(LeaveStatus.APPROVED);
        request.setReviewedAt(LocalDate.now().atStartOfDay());

        // Update leave balance
        leaveBalanceService.updateLeaveBalance(request.getEmployee(), request.getLeaveType(), request.getTotalDays());

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(Long requestId, User manager, String rejectionReason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        request.setManager(manager);
        request.setStatus(LeaveStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        request.setReviewedAt(LocalDate.now().atStartOfDay());

        return leaveRequestRepository.save(request);
    }

    public List<LeaveRequest> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        // Implementation for calendar view
        return leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED)
                .filter(lr -> !(lr.getEndDate().isBefore(startDate) || lr.getStartDate().isAfter(endDate)))
                .collect(Collectors.toList());
    }

    // Get all requests for a manager (all statuses)
    public List<LeaveRequest> getAllRequestsForManager(User manager) {
        return leaveRequestRepository.findByManager(manager);
    }

    // Get approved requests for a manager
    public List<LeaveRequest> getApprovedRequestsForManager(User manager) {
        return leaveRequestRepository.findByManagerAndStatus(manager, LeaveStatus.APPROVED);
    }

    // Get rejected requests for a manager
    public List<LeaveRequest> getRejectedRequestsForManager(User manager) {
        return leaveRequestRepository.findByManagerAndStatus(manager, LeaveStatus.REJECTED);
    }

    // Get all requests (for Admin/MD)
    public List<LeaveRequest> getAllRequests() {
        return leaveRequestRepository.findAllByOrderBySubmittedAtDesc();
    }

    // Get all approved requests (for Admin/MD)
    public List<LeaveRequest> getAllApprovedRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.APPROVED);
    }

    // Get all rejected requests (for Admin/MD)
    public List<LeaveRequest> getAllRejectedRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.REJECTED);
    }

    // Get requests by status
    public List<LeaveRequest> getRequestsByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(Long requestId, User approver) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        request.setManager(approver);
        request.setStatus(LeaveStatus.APPROVED);
        request.setReviewedAt(LocalDate.now().atStartOfDay());

        // Update leave balance
        leaveBalanceService.updateLeaveBalance(request.getEmployee(), request.getLeaveType(), request.getTotalDays());

        return leaveRequestRepository.save(request);
    }


}
