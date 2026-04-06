package com.ticketbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signing_key_pairs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigningKeyPair {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String publicKey;

    @Column(columnDefinition = "TEXT")
    private String privateKey;

    @Builder.Default
    private String algorithm = "RSA";

    @Builder.Default
    private int keySize = 2048;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime rotatedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
