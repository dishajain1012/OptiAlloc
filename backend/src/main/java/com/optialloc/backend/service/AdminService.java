package com.optialloc.backend.service;

import com.optialloc.backend.dto.AdminStatsDTO;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.status.ResourceStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;

    public AdminService(ResourceRepository resourceRepository, RequestRepository requestRepository) {
        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
    }

    public AdminStatsDTO getAdminStatistics() {
        long totalResources = resourceRepository.count();
        long availableResources = resourceRepository.countByStatus(ResourceStatus.AVAILABLE);
        long maintenanceResources = resourceRepository.countByStatus(ResourceStatus.MAINTENANCE);
        long inactiveResources = resourceRepository.countByStatus(ResourceStatus.INACTIVE);

        long totalRequests = requestRepository.count();
        long allocatedRequests = requestRepository.countByStatus("ALLOCATED");
        long pendingRequests = requestRepository.countByStatus("PENDING");
        long conflictRequests = requestRepository.countByStatus("CONFLICT");

        return new AdminStatsDTO(
                totalResources,
                availableResources,
                maintenanceResources,
                inactiveResources,
                totalRequests,
                allocatedRequests,
                pendingRequests,
                conflictRequests
        );
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }
}
