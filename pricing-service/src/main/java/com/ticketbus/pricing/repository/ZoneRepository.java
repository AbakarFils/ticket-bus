package com.ticketbus.pricing.repository;

import com.ticketbus.common.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByCode(String code);
    List<Zone> findByOperatorId(Long operatorId);
    List<Zone> findByActiveTrue();
}

