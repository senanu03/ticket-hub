package com.tp.snapseat.event_service.entity;

import com.tp.snapseat.event_service.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "show_prices", uniqueConstraints = {
                @UniqueConstraint(name = "uk_show_seat_type", columnNames = {"show_id", "seat_type"})
        }
)
public class ShowPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

}
