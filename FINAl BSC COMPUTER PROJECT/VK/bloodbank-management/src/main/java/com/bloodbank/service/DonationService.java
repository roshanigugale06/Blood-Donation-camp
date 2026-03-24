package com.bloodbank.service;

import com.bloodbank.model.Donation;
import com.bloodbank.model.User;
import com.bloodbank.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private BloodStockService bloodStockService;

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public List<Donation> getUserDonations(User user) {
        return donationRepository.findByUser(user);
    }

    public List<Donation> getPendingDonations() {
        return donationRepository.findByStatus("PENDING");
    }

    public Donation getDonationById(Long id) {
        return donationRepository.findById(id).orElse(null);
    }

    @Transactional
    public Donation saveDonation(Donation donation) {
        donation.setDonationDate(LocalDateTime.now());
        donation.setNextEligibleDate(LocalDateTime.now().plusMonths(3));
        donation.setStatus("PENDING");
        donation.setTested(false);
        return donationRepository.save(donation);
    }

  @Transactional
public Donation approveDonation(Long donationId) {
    Donation donation = getDonationById(donationId);
    if (donation != null && "PENDING".equals(donation.getStatus())) {
        donation.setStatus("APPROVED");
        donation.setTested(true);
        if (donation.getTestResults() == null || donation.getTestResults().isEmpty()) {
            donation.setTestResults("All tests passed. Blood is healthy.");
        }
        
        // Add to blood stock - Convert ml to units (1 unit ≈ 450ml)
        int units = donation.getQuantity() / 450;
        if (units > 0) {
            bloodStockService.addStock(donation.getBloodGroup(), units);
            System.out.println("Added " + units + " units of " + donation.getBloodGroup() + " to stock from donation #" + donationId);
        }
        
        return donationRepository.save(donation);
    }
    return null;
}
    @Transactional
    public Donation rejectDonation(Long donationId, String reason) {
        Donation donation = getDonationById(donationId);
        if (donation != null && "PENDING".equals(donation.getStatus())) {
            donation.setStatus("REJECTED");
            donation.setTested(true);
            donation.setTestResults(reason);
            return donationRepository.save(donation);
        }
        return null;
    }

    public long getTotalDonations() {
        return donationRepository.count();
    }

    public Long getTotalBloodCollected() {
        Long total = donationRepository.getTotalBloodCollected();
        return total != null ? total : 0L;
    }

    public List<Donation> getDonationsInDateRange(LocalDateTime start, LocalDateTime end) {
        return donationRepository.findByDateRange(start, end);
    }

    public boolean isEligibleToDonate(User user) {
        List<Donation> userDonations = getUserDonations(user);
        if (userDonations.isEmpty()) {
            return true;
        }
        
        Donation lastDonation = userDonations.stream()
                .filter(d -> "APPROVED".equals(d.getStatus()))
                .max((d1, d2) -> d1.getDonationDate().compareTo(d2.getDonationDate()))
                .orElse(null);
        
        if (lastDonation != null) {
            LocalDateTime nextEligible = lastDonation.getDonationDate().plusMonths(3);
            return LocalDateTime.now().isAfter(nextEligible);
        }
        
        return true;
    }

    @Transactional
    public void deleteDonation(Long id) {
        donationRepository.deleteById(id);
    }
}