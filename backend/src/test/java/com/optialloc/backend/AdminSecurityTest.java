package com.optialloc.backend;

import com.optialloc.backend.controller.AdminController;
import com.optialloc.backend.controller.ResourceController;
import com.optialloc.backend.dto.AdminStatsDTO;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.status.ResourceStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminSecurityTest {

    @Autowired
    private ResourceController resourceController;

    @Autowired
    private AdminController adminController;

    @Autowired
    private ResourceRepository resourceRepository;

    private Resource testResource;

    @BeforeEach
    void setUp() {
        testResource = new Resource(
                "Test Lab 101",
                "LAB",
                25,
                "Building T",
                ResourceStatus.AVAILABLE
        );
        testResource = resourceRepository.save(testResource);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(String username, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testAdminCanCreateResource() {
        setAuth("admin@optialloc.com", "ADMIN");
        Resource newResource = new Resource(
                "Admin Hall",
                "CLASSROOM",
                100,
                "Main Building",
                ResourceStatus.AVAILABLE
        );

        Resource created = resourceController.createResource(newResource);
        assertNotNull(created.getId());
        assertEquals("Admin Hall", created.getName());
    }

    @Test
    void testAdminCanUpdateResource() {
        setAuth("admin@optialloc.com", "ADMIN");
        testResource.setName("Updated Lab 101");
        Resource updated = resourceController.updateResource(testResource.getId(), testResource);
        assertEquals("Updated Lab 101", updated.getName());
    }

    @Test
    void testAdminCanDeleteResource() {
        setAuth("admin@optialloc.com", "ADMIN");
        resourceController.deleteResource(testResource.getId());
        assertTrue(resourceRepository.findById(testResource.getId()).isEmpty());
    }

    @Test
    void testUserCanRetrieveResources() {
        setAuth("user@optialloc.com", "USER");
        List<Resource> resources = resourceController.getAllResources();
        assertNotNull(resources);
    }

    @Test
    void testUserCannotCreateResource() {
        setAuth("user@optialloc.com", "USER");
        Resource newResource = new Resource(
                "Unauthorized Hall",
                "CLASSROOM",
                50,
                "Building X",
                ResourceStatus.AVAILABLE
        );

        assertThrows(AccessDeniedException.class, () -> resourceController.createResource(newResource));
    }

    @Test
    void testUserCannotUpdateResource() {
        setAuth("user@optialloc.com", "USER");
        testResource.setName("Hacked Lab Name");
        assertThrows(AccessDeniedException.class, () -> resourceController.updateResource(testResource.getId(), testResource));
    }

    @Test
    void testUserCannotDeleteResource() {
        setAuth("user@optialloc.com", "USER");
        assertThrows(AccessDeniedException.class, () -> resourceController.deleteResource(testResource.getId()));
    }

    @Test
    void testUserCannotAccessAdminRequestData() {
        setAuth("user@optialloc.com", "USER");
        assertThrows(AccessDeniedException.class, () -> adminController.getAllRequests());
    }

    @Test
    void testUserCannotAccessAdminStatistics() {
        setAuth("user@optialloc.com", "USER");
        assertThrows(AccessDeniedException.class, () -> adminController.getAdminStatistics());
    }

    @Test
    void testAdminCanAccessAdminStatistics() {
        setAuth("admin@optialloc.com", "ADMIN");
        AdminStatsDTO stats = adminController.getAdminStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalResources() >= 1);
    }
}
