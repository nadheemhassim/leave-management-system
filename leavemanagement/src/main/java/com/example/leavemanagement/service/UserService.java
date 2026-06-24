package com.example.leavemanagement.service;

import com.example.leavemanagement.model.Role;
import com.example.leavemanagement.model.User;
import com.example.leavemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getEmployees() {
        return userRepository.findByRole(Role.EMPLOYEE);
    }

    public List<User> getManagers() {
        return userRepository.findByRole(Role.MANAGER);
    }

//    public User createUser(User user) {
//        if (userRepository.existsByUsername(user.getUsername())) {
//            throw new RuntimeException("Username already exists");
//        }
//        user.setCreatedAt(LocalDateTime.now());
//        user.setUpdatedAt(LocalDateTime.now());
//        // Default isIntern to false if not set
//        user.setIntern(userDetails.isIntern());
//
//
//        return userRepository.save(user);
//    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // isIntern defaults to false automatically (primitive boolean default)
        // No null check needed
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setDepartment(userDetails.getDepartment());
        user.setRole(userDetails.getRole());
        user.setIntern(userDetails.isIntern()); // Add this line
        user.setUpdatedAt(LocalDateTime.now());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean checkLogin(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password).isPresent();
    }

    // Optional: Get all interns
    public List<User> getInterns() {
        return userRepository.findByIsInternTrue();
    }
}
