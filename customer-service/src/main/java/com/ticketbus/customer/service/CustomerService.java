package com.ticketbus.customer.service;

import com.ticketbus.common.domain.Role;
import com.ticketbus.common.domain.TripHistory;
import com.ticketbus.common.domain.User;
import com.ticketbus.customer.repository.TripHistoryRepository;
import com.ticketbus.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

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

