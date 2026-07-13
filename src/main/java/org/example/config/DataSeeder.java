package org.example.config;

import org.example.entity.ServiceEntity;
import org.example.model.CategoryConstants;
import org.example.model.ServiceStatus;
import org.example.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ServiceRepository serviceRepository;

    public DataSeeder(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void run(String... args) {
        if (serviceRepository.count() > 0) {
            log.info("Services already seeded, skipping data initialization");
            return;
        }

        log.info("Seeding predefined services...");

        List<ServiceEntity> services = List.of(
                // ===== MASSAGE SERVICES =====
                new ServiceEntity("Swedish Massage",
                        "A gentle full-body massage using long strokes, kneading, and circular movements to relax and energize.",
                        CategoryConstants.MASSAGE, new BigDecimal("60.00"), 60, ServiceStatus.ACTIVE),
                new ServiceEntity("Deep Tissue Massage",
                        "Targets deep muscle layers and connective tissue to release chronic tension and knots.",
                        CategoryConstants.MASSAGE, new BigDecimal("75.00"), 60, ServiceStatus.ACTIVE),
                new ServiceEntity("Hot Stone Massage",
                        "Smooth heated stones placed on key points of the body to warm and loosen tight muscles.",
                        CategoryConstants.MASSAGE, new BigDecimal("85.00"), 75, ServiceStatus.ACTIVE),
                new ServiceEntity("Aromatherapy Massage",
                        "Combines gentle massage techniques with essential oils to enhance relaxation and well-being.",
                        CategoryConstants.MASSAGE, new BigDecimal("70.00"), 60, ServiceStatus.ACTIVE),
                new ServiceEntity("Sports Massage",
                        "Focuses on muscle groups used during exercise to prevent injury and improve performance.",
                        CategoryConstants.MASSAGE, new BigDecimal("65.00"), 45, ServiceStatus.ACTIVE),

                // ===== HAIR SERVICES =====
                new ServiceEntity("Classic Haircut",
                        "A precise haircut tailored to your face shape and style preferences.",
                        CategoryConstants.HAIR, new BigDecimal("30.00"), 30, ServiceStatus.ACTIVE),
                new ServiceEntity("Hair Styling",
                        "Professional blow-dry and styling for any occasion, from casual to formal.",
                        CategoryConstants.HAIR, new BigDecimal("45.00"), 45, ServiceStatus.ACTIVE),
                new ServiceEntity("Hair Coloring",
                        "Full-color application using premium products for vibrant, long-lasting results.",
                        CategoryConstants.HAIR, new BigDecimal("80.00"), 90, ServiceStatus.ACTIVE),
                new ServiceEntity("Hair Treatment",
                        "Deep conditioning and repair treatment to restore moisture and shine to damaged hair.",
                        CategoryConstants.HAIR, new BigDecimal("50.00"), 45, ServiceStatus.ACTIVE),
                new ServiceEntity("Beard Grooming",
                        "Expert beard trim, shaping, and conditioning for a polished look.",
                        CategoryConstants.HAIR, new BigDecimal("20.00"), 20, ServiceStatus.ACTIVE),

                // ===== FACIAL SERVICES =====
                new ServiceEntity("Classic Facial",
                        "Deep cleansing, exfoliation, extraction, and mask to refresh and revitalize your skin.",
                        CategoryConstants.FACIAL, new BigDecimal("55.00"), 45, ServiceStatus.ACTIVE),
                new ServiceEntity("Anti-Aging Facial",
                        "Targets fine lines and wrinkles with specialized products and techniques for youthful skin.",
                        CategoryConstants.FACIAL, new BigDecimal("75.00"), 60, ServiceStatus.ACTIVE),
                new ServiceEntity("Acne Treatment Facial",
                        "Designed for acne-prone skin with deep cleansing, antibacterial treatments, and soothing masks.",
                        CategoryConstants.FACIAL, new BigDecimal("65.00"), 45, ServiceStatus.ACTIVE),
                new ServiceEntity("Hydrating Facial",
                        "Intense moisture infusion to combat dryness and restore skin's natural glow.",
                        CategoryConstants.FACIAL, new BigDecimal("60.00"), 50, ServiceStatus.ACTIVE),
                new ServiceEntity("Brightening Facial",
                        "Vitamin-infused treatment to even out skin tone and reveal a radiant complexion.",
                        CategoryConstants.FACIAL, new BigDecimal("70.00"), 60, ServiceStatus.ACTIVE),

                // ===== NAIL SERVICES =====
                new ServiceEntity("Classic Manicure",
                        "Nail shaping, cuticle care, and polish application for clean, polished hands.",
                        CategoryConstants.NAILS, new BigDecimal("25.00"), 30, ServiceStatus.ACTIVE),
                new ServiceEntity("Gel Manicure",
                        "Long-lasting gel polish application with chip-resistant finish for up to 2 weeks.",
                        CategoryConstants.NAILS, new BigDecimal("35.00"), 45, ServiceStatus.ACTIVE),
                new ServiceEntity("Classic Pedicure",
                        "Foot soak, nail care, exfoliation, and polish for refreshed and smooth feet.",
                        CategoryConstants.NAILS, new BigDecimal("35.00"), 45, ServiceStatus.ACTIVE),
                new ServiceEntity("Nail Art Design",
                        "Custom nail art with intricate designs, patterns, and embellishments.",
                        CategoryConstants.NAILS, new BigDecimal("45.00"), 60, ServiceStatus.ACTIVE),
                new ServiceEntity("Acrylic Full Set",
                        "Full set of acrylic nail extensions shaped and polished to your preference.",
                        CategoryConstants.NAILS, new BigDecimal("55.00"), 75, ServiceStatus.ACTIVE)
        );

        serviceRepository.saveAll(services);
        log.info("Seeded {} predefined services across categories: MASSAGE, HAIR, FACIAL, NAILS", services.size());
    }
}
