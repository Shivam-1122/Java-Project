package com.ticketreservation.ai;

import com.ticketreservation.model.*;

import java.util.*;

/**
 * AI Tools Engine providing:
 * 1. AI Intelligent Seat Recommendation (by preference, age, comfort, group).
 * 2. AI Dynamic Demand & Price Surge Predictor.
 * 3. AI Travel Assistant with intent recognition and travel guidance.
 */
public class AISmartEngine {

    public enum RecommendationType {
        SENIOR_CITIZEN("Senior Citizen (Lower Berths & Easy Access)"),
        WINDOW_SCENIC("Scenic / Window View"),
        COUPLE_TOGETHER("Pair / Couple (Adjacent Seats)"),
        FAMILY_BAY("Family / Group (Same Compartment)"),
        SOLO_REST("Quiet Sleep / Work (Upper Berth / Solo)");

        private final String label;
        RecommendationType(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    /**
     * Recommends optimal seats from the available list based on AI preference rules.
     */
    public static List<Seat> recommendSeats(List<Seat> allSeats, RecommendationType preference, int count) {
        List<Seat> available = new ArrayList<>();
        for (Seat s : allSeats) {
            if (s.isAvailable()) {
                available.add(s);
            }
        }

        if (available.isEmpty()) return Collections.emptyList();

        List<Seat> recommended = new ArrayList<>();

        switch (preference) {
            case SENIOR_CITIZEN:
                // Prioritize lower berths or aisle seats on lower rows
                for (Seat s : available) {
                    if (s.getSeatType() == SeatType.LOWER_BERTH || s.getSeatType() == SeatType.AISLE) {
                        recommended.add(s);
                        if (recommended.size() >= count) return recommended;
                    }
                }
                break;

            case WINDOW_SCENIC:
                // Prioritize Window seats
                for (Seat s : available) {
                    if (s.getSeatType() == SeatType.WINDOW || s.getSeatType() == SeatType.SIDE_LOWER) {
                        recommended.add(s);
                        if (recommended.size() >= count) return recommended;
                    }
                }
                break;

            case SOLO_REST:
                // Prioritize Upper Berths or back seats
                for (Seat s : available) {
                    if (s.getSeatType() == SeatType.UPPER_BERTH || s.getSeatType() == SeatType.SIDE_UPPER) {
                        recommended.add(s);
                        if (recommended.size() >= count) return recommended;
                    }
                }
                break;

            case COUPLE_TOGETHER:
                // Look for contiguous seats in same row
                Map<Integer, List<Seat>> byRow = new HashMap<>();
                for (Seat s : available) {
                    byRow.computeIfAbsent(s.getRowIndex(), k -> new ArrayList<>()).add(s);
                }
                for (List<Seat> rowSeats : byRow.values()) {
                    if (rowSeats.size() >= 2) {
                        rowSeats.sort(Comparator.comparingInt(Seat::getColIndex));
                        for (int i = 0; i < rowSeats.size() - 1; i++) {
                            Seat s1 = rowSeats.get(i);
                            Seat s2 = rowSeats.get(i + 1);
                            if (Math.abs(s1.getColIndex() - s2.getColIndex()) == 1) {
                                recommended.add(s1);
                                recommended.add(s2);
                                return recommended;
                            }
                        }
                    }
                }
                break;

            case FAMILY_BAY:
                // Look for 3-4 seats in the same compartment/row
                Map<Integer, List<Seat>> byComp = new HashMap<>();
                for (Seat s : available) {
                    byComp.computeIfAbsent(s.getRowIndex(), k -> new ArrayList<>()).add(s);
                }
                for (List<Seat> baySeats : byComp.values()) {
                    if (baySeats.size() >= count) {
                        return baySeats.subList(0, count);
                    }
                }
                break;
        }

        // Fallback: fill remaining from available
        for (Seat s : available) {
            if (!recommended.contains(s)) {
                recommended.add(s);
                if (recommended.size() >= count) break;
            }
        }

        return recommended;
    }

    /**
     * AI Prediction Result for demand and fare trends.
     */
    public static class DemandPrediction {
        public final double occupancyRate;
        public final String demandLevel;
        public final String priceTrend;
        public final int confirmationProbability;
        public final String advice;

        public DemandPrediction(double occupancyRate, String demandLevel, String priceTrend, int confirmationProbability, String advice) {
            this.occupancyRate = occupancyRate;
            this.demandLevel = demandLevel;
            this.priceTrend = priceTrend;
            this.confirmationProbability = confirmationProbability;
            this.advice = advice;
        }
    }

    /**
     * Calculates dynamic demand analytics based on total vs available seats.
     */
    public static DemandPrediction analyzeDemand(int totalSeats, int availableSeats) {
        int booked = Math.max(0, totalSeats - availableSeats);
        double occRate = totalSeats > 0 ? ((double) booked / totalSeats) * 100.0 : 0.0;

        String level;
        String trend;
        int confProb;
        String advice;

        if (occRate > 80.0) {
            level = "[HIGH DEMAND]";
            trend = "Surge pricing active (+18% expected)";
            confProb = 99;
            advice = "Critical availability! Only " + availableSeats + " seats remaining. Book immediately to secure your preferred berths.";
        } else if (occRate > 55.0) {
            level = "[MODERATE DEMAND]";
            trend = "Stable base fare";
            confProb = 95;
            advice = "Moderate occupancy. Window and lower berths are filling up quickly for this service.";
        } else {
            level = "[RELAXED DEMAND]";
            trend = "Best discount opportunity";
            confProb = 98;
            advice = "Plenty of choice seats available across all decks and coaches. Prime window seats open.";
        }

        return new DemandPrediction(occRate, level, trend, confProb, advice);
    }

    /**
     * AI Travel Assistant answering user queries with context-aware responses.
     */
    public static String getTravelAssistantResponse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Hello! I am your AI Travel Assistant. You can ask me about seat recommendations, luggage limits, refund policies, meal options, or booking tips!";
        }

        String q = query.toLowerCase().trim();

        if (q.contains("luggage") || q.contains("baggage") || q.contains("weight") || q.contains("limit")) {
            return "**Luggage Policy Guidance:**\n\n" +
                    "- **Trains (AC Coaches):** Passengers are allowed up to 40 kg - 50 kg free luggage allowance per ticket.\n" +
                    "- **Bus Services:** 1 medium trolley bag (up to 20 kg) and 1 laptop bag / personal backpack are permitted free of charge in the luggage compartment.\n" +
                    "- **Prohibited Items:** Inflammable materials, dangerous chemicals, and unregistered commercial goods are strictly prohibited.";
        }

        if (q.contains("cancel") || q.contains("refund") || q.contains("charges") || q.contains("return")) {
            return "**Cancellation & Refund Policy:**\n\n" +
                    "- **> 48 hours before departure:** 90% refund (Flat nominal cancellation clerkage of Rs. 60/seat).\n" +
                    "- **12 to 48 hours before departure:** 75% refund of ticket fare.\n" +
                    "- **4 to 12 hours before departure:** 50% refund.\n" +
                    "- **< 4 hours before departure:** Non-refundable.\n" +
                    "- *Instant Refund:* When you cancel a booking via 'Manage Bookings', your seats are instantly unlocked on the live matrix!";
        }

        if (q.contains("seat") || q.contains("berth") || q.contains("recommend") || q.contains("lower") || q.contains("window")) {
            return "**AI Seat Recommendations:**\n\n" +
                    "- **Lower Berth (LB):** Ideal for senior citizens, patients, and travelers who prefer minimal climbing.\n" +
                    "- **Upper Berth (UB):** Best for undisturbed sleep, solo passengers, and working on laptops without interruption.\n" +
                    "- **Window Seats:** Perfect for sightseeing on day-trains like Vande Bharat and Shatabdi!\n" +
                    "- *Tip:* Use the 'AI Seat Matcher' button above the seat matrix to automatically select the best pair of seats.";
        }

        if (q.contains("meal") || q.contains("food") || q.contains("catering") || q.contains("breakfast") || q.contains("dinner")) {
            return "**Meal & Catering Information:**\n\n" +
                    "- **Rajdhani & Vande Bharat:** Complimentary premium hot meals (Breakfast, Lunch, Evening Snacks, and Dinner) are included with your ticket depending on scheduled departure.\n" +
                    "- **Luxury Buses:** Mineral water bottle and blanket provided on all overnight multi-axle journeys. Mid-route hygiene refreshment stops are scheduled.";
        }

        if (q.contains("pnr") || q.contains("status") || q.contains("check")) {
            return "**PNR Tracking & Verification:**\n\n" +
                    "- Your unique PNR is generated as soon as your booking is finalized.\n" +
                    "- You can verify any past reservation by switching to the **'Manage Bookings & PNR'** tab.\n" +
                    "- Even if you restart the application, your bookings persist permanently in your JDBC MySQL / SQLite database!";
        }

        if (q.contains("mysql") || q.contains("database") || q.contains("workbench") || q.contains("schema")) {
            return "**MySQL Workbench & Database Setup:**\n\n" +
                    "- This application connects via standard JDBC to **MySQL Server 8.0**.\n" +
                    "- You can configure database connection settings directly in the `db.properties` file.\n" +
                    "- A complete `schema.sql` file has been provided in the project root. You can open and run it inside MySQL Workbench anytime!";
        }

        // Default friendly response
        return "**AI Travel Assistant:**\n\n" +
                "I'm here to help with your bus and train journey!\n\n" +
                "- Ask: *'What is the baggage limit?'*\n" +
                "- Ask: *'How does ticket cancellation work?'*\n" +
                "- Ask: *'Which seat is best for senior citizens?'*\n" +
                "- Ask: *'Are meals provided on Rajdhani?'*\n" +
                "- Ask: *'How do I connect to MySQL Workbench?'*";
    }
}
