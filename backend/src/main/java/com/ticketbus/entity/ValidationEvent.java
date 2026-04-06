package com.ticketbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    private String validatorDeviceId;
    private LocalDateTime validationTime;
    private String location;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ValidationStatus status;

    private String rejectionReason;

    @Builder.Default
    private boolean synced = false;

    private LocalDateTime syncedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
