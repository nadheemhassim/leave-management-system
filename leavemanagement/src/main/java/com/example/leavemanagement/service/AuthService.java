package com.example.leavemanagement.service;

import com.example.leavemanagement.model.Role;
import com.example.leavemanagement.model.User;
import com.example.leavemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

//    public User login(String username, String password) {
//        System.out.println("AuthService.login called with: " + username); // Debug log
//        Optional<User> user = userRepository.findByUsernameAndPassword(username, password);
//        return user.orElse(null);
//    }

    public User login(String username, String password) {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {

            User u = user.get();

            // plain password check (since DB is not encrypted)
            if (password.equals(u.getPassword())) {
                return u;
            }
        }

        return null;
    }

    public boolean hasAccess(User user, String requiredRole) {
        if (user == null) return false;

        Role role = user.getRole();

        switch (requiredRole.toUpperCase()) {
            case "MANAGING_DIRECTOR":
                return role == Role.MANAGING_DIRECTOR;
            case "ADMIN":
                return role == Role.MANAGING_DIRECTOR || role == Role.ADMIN;
            case "MANAGER":
                return role == Role.MANAGING_DIRECTOR || role == Role.ADMIN || role == Role.MANAGER;
            case "EMPLOYEE":
                return true;
            default:
                return false;
        }
    }
}
