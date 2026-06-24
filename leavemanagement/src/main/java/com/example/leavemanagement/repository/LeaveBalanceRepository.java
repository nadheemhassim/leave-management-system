package com.example.leavemanagement.repository;

import com.example.leavemanagement.model.LeaveBalance;
import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByUser(User user);
    Optional<LeaveBalance> findByUserAndLeaveTypeAndYear(User user, LeaveType leaveType, Integer year);
    List<LeaveBalance> findByUserAndYear(User user, Integer year);
}
