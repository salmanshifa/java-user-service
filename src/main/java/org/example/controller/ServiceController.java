package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.ApiResponse;
import org.example.controller.dto.CreateServiceRequest;
import org.example.controller.dto.UpdateServiceRequest;
import org.example.model.ServiceItem;
import org.example.model.ServiceStatus;
import org.example.security.AuthenticatedUser;
import org.example.service.ServiceService;
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
import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private static final Logger log = LoggerFactory.getLogger(ServiceController.class);

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceItem>>> listServices(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "ACTIVE") ServiceStatus status) {

        List<ServiceItem> services;

        if (category != null && !category.isBlank()) {
            log.info("Reading services by category: {}, status: {}", category, status);
            services = serviceService.findByCategoryAndStatus(category, status);
        } else {
            log.info("Reading services by status: {}", status);
            services = serviceService.findByStatus(status);
        }

        log.info("Total services returned: {}", services.size());
        return ResponseEntity.ok(ApiResponse.success("Services retrieved", services));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceItem>> getService(@PathVariable Long id) {
        log.info("Reading service with id: {}", id);
        return serviceService.findById(id)
                .map(service -> {
                    log.info("Service found for id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("Service retrieved", service));
                })
                .orElseGet(() -> {
                    log.warn("Service not found for id: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Service not found", null));
                });
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<ServiceItem>>> getServicesByOwner(Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Reading services for owner userId: {}", currentUserId);
        List<ServiceItem> services = serviceService.findByCreatedBy(currentUserId);
        log.info("Total services for owner {}: {}", currentUserId, services.size());
        return ResponseEntity.ok(ApiResponse.success("Services retrieved", services));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = serviceService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceItem>> createService(
            @Valid @RequestBody CreateServiceRequest request,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Creating service: name='{}', category='{}', by userId={}", request.name(), request.category(), currentUserId);
        ServiceItem created = serviceService.create(
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.durationMinutes(),
                request.status(),
                currentUserId
        );
        log.info("Service created with id: {}", created.id());
        return ResponseEntity.created(URI.create("/services/" + created.id()))
                .body(ApiResponse.success("Service created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceItem>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request,
            Authentication authentication) {
        Long currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        log.info("Updating service with id: {}, by userId={}", id, currentUserId);
        return serviceService.update(
                id,
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.durationMinutes(),
                request.status(),
                currentUserId
        ).map(service -> {
            log.info("Service updated for id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Service updated", service));
        }).orElseGet(() -> {
            log.warn("Service not found for id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Service not found", null));
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        log.info("Deleting service with id: {}", id);
        return serviceService.delete(id)
                ? ResponseEntity.ok(ApiResponse.success("Service deleted"))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Service not found", null));
    }
}
