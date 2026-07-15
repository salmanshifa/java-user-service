package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.StaffEntity;
import org.example.model.EmploymentStatus;
import org.example.model.PositionConstants;
import org.example.model.RoleConstants;
import org.example.model.Staff;
import org.example.model.User;
import org.example.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffService {

    private static final Logger log = LoggerFactory.getLogger(StaffService.class);

    private final StaffRepository staffRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public StaffService(StaffRepository staffRepository, UserService userService, ObjectMapper objectMapper) {
        this.staffRepository = staffRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public List<Staff> findAll() {
        return staffRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<Staff> findById(Long id) {
        return staffRepository.findById(id).map(this::toModel);
    }

    public List<Staff> findByUserId(Long userId) {
        return staffRepository.findByUserId(userId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Staff> findByEmploymentStatus(EmploymentStatus status) {
        return staffRepository.findByEmploymentStatus(status).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Staff> findBySpecialization(String specialization) {
        return staffRepository.findBySpecialization(specialization).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<Staff> findByPosition(String position) {
        return staffRepository.findByPosition(position).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public Staff create(String firstName, String lastName, String email,
                        String position, String specialization, LocalDate hireDate,
                        EmploymentStatus employmentStatus, String workSchedule,
                        String username, String password, String phone,
                        String specialty, List<String> serviceCategories,
                        Long createdBy) {
        // Validate against both tables first, before any writes
        validateUniqueEmail(email, null);
        validatePosition(position);
        userService.validateUniqueUserFields(username, email, phone, null);

        // Derive specialization (category) from position if not explicitly provided
        String resolvedSpecialization = (specialization != null && !specialization.isBlank())
                ? specialization.toUpperCase()
                : PositionConstants.toCategory(position);

        // Create user login data for staff
        User createdUser = userService.create(username, email, phone, password, true, RoleConstants.STAFF);
        log.info("Created user for staff: id={}, username='{}', role=STAFF", createdUser.id(), createdUser.username());

        // Create staff record linked to the newly created user
        String serviceCategoriesJson = serializeServiceCategories(serviceCategories);
        StaffEntity staffEntity = new StaffEntity(firstName, lastName, email, phone,
                position, resolvedSpecialization, hireDate, employmentStatus, workSchedule,
                specialty, serviceCategoriesJson);
        staffEntity.setUserId(createdUser.id());
        staffEntity.setCreatedBy(createdBy);
        staffEntity.setUpdatedBy(createdBy);

        StaffEntity saved = staffRepository.save(staffEntity);
        log.info("Created staff: id={}, name='{} {}', position='{}', status={}, user={}, createdBy={}",
                saved.getId(), saved.getFirstName(), saved.getLastName(),
                saved.getPosition(), saved.getEmploymentStatus(), saved.getUserId(), saved.getCreatedBy());
        return toModel(saved);
    }

    public String generateJwtToken(User user) {
        return userService.generateJwtToken(user);
    }

    public Optional<Staff> update(Long id, String firstName, String lastName, String email, String phone,
                                  String position, String specialization, LocalDate hireDate,
                                  EmploymentStatus employmentStatus, String workSchedule,
                                  String specialty, List<String> serviceCategories, Long userId,
                                  Long updatedBy) {
        return staffRepository.findById(id).map(entity -> {
            validateUniqueEmail(email, id);
            validatePosition(position);

            // Derive specialization (category) from position if not explicitly provided
            String resolvedSpecialization = (specialization != null && !specialization.isBlank())
                    ? specialization.toUpperCase()
                    : PositionConstants.toCategory(position);

            entity.setFirstName(firstName);
            entity.setLastName(lastName);
            entity.setEmail(email);
            entity.setPhone(phone);
            entity.setPosition(position);
            entity.setSpecialization(resolvedSpecialization);
            entity.setHireDate(hireDate);
            entity.setEmploymentStatus(employmentStatus);
            entity.setWorkSchedule(workSchedule);
            entity.setSpecialty(specialty);
            entity.setServiceCategories(serializeServiceCategories(serviceCategories));
            entity.setUserId(userId);
            entity.setUpdatedBy(updatedBy);

            StaffEntity saved = staffRepository.save(entity);
            log.info("Updated staff: id={}, name='{} {}', position='{}', status={}, updatedBy={}",
                    saved.getId(), saved.getFirstName(), saved.getLastName(),
                    saved.getPosition(), saved.getEmploymentStatus(), saved.getUpdatedBy());
            return toModel(saved);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        return staffRepository.findById(id).map(entity -> {
            Long userId = entity.getUserId();
            if (userId != null) {
                userService.delete(userId);
                log.info("Deleted associated user: id={}", userId);
            }
            staffRepository.delete(entity);
            log.info("Deleted staff: id={}, userId={}", id, entity.getUserId());
            return true;
        }).orElse(false);
    }

    public long count() {
        return staffRepository.count();
    }

    private void validatePosition(String position) {
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("Position is required");
        }
        String upper = position.toUpperCase();
        if (!upper.equals(PositionConstants.STYLIST)
                && !upper.equals(PositionConstants.BARBER)
                && !upper.equals(PositionConstants.NAIL_TECH)
                && !upper.equals(PositionConstants.MASSAGE_THERAPIST)
                && !upper.equals(PositionConstants.ESTHETICIAN)
                && !upper.equals(PositionConstants.RECEPTIONIST)
                && !upper.equals(PositionConstants.MANAGER)) {
            throw new IllegalArgumentException("Invalid position: must be one of STYLIST, BARBER, NAIL_TECH, MASSAGE_THERAPIST, ESTHETICIAN, RECEPTIONIST, MANAGER");
        }
    }

    private void validateUniqueEmail(String email, Long currentId) {
        staffRepository.findByEmail(email)
                .filter(entity -> currentId == null || !entity.getId().equals(currentId))
                .ifPresent(entity -> {
                    throw new IllegalArgumentException("A staff member with this email already exists");
                });
    }

    private String serializeServiceCategories(List<String> serviceCategories) {
        if (serviceCategories == null || serviceCategories.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(serviceCategories);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize service categories: {}", serviceCategories, e);
            return null;
        }
    }

    private List<String> deserializeServiceCategories(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize service categories: {}", json, e);
            return Collections.emptyList();
        }
    }

    private Staff toModel(StaffEntity entity) {
        return new Staff(
                entity.getId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPosition(),
                entity.getSpecialization(),
                entity.getHireDate(),
                entity.getEmploymentStatus(),
                entity.getWorkSchedule(),
                entity.getSpecialty(),
                deserializeServiceCategories(entity.getServiceCategories()),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt()
        );
    }
}
