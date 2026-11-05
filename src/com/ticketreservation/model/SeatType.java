package com.ticketreservation.model;

/**
 * Seat categories for buses and trains.
 */
public enum SeatType {
    WINDOW("Window", "[W]"),
    AISLE("Aisle", "[A]"),
    MIDDLE("Middle", "[M]"),
    LOWER_BERTH("Lower Berth (LB)", "[LB]"),
    MIDDLE_BERTH("Middle Berth (MB)", "[MB]"),
    UPPER_BERTH("Upper Berth (UB)", "[UB]"),
    SIDE_LOWER("Side Lower (SL)", "[SL]"),
    SIDE_UPPER("Side Upper (SU)", "[SU]");

    private final String label;
    private final String symbol;

    SeatType(String label, String symbol) {
        this.label = label;
        this.symbol = symbol;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isWindow() {
        return this == WINDOW;
    }

    public boolean isBerth() {
        return this == LOWER_BERTH || this == MIDDLE_BERTH || this == UPPER_BERTH ||
               this == SIDE_LOWER || this == SIDE_UPPER;
    }
}
