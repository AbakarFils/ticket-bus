package com.ticketbus.common.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private Long operatorId;

    private String description;

    @Builder.Default
    private boolean active = true;
}

