package com.bloodbank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
public class Donation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String bloodGroup;
    private Integer quantity;
    private LocalDateTime donationDate;
    private LocalDateTime nextEligibleDate;
    private String hospitalName;
    private String donationType;
    private String healthStatus;
    private Boolean tested;
    private String testResults;
    private String status;
    
    private String notes;
    private LocalDateTime createdDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        tested = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getBloodGroup() {
        return bloodGroup;
    }
    
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public LocalDateTime getDonationDate() {
        return donationDate;
    }
    
    public void setDonationDate(LocalDateTime donationDate) {
        this.donationDate = donationDate;
    }
    
    public LocalDateTime getNextEligibleDate() {
        return nextEligibleDate;
    }
    
    public void setNextEligibleDate(LocalDateTime nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }
    
    public String getHospitalName() {
        return hospitalName;
    }
    
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
    
    public String getDonationType() {
        return donationType;
    }
    
    public void setDonationType(String donationType) {
        this.donationType = donationType;
    }
    
    public String getHealthStatus() {
        return healthStatus;
    }
    
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
    
    public Boolean getTested() {
        return tested;
    }
    
    public void setTested(Boolean tested) {
        this.tested = tested;
    }
    
    public String getTestResults() {
        return testResults;
    }
    
    public void setTestResults(String testResults) {
        this.testResults = testResults;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}