package org.example.repository;

import org.example.entity.BookingEntity;
import org.example.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByCustomerUserId(Long customerUserId);

    List<BookingEntity> findByStaffId(Long staffId);

    List<BookingEntity> findByServiceId(Long serviceId);

    List<BookingEntity> findByStatus(BookingStatus status);

    List<BookingEntity> findByStaffIdAndStatus(Long staffId, BookingStatus status);

    List<BookingEntity> findByStaffIdAndAppointmentTimeBetween(Long staffId, LocalDateTime start, LocalDateTime end);

    List<BookingEntity> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

    List<BookingEntity> findByCreatedBy(Long createdBy);

    boolean existsByStaffIdAndAppointmentTimeBetweenAndStatusNot(
            Long staffId, LocalDateTime start, LocalDateTime end, BookingStatus status);

    List<BookingEntity> findByStaffIdIn(List<Long> staffIds);

    long countByStatus(BookingStatus status);
}
