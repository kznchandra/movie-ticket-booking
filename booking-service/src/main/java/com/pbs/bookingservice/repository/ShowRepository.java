package com.pbs.bookingservice.repository;

import com.pbs.bookingservice.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {


    @Query("SELECT s FROM Show s WHERE s.movieId = :movieId")
    Show findByMovieId(String  movieId);

    @Query("SELECT s FROM Show s WHERE s.theatreId = :theatreId")
    Show findByTheatreId(Long theatreId);

    @Query("SELECT s FROM Show s WHERE s.movieId = :movieId AND s.theatreId = :theatreId")
    Show findByMovieIdAndTheatreId(String movieId, Long theatreId);


}
