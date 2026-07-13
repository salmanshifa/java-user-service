package org.example.repository;

import org.example.entity.ServiceEntity;
import org.example.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findByCategory(String category);

    List<ServiceEntity> findByStatus(ServiceStatus status);

    List<ServiceEntity> findByCategoryAndStatus(String category, ServiceStatus status);

    Optional<ServiceEntity> findByName(String name);

    boolean existsByName(String name);

    List<ServiceEntity> findByCreatedBy(Long createdBy);
}
