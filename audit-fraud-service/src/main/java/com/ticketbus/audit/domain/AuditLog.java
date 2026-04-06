package com.ticketbus.audit.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityType;
    private String entityId;
    private String action;
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }
}
