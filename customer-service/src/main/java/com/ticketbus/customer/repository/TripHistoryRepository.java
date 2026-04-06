package com.ticketbus.customer.repository;

import com.ticketbus.common.domain.TripHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripHistoryRepository extends JpaRepository<TripHistory, Long> {
    List<TripHistory> findByUserIdOrderByTimestampDesc(Long userId);
    List<TripHistory> findTop50ByUserIdOrderByTimestampDesc(Long userId);
}

