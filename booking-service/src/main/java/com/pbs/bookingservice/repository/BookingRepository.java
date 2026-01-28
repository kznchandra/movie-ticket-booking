package com.pbs.bookingservice.repository;

import com.pbs.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.expiryTime <= :now")
    List<Booking> findExpiredBookings(LocalDateTime now);


    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId AND b.userId = :userId")
    Optional<Booking> findByBookingIdAndUserId(Long bookingId, Long userId);
}
