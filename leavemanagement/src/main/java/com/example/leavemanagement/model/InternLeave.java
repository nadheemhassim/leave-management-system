package com.example.leavemanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "intern_leaves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternLeave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Integer year;
    private Integer month;

    @Column(nullable = false)
    private BigDecimal totalDays;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private String reason;
    private String rejectionReason;

    @Column(name = "is_paid")
    private boolean paid = false;  // false = unpaid leave

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        status = LeaveStatus.PENDING;
    }
}
