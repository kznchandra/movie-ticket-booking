# Movie Ticket Booking System

A microservices-based movie ticket booking system built with Spring Boot that provides comprehensive booking management
capabilities.

## Overview

This system allows users to browse movies, select showtimes, book seats, and manage their reservations. The application
follows a microservices architecture with separate services handling different business domains.

## Features

- **Movie Booking Management**: Create, update, and manage movie ticket bookings
- **Seat Inventory Management**: Track and manage seat availability for movie showings
- **Multi-Seat Booking**: Support for booking multiple seats in a single transaction
- **Booking Status Tracking**: Real-time tracking of booking and seat statuses
- **RESTful API**: Clean and intuitive API endpoints for integration

## Architecture

The system follows a microservices architecture pattern with the following components:

### Booking Service

Handles all booking-related operations including:

- Creating new bookings
- Confirm Booking
- Get Booking By Id
- Get Bookings By User Id
- Managing booking seats
- Tracking seat inventory
- Processing booking status updates

## Technology Stack

- **Java 21+**
- **Spring Boot 3.x**
- **Spring Data JPA**: Database operations
- **Jakarta Persistence API**: ORM mapping
- **Lombok**: Reduce boilerplate code
- **Jackson**: JSON serialization/deserialization
- **PostgreSQL**: Relational database (configurable)

## Data Models

### Booking

Represents a movie ticket booking transaction containing multiple seats.

### BookingSeat

Represents an individual seat within a booking with the following attributes:

- `id`: Unique identifier
- `booking`: Reference to parent booking
- `seatInventoryId`: Reference to seat in inventory
- `seatNumber`: Seat identifier (e.g., "A1", "B5")
- `pricePaid`: Amount paid for the seat
- `status`: Current status (PENDING, CONFIRMED, CANCELLED)

### SeatInventory

Manages the availability and details of seats for movie showings.

## API Response Structure

### BookingResponse

POST /api/V1/bookings

```json
{
  "userId": 1,
  "offerCode": "THIRD_TICKET_50_DISCOUNT",
  "showId": 1,
  "seatCount": 1,
  "seatNumbers": [
    "A1","A2","A3"
  ]
}

RESPOSNE 201

PUT /api/v1/bookings/{bookingId}/confirm
RESPONSE: 200

GET /api/v1/bookings/{bookingId}
RESPONSE: 200