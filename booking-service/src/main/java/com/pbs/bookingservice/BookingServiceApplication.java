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
public class BookingServiceApplication implements CommandLineRunner {

    private final BookingService bookingService;
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        BookingResponse bookingResponse = bookingService.initiateBooking(
                BookingRequest.builder()
                        .userId(1L)
                        .showId(1L)
                        .showId(1L)
                        .offerCode("THIRD_50")
                        .seatNumbers(List.of("A1","A2"))
                        .seatCount(3)
                        .build()
        );
        System.err.println(bookingResponse.getBooking().getBookingReference());
    }
}
