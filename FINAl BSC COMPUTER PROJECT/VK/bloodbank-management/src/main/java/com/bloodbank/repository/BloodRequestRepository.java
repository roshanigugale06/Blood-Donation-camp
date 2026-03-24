package com.bloodbank.repository;

import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    
    List<BloodRequest> findByUser(User user);
    
    List<BloodRequest> findByStatus(String status);
    
    long countByUser(User user);  // Add this method
    
    @Query("SELECT r FROM BloodRequest r WHERE r.bloodGroup = :bloodGroup AND r.status = 'PENDING'")
    List<BloodRequest> findPendingRequestsByBloodGroup(@Param("bloodGroup") String bloodGroup);
    
    @Query("SELECT COUNT(r) FROM BloodRequest r WHERE r.status = 'PENDING'")
    long countPendingRequests();
}