package com.ticketbus.audit.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long ticketId;

    private String reason;
    private String blacklistedBy;

    private LocalDateTime blacklistedAt;

    @PrePersist
    public void prePersist() {
        blacklistedAt = LocalDateTime.now();
    }
}
