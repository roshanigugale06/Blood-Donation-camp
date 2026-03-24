package com.bloodbank;

import com.bloodbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BloodBankManagementApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(BloodBankManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================");
        System.out.println("Starting Blood Bank Management System...");
        System.out.println("=========================================");
        
        // Create default admin user if not exists
        userService.createDefaultAdmin();
        
        System.out.println("=========================================");
        System.out.println("Application Started!");
        System.out.println("Access URLs:");
        System.out.println("Home: http://localhost:8080/");
        System.out.println("Admin Login: http://localhost:8080/admin/login");
        System.out.println("User Login: http://localhost:8080/user/login");
        System.out.println("=========================================");
    }
}