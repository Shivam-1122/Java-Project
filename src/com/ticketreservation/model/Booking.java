package com.ticketreservation.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a ticket reservation booking with PNR.
 */
public class Booking {
    private String pnr; // e.g., "PNR-849201"
    private int tripId;
    private VehicleType vehicleType;
    private String vehicleName;
    private String vehicleNumber;
    private String travelClass;
    private String routeName;
    private String journeyDate;
    private String departureTime;
    private String arrivalTime;
    private String contactEmail;
    private String contactPhone;
    private double totalFare;
    private String status; // "CONFIRMED", "CANCELLED"
    private LocalDateTime bookingTimestamp;
    private List<Passenger> passengers;

    public Booking(String pnr, int tripId, VehicleType vehicleType, String vehicleName,
                   String vehicleNumber, String travelClass, String routeName,
                   String journeyDate, String departureTime, String arrivalTime,
                   String contactEmail, String contactPhone, double totalFare,
                   String status, LocalDateTime bookingTimestamp) {
        this.pnr = pnr;
        this.tripId = tripId;
        this.vehicleType = vehicleType;
        this.vehicleName = vehicleName;
        this.vehicleNumber = vehicleNumber;
        this.travelClass = travelClass;
        this.routeName = routeName;
        this.journeyDate = journeyDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.totalFare = totalFare;
        this.status = status;
        this.bookingTimestamp = bookingTimestamp;
        this.passengers = new ArrayList<>();
    }

    public String getPnr() {
        return pnr;
    }

    public int getTripId() {
        return tripId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getJourneyDate() {
        return journeyDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBookingTimestamp() {
        return bookingTimestamp;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public void addPassenger(Passenger passenger) {
        this.passengers.add(passenger);
    }

    public String getSeatNumbersSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passengers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(passengers.get(i).getSeatNumber());
        }
        return sb.toString();
    }

    public String getPassengerNamesSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passengers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(passengers.get(i).getName());
        }
        return sb.toString();
    }
}
