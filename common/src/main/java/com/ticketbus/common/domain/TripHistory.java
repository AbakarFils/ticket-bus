
package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long ticketId;

    private Long validationEventId;

    private String boardingZone;

    private String alightingZone;

    private Long operatorId;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}

