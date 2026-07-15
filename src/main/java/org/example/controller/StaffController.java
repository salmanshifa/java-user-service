package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.ApiResponse;
import org.example.controller.dto.CreateStaffRequest;
import org.example.controller.dto.CreateStaffResponse;
import org.example.controller.dto.UpdateStaffRequest;
import org.example.model.Booking;
import org.example.model.BookingStatus;
import org.example.model.EmploymentStatus;
import org.example.model.Staff;
import org.example.model.User;
import org.example.security.AuthenticatedUser;
import org.example.service.BookingService;
import org.example.service.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/staff")
public class StaffController {

    private static final Logger log = LoggerFactory.getLogger(StaffController.class);

    private final StaffService staffService;
    private final BookingService bookingService;

    public StaffController(StaffService staffService, BookingService bookingService) {
        this.staffService = staffService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Staff>>> listStaff(
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String specialization) {

        List<Staff> staffList;

        if (status != null) {
            log.info("Reading staff by status: {}", status);
            staffList = staffService.findByEmploymentStatus(status);
        } else if (position != null && !position.isBlank()) {
            log.info("Reading staff by position: {}", position);
            staffList = staffService.findByPosition(position);
        } else if (specialization != null && !specialization.isBlank()) {
            log.info("Reading staff by specialization: {}", specialization);
            staffList = staffService.findBySpecialization(specialization);
        } else {
            log.info("Reading all staff");
            staffList = staffService.findAll();
        }

        log.info("Total staff returned: {}", staffList.size());
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", staffList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Staff>> getStaff(@PathVariable Long id) {
        log.info("Reading staff with id: {}", id);
        return staffService.findById(id)
                .map(staff -> {
                    log.info("Staff found for id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("Staff retrieved", staff));
                })
                .orElseGet(() -> {
                    log.warn("Staff not found for id: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Staff not found", null));
                });
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Staff>>> getStaffByUser(@PathVariable Long userId) {
        log.info("Reading staff for user id: {}", userId);
        List<Staff> staffList = staffService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", staffList));
    }

    @GetMapping("/{staffId}/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getStaffBookings(
            @PathVariable Long staffId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Reading bookings for staff id: {}", staffId);

        List<Booking> bookings;

        if (date != null && status != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            log.info("Filtering staff bookings by status: {} and date: {}", status, date);
            bookings = bookingService.findByStaffIdAndStatusAndDateRange(staffId, status, start, end);
        } else if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            log.info("Filtering staff bookings by date: {}", date);
            bookings = bookingService.findByStaffIdAndDateRange(staffId, start, end);
        } else if (status != null) {
            log.info("Filtering staff bookings by status: {}", status);
            bookings = bookingService.findByStaffIdAndStatus(staffId, status);
        } else {
            bookings = bookingService.findByStaffId(staffId);
        }

        log.info("Total bookings for staff {}: {}", staffId, bookings.size());
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved", bookings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateStaffResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Creating staff with user: firstName='{}', lastName='{}', position='{}', username='{}', by userId={}",
                request.firstName(), request.lastName(), request.position(), request.username(), currentUserId);

        Staff created = staffService.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.position(),
                request.specialization(),
                request.hireDate(),
                request.employmentStatus(),
                request.workSchedule(),
                request.username(),
                request.password(),
                request.phone(),
                request.specialty(),
                request.serviceCategories(),
                currentUserId
        );

        // Build the user model from the newly created user
        User user = new User(created.userId(), request.username(), request.email(), request.phone(), org.example.model.RoleConstants.STAFF, true);
        String token = staffService.generateJwtToken(user);
        CreateStaffResponse response = CreateStaffResponse.of(created, user, token);

        log.info("Staff created with id: {}, userId: {}", created.id(), created.userId());
        return ResponseEntity.created(URI.create("/staff/" + created.id()))
                .body(ApiResponse.success("Staff created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Staff>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Updating staff with id: {}, by userId={}", id, currentUserId);

        return staffService.update(
                id,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.position(),
                request.specialization(),
                request.hireDate(),
                request.employmentStatus(),
                request.workSchedule(),
                request.specialty(),
                request.serviceCategories(),
                request.userId(),
                currentUserId
        ).map(staff -> {
            log.info("Staff updated for id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Staff updated", staff));
        }).orElseGet(() -> {
            log.warn("Staff not found for id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Staff not found", null));
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        log.info("Deleting staff with id: {}", id);
        return staffService.delete(id)
                ? ResponseEntity.ok(ApiResponse.success("Staff deleted"))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Staff not found", null));
    }
}
