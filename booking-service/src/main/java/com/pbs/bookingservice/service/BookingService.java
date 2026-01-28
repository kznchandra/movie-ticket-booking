package com.pbs.bookingservice.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.pbs.bookingservice.common.ex.BookingNotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final int BOOKING_EXPIRY_MINUTES = 10;
    private static final long BOOKING_EXPIRY_CHECK_DELAY_MS = 60000;

    private final RedisTemplate<String,String> redisTemplate;
    private final KafkaEventPublisher kafkaPublisher;
    private final PricingService pricingService;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final OutboxService outboxService;

    public BookingResponse initiateBooking(BookingRequest request) {
        validateBookingRequest(request);

        // Acquire seat locks
        List<SeatInventory> seats = validateAndLockSeats(
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
        cacheBooking(booking);

        // Publish Kafka event
        publishBookingInitiatedEventAsync(booking);
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
        booking.setExpiryTime(LocalDateTime.now().plusMinutes(BOOKING_EXPIRY_MINUTES));

        Booking saved = bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> createBookingSeat(saved, seat))
                .toList();

        saved.setBookingSeats(bookingSeats);
        return bookingRepository.save(saved);
    }


    private List<SeatInventory> validateAndLockSeats(
            Long showId,
            List<String> seatNumbers,
            Long userId) {

        List<SeatInventory> seats =
                seatInventoryRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            throw new BusinessException("Some seats not found");
        }

        for (SeatInventory seat : seats) {
            validateSeatAvailability(seat);
            seatLockService.lockSeat(seat.getId(), userId);
        }
        return seats;
    }

    @Transactional
    public void confirmBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        validateBookingForConfirmation(booking);

        booking.setStatus(BookingStatus.CONFIRMED_BOOKING);
        bookingRepository.save(booking);

        outboxService.saveBookingConfirmedEvent(booking);

        List<SeatInventory> seats = getSeatsForBooking(booking);

        updateSeatsStatus(seats, SeatStatus.BOOKED);
        unlockSeats(seats);

        seatInventoryRepository.saveAll(seats);

        publishBookingConfirmedEventAsync(booking);
    }

    @Scheduled(fixedDelay = BOOKING_EXPIRY_CHECK_DELAY_MS)
    @Transactional
    public void expireBookings() {

        List<Booking> expiredBookings =
                bookingRepository.findExpiredBookings(LocalDateTime.now());

        if (expiredBookings == null || expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {
            expireBooking(booking);
        }
    }

    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        validateBookingOwnership(booking, userId);

        List<SeatInventory> seats = getSeatsForBooking(booking);

        return BookingResponse.from(booking, seats);
    }

    private void validateBookingRequest(BookingRequest request) {
        if (request.getShowId() == null) {
            throw new BusinessException("Show ID is required");
        }
        if (request.getUserId() == null) {
            throw new BusinessException("User ID is required");
        }
        if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            throw new BusinessException("Seat numbers are required");
        }
    }

    private void validateSeatAvailability(SeatInventory seat) {
        if (!SeatStatus.AVAILABLE.equals(seat.getSeatStatus())) {
            throw new SeatUnavailableException(seat.getSeatNumber());
        }
    }

    private void validateBookingForConfirmation(Booking booking) {
        if (!BookingStatus.PENDING_PAYMENT.equals(booking.getStatus())) {
            throw new BusinessException("Invalid booking state");
        }
        if (booking.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Booking has expired");
        }
    }

    private void validateBookingOwnership(Booking booking, Long userId) {
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("Booking does not belong to this user");
        }
    }

    private BookingSeat createBookingSeat(Booking booking, SeatInventory seat) {
        BookingSeat bookingSeat = new BookingSeat();
        bookingSeat.setBooking(booking);
        bookingSeat.setSeatInventoryId(seat.getId());
        bookingSeat.setPricePaid(seat.getPrice());
        return bookingSeat;
    }

    private List<SeatInventory> getSeatsForBooking(Booking booking) {
        List<Long> seatInventoryIds = booking.getBookingSeats()
                .stream()
                .map(BookingSeat::getSeatInventoryId)
                .toList();
        return seatInventoryRepository.findAllById(seatInventoryIds);
    }

    private void updateSeatsStatus(List<SeatInventory> seats, SeatStatus status) {
        seats.forEach(seat -> seat.setSeatStatus(status));
        seatInventoryRepository.saveAll(seats);
    }

    private void unlockSeats(List<SeatInventory> seats) {
        seats.forEach(seat -> seatLockService.unlockSeat(seat.getId()));
    }

    private void cacheBooking(Booking booking) {
        redisTemplate.opsForValue().set(
                "BOOKING:" + booking.getBookingReference(),
                booking.getId().toString(),
                BOOKING_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );
    }

    private void expireBooking(Booking booking) {
        booking.setStatus(BookingStatus.EXPIRED_BOOKING);
        bookingRepository.save(booking);

        outboxService.saveBookingExpiredEvent(booking);

        List<SeatInventory> seats = getSeatsForBooking(booking);

        seats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seatLockService.unlockSeat(seat.getId());
        });

        seatInventoryRepository.saveAll(seats);
        publishBookingExpiredEventAsync(booking);
    }

    private void publishBookingInitiatedEventAsync(Booking booking) {
        CompletableFuture.runAsync(() -> kafkaPublisher.bookingInitiated(booking));
    }

    private void publishBookingConfirmedEventAsync(Booking booking) {
        CompletableFuture.runAsync(() -> kafkaPublisher.bookingConfirmed(booking));
    }

    private void publishBookingExpiredEventAsync(Booking booking) {
        CompletableFuture.runAsync(() -> kafkaPublisher.bookingExpired(booking));
    }
}
