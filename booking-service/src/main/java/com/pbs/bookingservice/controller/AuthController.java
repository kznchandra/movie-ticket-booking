package com.pbs.bookingservice.controller;

import com.pbs.bookingservice.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppService appService;
    @GetMapping("/token")
    public String getToken() {
        return appService.getJwtToken();
    }
}
