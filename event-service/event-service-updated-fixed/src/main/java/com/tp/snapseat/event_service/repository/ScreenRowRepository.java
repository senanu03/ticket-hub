package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.ScreenRow;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ScreenRowRepository extends JpaRepository<ScreenRow, Long> {
    boolean existsByScreenIdAndRowLabel(@NotNull(message = "Screen ID is required") Long aLong, @NotBlank(message = "Row label is required") String s);

    List<ScreenRow> findByScreenIdOrderByRowLabelAsc(Long screenId);

    List<ScreenRow> findByScreenId(Long screenId);
}
