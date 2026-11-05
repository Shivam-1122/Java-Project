package com.ticketreservation.model;

/**
 * Model representing a passenger travelling on a booked seat.
 */
public class Passenger {
    private int id;
    private String pnr;
    private String name;
    private int age;
    private String gender; // "Male", "Female", "Other"
    private String seatNumber;
    private String berthPreference;
    private double fare;

    public Passenger(String name, int age, String gender, String seatNumber, String berthPreference, double fare) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.seatNumber = seatNumber;
        this.berthPreference = berthPreference;
        this.fare = fare;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getBerthPreference() {
        return berthPreference;
    }

    public double getFare() {
        return fare;
    }

    @Override
    public String toString() {
        return name + " (" + age + " / " + gender + ") - Seat " + seatNumber;
    }
}
