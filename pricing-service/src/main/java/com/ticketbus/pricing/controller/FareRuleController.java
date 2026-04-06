package com.ticketbus.pricing.controller;

import com.ticketbus.common.domain.FareRule;
import com.ticketbus.pricing.repository.FareRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing/fare-rules")
@RequiredArgsConstructor
public class FareRuleController {

    private final FareRuleRepository fareRuleRepository;

    @GetMapping
    public ResponseEntity<List<FareRule>> getActiveFareRules() {
        return ResponseEntity.ok(fareRuleRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRule> getFareRule(@PathVariable Long id) {
        return fareRuleRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FareRule> createFareRule(@RequestBody FareRule fareRule) {
        return ResponseEntity.ok(fareRuleRepository.save(fareRule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FareRule> updateFareRule(@PathVariable Long id, @RequestBody FareRule fareRule) {
        if (!fareRuleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        fareRule.setId(id);
        return ResponseEntity.ok(fareRuleRepository.save(fareRule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFareRule(@PathVariable Long id) {
        if (!fareRuleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        fareRuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

