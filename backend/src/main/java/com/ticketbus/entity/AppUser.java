package com.ticketbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder.Default
    private boolean enabled = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
