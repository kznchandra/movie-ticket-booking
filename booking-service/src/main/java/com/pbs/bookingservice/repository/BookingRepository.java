package com.pbs.bookingservice.repository;

import com.pbs.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.expiryTime <= :now")
    List<Booking> findExpiredBookings(LocalDateTime now);
}
