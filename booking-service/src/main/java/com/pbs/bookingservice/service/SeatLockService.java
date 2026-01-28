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
    private final RedisTemplate redisTemplate;
    public void lockSeat(Long id, Long userId) {
        String lockKey = "seat:lock:" + id;
        redisTemplate.opsForValue().set(lockKey, userId.toString(), LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    public void unlockSeat(Long id) {
        String lockKey = "seat:lock:" + id;
        redisTemplate.delete(lockKey);
    }
}
