package com.optialloc.backend;

import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.scheduler.SchedulingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ConcurrencyTest {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Unique resource type for every test execution
    private String concurrencyResourceType;

    @BeforeEach
    void setup() {

        // Create a unique resource type for this test run
        concurrencyResourceType =
                "CONCURRENCY_TEST_" + UUID.randomUUID();

        // Create exactly ONE resource for this test
        Resource resource = new Resource(
                "CONCURRENCY-ROOM",
                concurrencyResourceType,
                100,
                "TEST-BLOCK",
                "AVAILABLE"
        );

        resourceRepository.save(resource);
    }

    @Test
    void shouldPreventDoubleAllocationUnderConcurrency()
            throws Exception {

        LocalDateTime start =
                LocalDateTime.of(2026, 9, 25, 9, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 9, 25, 11, 0);

        // ==============================
        // CREATE REQUEST 1
        // ==============================

        Request request1 = new Request(
                start,
                end,
                90,
                10,
                concurrencyResourceType,
                "PENDING"
        );

        // ==============================
        // CREATE REQUEST 2
        // ==============================

        Request request2 = new Request(
                start,
                end,
                90,
                9,
                concurrencyResourceType,
                "PENDING"
        );

        request1 = requestRepository.save(request1);
        request2 = requestRepository.save(request2);

        Long requestId1 = request1.getId();
        Long requestId2 = request2.getId();

        System.out.println();
        System.out.println("====================================");
        System.out.println("CONCURRENCY TEST");
        System.out.println("Resource Type = " + concurrencyResourceType);
        System.out.println("Request 1 ID = " + requestId1);
        System.out.println("Request 2 ID = " + requestId2);
        System.out.println("====================================");

        // ==============================
        // CREATE TWO THREADS
        // ==============================

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startSignal =
                new CountDownLatch(1);

        // ==============================
        // TASK 1
        // ==============================

        Callable<String> task1 = () -> {

            // Wait until both threads are ready
            startSignal.await();

            try {

                Booking booking =
                        schedulingService
                                .scheduleRequest(requestId1);

                return "Request " + requestId1
                        + " SUCCESS -> Booking "
                        + booking.getId()
                        + " -> Resource "
                        + booking.getResource().getId();

            } catch (Exception e) {

                return "Request " + requestId1
                        + " FAILED -> "
                        + e.getClass().getSimpleName()
                        + " -> "
                        + e.getMessage();
            }
        };

        // ==============================
        // TASK 2
        // ==============================

        Callable<String> task2 = () -> {

            // Wait until both threads are ready
            startSignal.await();

            try {

                Booking booking =
                        schedulingService
                                .scheduleRequest(requestId2);

                return "Request " + requestId2
                        + " SUCCESS -> Booking "
                        + booking.getId()
                        + " -> Resource "
                        + booking.getResource().getId();

            } catch (Exception e) {

                return "Request " + requestId2
                        + " FAILED -> "
                        + e.getClass().getSimpleName()
                        + " -> "
                        + e.getMessage();
            }
        };

        // ==============================
        // START BOTH TASKS
        // ==============================

        Future<String> future1 =
                executor.submit(task1);

        Future<String> future2 =
                executor.submit(task2);

        // Release both threads at almost the same time
        startSignal.countDown();

        // Wait for both threads to finish
        String result1 = future1.get();
        String result2 = future2.get();

        executor.shutdown();

        // ==============================
        // PRINT RESULTS
        // ==============================

        System.out.println();
        System.out.println("========== RESULTS ==========");
        System.out.println(result1);
        System.out.println(result2);
        System.out.println("=============================");

        // ==============================
        // COUNT ONLY BOOKINGS CREATED
        // BY THESE TWO REQUESTS
        // ==============================

        long bookingCount =
                bookingRepository.findAll()
                        .stream()
                        .filter(booking ->
                                booking.getRequest()
                                        .getId()
                                        .equals(requestId1)
                                ||
                                booking.getRequest()
                                        .getId()
                                        .equals(requestId2)
                        )
                        .count();

        System.out.println(
                "Bookings created by concurrent requests = "
                        + bookingCount
        );

        // ==============================
        // ASSERTION
        // ==============================

        assertEquals(
                1,
                bookingCount,
                "CONCURRENCY FAILURE: More than one booking was created!"
        );
    }
}