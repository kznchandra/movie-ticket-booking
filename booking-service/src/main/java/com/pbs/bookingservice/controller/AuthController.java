package com.pbs.bookingservice.controller;

import com.pbs.bookingservice.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "API for authentication operations")
public class AuthController {
    private final AppService appService;

    @Operation(summary = "Get JWT token", description = "Returns JWT token for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token retrieved successfully"),
    })
    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        return ResponseEntity.ok(appService.getJwtToken());
    }
}
