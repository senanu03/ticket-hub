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
@Table(name = "screen_rows", uniqueConstraints = {
                @UniqueConstraint(name = "uk_screen_row_label", columnNames = {"screen_id", "row_label"})
        }
)
public class ScreenRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

}
