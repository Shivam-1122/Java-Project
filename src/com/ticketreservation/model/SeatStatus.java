package com.ticketreservation.model;

/**
 * Seat occupancy and availability status.
 */
public enum SeatStatus {
    AVAILABLE("Available", "#22C55E", "Ready to book"),
    SELECTED("Selected", "#3B82F6", "Selected by you"),
    OCCUPIED("Occupied", "#EF4444", "Already booked"),
    LADIES_ONLY("Ladies Special", "#A855F7", "Reserved for female passengers");

    private final String title;
    private final String hexColor;
    private final String description;

    SeatStatus(String title, String hexColor, String description) {
        this.title = title;
        this.hexColor = hexColor;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getDescription() {
        return description;
    }
}
