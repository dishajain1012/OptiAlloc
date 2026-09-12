package com.optialloc.backend.service;

import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.status.ResourceStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @CacheEvict(value = "resources", allEntries = true)
    public Resource createResource(Resource resource) {
        validateResource(resource);
        return resourceRepository.save(resource);
    }

    @Cacheable("resources")
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    @Cacheable(value = "resource", key = "#id")
    public Optional<Resource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    @CacheEvict(
            value = {"resources", "resource"},
            allEntries = true
    )
    public Resource updateResource(Long id, Resource updatedResource) {

        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Resource not found"));

        validateResource(updatedResource);

        existingResource.setName(updatedResource.getName());
        existingResource.setType(updatedResource.getType());
        existingResource.setCapacity(updatedResource.getCapacity());
        existingResource.setLocation(updatedResource.getLocation());
        existingResource.setStatus(updatedResource.getStatus());

        return resourceRepository.save(existingResource);
    }

    @CacheEvict(
            value = {"resources", "resource"},
            allEntries = true
    )
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    private void validateResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is required");
        }

        if (resource.getName() == null || resource.getName().isBlank()) {
            throw new IllegalArgumentException("Resource name is required");
        }

        if (resource.getType() == null || resource.getType().isBlank()) {
            throw new IllegalArgumentException("Resource type is required");
        }

        if (resource.getCapacity() == null || resource.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }

        String normalizedStatus = resource.getStatus() == null
                ? ResourceStatus.AVAILABLE
                : resource.getStatus().trim().toUpperCase();

        if (!ResourceStatus.isValid(normalizedStatus)) {
            throw new IllegalArgumentException("Resource status must be AVAILABLE, MAINTENANCE, or INACTIVE");
        }

        resource.setStatus(normalizedStatus);
    }
}