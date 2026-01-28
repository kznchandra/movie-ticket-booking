package com.pbs.bookingservice.controller;

import com.pbs.bookingservice.common.req.BookingRequest;
import com.pbs.bookingservice.common.resp.BookingResponse;
import com.pbs.bookingservice.common.security.UserPrincipal;
import com.pbs.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/booking")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        request.setUserId(user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.initiateBooking(request));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal user) {

        return bookingService.getBookingById(bookingId, user.getId());
    }

}
