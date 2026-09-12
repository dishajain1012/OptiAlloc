package com.optialloc.backend.controller;

import com.optialloc.backend.dto.BookingDTO;
import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Confirmed resource allocation booking endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create booking record", description = "Creates a booking record directly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token")
    })
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingService.createBooking(booking);
    }

    @GetMapping
    @Operation(summary = "Get user bookings", description = "Retrieves all confirmed booking allocations for the authenticated user (or all bookings for ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of BookingDTO objects returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token")
    })
    public List<BookingDTO> getAllBookings(Principal principal) {
        return bookingService.getBookingsForCurrentUser(principal);
    }
}