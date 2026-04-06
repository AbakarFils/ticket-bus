package com.ticketbus.pricing.controller;

import com.ticketbus.common.domain.Zone;
import com.ticketbus.pricing.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneRepository zoneRepository;

    @GetMapping
    public ResponseEntity<List<Zone>> getActiveZones() {
        return ResponseEntity.ok(zoneRepository.findByActiveTrue());
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<Zone>> getByOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(zoneRepository.findByOperatorId(operatorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZone(@PathVariable Long id) {
        return zoneRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.ok(zoneRepository.save(zone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Long id, @RequestBody Zone zone) {
        if (!zoneRepository.existsById(id)) return ResponseEntity.notFound().build();
        zone.setId(id);
        return ResponseEntity.ok(zoneRepository.save(zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        if (!zoneRepository.existsById(id)) return ResponseEntity.notFound().build();
        zoneRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

