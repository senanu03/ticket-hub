package com.tp.snapseat.event_service.entity;

import com.tp.snapseat.event_service.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "show_seats", uniqueConstraints = {
                @UniqueConstraint(name = "uk_show_seat", columnNames = {"show_id", "seat_id"})
        }
)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Version
    @Column(nullable = false)
    private Long version;

}
