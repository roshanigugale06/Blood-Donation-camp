package com.bloodbank.controller;

import com.bloodbank.model.User;
import com.bloodbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test-auth")
    public String testAuth(@RequestParam String username, @RequestParam String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "❌ User not found: " + username;
        }
        
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        
        StringBuilder result = new StringBuilder();
        result.append("📋 User Details:\n");
        result.append("Username: ").append(user.getUsername()).append("\n");
        result.append("Password in DB: ").append(user.getPassword()).append("\n");
        result.append("Password entered: ").append(password).append("\n");
        result.append("Password matches: ").append(passwordMatches ? "✅ YES" : "❌ NO").append("\n");
        result.append("Role: ").append(user.getRole()).append("\n");
        result.append("Enabled: ").append(user.isEnabled()).append("\n");
        
        if (passwordMatches) {
            result.append("\n✅ LOGIN SHOULD WORK!");
        } else {
            result.append("\n❌ LOGIN WILL FAIL - Password mismatch");
        }
        
        return result.toString().replace("\n", "<br>");
    }

    @GetMapping("/create-test-admin")
    public String createTestAdmin() {
        try {
            // Delete existing
            userRepository.findByUsername("testadmin").ifPresent(user -> userRepository.delete(user));
            
            User admin = new User();
            admin.setUsername("testadmin");
            admin.setPassword(passwordEncoder.encode("test123"));
            admin.setFullName("Test Admin");
            admin.setEmail("test@admin.com");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin.setRegistrationDate(java.time.LocalDateTime.now());
            
            userRepository.save(admin);
            
            return "✅ Test admin created: testadmin / test123";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}