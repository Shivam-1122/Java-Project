package com.ticketreservation.model;

/**
 * Model representing a travel route between two stations/cities.
 */
public class Route {
    private int id;
    private String origin;
    private String destination;
    private double distanceKm;
    private String estimatedDuration; // e.g. "5 hrs 30 mins"

    public Route(int id, String origin, String destination, double distanceKm, String estimatedDuration) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.estimatedDuration = estimatedDuration;
    }

    public int getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public String getRouteName() {
        return origin + " -> " + destination;
    }

    @Override
    public String toString() {
        return getRouteName() + " (" + distanceKm + " km, " + estimatedDuration + ")";
    }
}
