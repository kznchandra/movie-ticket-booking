package com.pbs.bookingservice.repository;

import com.pbs.bookingservice.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    BookingSeat findByBookingIdAndSeatNumber(Long bookingId, Integer seatNumber);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    BookingSeat findByBookingId(Long bookingId);
}
