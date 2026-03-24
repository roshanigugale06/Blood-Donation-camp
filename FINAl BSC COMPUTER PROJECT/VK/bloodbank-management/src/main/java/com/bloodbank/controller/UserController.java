package com.bloodbank.controller;

import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.Donation;
import com.bloodbank.model.User;
import com.bloodbank.service.BloodRequestService;
import com.bloodbank.service.BloodStockService;
import com.bloodbank.service.DonationService;
import com.bloodbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BloodStockService bloodStockService;

    @Autowired
    private BloodRequestService bloodRequestService;

    @Autowired
    private DonationService donationService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        model.addAttribute("user", user);
        model.addAttribute("recentRequests", bloodRequestService.getUserRequests(user));
        model.addAttribute("recentDonations", donationService.getUserDonations(user));
        model.addAttribute("availableStocks", bloodStockService.getAllStocks());
        
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User existingUser = userService.findByUsername(username);
        
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setCity(updatedUser.getCity());
        existingUser.setState(updatedUser.getState());
        existingUser.setBloodGroup(updatedUser.getBloodGroup());
        existingUser.setAge(updatedUser.getAge());
        existingUser.setGender(updatedUser.getGender());
        
        userService.updateUser(existingUser);
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
        return "redirect:/user/profile";
    }

    @GetMapping("/request-blood")
    public String showRequestForm(Model model) {
        model.addAttribute("bloodRequest", new BloodRequest());
        model.addAttribute("bloodGroups", bloodStockService.getAllBloodGroups());
        return "user/request-blood";
    }

    @PostMapping("/request-blood")
    public String submitRequest(@ModelAttribute BloodRequest bloodRequest, 
                                Authentication authentication,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            // Validate required fields
            if (bloodRequest.getPatientName() == null || bloodRequest.getPatientName().trim().isEmpty()) {
                model.addAttribute("error", "Patient name is required");
                model.addAttribute("bloodGroups", bloodStockService.getAllBloodGroups());
                return "user/request-blood";
            }
            
            if (bloodRequest.getBloodGroup() == null || bloodRequest.getBloodGroup().trim().isEmpty()) {
                model.addAttribute("error", "Blood group is required");
                model.addAttribute("bloodGroups", bloodStockService.getAllBloodGroups());
                return "user/request-blood";
            }
            
            if (bloodRequest.getUnits() == null || bloodRequest.getUnits() < 1) {
                model.addAttribute("error", "Valid units are required");
                model.addAttribute("bloodGroups", bloodStockService.getAllBloodGroups());
                return "user/request-blood";
            }
            
            bloodRequest.setUser(user);
            bloodRequestService.createRequest(bloodRequest);
            
            redirectAttributes.addFlashAttribute("message", "Blood request submitted successfully!");
            return "redirect:/user/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error submitting request: " + e.getMessage());
            model.addAttribute("bloodGroups", bloodStockService.getAllBloodGroups());
            return "user/request-blood";
        }
    }

    @GetMapping("/donation-history")
    public String donationHistory(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        model.addAttribute("donations", donationService.getUserDonations(user));
        return "user/donation-history";
    }

    @GetMapping("/register-donation")
    public String showDonationForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        model.addAttribute("donation", new Donation());
        
        // Check if user is eligible to donate
        boolean isEligible = donationService.isEligibleToDonate(user);
        model.addAttribute("isEligible", isEligible);
        
        return "user/register-donation";
    }

    @PostMapping("/register-donation")
    public String registerDonation(@ModelAttribute Donation donation,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            // Validate required fields
            if (donation.getBloodGroup() == null || donation.getBloodGroup().trim().isEmpty()) {
                model.addAttribute("error", "Blood group is required");
                model.addAttribute("donation", donation);
                return "user/register-donation";
            }
            
            if (donation.getQuantity() == null || donation.getQuantity() < 350 || donation.getQuantity() > 450) {
                model.addAttribute("error", "Valid quantity between 350-450 ml is required");
                model.addAttribute("donation", donation);
                return "user/register-donation";
            }
            
            if (donation.getHospitalName() == null || donation.getHospitalName().trim().isEmpty()) {
                model.addAttribute("error", "Hospital name is required");
                model.addAttribute("donation", donation);
                return "user/register-donation";
            }
            
            if (donation.getDonationType() == null || donation.getDonationType().trim().isEmpty()) {
                model.addAttribute("error", "Donation type is required");
                model.addAttribute("donation", donation);
                return "user/register-donation";
            }
            
            donation.setUser(user);
            donation.setStatus("PENDING");
            donationService.saveDonation(donation);
            
            redirectAttributes.addFlashAttribute("message", "Donation registered successfully! Awaiting approval.");
            return "redirect:/user/donation-history";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error registering donation: " + e.getMessage());
            model.addAttribute("donation", donation);
            return "user/register-donation";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, 
                               RedirectAttributes redirectAttributes,
                               Model model) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                model.addAttribute("error", "Username already exists!");
                model.addAttribute("user", user);
                return "user/register";
            }
            
            if (userService.existsByEmail(user.getEmail())) {
                model.addAttribute("error", "Email already registered!");
                model.addAttribute("user", user);
                return "user/register";
            }
            
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/user/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error during registration: " + e.getMessage());
            model.addAttribute("user", user);
            return "user/register";
        }
    }
}