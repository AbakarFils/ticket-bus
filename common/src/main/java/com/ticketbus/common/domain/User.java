package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keycloakId;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    private String firstName;

    private String lastName;

    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.CLIENT;

    private Long operatorId;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
