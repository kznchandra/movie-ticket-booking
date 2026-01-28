package com.pbs.bookingservice.service;

import com.pbs.bookingservice.common.ex.SeatLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private static final long LOCK_TIMEOUT_MINUTES = 10;
    private static final String SEAT_LOCK_KEY_PATTERN = "seat:lock:%d";

    private final RedisTemplate<String, String> redisTemplate;

    public boolean lockSeat(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("Seat ID and User ID cannot be null");
        }
        log.debug("Attempting to lock seat with ID: {} for user ID: {}", id, userId);
        try {
            String lockKey = generateLockKey(id);
            redisTemplate.opsForValue().set(lockKey, userId.toString(), LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            log.info("Successfully locked seat ID: {} for user ID: {} with timeout: {} minutes",
                    id, userId, LOCK_TIMEOUT_MINUTES);
            return true;
        } catch (Exception ex) {
            log.error("Failed to lock seat ID: {} for user ID: {}. Error: {}", id, userId, ex.getMessage(), ex);
            throw new SeatLockException(ex.getMessage(), ex);
        }
    }

    public void unlockSeat(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Seat ID cannot be null");
        }
        log.debug("Attempting to unlock seat with ID: {}", id);
        try {
            String lockKey = generateLockKey(id);
            redisTemplate.delete(lockKey);
            log.info("Successfully unlocked seat ID: {}", id);
        } catch (Exception ex) {
            log.error("Failed to unlock seat ID: {}. Error: {}", id, ex.getMessage(), ex);
            throw new SeatLockException(ex.getMessage(), ex);
        }
    }

    private String generateLockKey(Long seatId) {
        return String.format(SEAT_LOCK_KEY_PATTERN, seatId);
    }
}
