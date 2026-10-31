package com.tp.snapseat.event_service.entity;


import com.tp.snapseat.event_service.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_screen_seat_label", columnNames = {"screen_id", "seat_label"})
}
)
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_row_id", nullable = false)
    private ScreenRow screenRow;

    @Column(name = "seat_label", nullable = false)
    private String seatLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private Boolean active;
}
