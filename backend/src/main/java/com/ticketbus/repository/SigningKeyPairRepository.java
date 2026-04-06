package com.ticketbus.repository;

import com.ticketbus.entity.SigningKeyPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SigningKeyPairRepository extends JpaRepository<SigningKeyPair, UUID> {
    Optional<SigningKeyPair> findByActiveTrue();
}
