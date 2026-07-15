package org.example.service;

import org.example.entity.BookingEntity;
import org.example.model.Booking;
import org.example.model.BookingStatus;
import org.example.repository.BookingRepository;
import org.example.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final StaffRepository staffRepository;

    public BookingService(BookingRepository bookingRepository, StaffRepository staffRepository) {
        this.bookingRepository = bookingRepository;
        this.staffRepository = staffRepository;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id).map(this::toModel);
    }

    public List<Booking> findByCustomerUserId(Long customerUserId) {
        return bookingRepository.findByCustomerUserId(customerUserId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByStaffId(Long staffId) {
        return bookingRepository.findByStaffId(staffId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByStaffIdAndStatus(Long staffId, BookingStatus status) {
        return bookingRepository.findByStaffIdAndStatus(staffId, status).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByStaffIdAndDateRange(Long staffId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByStaffIdAndAppointmentTimeBetween(staffId, start, end).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByStaffIdAndStatusAndDateRange(Long staffId, BookingStatus status,
                                                             LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByStaffIdAndAppointmentTimeBetween(staffId, start, end).stream()
                .filter(entity -> entity.getStatus() == status)
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByServiceId(Long serviceId) {
        return bookingRepository.findByServiceId(serviceId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByAppointmentTimeBetween(start, end).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Booking> findByCreatedBy(Long createdBy) {
        return bookingRepository.findByCreatedBy(createdBy).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a shop owner's business.
     * This includes:
     * 1. Bookings directly created by the owner
     * 2. Bookings assigned to staff members who belong to the owner
     * 3. Deduplicates results by booking ID
     */
    public List<Booking> findAllForOwner(Long ownerUserId) {
        // 1. Bookings directly created by the owner
        List<BookingEntity> directEntities = bookingRepository.findByCreatedBy(ownerUserId);

        // 2. Find all staff belonging to this owner
        List<Long> ownerStaffIds = staffRepository.findByCreatedBy(ownerUserId).stream()
                .map(org.example.entity.StaffEntity::getId)
                .collect(Collectors.toList());

        // 3. Bookings for the owner's staff
        List<BookingEntity> staffEntities = Collections.emptyList();
        if (!ownerStaffIds.isEmpty()) {
            staffEntities = bookingRepository.findByStaffIdIn(ownerStaffIds);
        }

        // Combine and deduplicate by booking ID using a map
        Map<Long, BookingEntity> uniqueEntities = new HashMap<>();
        for (BookingEntity e : directEntities) uniqueEntities.put(e.getId(), e);
        for (BookingEntity e : staffEntities) uniqueEntities.put(e.getId(), e);

        return uniqueEntities.values().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for an owner, filtered by status.
     */
    public List<Booking> findAllForOwnerByStatus(Long ownerUserId, BookingStatus status) {
        return findAllForOwner(ownerUserId).stream()
                .filter(booking -> booking.status() == status)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for an owner within a date range.
     */
    public List<Booking> findAllForOwnerByDateRange(Long ownerUserId, LocalDateTime start, LocalDateTime end) {
        // First get all owner-related staff IDs
        List<Long> ownerStaffIds = staffRepository.findByCreatedBy(ownerUserId).stream()
                .map(org.example.entity.StaffEntity::getId)
                .collect(Collectors.toList());

        // Find bookings in date range that are either created by owner or for owner's staff
        List<BookingEntity> dateRangeEntities = bookingRepository.findByAppointmentTimeBetween(start, end);

        return dateRangeEntities.stream()
                .filter(entity ->
                        entity.getCreatedBy() != null && entity.getCreatedBy().equals(ownerUserId) ||
                        entity.getStaffId() != null && ownerStaffIds.contains(entity.getStaffId())
                )
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public Booking create(Long customerUserId, Long staffId, Long serviceId,
                          LocalDateTime appointmentTime, LocalDateTime endTime,
                          BookingStatus status, String notes, Long createdBy) {
        // Validate time range
        if (!endTime.isAfter(appointmentTime)) {
            throw new IllegalArgumentException("End time must be after appointment time");
        }

        // Check for staff availability if staff is assigned
        if (staffId != null) {
            boolean hasConflict = bookingRepository
                    .existsByStaffIdAndAppointmentTimeBetweenAndStatusNot(
                            staffId, appointmentTime, endTime, BookingStatus.CANCELLED);
            if (hasConflict) {
                throw new IllegalArgumentException("Staff member is not available during this time slot");
            }
        }

        BookingStatus bookingStatus = (status != null) ? status : BookingStatus.PENDING;

        BookingEntity entity = new BookingEntity(customerUserId, staffId, serviceId,
                appointmentTime, endTime, bookingStatus, notes);
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(createdBy);

        BookingEntity saved = bookingRepository.save(entity);
        log.info("Created booking: id={}, customerUserId={}, staffId={}, serviceId={}, time={}-{}, status={}",
                saved.getId(), saved.getCustomerUserId(), saved.getStaffId(), saved.getServiceId(),
                saved.getAppointmentTime(), saved.getEndTime(), saved.getStatus());
        return toModel(saved);
    }

    @Transactional
    public Optional<Booking> update(Long id, Long customerUserId, Long staffId, Long serviceId,
                                    LocalDateTime appointmentTime, LocalDateTime endTime,
                                    BookingStatus status, String notes, Long updatedBy) {
        return bookingRepository.findById(id).map(entity -> {
            // Validate time range if times are being updated
            LocalDateTime newAppointmentTime = (appointmentTime != null) ? appointmentTime : entity.getAppointmentTime();
            LocalDateTime newEndTime = (endTime != null) ? endTime : entity.getEndTime();

            if (!newEndTime.isAfter(newAppointmentTime)) {
                throw new IllegalArgumentException("End time must be after appointment time");
            }

            // Check for staff availability if staff or time changed
            Long newStaffId = (staffId != null) ? staffId : entity.getStaffId();
            if (newStaffId != null &&
                (!newStaffId.equals(entity.getStaffId()) ||
                 !newAppointmentTime.equals(entity.getAppointmentTime()) ||
                 !newEndTime.equals(entity.getEndTime()))) {

                boolean hasConflict = bookingRepository
                        .existsByStaffIdAndAppointmentTimeBetweenAndStatusNot(
                                newStaffId, newAppointmentTime, newEndTime, BookingStatus.CANCELLED);
                if (hasConflict) {
                    throw new IllegalArgumentException("Staff member is not available during this time slot");
                }
            }

            if (customerUserId != null) entity.setCustomerUserId(customerUserId);
            if (staffId != null) entity.setStaffId(staffId);
            if (serviceId != null) entity.setServiceId(serviceId);
            if (appointmentTime != null) entity.setAppointmentTime(appointmentTime);
            if (endTime != null) entity.setEndTime(endTime);
            if (status != null) entity.setStatus(status);
            if (notes != null) entity.setNotes(notes);
            entity.setUpdatedBy(updatedBy);

            BookingEntity saved = bookingRepository.save(entity);
            log.info("Updated booking: id={}, status={}, updatedBy={}",
                    saved.getId(), saved.getStatus(), saved.getUpdatedBy());
            return toModel(saved);
        });
    }

    @Transactional
    public Optional<Booking> updateStatus(Long id, BookingStatus status, Long updatedBy) {
        return bookingRepository.findById(id).map(entity -> {
            entity.setStatus(status);
            entity.setUpdatedBy(updatedBy);
            BookingEntity saved = bookingRepository.save(entity);
            log.info("Updated booking status: id={}, status={}, updatedBy={}",
                    saved.getId(), saved.getStatus(), saved.getUpdatedBy());
            return toModel(saved);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            return false;
        }
        bookingRepository.deleteById(id);
        log.info("Deleted booking: id={}", id);
        return true;
    }

    public long count() {
        return bookingRepository.count();
    }

    public long countByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    private Booking toModel(BookingEntity entity) {
        return new Booking(
                entity.getId(),
                entity.getCustomerUserId(),
                entity.getStaffId(),
                entity.getServiceId(),
                entity.getAppointmentTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt()
        );
    }
}
