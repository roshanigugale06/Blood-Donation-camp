package com.bloodbank.repository;

import com.bloodbank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(String role);
    
    // Add this method to get only non-deleted users by role
    List<User> findByRoleAndDeletedFalse(String role);
    
    @Query("SELECT u FROM User u WHERE u.bloodGroup = :bloodGroup AND u.role = 'USER' AND u.deleted = false")
    List<User> findDonorsByBloodGroup(@Param("bloodGroup") String bloodGroup);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
}