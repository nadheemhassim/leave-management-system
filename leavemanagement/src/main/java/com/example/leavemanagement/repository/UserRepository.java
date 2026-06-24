package com.example.leavemanagement.repository;

import com.example.leavemanagement.model.Role;
import com.example.leavemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
    List<User> findByRole(Role role);
    List<User> findByRoleIn(List<Role> roles);
    boolean existsByUsername(String username);

    // Add this method to find interns
    List<User> findByIsInternTrue();
}
