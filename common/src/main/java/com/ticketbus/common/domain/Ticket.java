package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Column(unique = true, nullable = false)
    private String nonce;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    private String signature;

    @Builder.Default
    private int usageCount = 0;

    private int maxUsage;

    private String zone;

    private Long operatorId;

    /** Secret for dynamic QR rotation (TOTP-like), never exposed in QR */
    private String secret;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TicketStatus.ACTIVE;
        }
    }
}
