package com.prayerroutine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a user's prayer routine request.
 * Contains all parameters needed to generate a personalized prayer routine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrayerRequest {

    private String religiousTradition;
    private String denomination;
    private Integer durationMinutes;
    private String timeOfDay;
    private String intentions;
    private String preferredScriptures;
    private String preferredPrayerStyles;
    private String tone;
    private String format;
    private String specialInstructions;

    // Helper methods for validation

    /**
     * Checks if the request has the minimum required information to generate a prayer routine.
     *
     * @return true if the request has at least a religious tradition and duration
     */
    public boolean hasMinimumRequiredInfo() {
        return religiousTradition != null && !religiousTradition.trim().isEmpty() &&
                durationMinutes != null && durationMinutes > 0;
    }

    /**
     * Creates a default prayer request with basic Christian settings.
     *
     * @return A default prayer request
     */
    public static PrayerRequest createDefault() {
        return PrayerRequest.builder()
                .religiousTradition("Christian")
                .durationMinutes(15)
                .format("detailed")
                .tone("reverent and inspirational")
                .build();
    }
}