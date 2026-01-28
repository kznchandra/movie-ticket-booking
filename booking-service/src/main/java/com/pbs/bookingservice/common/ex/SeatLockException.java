package com.pbs.bookingservice.common.ex;

public class SeatLockException extends RuntimeException{
    public SeatLockException(String message) {
        super(message);
    }
    public SeatLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
