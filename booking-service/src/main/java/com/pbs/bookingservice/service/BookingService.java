package com.pbs.bookingservice.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.pbs.bookingservice.common.ex.BusinessException;
import com.pbs.bookingservice.common.ex.SeatUnavailableException;
import com.pbs.bookingservice.common.req.BookingRequest;
import com.pbs.bookingservice.common.resp.BookingResponse;
import com.pbs.bookingservice.common.resp.PricingDetails;
import com.pbs.bookingservice.entity.Booking;
import com.pbs.bookingservice.entity.BookingSeat;
import com.pbs.bookingservice.entity.SeatInventory;
import com.pbs.bookingservice.entity.enums.BookingStatus;
import com.pbs.bookingservice.entity.enums.SeatStatus;
import com.pbs.bookingservice.kafka.KafkaEventPublisher;
import com.pbs.bookingservice.repository.BookingRepository;
import com.pbs.bookingservice.repository.SeatInventoryRepository;
import com.pbs.bookingservice.saga.OutboxService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final RedisTemplate redisTemplate;
    private final KafkaEventPublisher kafkaPublisher;
    private final PricingService pricingService;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final OutboxService outboxService;

    public BookingResponse initiateBooking(BookingRequest request) {

        // Acquire seat locks
        List<SeatInventory> seats = acquireSeatsWithLock(
                request.getShowId(),
                request.getSeatNumbers(),
                request.getUserId()
        );

        // Pricing
        PricingDetails pricing =
                pricingService.calculatePricing(seats, request.getOfferCode());

        // Create booking
        Booking booking = createBookingRecord(request, seats, pricing);

        // Write Outbox Event (same transaction)
        outboxService.saveBookingInitiatedEvent(booking);

        // Cache booking temporarily
        redisTemplate.opsForValue().set(
                "BOOKING:" + booking.getBookingReference(),
                booking,
                10,
                TimeUnit.MINUTES
        );

        // Publish Kafka event
       // kafkaPublisher.bookingInitiated(booking);
        return BookingResponse.from(booking, seats);
    }

    private Booking createBookingRecord(
            BookingRequest request,
            List<SeatInventory> seats,
            PricingDetails pricing) {

        Booking booking = new Booking();
        booking.setBookingReference(UlidCreator.getUlid().toString());
        booking.setUserId(request.getUserId());
        booking.setShowId(request.getShowId());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setBaseAmount(pricing.getBaseAmount());
        booking.setDiscountAmount(pricing.getDiscountAmount());
        booking.setFinalAmount(pricing.getFinalAmount());
        booking.setBookingTime(LocalDateTime.now());
        booking.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        Booking saved = bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> {
                    BookingSeat bs = new BookingSeat();
                    bs.setBooking(saved);
                    bs.setSeatInventoryId(seat.getId());
                    bs.setPricePaid(seat.getPrice());
                    return bs;
                }).toList();

        saved.setBookingSeats(bookingSeats);
        return bookingRepository.save(saved);
    }


    private List<SeatInventory> acquireSeatsWithLock(
            Long showId,
            List<String> seatNumbers,
            Long userId) {

        List<SeatInventory> seats =
                seatInventoryRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            throw new BusinessException("Some seats not found");
        }

        for (SeatInventory seat : seats) {
            if (!SeatStatus.AVAILABLE.equals(seat.getSeatStatus())) {
                throw new SeatUnavailableException(seat.getSeatNumber());
            }
            seatLockService.lockSeat(seat.getId(), userId);
        }
        return seats;
    }

    @Transactional
    public void confirmBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!BookingStatus.PENDING_PAYMENT.equals(booking.getStatus())) {
            throw new BusinessException("Invalid booking state");
        }

        booking.setStatus(BookingStatus.CONFIRMED_BOOKING);
        bookingRepository.save(booking);

        outboxService.saveBookingConfirmedEvent(booking);

        List<SeatInventory> seats =
                seatInventoryRepository.findAllById(
                        booking.getBookingSeats()
                                .stream()
                                .map(BookingSeat::getSeatInventoryId)
                                .toList()
                );

        seats.forEach(seat -> seat.setSeatStatus(SeatStatus.BOOKED));
        seatInventoryRepository.saveAll(seats);

        seats.forEach(seat -> seatLockService.unlockSeat(seat.getId()));

        kafkaPublisher.bookingConfirmed(booking);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireBookings() {

        List<Booking> expired =
                bookingRepository.findExpiredBookings(LocalDateTime.now());

        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.EXPIRED_BOOKING);
            bookingRepository.save(booking);

           outboxService.saveBookingExpiredEvent(booking);

            List<SeatInventory> seats =
                    seatInventoryRepository.findAllById(
                            booking.getBookingSeats()
                                    .stream()
                                    .map(BookingSeat::getSeatInventoryId)
                                    .toList()
                    );

            seats.forEach(seat -> {
                seat.setSeatStatus(SeatStatus.AVAILABLE);
                seatLockService.unlockSeat(seat.getId());
            });

            seatInventoryRepository.saveAll(seats);
            kafkaPublisher.bookingExpired(booking);
        }
    }

    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("Booking does not belong to this user");
        }

        List<SeatInventory> seats =
                seatInventoryRepository.findAllById(
                        booking.getBookingSeats()
                                .stream()
                                .map(BookingSeat::getSeatInventoryId)
                                .toList()
                );

        return BookingResponse.from(booking, seats);
    }
}
