package com.example.leavemanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String department;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Add this new field for Intern status
    @Column(name = "is_intern", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isIntern = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LeaveBalance> leaveBalances;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "manager")
    @JsonIgnore
    private List<LeaveRequest> approvedRequests;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
