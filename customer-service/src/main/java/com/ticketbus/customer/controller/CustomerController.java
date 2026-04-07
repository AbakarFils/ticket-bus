package com.ticketbus.customer.controller;

<<<<<<< HEAD
import com.ticketbus.customer.dto.CustomerDto;
import com.ticketbus.customer.dto.UpdateProfileRequest;
=======
import com.ticketbus.common.domain.TripHistory;
import com.ticketbus.common.domain.User;
>>>>>>> 6a79295c (phase 3)
import com.ticketbus.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
<<<<<<< HEAD
import java.util.Map;
=======
>>>>>>> 6a79295c (phase 3)

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
<<<<<<< HEAD
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(customerService.getProfile(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        try {
            return ResponseEntity.ok(customerService.updateProfile(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        try {
            customerService.deactivate(id);
            return ResponseEntity.ok(Map.of("message", "Customer deactivated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/trips")
    public ResponseEntity<?> getTripHistory(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getTripHistory(id));
    }
}
=======
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

>>>>>>> 6a79295c (phase 3)
