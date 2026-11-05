# 🚆 🚌 Bus & Train Ticket Reservation System

An enterprise-grade desktop ticket reservation system built using **Java AWT + Swing**, **JDBC**, **MySQL Workbench**, and **AI Tools**, featuring an interactive **Seat-map grid via JButton matrix showing live occupied/available colour status**.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **User Interface (UI)** | **Java AWT + Swing** + FlatLaf | Modern, dark-mode GUI with anti-aliasing, custom 2D graphics, dynamic seat matrix, and micro-interactions |
| **Database (DB)** | **JDBC** (Java Database Connectivity) | Transactional data persistence (`commit`, `rollback`), connection pooling & multi-driver support |
| **Database Server** | **MySQL Server 8.0 + MySQL Workbench** | Full relational schema (`schema.sql`) for MySQL Workbench with tables, foreign keys, and indexes *(includes zero-config embedded SQLite fallback)* |
| **AI Tools** | **AI Smart Engine** | • **AI Seat Matcher**: Recommends optimal seats by traveler preference (Senior Citizen lower berths, Scenic window, Couples adjacent, Family bay)<br>• **AI Dynamic Demand & Surge Forecaster**: Analyzes real-time occupancy and probability of confirmation<br>• **AI Travel Assistant**: Interactive chatbot for baggage rules, refund policies, and travel FAQs |
| **Seat Map Matrix** | **JButton Matrix Grid** | Real-time seat occupancy visualization with live color-coded status badges, tooltips, and hover animations |

---

## 🎨 Seat-Map Grid via JButton Matrix (Core Feature)

The seat reservation matrix dynamically generates a physical coach/cabin layout (Bus or Train) where each seat is a custom-rendered `SeatButton` (subclassed from `JButton`) with live color indicators:

* 🟢 **Available (Emerald Green `#22C55E`)**: Clickable seat ready for selection.
* 🔵 **Selected (Electric Blue `#3B82F6`)**: Seats currently selected in your active cart.
* 🔴 **Occupied (Crimson Red `#EF4444`)**: Seats booked by other travelers (displays booking reference/PNR in tooltip; clicking warns that seat is occupied).
* 🟣 **Ladies Reserved (Purple `#A855F7`)**: Seats reserved for female travelers.

### Real-Time Capabilities:
1. **Live Price & Seat Counter**: Dynamic recalculation of base fare, station fees, GST, and promo code discounts.
2. **Berth/Type Indicators**: Distinct visual tags for `LB` (Lower Berth), `MB` (Middle Berth), `UB` (Upper Berth), `SL` (Side Lower), `SU` (Side Upper), `WIN` (Window), and `AIS` (Aisle).
3. **Instant Seat Release on Cancellation**: When a booking is cancelled under *My Bookings*, its seats are immediately released back to **Available (Green)** on the live matrix!

---

## 🚀 How to Run the Project

### Option 1: Clone & Quick One-Click Launch (Windows)
```bash
git clone https://github.com/Shivam-1122/Java-Project.git
cd Java-Project
```
Double-click:
```
compile_and_run.bat
```
or run:
```powershell
.\compile.bat
.\run.bat
```

### Option 2: Manual Terminal Commands
```powershell
# 1. Compile all Java sources
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/ticketreservation/*.java src/com/ticketreservation/model/*.java src/com/ticketreservation/database/*.java src/com/ticketreservation/ai/*.java src/com/ticketreservation/ui/*.java

# 2. Launch Application
java -cp "bin;lib/*" com.ticketreservation.Main
```

---

## 🗄️ Setting Up MySQL & MySQL Workbench

The project includes full MySQL Server and MySQL Workbench support:

### Step 1: Open in MySQL Workbench
1. Open **MySQL Workbench**.
2. Connect to your local MySQL instance (`localhost:3306`).
3. Click **File ➔ Open SQL Script...** and select [`schema.sql`](schema.sql).
4. Click the ⚡ **Execute** button to run the script. This creates:
   * `ticket_reservation_db` database
   * `routes`, `trips`, `seats`, `bookings`, and `booking_passengers` tables
   * Sample schedules for Rajdhani Express, Vande Bharat, Volvo Multi-Axle buses, etc.

### Step 2: Configure MySQL in the Application
Configure your database connection in [`db.properties`](db.properties):
```properties
db.type=MYSQL
mysql.host=localhost
mysql.port=3306
mysql.database=ticket_reservation_db
mysql.user=root
mysql.password=YOUR_PASSWORD_HERE
```

> **Note on Zero-Setup Fallback**: If MySQL credentials are not yet entered, the application automatically uses the embedded SQLite driver (`ticket_reservation.db`), ensuring you can test and explore all features immediately without any setup roadblocks!

---

## 📂 Project Architecture

```
jp/
├── bin/                             # Compiled Java bytecode
├── lib/                             # Required JDBC & UI JARs
│   ├── flatlaf-3.4.1.jar            # Modern Flat Swing Look and Feel
│   ├── mysql-connector-j-8.3.0.jar  # Official MySQL JDBC Driver
│   ├── sqlite-jdbc-3.45.1.0.jar     # Embedded SQLite JDBC Driver
│   ├── slf4j-api-2.0.12.jar         # Logging abstraction
│   └── slf4j-simple-2.0.12.jar      # Logging implementation
├── src/
│   └── com/ticketreservation/
│       ├── Main.java                # Main entry point & LaF initialization
│       ├── model/                   # Domain Models
│       │   ├── VehicleType.java     # Bus or Train enum
│       │   ├── SeatType.java        # Window, Aisle, Lower/Upper Berth
│       │   ├── SeatStatus.java      # AVAILABLE, SELECTED, OCCUPIED, LADIES
│       │   ├── Seat.java            # Seat entity & matrix coordinates
│       │   ├── Route.java           # Origin, destination, distance, duration
│       │   ├── Trip.java            # Scheduled departures
│       │   ├── Passenger.java       # Passenger entity
│       │   └── Booking.java         # PNR master record
│       ├── database/                # Database & JDBC Layer
│       │   ├── DatabaseConfig.java  # db.properties loader & configuration
│       │   └── DatabaseManager.java # JDBC connection & transactional queries
│       ├── ai/                      # AI Tools Engine
│       │   └── AISmartEngine.java   # Seat Recommender, Demand Forecaster, Chatbot
│       └── ui/                      # Presentation Layer (AWT + Swing)
│           ├── Theme.java           # Design system, palettes & typography
│           ├── SeatButton.java      # Custom JButton matrix seat widget
│           ├── SeatMatrixPanel.java # Dynamic coach/cabin layout & AI toolbar
│           ├── SearchAndFilterPanel.java # Route search & train/bus toggle
│           ├── PassengerDetailsPanel.java # Passenger form & promo code checkout
│           ├── BookingConfirmationPanel.java # Digital E-Ticket boarding pass
│           ├── MyBookingsPanel.java # PNR lookup & instant seat cancellation
│           ├── AIAssistantDialog.java # Travel assistant chatbot dialog
│           └── MainApplicationFrame.java # CardLayout container & top navigation
├── schema.sql                       # Full MySQL Workbench SQL script
├── db.properties                    # Database configuration file
├── compile.bat                      # Compile script
├── run.bat                          # Run script
├── compile_and_run.bat              # One-click compile & launch script
└── README.md                        # Documentation
```

---

## 🤖 AI Tools Included

1. **AI Smart Seat Matcher**: Click *"🤖 AI Auto-Pick"* on the seat matrix and select your preference:
   * **Senior Citizen**: Selects Lower Berths (`LB`) near the entrance for minimal walking.
   * **Scenic View**: Selects Window seats with uninterrupted outdoor views.
   * **Pair / Couple**: Automatically finds 2 adjacent seats in the same row.
   * **Family Bay**: Allocates 3–4 seats together in the same compartment.
2. **AI Demand & Dynamic Pricing Forecaster**: Click *"📊 AI Demand Forecast"* to view current occupancy percentage, surge price trajectory, and instant ticket confirmation probability.
3. **Interactive AI Travel Assistant**: Click *"🤖 AI Travel Assistant"* at the top right to ask questions about luggage limits, cancellation/refund policies, meal timings, and MySQL Workbench connectivity.
