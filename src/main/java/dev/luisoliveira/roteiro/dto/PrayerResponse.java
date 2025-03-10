package com.prayerroutine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model class representing the response containing a generated prayer routine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrayerResponse {

    // The generated prayer routine content
    private String prayerRoutine;

    // Duration in minutes
    private Integer durationMinutes;

    // Religious tradition used
    private String religiousTradition;

    // Error message, if any
    private String error;

    // Timestamp of generation
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    /**
     * Checks if the response contains an error.
     *
     * @return true if there is an error message
     */
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    /**
     * Checks if the response contains a valid prayer routine.
     *
     * @return true if there is a non-empty prayer routine
     */
    public boolean hasValidContent() {
        return prayerRoutine != null && !prayerRoutine.isEmpty();
    }
}