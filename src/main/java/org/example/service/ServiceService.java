package org.example.service;

import org.example.entity.ServiceEntity;
import org.example.model.CategoryConstants;
import org.example.model.ServiceItem;
import org.example.model.ServiceStatus;
import org.example.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    private static final Logger log = LoggerFactory.getLogger(ServiceService.class);

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceItem> findAll() {
        return serviceRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ServiceItem> findByStatus(ServiceStatus status) {
        return serviceRepository.findByStatus(status).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ServiceItem> findByCategory(String category) {
        return serviceRepository.findByCategory(category.toUpperCase()).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ServiceItem> findByCategoryAndStatus(String category, ServiceStatus status) {
        return serviceRepository.findByCategoryAndStatus(category.toUpperCase(), status).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ServiceItem> findByCreatedBy(Long createdBy) {
        return serviceRepository.findByCreatedBy(createdBy).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<ServiceItem> findById(Long id) {
        return serviceRepository.findById(id).map(this::toModel);
    }

    public ServiceItem create(String name, String description, String category, BigDecimal price, Integer durationMinutes, ServiceStatus status, Long createdBy) {
        validateCategory(category);
        validateUniqueName(name, null);

        ServiceEntity entity = new ServiceEntity(name, description, category.toUpperCase(), price, durationMinutes, status);
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(createdBy);
        ServiceEntity saved = serviceRepository.save(entity);
        log.info("Created service: id={}, name='{}', category='{}', price={}, duration={}min, status={}, createdBy={}",
                saved.getId(), saved.getName(), saved.getCategory(), saved.getPrice(), saved.getDurationMinutes(), saved.getStatus(), saved.getCreatedBy());
        return toModel(saved);
    }

    public Optional<ServiceItem> update(Long id, String name, String description, String category, BigDecimal price, Integer durationMinutes, ServiceStatus status, Long updatedBy) {
        return serviceRepository.findById(id).map(entity -> {
            validateCategory(category);
            validateUniqueName(name, id);

            entity.setName(name);
            entity.setDescription(description);
            entity.setCategory(category.toUpperCase());
            entity.setPrice(price);
            entity.setDurationMinutes(durationMinutes);
            if (status != null) {
                entity.setStatus(status);
            }
            entity.setUpdatedBy(updatedBy);

            ServiceEntity saved = serviceRepository.save(entity);
            log.info("Updated service: id={}, name='{}', category='{}', price={}, duration={}min, status={}, updatedBy={}",
                    saved.getId(), saved.getName(), saved.getCategory(), saved.getPrice(), saved.getDurationMinutes(), saved.getStatus(), saved.getUpdatedBy());
            return toModel(saved);
        });
    }

    public boolean delete(Long id) {
        if (!serviceRepository.existsById(id)) {
            return false;
        }
        serviceRepository.deleteById(id);
        log.info("Deleted service: id={}", id);
        return true;
    }

    public long count() {
        return serviceRepository.count();
    }

    public List<String> getCategories() {
        return List.of(CategoryConstants.MASSAGE, CategoryConstants.HAIR, CategoryConstants.FACIAL, CategoryConstants.NAILS);
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
        String upper = category.toUpperCase();
        if (!upper.equals(CategoryConstants.MASSAGE) && !upper.equals(CategoryConstants.HAIR) && !upper.equals(CategoryConstants.FACIAL) && !upper.equals(CategoryConstants.NAILS)) {
            throw new IllegalArgumentException("Invalid category: must be one of MASSAGE, HAIR, FACIAL, or NAILS");
        }
    }

    private void validateUniqueName(String name, Long currentId) {
        serviceRepository.findByName(name)
                .filter(entity -> currentId == null || !entity.getId().equals(currentId))
                .ifPresent(entity -> {
                    throw new IllegalArgumentException("A service with this name already exists");
                });
    }

    private ServiceItem toModel(ServiceEntity entity) {
        return new ServiceItem(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getDurationMinutes(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt()
        );
    }
}
