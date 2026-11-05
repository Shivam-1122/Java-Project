package com.ticketreservation.database;

import com.ticketreservation.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * High-performance JDBC Database Manager supporting both MySQL and SQLite.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private final DatabaseConfig config;
    private Connection currentConnection;
    private String connectionStatusMessage = "Not connected";
    private boolean isConnected = false;

    private DatabaseManager() {
        this.config = new DatabaseConfig();
        init();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public String getConnectionStatusMessage() {
        return connectionStatusMessage;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public synchronized void init() {
        closeQuietly(currentConnection);

        if (config.getDbType() == DatabaseConfig.DatabaseType.MYSQL) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Try connecting to MySQL server directly first to ensure database exists
                String serverUrl = config.getMysqlUrl(false);
                try (Connection serverConn = DriverManager.getConnection(serverUrl, config.getMysqlUser(), config.getMysqlPassword());
                     Statement stmt = serverConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + config.getMysqlDatabase() +
                            " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }

                // Connect to the specific database
                currentConnection = DriverManager.getConnection(config.getMysqlUrl(true), config.getMysqlUser(), config.getMysqlPassword());
                isConnected = true;
                connectionStatusMessage = "Connected to MySQL (" + config.getMysqlHost() + ":" + config.getMysqlPort() + " / " + config.getMysqlDatabase() + ")";
                System.out.println("[OK] " + connectionStatusMessage);
            } catch (Exception e) {
                System.err.println("[WARN] MySQL connection failed: " + e.getMessage() + ". Falling back to SQLite.");
                fallbackToSqlite("MySQL unavailable (" + e.getMessage() + ")");
            }
        } else {
            fallbackToSqlite(null);
        }

        try {
            createTables();
            seedDataIfNeeded();
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fallbackToSqlite(String reason) {
        try {
            Class.forName("org.sqlite.JDBC");
            currentConnection = DriverManager.getConnection(config.getSqliteUrl());
            isConnected = true;
            if (reason != null) {
                connectionStatusMessage = "Offline SQLite Database (Fallback: " + reason + ")";
            } else {
                connectionStatusMessage = "Active SQLite Database (" + config.getSqlitePath() + ")";
            }
            System.out.println("[INFO] " + connectionStatusMessage);
        } catch (Exception e) {
            isConnected = false;
            connectionStatusMessage = "Connection failed: " + e.getMessage();
            System.err.println("[ERROR] Database fatal error: " + e.getMessage());
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (currentConnection == null || currentConnection.isClosed()) {
            init();
        }
        return currentConnection;
    }

    private void createTables() throws SQLException {
        Connection conn = getConnection();
        boolean isMySQL = (config.getDbType() == DatabaseConfig.DatabaseType.MYSQL);

        String autoInc = isMySQL ? "INT AUTO_INCREMENT PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";

        try (Statement stmt = conn.createStatement()) {
            // 1. Routes
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS routes (" +
                    "id " + autoInc + ", " +
                    "origin VARCHAR(100) NOT NULL, " +
                    "destination VARCHAR(100) NOT NULL, " +
                    "distance_km DOUBLE NOT NULL, " +
                    "duration VARCHAR(50) NOT NULL" +
                    ")");

            // 2. Trips
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS trips (" +
                    "id " + autoInc + ", " +
                    "vehicle_type VARCHAR(20) NOT NULL, " +
                    "vehicle_number VARCHAR(50) NOT NULL, " +
                    "vehicle_name VARCHAR(150) NOT NULL, " +
                    "travel_class VARCHAR(80) NOT NULL, " +
                    "route_id INT NOT NULL, " +
                    "journey_date VARCHAR(30) NOT NULL, " +
                    "departure_time VARCHAR(20) NOT NULL, " +
                    "arrival_time VARCHAR(20) NOT NULL, " +
                    "base_fare DOUBLE NOT NULL, " +
                    "total_seats INT NOT NULL, " +
                    "available_seats INT NOT NULL, " +
                    "rating DOUBLE DEFAULT 4.5" +
                    ")");

            // 3. Seats (JButton Matrix live status)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS seats (" +
                    "id " + autoInc + ", " +
                    "trip_id INT NOT NULL, " +
                    "seat_number VARCHAR(20) NOT NULL, " +
                    "row_index INT NOT NULL, " +
                    "col_index INT NOT NULL, " +
                    "seat_type VARCHAR(50) NOT NULL, " +
                    "status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', " +
                    "price DOUBLE NOT NULL, " +
                    "deck_or_coach VARCHAR(50) DEFAULT 'Main Deck', " +
                    "booked_by_pnr VARCHAR(50) DEFAULT NULL" +
                    ")");

            // 4. Bookings
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                    "pnr VARCHAR(50) PRIMARY KEY, " +
                    "trip_id INT NOT NULL, " +
                    "vehicle_type VARCHAR(20) NOT NULL, " +
                    "vehicle_name VARCHAR(150) NOT NULL, " +
                    "vehicle_number VARCHAR(50) NOT NULL, " +
                    "travel_class VARCHAR(80) NOT NULL, " +
                    "route_name VARCHAR(200) NOT NULL, " +
                    "journey_date VARCHAR(50) NOT NULL, " +
                    "departure_time VARCHAR(20) NOT NULL, " +
                    "arrival_time VARCHAR(20) NOT NULL, " +
                    "contact_email VARCHAR(120), " +
                    "contact_phone VARCHAR(50), " +
                    "total_fare DOUBLE NOT NULL, " +
                    "status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED', " +
                    "booking_time VARCHAR(50), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 5. Booking Passengers
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS booking_passengers (" +
                    "id " + autoInc + ", " +
                    "pnr VARCHAR(50) NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "age INT NOT NULL, " +
                    "gender VARCHAR(20) NOT NULL, " +
                    "seat_number VARCHAR(20) NOT NULL, " +
                    "berth_preference VARCHAR(50), " +
                    "fare DOUBLE NOT NULL" +
                    ")");

            // Safe column additions for existing SQLite databases
            try {
                stmt.executeUpdate("ALTER TABLE bookings ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            } catch (Exception ignored) {}
        }
    }

    private void seedDataIfNeeded() throws SQLException {
        Connection conn = getConnection();

        // Check if routes table has records
        boolean routesExist = false;
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM routes")) {
            if (rs.next() && rs.getInt(1) > 0) {
                routesExist = true;
            }
        }

        if (!routesExist) {
            System.out.println("[INIT] Seeding realistic Bus & Train routes...");

            // Insert initial routes
            String insertRoute = "INSERT INTO routes (origin, destination, distance_km, duration) VALUES (?, ?, ?, ?)";
            int r1, r2, r3, r4, r5;
            try (PreparedStatement pstmt = conn.prepareStatement(insertRoute, Statement.RETURN_GENERATED_KEYS)) {
                r1 = insertAndGetId(pstmt, "New Delhi (NDLS)", "Mumbai Central (MMCT)", 1386.0, "15 hrs 40 mins");
                r2 = insertAndGetId(pstmt, "Bengaluru (SBC)", "Chennai Central (MAS)", 359.0, "4 hrs 20 mins");
                r3 = insertAndGetId(pstmt, "Hyderabad (HYB)", "Pune Junction (PUNE)", 597.0, "8 hrs 45 mins");
                r4 = insertAndGetId(pstmt, "Kolkata (HWH)", "Patna Junction (PNBE)", 535.0, "7 hrs 15 mins");
                r5 = insertAndGetId(pstmt, "Mumbai (BCT)", "Goa (MAO)", 580.0, "9 hrs 10 mins");
            }

            LocalDate today = LocalDate.now();
            String dateStr = today.toString();

            // Insert Trips
            String insertTrip = "INSERT INTO trips (vehicle_type, vehicle_number, vehicle_name, travel_class, route_id, journey_date, departure_time, arrival_time, base_fare, total_seats, available_seats, rating) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertTrip, Statement.RETURN_GENERATED_KEYS)) {
                // Train 1: Rajdhani Express
                insertTripAndGetId(pstmt, "TRAIN", "12952", "Mumbai Rajdhani Express", "AC 3-Tier (3A)", r1, dateStr, "16:55", "08:35", 2150.0, 48, 36, 4.8);
                // Train 2: Vande Bharat Express
                insertTripAndGetId(pstmt, "TRAIN", "20608", "Vande Bharat Express", "Executive Chair (EC)", r2, dateStr, "05:45", "10:05", 1450.0, 40, 29, 4.9);
                // Train 3: Shatabdi Express
                insertTripAndGetId(pstmt, "TRAIN", "12009", "Ahmedabad Shatabdi Express", "AC Chair Car (CC)", r1, dateStr, "06:20", "12:45", 1120.0, 48, 39, 4.6);
                // Bus 1: GreenLine Multi-Axle Volvo
                insertTripAndGetId(pstmt, "BUS", "KA-01-F-8890", "GreenLine Multi-Axle Volvo", "AC Semi-Sleeper (2+2)", r2, dateStr, "22:30", "05:30", 850.0, 36, 26, 4.7);
                // Bus 2: Orange Travels Dreamliner
                insertTripAndGetId(pstmt, "BUS", "MH-04-AX-5512", "Orange Travels Dreamliner", "AC Sleeper (2+1)", r3, dateStr, "20:15", "05:45", 1250.0, 30, 21, 4.5);
                // Bus 3: Paulo Royal Cruiser
                insertTripAndGetId(pstmt, "BUS", "GA-02-B-3341", "Paulo Luxury Royal Cruiser", "Multi-Axle AC Sleeper", r5, dateStr, "21:00", "06:30", 1100.0, 32, 24, 4.6);
            }
        }

        // ALWAYS make sure every trip has seats generated in the seats table!
        ensureSeatsExistForTrips(conn);
        System.out.println("[OK] Database seeding verified successfully!");
    }

    public void ensureSeatsExistForTrips(Connection conn) throws SQLException {
        String sql = "SELECT id, vehicle_type, travel_class, base_fare, total_seats FROM trips";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int tripId = rs.getInt("id");
                String vType = rs.getString("vehicle_type");
                String tClass = rs.getString("travel_class");
                double fare = rs.getDouble("base_fare");
                int totalSeats = rs.getInt("total_seats");

                // Check if seats already exist for this trip
                try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM seats WHERE trip_id = ?")) {
                    check.setInt(1, tripId);
                    try (ResultSet countRs = check.executeQuery()) {
                        if (countRs.next() && countRs.getInt(1) > 0) {
                            continue; // already has seats
                        }
                    }
                }

                // Generate seats for this trip
                if ("TRAIN".equalsIgnoreCase(vType)) {
                    String coach = tClass.contains("EC") ? "Coach E1" : (tClass.contains("CC") ? "Coach C1" : "Coach B1");
                    generateSeatsForTrain(conn, tripId, totalSeats > 0 ? totalSeats : 48, fare, coach);
                } else {
                    boolean isSleeper = tClass.toLowerCase().contains("sleeper");
                    int cols = isSleeper ? 3 : 4;
                    int rows = (totalSeats > 0 ? totalSeats : 36) / cols;
                    generateSeatsForBus(conn, tripId, rows, cols, fare, "Main Deck", isSleeper);
                }
                System.out.println("[OK] Generated live seat matrix for trip " + tripId + " (" + vType + ")");
            }
        }
    }

    private void generateSeatsForTrain(Connection conn, int tripId, int totalSeats, double baseFare, String coachName) throws SQLException {
        String insertSeat = "INSERT INTO seats (trip_id, seat_number, row_index, col_index, seat_type, status, price, deck_or_coach, booked_by_pnr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Train coach with 6 berths per compartment (LB, MB, UB, LB, MB, UB + Side LB, Side UB)
        SeatType[] berthTypes = {
                SeatType.LOWER_BERTH, SeatType.MIDDLE_BERTH, SeatType.UPPER_BERTH,
                SeatType.LOWER_BERTH, SeatType.MIDDLE_BERTH, SeatType.UPPER_BERTH,
                SeatType.SIDE_LOWER, SeatType.SIDE_UPPER
        };

        Random rand = new Random(tripId * 31L);
        int rows = totalSeats / 6;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSeat)) {
            int seatNum = 1;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < 6; c++) {
                    if (seatNum > totalSeats) break;
                    String seatCode = coachName.substring(coachName.length() - 2) + "-" + seatNum;
                    SeatType type = berthTypes[c % berthTypes.length];
                    SeatStatus status = SeatStatus.AVAILABLE;

                    // Fixed demonstration seats to show occupied & ladies status clearly
                    if (seatNum == 1 || seatNum == 2) {
                        status = SeatStatus.OCCUPIED;
                    } else if (seatNum == 5 || seatNum == 12 || seatNum == 19 || seatNum == 26) {
                        status = SeatStatus.OCCUPIED;
                    } else if (seatNum == 8 || seatNum == 14) {
                        status = SeatStatus.LADIES_ONLY;
                    } else if (rand.nextInt(100) < 20) {
                        status = SeatStatus.OCCUPIED;
                    }

                    pstmt.setInt(1, tripId);
                    pstmt.setString(2, seatCode);
                    pstmt.setInt(3, r);
                    pstmt.setInt(4, c);
                    pstmt.setString(5, type.name());
                    pstmt.setString(6, status.name());
                    pstmt.setDouble(7, baseFare);
                    pstmt.setString(8, coachName);
                    pstmt.setString(9, (status == SeatStatus.OCCUPIED ? "PNR-928174" : null));
                    pstmt.addBatch();
                    seatNum++;
                }
            }
            pstmt.executeBatch();
        }
    }

    private void generateSeatsForBus(Connection conn, int tripId, int rows, int cols, double baseFare, String deckName, boolean isSleeper) throws SQLException {
        String insertSeat = "INSERT INTO seats (trip_id, seat_number, row_index, col_index, seat_type, status, price, deck_or_coach, booked_by_pnr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Random rand = new Random(tripId * 43L);
        int seatCounter = 1;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSeat)) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String seatNum = (isSleeper ? "SL-" : "S-") + seatCounter;
                    SeatType type;
                    if (c == 0 || c == cols - 1) {
                        type = SeatType.WINDOW;
                    } else if (c == 1) {
                        type = SeatType.AISLE;
                    } else {
                        type = SeatType.MIDDLE;
                    }

                    SeatStatus status = SeatStatus.AVAILABLE;
                    if (seatCounter == 3 || seatCounter == 4 || seatCounter == 9 || seatCounter == 15) {
                        status = SeatStatus.OCCUPIED;
                    } else if (seatCounter == 1 || seatCounter == 2) {
                        status = SeatStatus.LADIES_ONLY;
                    } else if (rand.nextInt(100) < 25) {
                        status = SeatStatus.OCCUPIED;
                    }

                    pstmt.setInt(1, tripId);
                    pstmt.setString(2, seatNum);
                    pstmt.setInt(3, r);
                    pstmt.setInt(4, c);
                    pstmt.setString(5, type.name());
                    pstmt.setString(6, status.name());
                    pstmt.setDouble(7, baseFare);
                    pstmt.setString(8, deckName);
                    pstmt.setString(9, (status == SeatStatus.OCCUPIED ? "PNR-SAMPLE" : null));
                    pstmt.addBatch();
                    seatCounter++;
                }
            }
            pstmt.executeBatch();
        }
    }

    private void insertSampleBooking(Connection conn, String pnr, int tripId, String vehicleType,
                                     String vehicleName, String vehicleNumber, String travelClass,
                                     String routeName, String dateStr, String depTime, String arrTime,
                                     String email, String phone, double totalFare) throws SQLException {
        String insertBooking = "INSERT INTO bookings (pnr, trip_id, vehicle_type, vehicle_name, vehicle_number, " +
                "travel_class, route_name, journey_date, departure_time, arrival_time, contact_email, " +
                "contact_phone, total_fare, status, booking_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertBooking)) {
            pstmt.setString(1, pnr);
            pstmt.setInt(2, tripId);
            pstmt.setString(3, vehicleType);
            pstmt.setString(4, vehicleName);
            pstmt.setString(5, vehicleNumber);
            pstmt.setString(6, travelClass);
            pstmt.setString(7, routeName);
            pstmt.setString(8, dateStr);
            pstmt.setString(9, depTime);
            pstmt.setString(10, arrTime);
            pstmt.setString(11, email);
            pstmt.setString(12, phone);
            pstmt.setDouble(13, totalFare);
            pstmt.setString(14, "CONFIRMED");
            pstmt.setString(15, LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.executeUpdate();
        }

        String insertPsg = "INSERT INTO booking_passengers (pnr, name, age, gender, seat_number, berth_preference, fare) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertPsg)) {
            pstmt.setString(1, pnr);
            pstmt.setString(2, "Arjun Sharma");
            pstmt.setInt(3, 32);
            pstmt.setString(4, "Male");
            pstmt.setString(5, "B1-1");
            pstmt.setString(6, "Lower Berth (LB)");
            pstmt.setDouble(7, 2150.0);
            pstmt.executeUpdate();

            pstmt.setString(1, pnr);
            pstmt.setString(2, "Pooja Sharma");
            pstmt.setInt(3, 29);
            pstmt.setString(4, "Female");
            pstmt.setString(5, "B1-2");
            pstmt.setString(6, "Middle Berth (MB)");
            pstmt.setDouble(7, 2150.0);
            pstmt.executeUpdate();
        }
    }

    private int insertAndGetId(PreparedStatement pstmt, String origin, String destination, double distance, String duration) throws SQLException {
        pstmt.setString(1, origin);
        pstmt.setString(2, destination);
        pstmt.setDouble(3, distance);
        pstmt.setString(4, duration);
        pstmt.executeUpdate();
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    private int insertTripAndGetId(PreparedStatement pstmt, String vehicleType, String num, String name,
                                  String travelClass, int routeId, String date, String dep, String arr,
                                  double fare, int totalSeats, int availSeats, double rating) throws SQLException {
        pstmt.setString(1, vehicleType);
        pstmt.setString(2, num);
        pstmt.setString(3, name);
        pstmt.setString(4, travelClass);
        pstmt.setInt(5, routeId);
        pstmt.setString(6, date);
        pstmt.setString(7, dep);
        pstmt.setString(8, arr);
        pstmt.setDouble(9, fare);
        pstmt.setInt(10, totalSeats);
        pstmt.setInt(11, availSeats);
        pstmt.setDouble(12, rating);
        pstmt.executeUpdate();
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    // =========================================================================
    // Public Query Operations
    // =========================================================================

    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT * FROM routes ORDER BY origin, destination";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                routes.add(new Route(
                        rs.getInt("id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getDouble("distance_km"),
                        rs.getString("duration")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }

    public List<Trip> searchTrips(VehicleType vehicleType, String origin, String destination) {
        List<Trip> trips = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.*, r.origin, r.destination, r.distance_km, r.duration " +
                "FROM trips t JOIN routes r ON t.route_id = r.id WHERE 1=1 ");

        if (vehicleType != null) {
            sql.append(" AND t.vehicle_type = '").append(vehicleType.name()).append("'");
        }
        if (origin != null && !origin.trim().isEmpty() && !origin.equalsIgnoreCase("All Stations")) {
            sql.append(" AND r.origin LIKE '%").append(origin.trim()).append("%'");
        }
        if (destination != null && !destination.trim().isEmpty() && !destination.equalsIgnoreCase("All Stations")) {
            sql.append(" AND r.destination LIKE '%").append(destination.trim()).append("%'");
        }
        sql.append(" ORDER BY t.departure_time ASC");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                Route route = new Route(
                        rs.getInt("route_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getDouble("distance_km"),
                        rs.getString("duration")
                );

                String dateStr = rs.getString("journey_date");
                LocalDate jDate = LocalDate.now();
                try {
                    jDate = LocalDate.parse(dateStr);
                } catch (Exception ignored) {}

                String depTimeStr = rs.getString("departure_time");
                String arrTimeStr = rs.getString("arrival_time");
                LocalTime dep = LocalTime.parse(depTimeStr.length() == 5 ? depTimeStr : depTimeStr.substring(0, 5));
                LocalTime arr = LocalTime.parse(arrTimeStr.length() == 5 ? arrTimeStr : arrTimeStr.substring(0, 5));

                trips.add(new Trip(
                        rs.getInt("id"),
                        VehicleType.valueOf(rs.getString("vehicle_type")),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_name"),
                        rs.getString("travel_class"),
                        route,
                        jDate,
                        dep,
                        arr,
                        rs.getDouble("base_fare"),
                        rs.getInt("total_seats"),
                        rs.getInt("available_seats"),
                        rs.getDouble("rating")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trips;
    }

    public List<Seat> getSeatsForTrip(int tripId) {
        List<Seat> seats = querySeatsForTrip(tripId);
        if (seats.isEmpty()) {
            try {
                Connection conn = getConnection();
                ensureSeatsExistForTrips(conn);
                seats = querySeatsForTrip(tripId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return seats;
    }

    private List<Seat> querySeatsForTrip(int tripId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE trip_id = ? ORDER BY row_index, col_index";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat(
                            rs.getInt("id"),
                            rs.getInt("trip_id"),
                            rs.getString("seat_number"),
                            rs.getInt("row_index"),
                            rs.getInt("col_index"),
                            SeatType.valueOf(rs.getString("seat_type")),
                            SeatStatus.valueOf(rs.getString("status")),
                            rs.getDouble("price"),
                            rs.getString("deck_or_coach")
                    );
                    seat.setBookedByPnr(rs.getString("booked_by_pnr"));
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    /**
     * Books the selected seats transactionally.
     */
    public synchronized boolean bookSeats(Booking booking) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Verify and update seats to OCCUPIED
            String updateSeatSql = "UPDATE seats SET status = 'OCCUPIED', booked_by_pnr = ? WHERE trip_id = ? AND seat_number = ? AND status IN ('AVAILABLE', 'LADIES_ONLY', 'SELECTED')";
            try (PreparedStatement seatStmt = conn.prepareStatement(updateSeatSql)) {
                for (Passenger psg : booking.getPassengers()) {
                    seatStmt.setString(1, booking.getPnr());
                    seatStmt.setInt(2, booking.getTripId());
                    seatStmt.setString(3, psg.getSeatNumber());
                    int rows = seatStmt.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        System.err.println("Seat " + psg.getSeatNumber() + " already taken! Rolling back.");
                        return false;
                    }
                }
            }

            // 2. Insert into bookings
            String insertBookingSql = "INSERT INTO bookings (pnr, trip_id, vehicle_type, vehicle_name, vehicle_number, " +
                    "travel_class, route_name, journey_date, departure_time, arrival_time, contact_email, " +
                    "contact_phone, total_fare, status, booking_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement bookStmt = conn.prepareStatement(insertBookingSql)) {
                bookStmt.setString(1, booking.getPnr());
                bookStmt.setInt(2, booking.getTripId());
                bookStmt.setString(3, booking.getVehicleType().name());
                bookStmt.setString(4, booking.getVehicleName());
                bookStmt.setString(5, booking.getVehicleNumber());
                bookStmt.setString(6, booking.getTravelClass());
                bookStmt.setString(7, booking.getRouteName());
                bookStmt.setString(8, booking.getJourneyDate());
                bookStmt.setString(9, booking.getDepartureTime());
                bookStmt.setString(10, booking.getArrivalTime());
                bookStmt.setString(11, booking.getContactEmail());
                bookStmt.setString(12, booking.getContactPhone());
                bookStmt.setDouble(13, booking.getTotalFare());
                bookStmt.setString(14, "CONFIRMED");
                bookStmt.setString(15, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                bookStmt.executeUpdate();
            }

            // 3. Insert passenger items
            String insertPassengerSql = "INSERT INTO booking_passengers (pnr, name, age, gender, seat_number, berth_preference, fare) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psgStmt = conn.prepareStatement(insertPassengerSql)) {
                for (Passenger psg : booking.getPassengers()) {
                    psgStmt.setString(1, booking.getPnr());
                    psgStmt.setString(2, psg.getName());
                    psgStmt.setInt(3, psg.getAge());
                    psgStmt.setString(4, psg.getGender());
                    psgStmt.setString(5, psg.getSeatNumber());
                    psgStmt.setString(6, psg.getBerthPreference());
                    psgStmt.setDouble(7, psg.getFare());
                    psgStmt.addBatch();
                }
                psgStmt.executeBatch();
            }

            // 4. Update available seats count in trips
            String updateTripSql = "UPDATE trips SET available_seats = available_seats - ? WHERE id = ?";
            try (PreparedStatement tripStmt = conn.prepareStatement(updateTripSql)) {
                tripStmt.setInt(1, booking.getPassengers().size());
                tripStmt.setInt(2, booking.getTripId());
                tripStmt.executeUpdate();
            }

            conn.commit(); // Transaction success!
            System.out.println("[OK] Booking successfully confirmed for PNR: " + booking.getPnr());
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    /**
     * Cancels a booking, refunds fare, and releases seats back to AVAILABLE.
     */
    public synchronized boolean cancelBooking(String pnr) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Get booking info
            int tripId = 0;
            int passengerCount = 0;
            String checkSql = "SELECT trip_id FROM bookings WHERE pnr = ? AND status = 'CONFIRMED'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, pnr);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        tripId = rs.getInt("trip_id");
                    } else {
                        conn.rollback();
                        return false; // not found or already cancelled
                    }
                }
            }

            // Count passengers
            String countSql = "SELECT COUNT(*) FROM booking_passengers WHERE pnr = ?";
            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setString(1, pnr);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        passengerCount = rs.getInt(1);
                    }
                }
            }

            // Update booking status to CANCELLED
            String updateBookingSql = "UPDATE bookings SET status = 'CANCELLED' WHERE pnr = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookingSql)) {
                stmt.setString(1, pnr);
                stmt.executeUpdate();
            }

            // Release seats back to original status (LADIES_ONLY or AVAILABLE)
            String releaseSeatsSql = "UPDATE seats SET status = CASE " +
                    "WHEN seat_number IN ('S-1', 'S-2', 'SL-1', 'SL-2') OR seat_number LIKE '%-8' OR seat_number LIKE '%-14' " +
                    "THEN 'LADIES_ONLY' ELSE 'AVAILABLE' END, booked_by_pnr = NULL WHERE trip_id = ? AND booked_by_pnr = ?";
            try (PreparedStatement stmt = conn.prepareStatement(releaseSeatsSql)) {
                stmt.setInt(1, tripId);
                stmt.setString(2, pnr);
                stmt.executeUpdate();
            }

            // Increment available seats count in trips
            String updateTripSql = "UPDATE trips SET available_seats = available_seats + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateTripSql)) {
                stmt.setInt(1, passengerCount);
                stmt.setInt(2, tripId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("[OK] Booking cancelled and seats freed for PNR: " + pnr);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY COALESCE(booking_time, pnr) DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String pnr = rs.getString("pnr");
                String timeStr = rs.getString("booking_time");
                if (timeStr == null || timeStr.trim().isEmpty()) {
                    try {
                        Timestamp ts = rs.getTimestamp("created_at");
                        if (ts != null) timeStr = ts.toString();
                    } catch (Exception ignored) {}
                }
                if (timeStr == null) timeStr = LocalDateTime.now().toString();

                LocalDateTime timestamp = LocalDateTime.now();
                try {
                    timestamp = LocalDateTime.parse(timeStr.replace(" ", "T"));
                } catch (Exception ignored) {}

                Booking b = new Booking(
                        pnr,
                        rs.getInt("trip_id"),
                        VehicleType.valueOf(rs.getString("vehicle_type")),
                        rs.getString("vehicle_name"),
                        rs.getString("vehicle_number"),
                        rs.getString("travel_class"),
                        rs.getString("route_name"),
                        rs.getString("journey_date"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getString("contact_email"),
                        rs.getString("contact_phone"),
                        rs.getDouble("total_fare"),
                        rs.getString("status"),
                        timestamp
                );

                // Load passengers for this booking
                b.setPassengers(getPassengersForBooking(conn, pnr));
                bookings.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public Booking getBookingByPnr(String targetPnr) {
        String sql = "SELECT * FROM bookings WHERE pnr = ? OR contact_phone LIKE ? ORDER BY booking_time DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, targetPnr.trim());
            pstmt.setString(2, "%" + targetPnr.trim() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String pnr = rs.getString("pnr");
                    String timeStr = rs.getString("booking_time");
                    if (timeStr == null || timeStr.trim().isEmpty()) {
                        try {
                            Timestamp ts = rs.getTimestamp("created_at");
                            if (ts != null) timeStr = ts.toString();
                        } catch (Exception ignored) {}
                    }
                    if (timeStr == null) timeStr = LocalDateTime.now().toString();

                    LocalDateTime timestamp = LocalDateTime.now();
                    try {
                        timestamp = LocalDateTime.parse(timeStr.replace(" ", "T"));
                    } catch (Exception ignored) {}

                    Booking b = new Booking(
                            pnr,
                            rs.getInt("trip_id"),
                            VehicleType.valueOf(rs.getString("vehicle_type")),
                            rs.getString("vehicle_name"),
                            rs.getString("vehicle_number"),
                            rs.getString("travel_class"),
                            rs.getString("route_name"),
                            rs.getString("journey_date"),
                            rs.getString("departure_time"),
                            rs.getString("arrival_time"),
                            rs.getString("contact_email"),
                            rs.getString("contact_phone"),
                            rs.getDouble("total_fare"),
                            rs.getString("status"),
                            timestamp
                    );
                    b.setPassengers(getPassengersForBooking(conn, pnr));
                    return b;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Passenger> getPassengersForBooking(Connection conn, String pnr) throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT * FROM booking_passengers WHERE pnr = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    passengers.add(new Passenger(
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("seat_number"),
                            rs.getString("berth_preference"),
                            rs.getDouble("fare")
                    ));
                }
            }
        }
        return passengers;
    }

    public boolean testMySQLConnection(String host, int port, String user, String pass) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection testConn = DriverManager.getConnection(url, user, pass)) {
                return testConn != null && !testConn.isClosed();
            }
        } catch (Exception e) {
            System.err.println("Test MySQL failed: " + e.getMessage());
            return false;
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try { closeable.close(); } catch (Exception ignored) {}
        }
    }
}
