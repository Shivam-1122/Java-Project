package com.ticketreservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Model representing a scheduled Bus or Train departure.
 */
public class Trip {
    private int id;
    private VehicleType vehicleType;
    private String vehicleNumber; // e.g., "12951", "KA-01-F-9921"
    private String vehicleName;   // e.g., "Mumbai Rajdhani Express", "Vande Bharat Express", "GreenLine Volvo Multi-Axle"
    private String travelClass;   // e.g., "AC 3-Tier (3A)", "Executive Sleeper", "AC Seater 2+2"
    private Route route;
    private LocalDate journeyDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private double baseFare;
    private int totalSeats;
    private int availableSeats;
    private double rating;

    public Trip(int id, VehicleType vehicleType, String vehicleNumber, String vehicleName,
                String travelClass, Route route, LocalDate journeyDate,
                LocalTime departureTime, LocalTime arrivalTime, double baseFare,
                int totalSeats, int availableSeats, double rating) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.vehicleName = vehicleName;
        this.travelClass = travelClass;
        this.route = route;
        this.journeyDate = journeyDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.baseFare = baseFare;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public Route getRoute() {
        return route;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getRating() {
        return rating;
    }

    public String getScheduleDisplay() {
        return departureTime + " -> " + arrivalTime + " (" + route.getEstimatedDuration() + ")";
    }

    @Override
    public String toString() {
        return vehicleName + " (" + vehicleNumber + ") - " + travelClass;
    }
}
