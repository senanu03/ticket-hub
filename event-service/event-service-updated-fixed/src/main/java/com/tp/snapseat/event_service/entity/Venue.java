package com.tp.snapseat.event_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String chain;

    @Column(nullable = false)
    private String city;

    private String address;

    private String pincode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(nullable = false)
    private Boolean active;
}
