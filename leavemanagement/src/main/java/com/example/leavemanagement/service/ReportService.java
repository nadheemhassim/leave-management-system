package com.example.leavemanagement.service;

import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.LeaveBalanceRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.repository.LeaveTypeRepository;
import com.example.leavemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    public Map<String, Object> generateUnusedLeaveReport() {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> employeeReports = new ArrayList<>();

        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        int currentYear = Year.now().getValue();

        BigDecimal totalFinancialLiability = BigDecimal.ZERO;

        for (User employee : employees) {
            Map<String, Object> employeeReport = new HashMap<>();
            employeeReport.put("employeeId", employee.getId());
            employeeReport.put("employeeName", employee.getFullName());
            employeeReport.put("department", employee.getDepartment());

            List<Map<String, Object>> leaveDetails = new ArrayList<>();
            BigDecimal employeeLiability = BigDecimal.ZERO;

            for (LeaveType leaveType : leaveTypes) {
                Optional<LeaveBalance> balance = leaveBalanceRepository
                        .findByUserAndLeaveTypeAndYear(employee, leaveType, currentYear);

                if (balance.isPresent()) {
                    BigDecimal unusedDays = balance.get().getAvailableDays();
                    if (unusedDays.compareTo(BigDecimal.ZERO) > 0) {
                        Map<String, Object> leaveDetail = new HashMap<>();
                        leaveDetail.put("leaveType", leaveType.getName());
                        leaveDetail.put("unusedDays", unusedDays);

                        // Assuming average daily salary calculation (you can adjust this)
                        BigDecimal dailyRate = BigDecimal.valueOf(1000); // Example rate
                        BigDecimal liability = unusedDays.multiply(dailyRate);
                        leaveDetail.put("financialLiability", liability);

                        leaveDetails.add(leaveDetail);
                        employeeLiability = employeeLiability.add(liability);
                    }
                }
            }

            employeeReport.put("leaveDetails", leaveDetails);
            employeeReport.put("totalLiability", employeeLiability);
            employeeReports.add(employeeReport);
            totalFinancialLiability = totalFinancialLiability.add(employeeLiability);
        }

        report.put("employeeReports", employeeReports);
        report.put("totalFinancialLiability", totalFinancialLiability);
        report.put("generatedDate", LocalDate.now());

        return report;
    }

    public Map<String, Object> generateLeaveSummaryReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED)
                .filter(lr -> !lr.getStartDate().isAfter(endDate) && !lr.getEndDate().isBefore(startDate))
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRequests", approvedLeaves.size());
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        Map<String, Integer> leavesByType = new HashMap<>();
        Map<String, Integer> leavesByDepartment = new HashMap<>();

        for (LeaveRequest leave : approvedLeaves) {
            // By type
            String typeName = leave.getLeaveType().getName();
            leavesByType.put(typeName, leavesByType.getOrDefault(typeName, 0) + 1);

            // By department
            String department = leave.getEmployee().getDepartment();
            if (department != null) {
                leavesByDepartment.put(department, leavesByDepartment.getOrDefault(department, 0) + 1);
            }
        }

        summary.put("leavesByType", leavesByType);
        summary.put("leavesByDepartment", leavesByDepartment);
        report.put("summary", summary);

        List<Map<String, Object>> leaveDetails = approvedLeaves.stream()
                .map(lr -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("employee", lr.getEmployee().getFullName());
                    detail.put("leaveType", lr.getLeaveType().getName());
                    detail.put("startDate", lr.getStartDate());
                    detail.put("endDate", lr.getEndDate());
                    detail.put("totalDays", lr.getTotalDays());
                    return detail;
                })
                .collect(Collectors.toList());

        report.put("details", leaveDetails);

        return report;
    }

    public Map<String, Object> generateDepartmentLeaveReport() {
        Map<String, Object> report = new HashMap<>();

        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        Map<String, List<User>> employeesByDepartment = employees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(User::getDepartment));

        Map<String, Map<String, Object>> departmentStats = new HashMap<>();

        for (Map.Entry<String, List<User>> entry : employeesByDepartment.entrySet()) {
            String department = entry.getKey();
            List<User> deptEmployees = entry.getValue();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEmployees", deptEmployees.size());

            // Calculate total leaves taken this year
            int currentYear = Year.now().getValue();
            BigDecimal totalLeavesTaken = BigDecimal.ZERO;

            for (User employee : deptEmployees) {
                List<LeaveBalance> balances = leaveBalanceRepository.findByUserAndYear(employee, currentYear);
                for (LeaveBalance balance : balances) {
                    totalLeavesTaken = totalLeavesTaken.add(balance.getUsedDays());
                }
            }

            stats.put("totalLeavesTaken", totalLeavesTaken);
            stats.put("averageLeavesPerEmployee", totalLeavesTaken.divide(BigDecimal.valueOf(deptEmployees.size()), 2, BigDecimal.ROUND_HALF_UP));

            departmentStats.put(department, stats);
        }

        report.put("departmentStats", departmentStats);
        report.put("generatedDate", LocalDate.now());

        return report;
    }
}
