package com.ticketbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String ticketNumber;

    private String passengerName;
    private String passengerEmail;
    private String routeName;
    private String departureLocation;
    private String arrivalLocation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(unique = true, nullable = false)
    private UUID nonce;

    @Builder.Default
    private int usageCount = 0;

    @Builder.Default
    private int maxUsageCount = 1;

    private LocalDateTime activationWindowStart;
    private LocalDateTime activationWindowEnd;

    @Column(columnDefinition = "TEXT")
    private String qrCodeData;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
