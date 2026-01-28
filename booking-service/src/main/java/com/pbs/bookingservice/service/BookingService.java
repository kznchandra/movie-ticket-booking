package com.pbs.bookingservice.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.pbs.bookingservice.common.ex.*;
import com.pbs.bookingservice.common.req.BookingRequest;
import com.pbs.bookingservice.common.resp.BookingResponse;
import com.pbs.bookingservice.common.resp.PricingDetails;
import com.pbs.bookingservice.entity.Booking;
import com.pbs.bookingservice.entity.BookingSeat;
import com.pbs.bookingservice.entity.SeatInventory;
import com.pbs.bookingservice.entity.Show;
import com.pbs.bookingservice.entity.enums.BookingSeatStatus;
import com.pbs.bookingservice.entity.enums.BookingStatus;
import com.pbs.bookingservice.entity.enums.SeatStatus;
import com.pbs.bookingservice.kafka.KafkaEventPublisher;
import com.pbs.bookingservice.repository.BookingRepository;
import com.pbs.bookingservice.repository.SeatInventoryRepository;
import com.pbs.bookingservice.repository.ShowRepository;
import com.pbs.bookingservice.saga.OutboxService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private static final int BOOKING_EXPIRY_MINUTES = 15;
    private static final long BOOKING_EXPIRY_CHECK_DELAY_MS = 60000;

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaEventPublisher kafkaPublisher;
    private final PricingService pricingService;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final OutboxService outboxService;
    private final ShowRepository showRepository;
    public BookingResponse initiateBooking(BookingRequest request) {
        log.debug("Initiating booking for userId: {}, showId: {}, seats: {}",
                request.getUserId(), request.getShowId(), request.getSeatNumbers());

        validateBookingRequest(request);

        // Acquire seat locks
        List<SeatInventory> seats = validateAndLockSeats(
                request.getShowId(),
                request.getSeatNumbers(),
                request.getUserId()
        );
        log.info("Successfully locked {} seats for userId: {}, showId: {}",
                seats.size(), request.getUserId(), request.getShowId());

        // Pricing
        PricingDetails pricing =
                pricingService.calculatePricing(seats, request.getOfferCode());
        log.info("Pricing calculated - Base: {}, Discount: {}, Final: {} for userId: {}",
                pricing.getBaseAmount(), pricing.getDiscountAmount(), pricing.getFinalAmount(), request.getUserId());

        // Create booking
        Booking booking = createBookingRecord(request, seats, pricing);
        log.info("Booking created with reference: {} for userId: {}",
                booking.getBookingReference(), request.getUserId());

        // Write Outbox Event (same transaction)
        outboxService.saveBookingInitiatedEvent(booking);

        // Cache booking temporarily
        cacheBooking(booking);

        // Publish Kafka event
        // publishBookingInitiatedEvent(booking);
        log.info("Booking initiated successfully with reference: {}", booking.getBookingReference());
        return new BookingResponse(booking, seats);
    }

    private Booking createBookingRecord(
            BookingRequest request,
            List<SeatInventory> seats,
            PricingDetails pricing) {
        log.debug("Creating booking record for userId: {}, showId: {}, seats count: {}",
                request.getUserId(), request.getShowId(), seats.size());

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

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> createBookingSeat(booking, seat))
                .toList();

        booking.setBookingSeats(bookingSeats);
        return bookingRepository.save(booking);
    }


    private List<SeatInventory> validateAndLockSeats(
            Long showId,
            List<String> seatNumbers,
            Long userId) {
        log.debug("Validating and locking seats for showId: {}, userId: {}, seatNumbers: {}",
                showId, userId, seatNumbers);

        List<SeatInventory> seats =
                seatInventoryRepository.findByShowIdAndSeatNumberIn(showId, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            log.error("Seats not found - Requested: {}, Found: {} for showId: {}",
                    seatNumbers.size(), seats.size(), showId);
            throw new BookingException("Some seats not found");
        }

        boolean isAllSeatsLocked = seats.stream().allMatch(seat -> {
            validateSeatAvailability(seat);
            return seatLockService.lockSeat(seat.getId(), userId);
        });
        if (!isAllSeatsLocked) {
            log.error("Failed to lock all seats for userId: {}, showId: {}, seatNumbers: {}",
                    userId, showId, seatNumbers);
            throw new SeatLockException("Failed to lock all seats");
        }
        log.info("Successfully validated and locked {} seats for userId: {}, showId: {}",
                seats.size(), userId, showId);
        return seats;
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        log.debug("Confirming booking with bookingId: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        validateBookingForConfirmation(booking);

        outboxService.saveBookingConfirmedEvent(booking);

        List<SeatInventory> seats = getSeatsForBooking(booking);

        updateSeatsStatus(seats, SeatStatus.BOOKED);
        unlockSeats(seats);

        booking.setStatus(BookingStatus.CONFIRMED_BOOKING);
        booking.getBookingSeats()
                .forEach(bookingSeat -> bookingSeat.setStatus(BookingSeatStatus.BOOKED));
        bookingRepository.save(booking);

        Show show = showRepository.findById(booking.getShowId())
                .orElseThrow(() -> new BookingException("Show not found"));
        show.setAvailableSeats(show.getAvailableSeats() - seats.size());
        showRepository.save(show);

        //publishBookingConfirmedEvent(booking);
        log.info("Booking confirmed successfully - bookingId: {}, reference: {}, userId: {}",
                bookingId, booking.getBookingReference(), booking.getUserId());

    }

    @Scheduled(fixedDelay = BOOKING_EXPIRY_CHECK_DELAY_MS)
    @Transactional
    public void expireBookings() {
        log.debug("Running scheduled task to expire bookings");

        List<Booking> expiredBookings =
                bookingRepository.findExpiredBookings(LocalDateTime.now());

        if (expiredBookings == null || expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired bookings to process", expiredBookings.size());
        for (Booking booking : expiredBookings) {
            expireBooking(booking);
        }
    }

    public BookingResponse getBookingById(Long bookingId, Long userId) {
        log.debug("Retrieving booking - bookingId: {}, userId: {}", bookingId, userId);
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        // suppose payment done -> change the status confirm to test it
        booking.setStatus(BookingStatus.CONFIRMED_BOOKING);
        bookingRepository.save(booking);

        validateBookingOwnership(booking, userId);

        List<SeatInventory> seats = getSeatsForBooking(booking);

        return new BookingResponse(booking, seats);
    }

    private void validateBookingRequest(BookingRequest request) {
        if (request.getShowId() == null) {
            log.error("Booking validation failed: Show ID is missing");
            throw new BookingException("Show ID is required");
        }
        if (request.getUserId() == null) {
            log.error("Booking validation failed: User ID is missing");
            throw new BookingException("User ID is required");
        }
        if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            log.error("Booking validation failed: Seat numbers are missing or empty");
            throw new BookingException("Seat numbers are required");
        }
    }

    private void validateSeatAvailability(SeatInventory seat) {
        if (!SeatStatus.AVAILABLE.equals(seat.getSeatStatus())) {
            log.error("Seat validation failed - Seat {} is not available, current status: {}",
                    seat.getSeatNumber(), seat.getSeatStatus());
            throw new SeatUnavailableException("Seat is not available. " + seat.getSeatNumber());
        }
    }

    private void validateBookingForConfirmation(Booking booking) {
        if (!BookingStatus.PENDING_PAYMENT.equals(booking.getStatus())) {
            log.error("Booking confirmation validation failed - Invalid state: {} for booking ref: {}",
                    booking.getStatus(), booking.getBookingReference());
            throw new BookingValidationException("Invalid booking state for booking ref" + booking.getBookingReference());
        }
        // temp commented to testing booking
//        if (booking.getExpiryTime().isBefore(LocalDateTime.now())) {
//            throw new BookingValidationException("Booking has expired for booking ref "+booking.getBookingReference());
//        }
    }

    private void validateBookingOwnership(Booking booking, Long userId) {
        if (!booking.getUserId().equals(userId)) {
            log.error("Booking ownership validation failed - bookingId: {}, expected userId: {}, actual userId: {}",
                    booking.getId(), userId, booking.getUserId());
            throw new BookingValidationException("Booking does not belong to this user");
        }
    }

    private BookingSeat createBookingSeat(Booking booking, SeatInventory seat) {
        BookingSeat bookingSeat = new BookingSeat();
        bookingSeat.setBooking(booking);
        bookingSeat.setSeatInventoryId(seat.getId());
        bookingSeat.setPricePaid(seat.getPrice());
        bookingSeat.setStatus(BookingSeatStatus.PENDING);
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
        log.debug("Caching booking - reference: {}, bookingId: {}, expiry: {} minutes",
                booking.getBookingReference(), booking.getId(), BOOKING_EXPIRY_MINUTES);
        redisTemplate.opsForValue().set(
                "BOOKING:" + booking.getBookingReference(),
                booking.getId().toString(),
                BOOKING_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );
    }

    private void expireBooking(Booking booking) {
        log.info("Expiring booking - bookingId: {}, reference: {}, userId: {}",
                booking.getId(), booking.getBookingReference(), booking.getUserId());
        booking.setStatus(BookingStatus.EXPIRED_BOOKING);
        booking.getBookingSeats().forEach(bookingSeat -> bookingSeat.setStatus(BookingSeatStatus.EXPIRED));
        bookingRepository.save(booking);

        outboxService.saveBookingExpiredEvent(booking);

        List<SeatInventory> seats = getSeatsForBooking(booking);

        seats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seatLockService.unlockSeat(seat.getId());
        });

        seatInventoryRepository.saveAll(seats);
        // publishBookingExpiredEvent(booking);
    }

    private void publishBookingInitiatedEvent(Booking booking) {
        kafkaPublisher.bookingInitiated(booking);

    }

    private void publishBookingConfirmedEvent(Booking booking) {
        kafkaPublisher.bookingConfirmed(booking);

    }

    private void publishBookingExpiredEvent(Booking booking) {
        kafkaPublisher.bookingExpired(booking);
    }

    public List<BookingResponse> getAllBookingsByUserId(long userId) {
        log.debug("Retrieving all bookings for userId: {}", userId);

        List<Booking> bookings = bookingRepository.findByUserId(userId);
        if (bookings == null || bookings.isEmpty()) {
            log.warn("No bookings found for userId: {}", userId);
            throw new BookingNotFoundException("No bookings found for userId: " + userId);
        }

        log.info("Found {} bookings for userId: {}", bookings.size(), userId);

        return bookings.stream()
                .map(booking -> {
                    List<SeatInventory> seats = getSeatsForBooking(booking);
                    return new BookingResponse(booking, seats);
                })
                .toList();
    }
}
