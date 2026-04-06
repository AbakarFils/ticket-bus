package com.ticketbus.controller;

import com.ticketbus.dto.ValidationRequest;
import com.ticketbus.dto.ValidationResponse;
import com.ticketbus.entity.ValidationEvent;
import com.ticketbus.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/validate")
    public ValidationResponse validateTicket(@RequestBody ValidationRequest request) {
        return validationService.validateTicket(request);
    }

    @GetMapping("/events")
    public Page<ValidationEvent> listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return validationService.listEvents(PageRequest.of(page, size));
    }

    @GetMapping("/events/{ticketId}")
    public Page<ValidationEvent> getEventsForTicket(
            @PathVariable UUID ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return validationService.getEventsForTicket(ticketId, PageRequest.of(page, size));
    }
}
