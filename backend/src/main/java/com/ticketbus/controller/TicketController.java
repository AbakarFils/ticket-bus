package com.ticketbus.controller;

import com.ticketbus.dto.TicketRequest;
import com.ticketbus.dto.TicketResponse;
import com.ticketbus.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@RequestBody TicketRequest request) {
        return ticketService.createTicket(request);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable UUID id) {
        return ticketService.getTicket(id);
    }

    @GetMapping
    public Page<TicketResponse> listTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ticketService.listTickets(PageRequest.of(page, size));
    }

    @PutMapping("/{id}/cancel")
    public TicketResponse cancelTicket(@PathVariable UUID id) {
        return ticketService.cancelTicket(id);
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getQrCode(@PathVariable UUID id) {
        TicketResponse ticket = ticketService.getTicket(id);
        byte[] imageBytes = ticketService.generateQrCodeImage(ticket.qrCodeData());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return ResponseEntity.ok().headers(headers).body(imageBytes);
    }
}
