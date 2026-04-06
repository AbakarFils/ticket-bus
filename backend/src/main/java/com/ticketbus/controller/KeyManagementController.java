package com.ticketbus.controller;

import com.ticketbus.dto.PublicKeyDTO;
import com.ticketbus.service.CryptoService;
import com.ticketbus.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class KeyManagementController {

    private final CryptoService cryptoService;
    private final SyncService syncService;

    @PostMapping("/rotate")
    public void rotateKeys() {
        cryptoService.rotateKeys();
    }

    @GetMapping("/current")
    public PublicKeyDTO getCurrentKey() {
        return syncService.getPublicKeyUpdate();
    }
}
