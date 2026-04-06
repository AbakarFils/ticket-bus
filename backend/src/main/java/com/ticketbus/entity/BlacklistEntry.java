package com.ticketbus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blacklist_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String ticketNumber;
    private String reason;
    private LocalDateTime blacklistedAt;

    @Builder.Default
    private boolean active = true;
}
