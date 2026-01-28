package com.pbs.bookingservice.common.ex;

public class BookingValidationException extends RuntimeException {
    public BookingValidationException(String message) {
        super(message);
    }
    public BookingValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
