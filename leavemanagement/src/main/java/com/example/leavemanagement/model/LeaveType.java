package com.example.leavemanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "leave_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    private BigDecimal accrualRate; // days per year

    private Integer maxCarryover;

    private Boolean requiresDocumentation;

    private Boolean isActive;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "leaveType")
    @JsonIgnore
    private List<LeaveBalance> leaveBalances;

    @OneToMany(mappedBy = "leaveType")
    @JsonIgnore
    private List<LeaveRequest> leaveRequests;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
