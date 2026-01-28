package com.pbs.bookingservice.controller;

import com.pbs.bookingservice.common.req.BookingRequest;
import com.pbs.bookingservice.common.resp.BookingResponse;
import com.pbs.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/booking")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Tag(name = "Booking API", description = "API for booking operations")
public class BookingController {
    
    private final BookingService bookingService;

    @Operation(
            summary = "Create a new booking",
            description = "Initiates a new booking for the authenticated user with the provided booking details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal Authentication user) {
        request.setUserId(1L); // dummy user id for testing
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.initiateBooking(request));
    }

    @Operation(
            summary = "Get booking details",
            description = "Retrieves booking details by booking ID for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/{bookingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Authentication user) {

        return ResponseEntity.ok(bookingService.getBookingById(bookingId, 1L)); // dummy user id for testing
    }

    @Operation(
            summary = "Confirm booking",
            description = "Confirms an existing booking by booking ID for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "/{bookingId}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> confirmBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Authentication user) {
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get all bookings for user",
            description = "Retrieves all bookings for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "{userId}/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<java.util.List<BookingResponse>> getAllBookingsForUser(@PathVariable Long userId,
            @AuthenticationPrincipal Authentication user) {
        return ResponseEntity.ok(bookingService.getAllBookingsByUserId(1L)); // dummy user id for testing
    }

}
