package com.example.leavemanagement.service;

import com.example.leavemanagement.model.LeaveBalance;
import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.model.User;
import com.example.leavemanagement.repository.LeaveBalanceRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.repository.LeaveTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Service
public class LeaveBalanceService {
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Transactional
    public LeaveBalance initializeLeaveBalance(User user, LeaveType leaveType, Integer year) {
        LeaveBalance balance = new LeaveBalance();
        balance.setUser(user);
        balance.setLeaveType(leaveType);
        balance.setYear(year);
        balance.setTotalDays(leaveType.getAccrualRate());
        balance.setUsedDays(BigDecimal.ZERO);
        balance.setAvailableDays(leaveType.getAccrualRate());
        return leaveBalanceRepository.save(balance);
    }

    public List<LeaveBalance> getUserLeaveBalances(User user) {
        int currentYear = Year.now().getValue();
        return leaveBalanceRepository.findByUserAndYear(user, currentYear);
    }

    @Transactional
    public void updateLeaveBalance(User user, LeaveType leaveType, BigDecimal daysUsed) {
        int currentYear = Year.now().getValue();
        LeaveBalance balance = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, currentYear)
                .orElseGet(() -> initializeLeaveBalance(user, leaveType, currentYear));

        balance.setUsedDays(balance.getUsedDays().add(daysUsed));
        balance.setAvailableDays(balance.getTotalDays().subtract(balance.getUsedDays()));
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void addLeaveBalance(Long userId, Long leaveTypeId, BigDecimal daysToAdd) {
        User user = new User();
        user.setId(userId);

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        int currentYear = Year.now().getValue();
        LeaveBalance balance = leaveBalanceRepository
                .findByUserAndLeaveTypeAndYear(user, leaveType, currentYear)
                .orElseGet(() -> initializeLeaveBalance(user, leaveType, currentYear));

        balance.setTotalDays(balance.getTotalDays().add(daysToAdd));
        balance.setAvailableDays(balance.getAvailableDays().add(daysToAdd));
        leaveBalanceRepository.save(balance);
    }
}
