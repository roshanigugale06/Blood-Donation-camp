package com.bloodbank.service;

import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.User;
import com.bloodbank.repository.BloodRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BloodRequestService {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private BloodStockService bloodStockService;

    public List<BloodRequest> getAllRequests() {
        return bloodRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "requestDate"));
    }

    public List<BloodRequest> getUserRequests(User user) {
        return bloodRequestRepository.findByUser(user);
    }

    public List<BloodRequest> getPendingRequests() {
        return bloodRequestRepository.findByStatus("PENDING");
    }

    public List<BloodRequest> getRecentRequests(int limit) {
        return bloodRequestRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "requestDate"))
        ).getContent();
    }

    public BloodRequest getRequestById(Long id) {
        return bloodRequestRepository.findById(id).orElse(null);
    }

    @Transactional
    public BloodRequest createRequest(BloodRequest request) {
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("PENDING");
        return bloodRequestRepository.save(request);
    }

    @Transactional
    public boolean approveRequest(Long requestId, User admin) {
        BloodRequest request = getRequestById(requestId);
        if (request != null && "PENDING".equals(request.getStatus())) {
            if (bloodStockService.isStockAvailable(request.getBloodGroup(), request.getUnits())) {
                boolean deducted = bloodStockService.deductStock(request.getBloodGroup(), request.getUnits());
                if (deducted) {
                    request.setStatus("APPROVED");
                    request.setProcessedBy(admin);
                    request.setProcessedDate(LocalDateTime.now());
                    bloodRequestRepository.save(request);
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional
    public void rejectRequest(Long requestId, User admin, String reason) {
        BloodRequest request = getRequestById(requestId);
        if (request != null) {
            request.setStatus("REJECTED");
            request.setProcessedBy(admin);
            request.setProcessedDate(LocalDateTime.now());
            request.setReason(reason);
            bloodRequestRepository.save(request);
        }
    }

    @Transactional
    public void completeRequest(Long requestId) {
        BloodRequest request = getRequestById(requestId);
        if (request != null && "APPROVED".equals(request.getStatus())) {
            request.setStatus("COMPLETED");
            bloodRequestRepository.save(request);
        }
    }

    public long getPendingRequestsCount() {
        return bloodRequestRepository.countPendingRequests();
    }

    public List<BloodRequest> getRequestsByBloodGroup(String bloodGroup) {
        return bloodRequestRepository.findPendingRequestsByBloodGroup(bloodGroup);
    }

    @Transactional
    public void cancelRequest(Long requestId, User user) {
        BloodRequest request = getRequestById(requestId);
        if (request != null && request.getUser().getId().equals(user.getId())) {
            if ("PENDING".equals(request.getStatus())) {
                request.setStatus("CANCELLED");
                bloodRequestRepository.save(request);
            }
        }
    }
}