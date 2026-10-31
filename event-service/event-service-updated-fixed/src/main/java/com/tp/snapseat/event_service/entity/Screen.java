package com.tp.snapseat.event_service.entity;

import com.tp.snapseat.event_service.enums.ScreenType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "screens")
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false)
    private ScreenType screenType;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Boolean active;
}
