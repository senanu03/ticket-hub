package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    Page<Venue> findByActiveTrue(Pageable pageable);

    Page<Venue> findByCityIgnoreCaseAndActiveTrue(String city, Pageable pageable);


}
