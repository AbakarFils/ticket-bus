package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    private String terminalId;

    private String location;

    private LocalDateTime timestamp;

    private boolean offline;

    @Enumerated(EnumType.STRING)
    private ValidationResult result;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
