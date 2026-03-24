package com.bloodbank.repository;

import com.bloodbank.model.Donation;
import com.bloodbank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    
    List<Donation> findByUser(User user);
    
    List<Donation> findByStatus(String status);
    
    long countByUser(User user);  // Add this method
    
    @Query("SELECT d FROM Donation d WHERE d.donationDate BETWEEN :start AND :end")
    List<Donation> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(d.quantity) FROM Donation d WHERE d.status = 'APPROVED'")
    Long getTotalBloodCollected();
}