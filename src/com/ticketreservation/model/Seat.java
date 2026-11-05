package com.ticketreservation.model;

/**
 * Model representing an individual seat in a bus or train.
 */
public class Seat {
    private int id;
    private int tripId;
    private String seatNumber; // e.g., "1A", "LB-12", "S-24"
    private int rowIndex;
    private int colIndex;
    private SeatType seatType;
    private SeatStatus status;
    private SeatStatus baseStatus; // Remembers whether seat is LADIES_ONLY or AVAILABLE
    private double price;
    private String deckOrCoach; // e.g. "Lower Deck", "Upper Deck", "Coach B1"
    private String bookedByPnr;

    public Seat(int id, int tripId, String seatNumber, int rowIndex, int colIndex,
                SeatType seatType, SeatStatus status, double price, String deckOrCoach) {
        this.id = id;
        this.tripId = tripId;
        this.seatNumber = seatNumber;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.seatType = seatType;
        this.status = status;
        this.baseStatus = (status == SeatStatus.LADIES_ONLY) ? SeatStatus.LADIES_ONLY : SeatStatus.AVAILABLE;
        this.price = price;
        this.deckOrCoach = deckOrCoach;
        this.bookedByPnr = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
        if (status == SeatStatus.LADIES_ONLY) {
            this.baseStatus = SeatStatus.LADIES_ONLY;
        }
    }

    public SeatStatus getBaseStatus() {
        return (baseStatus != null) ? baseStatus : SeatStatus.AVAILABLE;
    }

    public void setBaseStatus(SeatStatus baseStatus) {
        this.baseStatus = baseStatus;
    }

    public boolean isLadiesOnly() {
        return baseStatus == SeatStatus.LADIES_ONLY || status == SeatStatus.LADIES_ONLY;
    }

    /**
     * Reverts selected seat back to its original status (LADIES_ONLY or AVAILABLE).
     */
    public void deselect() {
        if (this.status == SeatStatus.SELECTED) {
            this.status = getBaseStatus();
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDeckOrCoach() {
        return deckOrCoach;
    }

    public void setDeckOrCoach(String deckOrCoach) {
        this.deckOrCoach = deckOrCoach;
    }

    public String getBookedByPnr() {
        return bookedByPnr;
    }

    public void setBookedByPnr(String bookedByPnr) {
        this.bookedByPnr = bookedByPnr;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE || status == SeatStatus.LADIES_ONLY;
    }

    public boolean isOccupied() {
        return status == SeatStatus.OCCUPIED;
    }

    public boolean isSelected() {
        return status == SeatStatus.SELECTED;
    }

    @Override
    public String toString() {
        return "Seat " + seatNumber + " [" + seatType.getLabel() + "] - Rs. " + (int)price + " (" + status.getTitle() + ")";
    }
}
