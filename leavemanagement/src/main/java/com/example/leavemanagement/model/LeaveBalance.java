package com.example.leavemanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    @JsonIgnore
    private LeaveType leaveType;

    @Column(nullable = false)
    private BigDecimal totalDays;

    @Column(nullable = false)
    private BigDecimal usedDays;

    @Column(nullable = false)
    private BigDecimal availableDays;

    private Integer year;

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
