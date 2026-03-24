package com.bloodbank.repository;

import com.bloodbank.model.BloodStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodStockRepository extends JpaRepository<BloodStock, Long> {
    
    Optional<BloodStock> findByBloodGroup(String bloodGroup);
    
    @Query("SELECT b FROM BloodStock b WHERE b.units < :threshold")
    List<BloodStock> findLowStock(@Param("threshold") int threshold);
    
    @Modifying
    @Query("UPDATE BloodStock b SET b.units = b.units - :units WHERE b.bloodGroup = :bloodGroup")
    int deductStock(@Param("bloodGroup") String bloodGroup, @Param("units") int units);
    
    @Modifying
    @Query("UPDATE BloodStock b SET b.units = b.units + :units WHERE b.bloodGroup = :bloodGroup")
    int addStock(@Param("bloodGroup") String bloodGroup, @Param("units") int units);
}