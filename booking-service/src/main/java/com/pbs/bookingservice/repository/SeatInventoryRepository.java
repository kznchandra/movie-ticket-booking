package com.pbs.bookingservice.repository;

import com.pbs.bookingservice.entity.SeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    @Query("SELECT s FROM SeatInventory s WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers")
    public List<SeatInventory> findByShowIdAndSeatNumberIn(Long showId, List<String> seatNumbers);

    @Query("SELECT s FROM SeatInventory s WHERE s.id IN :list")
    List<SeatInventory> findAllById(List<Long> list);
}
