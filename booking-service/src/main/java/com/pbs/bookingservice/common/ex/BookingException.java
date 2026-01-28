package com.pbs.bookingservice.common.ex;

public class BookingException extends RuntimeException{
    public BookingException(String message) {
        super(message);
    }
    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
