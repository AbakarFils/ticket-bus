package com.ticketbus.ticketing.controller;

import com.ticketbus.common.domain.Product;
import com.ticketbus.common.domain.Ticket;
import com.ticketbus.common.dto.TicketDto;
import com.ticketbus.ticketing.repository.ProductRepository;
import com.ticketbus.ticketing.service.QrSigningService;
import com.ticketbus.ticketing.service.TicketingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final QrSigningService qrSigningService;

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseTicket(@RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            Long productId = body.get("productId");
            Ticket ticket = ticketingService.purchaseTicket(userId, productId);
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
    public ResponseEntity<TicketDto> getTicket(@PathVariable Long id) {
        Ticket ticket = ticketingService.getTicket(id);
        String productName = productRepository.findById(ticket.getProductId())
            .map(Product::getName).orElse("Unknown");
        return ResponseEntity.ok(ticketingService.toDto(ticket, productName));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDto>> getTicketsByUser(@PathVariable Long userId) {
        List<Ticket> tickets = ticketingService.getTicketsByUser(userId);
        List<TicketDto> dtos = tickets.stream().map(t -> {
            String productName = productRepository.findById(t.getProductId())
                .map(Product::getName).orElse("Unknown");
            return ticketingService.toDto(t, productName);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", qrSigningService.getPublicKeyBase64()));
    }
}
