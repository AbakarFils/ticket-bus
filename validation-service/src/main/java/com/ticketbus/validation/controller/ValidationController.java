package com.ticketbus.validation.controller;

import com.ticketbus.common.domain.ValidationEvent;
import com.ticketbus.validation.dto.TicketScanRequest;
import com.ticketbus.validation.dto.ValidationResponse;
import com.ticketbus.validation.repository.ValidationEventRepository;
import com.ticketbus.validation.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;
    private final ValidationEventRepository validationEventRepository;

    @PostMapping
    public ResponseEntity<ValidationResponse> validate(@RequestBody TicketScanRequest request) {
        return ResponseEntity.ok(validationService.validate(request));
    }

    @GetMapping("/events")
    public ResponseEntity<List<ValidationEvent>> getRecentEvents() {
        return ResponseEntity.ok(validationEventRepository.findTop100ByOrderByTimestampDesc());
    }

    @GetMapping("/events/ticket/{ticketId}")
    public ResponseEntity<List<ValidationEvent>> getEventsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(validationEventRepository.findByTicketId(ticketId));
    }
}
