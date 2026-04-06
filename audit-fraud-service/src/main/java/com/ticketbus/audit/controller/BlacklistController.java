package com.ticketbus.audit.controller;

import com.ticketbus.audit.domain.BlacklistedTicket;
import com.ticketbus.audit.dto.BlacklistRequest;
import com.ticketbus.audit.service.FraudAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fraud/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final FraudAlertService fraudAlertService;

    @PostMapping
    public ResponseEntity<?> blacklistTicket(@RequestBody BlacklistRequest request) {
        try {
            BlacklistedTicket bt = fraudAlertService.blacklistTicket(request);
            return ResponseEntity.ok(bt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Map<String, Boolean>> isBlacklisted(@PathVariable Long ticketId) {
        return ResponseEntity.ok(Map.of("blacklisted", fraudAlertService.isBlacklisted(ticketId)));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(@PathVariable Long ticketId) {
        fraudAlertService.removeFromBlacklist(ticketId);
        return ResponseEntity.ok(Map.of("message", "Ticket removed from blacklist"));
    }
}
