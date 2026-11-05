-- ====================================================================
-- Bus & Train Ticket Reservation System Database Schema
-- Compatible with MySQL 8.0+ and MySQL Workbench
-- ====================================================================

-- 1. Create Database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS ticket_reservation_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ticket_reservation_db;

-- 2. Routes Table
CREATE TABLE IF NOT EXISTS routes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    distance_km DOUBLE NOT NULL,
    duration VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Trips Table (Scheduled departures for Trains & Buses)
CREATE TABLE IF NOT EXISTS trips (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type VARCHAR(20) NOT NULL, -- 'TRAIN' or 'BUS'
    vehicle_number VARCHAR(50) NOT NULL,
    vehicle_name VARCHAR(150) NOT NULL,
    travel_class VARCHAR(80) NOT NULL,
    route_id INT NOT NULL,
    journey_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    base_fare DOUBLE NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    rating DOUBLE DEFAULT 4.5,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);

-- 4. Seats Table (Matrix Grid Seats)
CREATE TABLE IF NOT EXISTS seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    row_index INT NOT NULL,
    col_index INT NOT NULL,
    seat_type VARCHAR(50) NOT NULL, -- 'WINDOW', 'AISLE', 'LOWER_BERTH', etc.
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'OCCUPIED', 'SELECTED', 'LADIES_ONLY'
    price DOUBLE NOT NULL,
    deck_or_coach VARCHAR(50) DEFAULT 'Main Deck',
    booked_by_pnr VARCHAR(50) DEFAULT NULL,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- 5. Bookings Table (PNR Master)
CREATE TABLE IF NOT EXISTS bookings (
    pnr VARCHAR(50) PRIMARY KEY,
    trip_id INT NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    vehicle_name VARCHAR(150) NOT NULL,
    vehicle_number VARCHAR(50) NOT NULL,
    travel_class VARCHAR(80) NOT NULL,
    route_name VARCHAR(200) NOT NULL,
    journey_date VARCHAR(50) NOT NULL,
    departure_time VARCHAR(20) NOT NULL,
    arrival_time VARCHAR(20) NOT NULL,
    contact_email VARCHAR(120),
    contact_phone VARCHAR(50),
    total_fare DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- 6. Booking Passengers Table (Itemized passenger per seat)
CREATE TABLE IF NOT EXISTS booking_passengers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pnr VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    berth_preference VARCHAR(50),
    fare DOUBLE NOT NULL,
    FOREIGN KEY (pnr) REFERENCES bookings(pnr) ON DELETE CASCADE
);

-- ====================================================================
-- Sample Seed Data (Routes)
-- ====================================================================
INSERT IGNORE INTO routes (id, origin, destination, distance_km, duration) VALUES
(1, 'New Delhi (NDLS)', 'Mumbai Central (MMCT)', 1386, '15 hrs 40 mins'),
(2, 'Bengaluru (SBC)', 'Chennai Central (MAS)', 359, '4 hrs 20 mins'),
(3, 'Hyderabad (HYB)', 'Pune Junction (PUNE)', 597, '8 hrs 45 mins'),
(4, 'Kolkata (HWH)', 'Patna Junction (PNBE)', 535, '7 hrs 15 mins'),
(5, 'Mumbai (BCT)', 'Goa (MAO)', 580, '9 hrs 10 mins');

-- ====================================================================
-- Sample Seed Data (Trips: Trains & Buses)
-- ====================================================================
INSERT IGNORE INTO trips (id, vehicle_type, vehicle_number, vehicle_name, travel_class, route_id, journey_date, departure_time, arrival_time, base_fare, total_seats, available_seats, rating) VALUES
(1, 'TRAIN', '12952', 'Mumbai Rajdhani Express', 'AC 3-Tier (3A)', 1, CURDATE(), '16:55:00', '08:35:00', 2150.00, 48, 38, 4.8),
(2, 'TRAIN', '20608', 'Vande Bharat Express', 'Executive Chair (EC)', 2, CURDATE(), '05:45:00', '10:05:00', 1450.00, 40, 31, 4.9),
(3, 'TRAIN', '12009', 'Shatabdi Express', 'AC Chair Car (CC)', 1, CURDATE(), '06:20:00', '12:45:00', 1120.00, 48, 42, 4.6),
(4, 'BUS', 'KA-01-F-8890', 'GreenLine Multi-Axle Volvo', 'AC Semi-Sleeper (2+2)', 2, CURDATE(), '22:30:00', '05:30:00', 850.00, 36, 26, 4.7),
(5, 'BUS', 'MH-04-AX-5512', 'Orange Travels Dreamliner', 'AC Sleeper (2+1)', 3, CURDATE(), '20:15:00', '05:45:00', 1250.00, 30, 22, 4.5),
(6, 'BUS', 'GA-02-B-3341', 'Paulo Luxury Royal Cruiser', 'Multi-Axle AC Sleeper', 5, CURDATE(), '21:00:00', '06:30:00', 1100.00, 32, 25, 4.6);

-- Sample Bookings for demonstration
INSERT IGNORE INTO bookings (pnr, trip_id, vehicle_type, vehicle_name, vehicle_number, travel_class, route_name, journey_date, departure_time, arrival_time, contact_email, contact_phone, total_fare, status) VALUES
('PNR-928174', 1, 'TRAIN', 'Mumbai Rajdhani Express', '12952', 'AC 3-Tier (3A)', 'New Delhi (NDLS) ➔ Mumbai Central (MMCT)', DATE_FORMAT(CURDATE(), '%Y-%m-%d'), '16:55:00', '08:35:00', 'arjun.sharma@example.com', '+91 98765 43210', 4300.00, 'CONFIRMED');

INSERT IGNORE INTO booking_passengers (pnr, name, age, gender, seat_number, berth_preference, fare) VALUES
('PNR-928174', 'Arjun Sharma', 32, 'Male', '1A', 'Lower Berth (LB)', 2150.00),
('PNR-928174', 'Pooja Sharma', 29, 'Female', '1B', 'Middle Berth (MB)', 2150.00);

-- Query to verify live seat matrix status
-- SELECT trip_id, seat_number, status, price, seat_type FROM seats WHERE trip_id = 1 ORDER BY row_index, col_index;
