package org.example.repository;

import org.example.entity.StaffEntity;
import org.example.model.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, Long> {

    List<StaffEntity> findByUserId(Long userId);

    List<StaffEntity> findByEmploymentStatus(EmploymentStatus status);

    List<StaffEntity> findBySpecialization(String specialization);

    List<StaffEntity> findByPosition(String position);

    boolean existsByEmail(String email);

    Optional<StaffEntity> findByEmail(String email);
}
