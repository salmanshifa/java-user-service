package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.ApiResponse;
import org.example.controller.dto.CreateBookingRequest;
import org.example.controller.dto.UpdateBookingRequest;
import org.example.model.Booking;
import org.example.model.BookingStatus;
import org.example.security.AuthenticatedUser;
import org.example.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> listBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Booking> bookings;

        if (customerId != null) {
            log.info("Reading bookings for customer userId: {}", customerId);
            bookings = bookingService.findByCustomerUserId(customerId);
        } else if (staffId != null) {
            log.info("Reading bookings for staff id: {}", staffId);
            bookings = bookingService.findByStaffId(staffId);
        } else if (status != null) {
            log.info("Reading bookings by status: {}", status);
            bookings = bookingService.findByStatus(status);
        } else if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            log.info("Reading bookings for date: {}", date);
            bookings = bookingService.findByDateRange(start, end);
        } else {
            log.info("Reading all bookings");
            bookings = bookingService.findAll();
        }

        log.info("Total bookings returned: {}", bookings.size());
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved", bookings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBooking(@PathVariable Long id) {
        log.info("Reading booking with id: {}", id);
        return bookingService.findById(id)
                .map(booking -> {
                    log.info("Booking found for id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("Booking retrieved", booking));
                })
                .orElseGet(() -> {
                    log.warn("Booking not found for id: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Booking not found", null));
                });
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByOwner(
            Authentication authentication,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Reading bookings for owner userId: {}", currentUserId);

        List<Booking> bookings;

        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            log.info("Filtering owner bookings by date: {}", date);
            bookings = bookingService.findAllForOwnerByDateRange(currentUserId, start, end);
        } else if (status != null) {
            log.info("Filtering owner bookings by status: {}", status);
            bookings = bookingService.findAllForOwnerByStatus(currentUserId, status);
        } else {
            bookings = bookingService.findAllForOwner(currentUserId);
        }

        log.info("Total bookings for owner {}: {}", currentUserId, bookings.size());
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved", bookings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Creating booking: customerUserId={}, staffId={}, serviceId={}, by userId={}",
                request.customerUserId(), request.staffId(), request.serviceId(), currentUserId);

        Booking created = bookingService.create(
                request.customerUserId(),
                request.staffId(),
                request.serviceId(),
                request.appointmentTime(),
                request.endTime(),
                request.status(),
                request.notes(),
                currentUserId
        );

        log.info("Booking created with id: {}", created.id());
        return ResponseEntity.created(URI.create("/bookings/" + created.id()))
                .body(ApiResponse.success("Booking created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Updating booking with id: {}, by userId={}", id, currentUserId);

        return bookingService.update(
                id,
                request.customerUserId(),
                request.staffId(),
                request.serviceId(),
                request.appointmentTime(),
                request.endTime(),
                request.status(),
                request.notes(),
                currentUserId
        ).map(booking -> {
            log.info("Booking updated for id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Booking updated", booking));
        }).orElseGet(() -> {
            log.warn("Booking not found for id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Booking not found", null));
        });
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Updating booking status: id={}, status={}, by userId={}", id, status, currentUserId);

        return bookingService.updateStatus(id, status, currentUserId)
                .map(booking -> {
                    log.info("Booking status updated for id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("Booking status updated", booking));
                })
                .orElseGet(() -> {
                    log.warn("Booking not found for id: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Booking not found", null));
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        log.info("Deleting booking with id: {}", id);
        return bookingService.delete(id)
                ? ResponseEntity.ok(ApiResponse.success("Booking deleted"))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Booking not found", null));
    }
}
