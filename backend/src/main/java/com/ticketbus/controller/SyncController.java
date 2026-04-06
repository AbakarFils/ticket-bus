package com.ticketbus.controller;

import com.ticketbus.dto.*;
import com.ticketbus.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/upload")
    public SyncResponse syncValidationEvents(@RequestBody SyncRequest request) {
        return syncService.syncValidationEvents(request);
    }

    @GetMapping("/blacklist")
    public List<BlacklistDTO> getBlacklistUpdates(@RequestParam(required = false) String since) {
        LocalDateTime sinceTime = since != null ? LocalDateTime.parse(since) : null;
        return syncService.getBlacklistUpdates(sinceTime);
    }

    @GetMapping("/public-key")
    public PublicKeyDTO getPublicKeyUpdate() {
        return syncService.getPublicKeyUpdate();
    }
}
