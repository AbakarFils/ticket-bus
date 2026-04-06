package com.ticketbus.customer.repository;

import com.ticketbus.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByKeycloakId(String keycloakId);
    List<User> findByOperatorId(Long operatorId);
    List<User> findByRole(com.ticketbus.common.domain.Role role);
    List<User> findTop100ByOrderByCreatedAtDesc();
}

