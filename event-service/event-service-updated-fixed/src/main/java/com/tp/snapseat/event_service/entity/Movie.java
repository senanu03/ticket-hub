package com.tp.snapseat.event_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String language;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    private String genre;

    private String certificate;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(nullable = false)
    private Boolean active;
}
