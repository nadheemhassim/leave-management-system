package com.example.leavemanagement.repository;

import com.example.leavemanagement.model.InternLeave;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InternLeaveRepository  extends JpaRepository<InternLeave, Long> {
    List<InternLeave> findByUser(User user);
    List<InternLeave> findByUserAndStatus(User user, LeaveStatus status);
    List<InternLeave> findByUserAndYearAndMonth(User user, Integer year, Integer month);

    @Query("SELECT SUM(il.totalDays) FROM InternLeave il WHERE il.user = :user AND il.year = :year AND il.month = :month AND il.paid = true AND il.status = 'APPROVED'")
    BigDecimal getTotalPaidLeavesInMonth(@Param("user") User user, @Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT SUM(il.totalDays) FROM InternLeave il WHERE il.user = :user AND il.year = :year AND il.month = :month AND il.paid = false AND il.status = 'APPROVED'")
    BigDecimal getTotalUnpaidLeavesInMonth(@Param("user") User user, @Param("year") Integer year, @Param("month") Integer month);

    List<InternLeave> findByStatus(LeaveStatus status);  // Add this method



}
