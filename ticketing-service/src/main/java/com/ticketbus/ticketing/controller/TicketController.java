package com.ticketbus.ticketing.controller;

import com.ticketbus.common.domain.Product;
import com.ticketbus.common.domain.Ticket;
import com.ticketbus.common.domain.TicketStatus;
import com.ticketbus.common.dto.TicketDto;
import com.ticketbus.ticketing.repository.ProductRepository;
import com.ticketbus.ticketing.repository.TicketRepository;
import com.ticketbus.ticketing.service.DynamicQrService;
import com.ticketbus.ticketing.service.QrSigningService;
import com.ticketbus.ticketing.service.QrValidationService;
import com.ticketbus.ticketing.service.TicketingService;
import com.ticketbus.ticketing.service.TicketPrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketingService ticketingService;
    private final ProductRepository productRepository;
    private final TicketRepository ticketRepository;
    private final QrSigningService qrSigningService;
    private final DynamicQrService dynamicQrService;
    private final TicketPrintService ticketPrintService;
    private final QrValidationService qrValidationService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Ticket> active = ticketRepository.findByStatus(TicketStatus.ACTIVE);
        long totalActive = active.size();
        long passActive = active.stream()
            .filter(t -> {
                return productRepository.findById(t.getProductId())
                    .map(p -> p.getType() == com.ticketbus.common.domain.ProductType.PASS)
                    .orElse(false);
            }).count();
        long carnetActive = active.stream()
            .filter(t -> {
                return productRepository.findById(t.getProductId())
                    .map(p -> p.getType() == com.ticketbus.common.domain.ProductType.CARNET)
                    .orElse(false);
            }).count();
        // Average usage rate of active carnets
        double carnetUsageRate = active.stream()
            .filter(t -> productRepository.findById(t.getProductId())
                .map(p -> p.getType() == com.ticketbus.common.domain.ProductType.CARNET)
                .orElse(false))
            .mapToDouble(t -> t.getMaxUsage() > 0 ? (double) t.getUsageCount() / t.getMaxUsage() * 100 : 0)
            .average().orElse(0);

        return ResponseEntity.ok(Map.of(
            "totalActive", totalActive,
            "passActive", passActive,
            "carnetActive", carnetActive,
            "carnetUsageRatePercent", Math.round(carnetUsageRate)
        ));
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseTicket(@RequestBody Map<String, Object> body) {
        try {
            Object userIdObj = body.get("userId");
            Object productIdObj = body.get("productId");
            String paymentMethod = (String) body.getOrDefault("paymentMethod", "WALLET");

            if (userIdObj == null || productIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId and productId are required"));
            }

            Long userId = userIdObj instanceof Integer ? ((Integer) userIdObj).longValue() : (Long) userIdObj;
            Long productId = productIdObj instanceof Integer ? ((Integer) productIdObj).longValue() : (Long) productIdObj;

            if (!paymentMethod.equals("WALLET") && !paymentMethod.equals("CASH")) {
                return ResponseEntity.badRequest().body(Map.of("error", "paymentMethod must be WALLET or CASH"));
            }

            Ticket ticket = ticketingService.purchaseTicket(userId, productId, paymentMethod);
            String productName = productRepository.findById(productId)
                .map(Product::getName).orElse("Unknown");
            return ResponseEntity.ok(ticketingService.toDto(ticket, productName));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error purchasing ticket", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicket(@PathVariable("id") Long id) {
        Ticket ticket = ticketingService.getTicket(id);
        String productName = productRepository.findById(ticket.getProductId())
            .map(Product::getName).orElse("Unknown");
        return ResponseEntity.ok(ticketingService.toDto(ticket, productName));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDto>> getTicketsByUser(@PathVariable("userId") Long userId) {
        List<Ticket> tickets = ticketingService.getTicketsByUser(userId);
        List<TicketDto> dtos = tickets.stream().map(t -> {
            String productName = productRepository.findById(t.getProductId())
                .map(Product::getName).orElse("Unknown");
            return ticketingService.toDto(t, productName);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TicketDto>> getRecentTickets() {
        List<Ticket> tickets = ticketRepository.findTop100ByOrderByCreatedAtDesc();
        List<TicketDto> dtos = tickets.stream().map(t -> {
            String productName = productRepository.findById(t.getProductId())
                .map(Product::getName).orElse("Unknown");
            return ticketingService.toDto(t, productName);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<?> revokeTicket(@PathVariable("id") Long id) {
        try {
            Ticket ticket = ticketingService.getTicket(id);
            if (ticket.getStatus() == TicketStatus.REVOKED || ticket.getStatus() == TicketStatus.CANCELLED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ticket already revoked/cancelled"));
            }
            ticket.setStatus(TicketStatus.REVOKED);
            ticketRepository.save(ticket);
            String productName = productRepository.findById(ticket.getProductId())
                .map(Product::getName).orElse("Unknown");
            return ResponseEntity.ok(ticketingService.toDto(ticket, productName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", qrSigningService.getPublicKeyBase64()));
    }

    /**
     * Dynamic QR code endpoint. Called every ~30s by the mobile app.
     * Returns a fresh QR payload with a rotating nonce + new RSA signature.
     */
    @GetMapping("/{id}/qr-live")
    public ResponseEntity<?> getLiveQr(@PathVariable("id") Long id) {
        try {
            Ticket ticket = ticketingService.getTicket(id);
            if (ticket.getSecret() == null || ticket.getSecret().isBlank()) {
                // Fallback: return static QR
                String productName = productRepository.findById(ticket.getProductId())
                    .map(Product::getName).orElse("Unknown");
                return ResponseEntity.ok(Map.of(
                    "qrPayload", ticketingService.toDto(ticket, productName).getQrPayload(),
                    "rotationSeconds", 0,
                    "dynamic", false
                ));
            }
            String livePayload = dynamicQrService.buildLiveQrPayload(ticket);
            return ResponseEntity.ok(Map.of(
                "qrPayload", livePayload,
                "rotationSeconds", dynamicQrService.getRotationSeconds(),
                "dynamic", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Génère un PDF imprimable pour le ticket avec QR code
     */
    @GetMapping("/{id}/print")
    public ResponseEntity<byte[]> printTicket(@PathVariable("id") Long id) {
        try {
            byte[] pdfBytes = ticketPrintService.generateTicketPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket-" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating PDF for ticket {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Génère une image QR code pour le ticket
     */
    @GetMapping("/{id}/qr-image")
    public ResponseEntity<byte[]> getTicketQrImage(@PathVariable("id") Long id,
                                                   @RequestParam(defaultValue = "300") int size) {
        try {
            Ticket ticket = ticketingService.getTicket(id);
            String productName = productRepository.findById(ticket.getProductId())
                .map(Product::getName).orElse("Unknown");
            String qrPayload = ticketingService.toDto(ticket, productName).getQrPayload();

            byte[] qrBytes = ticketPrintService.generateQrCodeImage(qrPayload, size, size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrBytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(qrBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating QR image for ticket {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Valide un QR code scanné
     */
    @PostMapping("/validate-qr")
    public ResponseEntity<Map<String, Object>> validateQr(@RequestBody Map<String, String> request) {
        try {
            String qrPayload = request.get("qrPayload");
            if (qrPayload == null || qrPayload.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "reason", "QR code payload manquant"
                ));
            }

            QrValidationService.ValidationResult result = qrValidationService.validateQrCode(qrPayload);

            Map<String, Object> response = Map.of(
                "valid", result.valid,
                "reason", result.reason,
                "ticketInfo", result.ticketInfo != null ? result.ticketInfo : "",
                "ticketId", result.ticket != null ? result.ticket.getId() : 0,
                "usageCount", result.ticket != null ? result.ticket.getUsageCount() : 0,
                "maxUsage", result.ticket != null ? result.ticket.getMaxUsage() : 0
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating QR code", e);
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "reason", "Erreur lors de la validation"
            ));
        }
    }

    /**
     * Utilise un ticket (incrémente le compteur d'usage)
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<Map<String, Object>> useTicket(@PathVariable("id") Long id) {
        try {
            // D'abord valider le ticket
            Ticket ticket = ticketingService.getTicket(id);
            String productName = productRepository.findById(ticket.getProductId())
                .map(Product::getName).orElse("Unknown");
            String qrPayload = ticketingService.toDto(ticket, productName).getQrPayload();

            QrValidationService.ValidationResult validation = qrValidationService.validateQrCode(qrPayload);
            if (!validation.valid) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "reason", validation.reason
                ));
            }

            // Utiliser le ticket
            boolean success = qrValidationService.useTicket(id);
            if (success) {
                // Récupérer le ticket mis à jour
                Ticket updatedTicket = ticketingService.getTicket(id);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ticket utilisé avec succès",
                    "usageCount", updatedTicket.getUsageCount(),
                    "maxUsage", updatedTicket.getMaxUsage(),
                    "status", updatedTicket.getStatus().toString()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "reason", "Impossible d'utiliser le ticket"
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "reason", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error using ticket {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "reason", "Erreur interne"
            ));
        }
    }
}
