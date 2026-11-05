package com.ticketreservation.model;

/**
 * Enumeration representing the type of vehicle.
 */
public enum VehicleType {
    TRAIN("Train", "[TRAIN]"),
    BUS("Bus", "[BUS]");

    private final String displayName;
    private final String icon;

    VehicleType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}
