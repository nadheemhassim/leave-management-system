package com.example.leavemanagement.service;

import com.example.leavemanagement.model.InternLeave;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.model.User;
import com.example.leavemanagement.repository.InternLeaveRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class InternLeaveService {

    private static final BigDecimal MONTHLY_PAID_LEAVE_DAYS = new BigDecimal("0.5");

    @Autowired
    private InternLeaveRepository internLeaveRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    public BigDecimal getRemainingPaidLeaveForMonth(User user, int year, int month) {
        BigDecimal usedPaid = internLeaveRepository.getTotalPaidLeavesInMonth(user, year, month);
        if (usedPaid == null) usedPaid = BigDecimal.ZERO;
        return MONTHLY_PAID_LEAVE_DAYS.subtract(usedPaid);
    }

    @Transactional
    public InternLeave requestInternLeave(InternLeave leaveRequest, Long userId, boolean isPaid) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isIntern()) {
            throw new RuntimeException("This feature is only for interns");
        }

        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        leaveRequest.setTotalDays(BigDecimal.valueOf(days));
        leaveRequest.setUser(user);
        leaveRequest.setYear(leaveRequest.getStartDate().getYear());
        leaveRequest.setMonth(leaveRequest.getStartDate().getMonthValue());
        leaveRequest.setPaid(isPaid);

        if (isPaid) {
            // Check if intern has remaining paid leave for this month
            BigDecimal remaining = getRemainingPaidLeaveForMonth(user, leaveRequest.getYear(), leaveRequest.getMonth());
            if (leaveRequest.getTotalDays().compareTo(remaining) > 0) {
                throw new RuntimeException("Insufficient paid leave for this month. Only " + remaining + " days remaining.");
            }
        }

        return internLeaveRepository.save(leaveRequest);
    }

    public List<InternLeave> getInternLeaves(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return internLeaveRepository.findByUser(user);
    }

    public List<InternLeave> getPendingInternLeaves() {
        return internLeaveRepository.findByStatus(LeaveStatus.PENDING);
    }

    @Transactional
    public InternLeave approveInternLeave(Long leaveId) {
        InternLeave leave = internLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedAt(LocalDateTime.now());
        return internLeaveRepository.save(leave);
    }

    @Transactional
    public InternLeave rejectInternLeave(Long leaveId, String reason) {
        InternLeave leave = internLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(reason);
        leave.setReviewedAt(LocalDateTime.now());
        return internLeaveRepository.save(leave);
    }

    public InternLeaveReport getInternLeaveReport(Long userId, Integer year) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<InternLeave> allLeaves = internLeaveRepository.findByUser(user);

        BigDecimal totalPaidDays = BigDecimal.ZERO;
        BigDecimal totalUnpaidDays = BigDecimal.ZERO;
        List<MonthlyReport> monthlyReports = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            BigDecimal paidThisMonth = internLeaveRepository.getTotalPaidLeavesInMonth(user, year, month);
            BigDecimal unpaidThisMonth = internLeaveRepository.getTotalUnpaidLeavesInMonth(user, year, month);

            if (paidThisMonth == null) paidThisMonth = BigDecimal.ZERO;
            if (unpaidThisMonth == null) unpaidThisMonth = BigDecimal.ZERO;

            totalPaidDays = totalPaidDays.add(paidThisMonth);
            totalUnpaidDays = totalUnpaidDays.add(unpaidThisMonth);

            monthlyReports.add(new MonthlyReport(month, paidThisMonth, unpaidThisMonth));
        }

        return new InternLeaveReport(user, year, totalPaidDays, totalUnpaidDays, monthlyReports);
    }

    // Inner classes for report
    public static class MonthlyReport {
        public int month;
        public BigDecimal paidDays;
        public BigDecimal unpaidDays;

        public MonthlyReport(int month, BigDecimal paidDays, BigDecimal unpaidDays) {
            this.month = month;
            this.paidDays = paidDays;
            this.unpaidDays = unpaidDays;
        }
    }

    public static class InternLeaveReport {
        public String employeeName;
        public String department;
        public int year;
        public BigDecimal totalPaidDays;
        public BigDecimal totalUnpaidDays;
        public List<MonthlyReport> monthlyReports;

        public InternLeaveReport(User user, int year, BigDecimal totalPaidDays, BigDecimal totalUnpaidDays, List<MonthlyReport> monthlyReports) {
            this.employeeName = user.getFullName();
            this.department = user.getDepartment();
            this.year = year;
            this.totalPaidDays = totalPaidDays;
            this.totalUnpaidDays = totalUnpaidDays;
            this.monthlyReports = monthlyReports;
        }
    }
}
