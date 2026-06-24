package com.example.leavemanagement.repository;

import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee(User employee);
    List<LeaveRequest> findByEmployeeAndStatus(User employee, LeaveStatus status);
    List<LeaveRequest> findByManager(User manager);
    List<LeaveRequest> findByManagerAndStatus(User manager, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.startDate BETWEEN :startDate AND :endDate")
    List<LeaveRequest> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status AND lr.startDate >= :startDate")
    List<LeaveRequest> findByStatusAndStartDateAfter(@Param("status") LeaveStatus status,
                                                     @Param("startDate") LocalDate startDate);

    // Get all requests (for admin)
    List<LeaveRequest> findAllByOrderBySubmittedAtDesc();

    List<LeaveRequest> findByStatus(LeaveStatus status);




}
