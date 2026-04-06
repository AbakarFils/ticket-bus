package com.ticketbus.pricing.controller;

import com.ticketbus.common.domain.Operator;
import com.ticketbus.pricing.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorRepository operatorRepository;

    @GetMapping
    public ResponseEntity<List<Operator>> getActiveOperators() {
        return ResponseEntity.ok(operatorRepository.findByActiveTrue());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Operator>> getAllOperators() {
        return ResponseEntity.ok(operatorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operator> getOperator(@PathVariable Long id) {
        return operatorRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Operator> createOperator(@RequestBody Operator operator) {
        return ResponseEntity.ok(operatorRepository.save(operator));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Operator> updateOperator(@PathVariable Long id, @RequestBody Operator operator) {
        if (!operatorRepository.existsById(id)) return ResponseEntity.notFound().build();
        operator.setId(id);
        return ResponseEntity.ok(operatorRepository.save(operator));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperator(@PathVariable Long id) {
        if (!operatorRepository.existsById(id)) return ResponseEntity.notFound().build();
        operatorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

