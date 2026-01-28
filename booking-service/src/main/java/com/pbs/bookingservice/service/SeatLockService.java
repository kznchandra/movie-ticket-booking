package com.pbs.bookingservice.service;

import com.pbs.bookingservice.repository.BookingRepository;
import com.pbs.bookingservice.repository.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatLockService {
    
    private static final long LOCK_TIMEOUT_MINUTES = 10;
    private static final String SEAT_LOCK_KEY_PATTERN = "seat:lock:%d";

    private final RedisTemplate<String, String> redisTemplate;

    public boolean lockSeat(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("Seat ID and User ID cannot be null");
        }
        String lockKey = generateLockKey(id);
        redisTemplate.opsForValue().set(lockKey, userId.toString(), LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        return true;
    }

    public boolean unlockSeat(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Seat ID cannot be null");
        }
        String lockKey = generateLockKey(id);
        Boolean result = redisTemplate.delete(lockKey);
        return Boolean.TRUE.equals(result);
    }

    private String generateLockKey(Long seatId) {
        return String.format(SEAT_LOCK_KEY_PATTERN, seatId);
    }
}
