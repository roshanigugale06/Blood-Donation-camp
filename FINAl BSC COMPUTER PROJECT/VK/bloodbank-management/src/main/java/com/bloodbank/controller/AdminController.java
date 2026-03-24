package com.bloodbank.controller;

import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.BloodStock;
import com.bloodbank.model.Donation;
import com.bloodbank.model.User;
import com.bloodbank.service.BloodStockService;
import com.bloodbank.service.UserService;
import com.bloodbank.service.BloodRequestService;
import com.bloodbank.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BloodStockService bloodStockService;

    @Autowired
    private BloodRequestService bloodRequestService;

    @Autowired
    private DonationService donationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.getDonors().size());
        model.addAttribute("totalDonations", donationService.getAllDonations().size());
        model.addAttribute("totalRequests", bloodRequestService.getAllRequests().size());
        model.addAttribute("pendingRequests", bloodRequestService.getPendingRequests().size());
        model.addAttribute("bloodStocks", bloodStockService.getAllStocks());
        model.addAttribute("recentRequests", bloodRequestService.getRecentRequests(5));
        return "admin/dashboard";
    }

    @GetMapping("/manage-users")
public String manageUsers(Model model) {
    List<User> users = userService.getDonors();
    model.addAttribute("users", users);
    
    // Calculate donors count (users with blood group set)
    long donorsCount = users.stream()
            .filter(user -> user.getBloodGroup() != null && !user.getBloodGroup().isEmpty())
            .count();
    model.addAttribute("donorsCount", donorsCount);
    
    return "admin/manage-users";
}
    @GetMapping("/manage-stock")
    public String manageStock(Model model) {
        model.addAttribute("stocks", bloodStockService.getAllStocks());
        return "admin/manage-stock";
    }

    @PostMapping("/update-stock")
public String updateStock(@ModelAttribute BloodStock stock, RedirectAttributes redirectAttributes) {
    try {
        BloodStock existingStock = bloodStockService.getStockByBloodGroup(stock.getBloodGroup());
        if (existingStock != null) {
            stock.setId(existingStock.getId());
            // Ensure quantity is calculated based on units
            stock.setQuantity(stock.getUnits() * 450);
            bloodStockService.updateStock(stock);
            redirectAttributes.addFlashAttribute("message", 
                "Stock updated successfully for " + stock.getBloodGroup());
        } else {
            // If stock doesn't exist, create it
            stock.setQuantity(stock.getUnits() * 450);
            bloodStockService.createNewStock(stock);
            redirectAttributes.addFlashAttribute("message", 
                "New stock created successfully for " + stock.getBloodGroup());
        }
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error updating stock: " + e.getMessage());
    }
    return "redirect:/admin/manage-stock";
}
    @PostMapping("/add-stock")
public String addStock(@RequestParam String bloodGroup, 
                       @RequestParam int units,
                       @RequestParam(required = false) String storageLocation,
                       @RequestParam(required = false) String expiryDate,
                       RedirectAttributes redirectAttributes) {
    try {
        BloodStock existingStock = bloodStockService.getStockByBloodGroup(bloodGroup);
        
        if (existingStock != null) {
            // Update existing stock
            bloodStockService.addStock(bloodGroup, units);
            redirectAttributes.addFlashAttribute("message", 
                "Added " + units + " units to existing " + bloodGroup + " stock!");
        } else {
            // Create new stock
            BloodStock newStock = new BloodStock();
            newStock.setBloodGroup(bloodGroup);
            newStock.setUnits(units);
            newStock.setQuantity(units * 450);
            newStock.setStorageLocation(storageLocation != null ? storageLocation : "Main Storage");
            if (expiryDate != null && !expiryDate.isEmpty()) {
                newStock.setExpiryDate(LocalDateTime.parse(expiryDate + "T23:59:59"));
            }
            bloodStockService.createNewStock(newStock);
            redirectAttributes.addFlashAttribute("message", 
                "New stock created for " + bloodGroup + " with " + units + " units!");
        }
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error adding stock: " + e.getMessage());
    }
    return "redirect:/admin/manage-stock";
}

    @GetMapping("/requests")
    public String viewRequests(Model model) {
        model.addAttribute("requests", bloodRequestService.getAllRequests());
        return "admin/requests";
    }

    @PostMapping("/request/{id}/approve")
    public String approveRequest(@PathVariable Long id, Authentication authentication, 
                                 RedirectAttributes redirectAttributes) {
        try {
            String adminUsername = authentication.getName();
            User admin = userService.findByUsername(adminUsername);
            
            boolean approved = bloodRequestService.approveRequest(id, admin);
            if (approved) {
                redirectAttributes.addFlashAttribute("message", "Request #" + id + " approved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to approve request #" + id + ". Insufficient stock!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving request: " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @PostMapping("/request/{id}/reject")
    public String rejectRequest(@PathVariable Long id, Authentication authentication,
                                @RequestParam String reason, RedirectAttributes redirectAttributes) {
        try {
            String adminUsername = authentication.getName();
            User admin = userService.findByUsername(adminUsername);
            
            bloodRequestService.rejectRequest(id, admin, reason);
            redirectAttributes.addFlashAttribute("message", "Request #" + id + " rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting request: " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @GetMapping("/donations")
    public String viewDonations(Model model) {
        model.addAttribute("donations", donationService.getAllDonations());
        return "admin/donations";
    }

    @PostMapping("/donation/{id}/approve")
    public String approveDonation(@PathVariable Long id, 
                                  @RequestParam(required = false) String testResults,
                                  RedirectAttributes redirectAttributes) {
        try {
            Donation donation = donationService.getDonationById(id);
            if (donation != null) {
                if (testResults != null && !testResults.isEmpty()) {
                    donation.setTestResults(testResults);
                }
                donation.setTested(true);
                Donation approvedDonation = donationService.approveDonation(id);
                if (approvedDonation != null) {
                    redirectAttributes.addFlashAttribute("message", 
                        "Donation #" + id + " approved and added to stock!");
                } else {
                    redirectAttributes.addFlashAttribute("error", 
                        "Failed to approve donation #" + id + "!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Donation #" + id + " not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving donation: " + e.getMessage());
        }
        return "redirect:/admin/donations";
    }

    @PostMapping("/donation/{id}/reject")
    public String rejectDonation(@PathVariable Long id, 
                                 @RequestParam String reason,
                                 RedirectAttributes redirectAttributes) {
        try {
            Donation donation = donationService.rejectDonation(id, reason);
            if (donation != null) {
                redirectAttributes.addFlashAttribute("message", 
                    "Donation #" + id + " rejected successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Failed to reject donation #" + id + "!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting donation: " + e.getMessage());
        }
        return "redirect:/admin/donations";
    }

@PostMapping("/user/{id}/delete")
public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        User user = userService.getUserById(id);
        if (user != null) {
            String userName = user.getFullName();
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "User '" + userName + "' deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/admin/manage-users";
}
    @GetMapping("/stock/check/{bloodGroup}")
    @ResponseBody
    public String checkStockAvailability(@PathVariable String bloodGroup, 
                                         @RequestParam int requiredUnits) {
        boolean isAvailable = bloodStockService.isStockAvailable(bloodGroup, requiredUnits);
        BloodStock stock = bloodStockService.getStockByBloodGroup(bloodGroup);
        
        if (stock != null) {
            return String.format("{\"available\": %b, \"currentUnits\": %d, \"bloodGroup\": \"%s\"}", 
                               isAvailable, stock.getUnits(), bloodGroup);
        } else {
            return String.format("{\"available\": false, \"currentUnits\": 0, \"bloodGroup\": \"%s\"}", bloodGroup);
        }
    }

    @GetMapping("/statistics")
    @ResponseBody
    public String getStatistics() {
        long totalUsers = userService.getDonors().size();
        long totalDonations = donationService.getAllDonations().size();
        long totalRequests = bloodRequestService.getAllRequests().size();
        long pendingRequests = bloodRequestService.getPendingRequests().size();
        long totalBloodUnits = bloodStockService.getTotalUnits();
        long totalBloodVolume = bloodStockService.getTotalQuantity();
        
        return String.format(
            "{\"totalUsers\": %d, \"totalDonations\": %d, \"totalRequests\": %d, " +
            "\"pendingRequests\": %d, \"totalBloodUnits\": %d, \"totalBloodVolume\": %d}",
            totalUsers, totalDonations, totalRequests, pendingRequests, 
            totalBloodUnits, totalBloodVolume
        );
    }
}