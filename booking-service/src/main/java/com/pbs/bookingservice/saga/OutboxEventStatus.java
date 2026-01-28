package com.pbs.bookingservice.saga;

public enum OutboxEventStatus {
    PENDING, PROCESSED, COMPLETED, FAILED
}
