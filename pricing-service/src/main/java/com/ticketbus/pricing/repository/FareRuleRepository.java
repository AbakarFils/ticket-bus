package com.ticketbus.pricing.repository;

import com.ticketbus.common.domain.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    List<FareRule> findByActiveTrue();
    List<FareRule> findByProductId(Long productId);
    List<FareRule> findByZoneAndActiveTrue(String zone);
}

