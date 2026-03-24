package com.bloodbank.service;

import com.bloodbank.model.User;
import com.bloodbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User registerAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ADMIN");
        user.setEnabled(true);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getDonors() {
        // Return only non-deleted users with role USER
        return userRepository.findByRoleAndDeletedFalse("USER");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (user != null) {
            // Soft delete - just mark as deleted and disable
            user.setDeleted(true);
            user.setEnabled(false);
            userRepository.save(user);
            System.out.println("User " + user.getUsername() + " has been soft deleted");
        }
    }

    @Transactional
    public void updateLastLogin(String username) {
        User user = findByUsername(username);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void createDefaultAdmin() {
        System.out.println("========== ATTEMPTING TO CREATE ADMIN ==========");
        try {
            // Check if admin already exists
            User existingAdmin = userRepository.findByUsername("admin1").orElse(null);
            
            if (existingAdmin == null) {
                System.out.println("Admin user not found. Creating new admin...");
                
                User admin = new User();
                admin.setUsername("admin1");
                
                // Encode password
                String rawPassword = "admin123@";
                String encodedPassword = passwordEncoder.encode(rawPassword);
                admin.setPassword(encodedPassword);
                
                admin.setFullName("System Administrator");
                admin.setEmail("admin@bloodbank.com");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                admin.setDeleted(false);
                admin.setRegistrationDate(LocalDateTime.now());
                
                User savedAdmin = userRepository.save(admin);
                
                System.out.println("✅ ADMIN CREATED SUCCESSFULLY!");
                System.out.println("   Username: " + savedAdmin.getUsername());
                System.out.println("   Password: admin123@");
                System.out.println("   Role: " + savedAdmin.getRole());
                System.out.println("   ID: " + savedAdmin.getId());
                
                // Verify password encoding
                boolean passwordMatches = passwordEncoder.matches("admin123@", savedAdmin.getPassword());
                System.out.println("   Password verification: " + (passwordMatches ? "✅ WORKS" : "❌ FAILED"));
                
            } else {
                System.out.println("✅ Admin user already exists!");
                System.out.println("   Username: " + existingAdmin.getUsername());
                System.out.println("   Role: " + existingAdmin.getRole());
                
                // Update password to ensure it's correct
                String encodedPassword = passwordEncoder.encode("admin123@");
                existingAdmin.setPassword(encodedPassword);
                existingAdmin.setDeleted(false);
                userRepository.save(existingAdmin);
                System.out.println("   Password updated to ensure correctness");
                
                // Verify password
                boolean passwordMatches = passwordEncoder.matches("admin123@", existingAdmin.getPassword());
                System.out.println("   Password verification: " + (passwordMatches ? "✅ WORKS" : "❌ FAILED"));
            }
            
            // Verify we can find the admin
            User verifyAdmin = userRepository.findByUsername("admin1").orElse(null);
            if (verifyAdmin != null) {
                System.out.println("✅ Admin verification: User found in database");
            } else {
                System.out.println("❌ Admin verification: User NOT found in database");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR CREATING ADMIN: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("========== ADMIN CREATION COMPLETE ==========");
    }
}