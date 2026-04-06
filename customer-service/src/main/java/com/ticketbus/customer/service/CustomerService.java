package com.ticketbus.customer.service;

import com.ticketbus.common.domain.Role;
<<<<<<< HEAD
import com.ticketbus.customer.domain.Customer;
import com.ticketbus.customer.dto.*;
import com.ticketbus.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
=======
import com.ticketbus.common.domain.TripHistory;
import com.ticketbus.common.domain.User;
import com.ticketbus.customer.repository.TripHistoryRepository;
import com.ticketbus.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
>>>>>>> 6a79295c (phase 3)

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

<<<<<<< HEAD
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${ticketing.url}")
    private String ticketingUrl;

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Customer customer = Customer.builder()
            .email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .firstName(req.firstName())
            .lastName(req.lastName())
            .phone(req.phone())
            .role(Role.CLIENT)
            .build();
        customer = customerRepository.save(customer);
        String token = jwtService.generateToken(customer.getId(), customer.getEmail(), customer.getRole().name());
        log.info("Registered customer: {}", customer.getEmail());
        return new LoginResponse(token, customer.getId(), customer.getEmail(), customer.getRole().name());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Customer customer = customerRepository.findByEmail(req.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!customer.isActive()) {
            throw new IllegalStateException("Account is disabled");
        }
        String token = jwtService.generateToken(customer.getId(), customer.getEmail(), customer.getRole().name());
        return new LoginResponse(token, customer.getId(), customer.getEmail(), customer.getRole().name());
    }

    @Transactional(readOnly = true)
    public CustomerDto getProfile(Long id) {
        return toDto(findById(id));
    }

    @Transactional
    public CustomerDto updateProfile(Long id, UpdateProfileRequest req) {
        Customer customer = findById(id);
        if (req.firstName() != null) customer.setFirstName(req.firstName());
        if (req.lastName() != null) customer.setLastName(req.lastName());
        if (req.phone() != null) customer.setPhone(req.phone());
        return toDto(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(Long id) {
        Customer customer = findById(id);
        customer.setActive(false);
        customerRepository.save(customer);
        log.info("Deactivated customer: {}", id);
    }

    public Object getTripHistory(Long userId) {
        try {
            return restTemplate.getForObject(ticketingUrl + "/api/tickets/user/" + userId, Object.class);
        } catch (Exception e) {
            log.warn("Could not fetch trip history for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    private CustomerDto toDto(Customer c) {
        return new CustomerDto(c.getId(), c.getEmail(), c.getFirstName(), c.getLastName(),
            c.getPhone(), c.getRole().name(), c.isActive(), c.getCreatedAt());
    }
}
=======
    private final UserRepository userRepository;
    private final TripHistoryRepository tripHistoryRepository;

    public List<User> getRecentCustomers() {
        return userRepository.findTop100ByOrderByCreatedAtDesc();
    }

    public User getCustomer(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with email: " + email));
    }

    @Transactional
    public User createCustomer(User user) {
        if (user.getRole() == null) user.setRole(Role.CLIENT);
        log.info("Creating customer: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long id, User updates) {
        User user = getCustomer(id);
        if (updates.getFirstName() != null) user.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) user.setLastName(updates.getLastName());
        if (updates.getPhone() != null) user.setPhone(updates.getPhone());
        if (updates.getAddress() != null) user.setAddress(updates.getAddress());
        log.info("Updated profile for customer {}", id);
        return userRepository.save(user);
    }

    public List<TripHistory> getTripHistory(Long userId) {
        return tripHistoryRepository.findTop50ByUserIdOrderByTimestampDesc(userId);
    }

    public List<User> getCustomersByOperator(Long operatorId) {
        return userRepository.findByOperatorId(operatorId);
    }
}

>>>>>>> 6a79295c (phase 3)
