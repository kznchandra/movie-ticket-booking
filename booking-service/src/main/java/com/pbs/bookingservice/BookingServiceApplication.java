package com.pbs.bookingservice;

import com.pbs.bookingservice.common.req.BookingRequest;
import com.pbs.bookingservice.common.resp.BookingResponse;
import com.pbs.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class BookingServiceApplication {

    private final BookingService bookingService;
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
