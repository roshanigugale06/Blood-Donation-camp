package com.bloodbank.service;

import com.bloodbank.model.BloodStock;
import com.bloodbank.repository.BloodStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BloodStockService {

    @Autowired
    private BloodStockRepository bloodStockRepository;

    public List<BloodStock> getAllStocks() {
        return bloodStockRepository.findAll();
    }

    public BloodStock getStockByBloodGroup(String bloodGroup) {
        return bloodStockRepository.findByBloodGroup(bloodGroup).orElse(null);
    }

    public List<BloodStock> getLowStock(int threshold) {
        return bloodStockRepository.findLowStock(threshold);
    }

    public List<String> getAllBloodGroups() {
        return List.of("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");
    }

    @Transactional
    public BloodStock updateStock(BloodStock stock) {
        stock.setLastUpdated(LocalDateTime.now());
        return bloodStockRepository.save(stock);
    }

   @Transactional
public BloodStock addStock(String bloodGroup, int units) {
    Optional<BloodStock> existingStock = bloodStockRepository.findByBloodGroup(bloodGroup);
    if (existingStock.isPresent()) {
        BloodStock stock = existingStock.get();
        stock.setUnits(stock.getUnits() + units);
        stock.setQuantity(stock.getQuantity() + (units * 450)); // 1 unit = 450ml
        stock.setLastUpdated(LocalDateTime.now());
        return bloodStockRepository.save(stock);
    } else {
        // Create new stock if it doesn't exist
        BloodStock newStock = new BloodStock();
        newStock.setBloodGroup(bloodGroup);
        newStock.setUnits(units);
        newStock.setQuantity(units * 450);
        newStock.setStorageLocation("Main Storage");
        newStock.setLastUpdated(LocalDateTime.now());
        return bloodStockRepository.save(newStock);
    }
}

    @Transactional
    public boolean deductStock(String bloodGroup, int units) {
        Optional<BloodStock> existingStock = bloodStockRepository.findByBloodGroup(bloodGroup);
        if (existingStock.isPresent()) {
            BloodStock stock = existingStock.get();
            if (stock.getUnits() >= units) {
                stock.setUnits(stock.getUnits() - units);
                stock.setQuantity(stock.getQuantity() - (units * 450));
                stock.setLastUpdated(LocalDateTime.now());
                bloodStockRepository.save(stock);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public BloodStock createNewStock(BloodStock stock) {
        stock.setLastUpdated(LocalDateTime.now());
        stock.setQuantity(stock.getUnits() * 450);
        return bloodStockRepository.save(stock);
    }

    public boolean isStockAvailable(String bloodGroup, int requiredUnits) {
        BloodStock stock = getStockByBloodGroup(bloodGroup);
        return stock != null && stock.getUnits() >= requiredUnits;
    }

    public long getTotalUnits() {
        return bloodStockRepository.findAll().stream()
                .mapToLong(BloodStock::getUnits)
                .sum();
    }

    public long getTotalQuantity() {
        return bloodStockRepository.findAll().stream()
                .mapToLong(BloodStock::getQuantity)
                .sum();
    }
}