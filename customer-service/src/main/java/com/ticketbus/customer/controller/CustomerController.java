package com.ticketbus.customer.controller;

import com.ticketbus.common.domain.TripHistory;
import com.ticketbus.common.domain.User;
import com.ticketbus.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<User>> getRecentCustomers() {
        return ResponseEntity.ok(customerService.getRecentCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getByEmail(email));
    }

    @PostMapping
    public ResponseEntity<User> createCustomer(@RequestBody User user) {
        return ResponseEntity.ok(customerService.createCustomer(user));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<User> updateProfile(@PathVariable Long id, @RequestBody User updates) {
        return ResponseEntity.ok(customerService.updateProfile(id, updates));
    }

    @GetMapping("/{id}/trips")
    public ResponseEntity<List<TripHistory>> getTripHistory(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getTripHistory(id));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<User>> getByOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(customerService.getCustomersByOperator(operatorId));
    }
}

