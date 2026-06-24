package com.example.leavemanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal totalDays;

    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private String rejectionReason;

    private String documentPath;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        status = LeaveStatus.PENDING;
    }
}
