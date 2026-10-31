package com.tp.snapseat.event_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovieRequestDTO(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Language is required")
        String language,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be greater than zero")
        Integer durationMin,

        String genre,

        String certificate,

        String posterUrl,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}
