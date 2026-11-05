package com.ticketreservation.ui;

import com.ticketreservation.ai.AISmartEngine;
import com.ticketreservation.database.DatabaseManager;
import com.ticketreservation.model.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern, visually stunning Seat Matrix Grid UI Panel.
 * Implements: "Seat-map grid via JButton matrix showing live occupied/available colour status"
 */
public class SeatMatrixPanel extends JPanel {
    private final Trip trip;
    private final List<Seat> seatList;
    private final List<Seat> selectedSeats = new ArrayList<>();
    private final List<SeatButton> seatButtons = new ArrayList<>();

    private JPanel selectedSeatsBadgesPanel;
    private JLabel lblCartSummary;
    private JLabel lblAvailableCount;
    private JLabel lblOccupiedCount;
    private JLabel lblSelectedCount;
    private JButton btnProceed;
    private JComboBox<AISmartEngine.RecommendationType> aiRecommendCombo;
    private JComboBox<Integer> seatCountCombo;

    private Runnable onBackAction;
    private java.util.function.Consumer<List<Seat>> onProceedAction;

    public SeatMatrixPanel(Trip trip, List<Seat> seats) {
        this.trip = trip;
        if (seats == null || seats.isEmpty()) {
            this.seatList = DatabaseManager.getInstance().getSeatsForTrip(trip.getId());
        } else {
            this.seatList = seats;
        }
        initUI();
    }

    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    public void setOnProceedAction(java.util.function.Consumer<List<Seat>> onProceedAction) {
        this.onProceedAction = onProceedAction;
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top Header Banner with Trip Specs, Stepper & AI Toolbar
        add(createTopSection(), BorderLayout.NORTH);

        // 2. Center: Vehicle Cabin View with Vector Elements
        add(createCabinScrollPane(), BorderLayout.CENTER);

        // 3. Bottom: Floating Checkout & Selected Seats Tray
        add(createFloatingCartTray(), BorderLayout.SOUTH);

        updateCartState();
    }

    private JPanel createTopSection() {
        JPanel container = new JPanel(new BorderLayout(10, 8));
        container.setOpaque(false);

        // Top Header Card with 2 cleanly separated rows
        JPanel card = Theme.createCardPanel();
        card.setLayout(new GridLayout(2, 1, 6, 8));

        // Row 1: Back Button, Vehicle Icon, Title, and Demand Forecast Button
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);

        JPanel row1Left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1Left.setOpaque(false);

        JButton btnBack = new JButton("< Schedules");
        btnBack.setFont(Theme.FONT_BOLD);
        btnBack.setBackground(new Color(51, 65, 85));
        btnBack.setForeground(Theme.TEXT_LIGHT);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });

        Icon vIcon = trip.getVehicleType() == VehicleType.TRAIN ?
                IconUtils.createTrainIcon(26, Theme.PRIMARY_BLUE) :
                IconUtils.createBusIcon(26, Theme.ACCENT_AMBER);
        JLabel iconLabel = new JLabel(vIcon);

        JLabel title = new JLabel(trip.getVehicleName() + " (" + trip.getVehicleNumber() + ") | " + trip.getTravelClass());
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        row1Left.add(btnBack);
        row1Left.add(iconLabel);
        row1Left.add(title);

        JPanel row1Right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        row1Right.setOpaque(false);

        JButton btnForecast = new JButton("AI Demand Forecast");
        btnForecast.setFont(Theme.FONT_BOLD);
        btnForecast.setBackground(new Color(67, 56, 202));
        btnForecast.setForeground(Color.WHITE);
        btnForecast.setFocusPainted(false);
        btnForecast.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForecast.addActionListener(e -> showAiDemandDialog());
        row1Right.add(btnForecast);

        row1.add(row1Left, BorderLayout.CENTER);
        row1.add(row1Right, BorderLayout.EAST);

        // Row 2: Route Specs (Left) & AI Seat Matcher (Right)
        JPanel row2 = new JPanel(new BorderLayout(12, 0));
        row2.setOpaque(false);

        JLabel route = new JLabel("Route: " + trip.getRoute().getRouteName() + "   |   Schedule: " +
                trip.getDepartureTime() + " -> " + trip.getArrivalTime() + " (" + trip.getRoute().getEstimatedDuration() + ")");
        route.setFont(Theme.FONT_REGULAR);
        route.setForeground(new Color(148, 163, 184));

        JPanel row2Right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        row2Right.setOpaque(false);

        JLabel lblAi = new JLabel("AI Seat Matcher:");
        lblAi.setFont(Theme.FONT_BOLD);
        lblAi.setForeground(Theme.TEXT_LIGHT);

        aiRecommendCombo = new JComboBox<>(AISmartEngine.RecommendationType.values());
        aiRecommendCombo.setFont(Theme.FONT_REGULAR);
        aiRecommendCombo.setPreferredSize(new Dimension(200, 30));

        seatCountCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 6});
        seatCountCombo.setSelectedIndex(1); // default 2
        seatCountCombo.setPreferredSize(new Dimension(55, 30));
        seatCountCombo.setToolTipText("Seats to auto-pick");

        JButton btnAiPick = new JButton("Auto-Pick Seats");
        btnAiPick.setFont(Theme.FONT_BOLD);
        btnAiPick.setBackground(new Color(13, 148, 136)); // Emerald Teal
        btnAiPick.setForeground(Color.WHITE);
        btnAiPick.setFocusPainted(false);
        btnAiPick.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAiPick.addActionListener(e -> performAiAutoPick());

        row2Right.add(lblAi);
        row2Right.add(aiRecommendCombo);
        row2Right.add(seatCountCombo);
        row2Right.add(btnAiPick);

        row2.add(route, BorderLayout.WEST);
        row2.add(row2Right, BorderLayout.EAST);

        card.add(row1);
        card.add(row2);

        container.add(card, BorderLayout.NORTH);
        container.add(createLegendBar(), BorderLayout.SOUTH);
        return container;
    }

    private JPanel createLegendBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 8));
        bar.setBackground(Theme.CARD_BG);
        bar.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 16, 4, 16)
        ));

        lblAvailableCount = new JLabel();
        lblOccupiedCount = new JLabel();
        lblSelectedCount = new JLabel();

        bar.add(createLegendBadge("Available", Theme.SEAT_AVAILABLE, lblAvailableCount));
        bar.add(createLegendBadge("Your Selection", Theme.SEAT_SELECTED, lblSelectedCount));
        bar.add(createLegendBadge("Occupied / Booked", Theme.SEAT_OCCUPIED, lblOccupiedCount));
        bar.add(createLegendBadge("Ladies Reserved", Theme.SEAT_LADIES, null));

        return bar;
    }

    private JPanel createLegendBadge(String label, Color color, JLabel countLabel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 18, 18, 6, 6);
                g2.setColor(color.darker());
                g2.drawRoundRect(0, 0, 18, 18, 6, 6);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(19, 19));
        dot.setOpaque(false);

        JLabel text = new JLabel(label);
        text.setFont(Theme.FONT_BOLD);
        text.setForeground(Theme.TEXT_LIGHT);

        p.add(dot);
        p.add(text);
        if (countLabel != null) {
            countLabel.setFont(Theme.FONT_BOLD);
            countLabel.setForeground(Theme.TEXT_MUTED);
            p.add(countLabel);
        }
        return p;
    }

    private JScrollPane createCabinScrollPane() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_DARK);

        // Vehicle Chassis Frame Panel
        JPanel chassis = new JPanel(new BorderLayout(14, 16));
        chassis.setBackground(Theme.CARD_BG);
        chassis.setBorder(new CompoundBorder(
                new LineBorder(new Color(71, 85, 105), 3, true),
                new EmptyBorder(20, 30, 20, 30)
        ));

        // Front Cockpit / Engine Banner
        JPanel frontBanner = new JPanel(new BorderLayout());
        frontBanner.setBackground(new Color(24, 33, 47));
        frontBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel frontLeft = new JLabel(trip.getVehicleType() == VehicleType.TRAIN ?
                ">> FORWARD ENGINE DIRECTION >>" :
                ">> DRIVER COCKPIT & ENTRY DOOR >>");
        frontLeft.setFont(Theme.FONT_BOLD);
        frontLeft.setForeground(new Color(148, 163, 184));

        JLabel frontRight = new JLabel("AIR-CONDITIONED | HIGH-SPEED WI-FI ONBOARD");
        frontRight.setFont(Theme.FONT_SMALL);
        frontRight.setForeground(new Color(100, 116, 139));

        frontBanner.add(frontLeft, BorderLayout.WEST);
        frontBanner.add(frontRight, BorderLayout.EAST);
        chassis.add(frontBanner, BorderLayout.NORTH);

        // Center Matrix Grid
        chassis.add(createMatrixGrid(), BorderLayout.CENTER);

        // Rear Banner
        JPanel rearBanner = new JPanel(new BorderLayout());
        rearBanner.setBackground(new Color(24, 33, 47));
        rearBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel rearLeft = new JLabel("<< REAR WASHROOMS & WATER DISPENSER");
        rearLeft.setFont(Theme.FONT_SMALL);
        rearLeft.setForeground(new Color(100, 116, 139));

        JLabel rearRight = new JLabel("EMERGENCY EXIT [REAR]");
        rearRight.setFont(Theme.FONT_BOLD);
        rearRight.setForeground(new Color(239, 68, 68));

        rearBanner.add(rearLeft, BorderLayout.WEST);
        rearBanner.add(rearRight, BorderLayout.EAST);
        chassis.add(rearBanner, BorderLayout.SOUTH);

        wrapper.add(chassis, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel createMatrixGrid() {
        seatButtons.clear();

        int maxRow = 0;
        int maxCol = 0;
        for (Seat s : seatList) {
            if (s.getRowIndex() > maxRow) maxRow = s.getRowIndex();
            if (s.getColIndex() > maxCol) maxCol = s.getColIndex();
        }
        int rows = maxRow + 1;
        int cols = maxCol + 1;

        Seat[][] grid = new Seat[rows][cols];
        for (Seat s : seatList) {
            grid[s.getRowIndex()][s.getColIndex()] = s;
        }

        JPanel matrix = new JPanel(new GridBagLayout());
        matrix.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.NONE;

        for (int r = 0; r < rows; r++) {
            // Row number pill
            gbc.gridx = 0;
            gbc.gridy = r;
            JLabel rowPill = new JLabel("Row " + (r + 1));
            rowPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
            rowPill.setForeground(new Color(148, 163, 184));
            matrix.add(rowPill, gbc);

            int colOffset = 1;
            for (int c = 0; c < cols; c++) {
                // Bus Aisle (Center Walkway)
                if (trip.getVehicleType() == VehicleType.BUS && cols == 4 && c == 2) {
                    gbc.gridx = colOffset;
                    gbc.gridy = r;
                    JLabel aisle = new JLabel("  ||  ");
                    aisle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    aisle.setForeground(new Color(100, 116, 139));
                    matrix.add(aisle, gbc);
                    colOffset++;
                }

                // Train Gangway
                if (trip.getVehicleType() == VehicleType.TRAIN && cols == 6 && c == 4) {
                    gbc.gridx = colOffset;
                    gbc.gridy = r;
                    JLabel gangway = new JLabel("  [AISLE]  ");
                    gangway.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    gangway.setForeground(new Color(100, 116, 139));
                    matrix.add(gangway, gbc);
                    colOffset++;
                }

                Seat seat = grid[r][c];
                gbc.gridx = colOffset;
                gbc.gridy = r;

                if (seat != null) {
                    SeatButton btn = new SeatButton(seat);
                    btn.setSelectionListener((toggledSeat, isSelected) -> {
                        if (isSelected) {
                            if (!selectedSeats.contains(toggledSeat)) {
                                selectedSeats.add(toggledSeat);
                            }
                        } else {
                            selectedSeats.remove(toggledSeat);
                        }
                        updateCartState();
                    });
                    seatButtons.add(btn);
                    matrix.add(btn, gbc);
                } else {
                    JPanel empty = new JPanel();
                    empty.setOpaque(false);
                    empty.setPreferredSize(new Dimension(68, 64));
                    matrix.add(empty, gbc);
                }
                colOffset++;
            }
        }

        return matrix;
    }

    private JPanel createFloatingCartTray() {
        JPanel tray = new JPanel(new BorderLayout(16, 10));
        tray.setBackground(new Color(15, 23, 42));
        tray.setBorder(new CompoundBorder(
                new LineBorder(Theme.PRIMARY_BLUE, 2, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Left: Selected Seats Chips
        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);

        lblCartSummary = new JLabel("Select seats to start booking");
        lblCartSummary.setFont(Theme.FONT_SUBTITLE);
        lblCartSummary.setForeground(Theme.TEXT_LIGHT);

        selectedSeatsBadgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selectedSeatsBadgesPanel.setOpaque(false);

        left.add(lblCartSummary, BorderLayout.NORTH);
        left.add(selectedSeatsBadgesPanel, BorderLayout.CENTER);

        // Right: Clear + Big Proceed Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        right.setOpaque(false);

        JButton btnClear = new JButton("Clear All");
        btnClear.setFont(Theme.FONT_REGULAR);
        btnClear.setBackground(new Color(51, 65, 85));
        btnClear.setForeground(Theme.TEXT_LIGHT);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearAllSelections());

        btnProceed = new JButton("Proceed to Booking >>");
        btnProceed.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnProceed.setBackground(new Color(34, 197, 94)); // Vibrant Green
        btnProceed.setForeground(Color.WHITE);
        btnProceed.setFocusPainted(false);
        btnProceed.setPreferredSize(new Dimension(280, 44));
        btnProceed.setEnabled(false);
        btnProceed.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProceed.addActionListener(e -> {
            if (!selectedSeats.isEmpty() && onProceedAction != null) {
                onProceedAction.accept(new ArrayList<>(selectedSeats));
            }
        });

        right.add(btnClear);
        right.add(btnProceed);

        tray.add(left, BorderLayout.CENTER);
        tray.add(right, BorderLayout.EAST);
        return tray;
    }

    private void updateCartState() {
        int avail = 0;
        int occ = 0;
        for (Seat s : seatList) {
            if (s.isOccupied()) occ++;
            else if (s.getStatus() == SeatStatus.AVAILABLE || s.getStatus() == SeatStatus.LADIES_ONLY) avail++;
        }

        lblAvailableCount.setText("(" + avail + ")");
        lblOccupiedCount.setText("(" + occ + ")");
        lblSelectedCount.setText("(" + selectedSeats.size() + ")");

        selectedSeatsBadgesPanel.removeAll();

        if (selectedSeats.isEmpty()) {
            lblCartSummary.setText("Click any green armchair above to select your preferred seats.");
            lblCartSummary.setForeground(Theme.TEXT_MUTED);
            btnProceed.setEnabled(false);
            btnProceed.setText("Proceed to Booking >>");
            btnProceed.setBackground(new Color(51, 65, 85));
        } else {
            double total = 0;
            for (Seat s : selectedSeats) {
                total += s.getPrice();
                selectedSeatsBadgesPanel.add(createSeatChip(s));
            }

            lblCartSummary.setText("Selected " + selectedSeats.size() + " Seat" + (selectedSeats.size() > 1 ? "s" : "") +
                    "  |  Total Fare: Rs. " + (int)total);
            lblCartSummary.setForeground(Theme.TEXT_LIGHT);
            btnProceed.setEnabled(true);
            btnProceed.setText("Confirm " + selectedSeats.size() + " Seats (Rs. " + (int)total + ") >>");
            btnProceed.setBackground(new Color(34, 197, 94));
        }

        selectedSeatsBadgesPanel.revalidate();
        selectedSeatsBadgesPanel.repaint();
    }

    private JPanel createSeatChip(Seat seat) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        chip.setBackground(new Color(30, 58, 138));
        chip.setBorder(new CompoundBorder(
                new LineBorder(new Color(59, 130, 246), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));

        String ladiesBadge = seat.isLadiesOnly() ? " [Ladies]" : "";
        JLabel lbl = new JLabel("Seat " + seat.getSeatNumber() + " (" + seat.getSeatType().getLabel() + ladiesBadge + ") | Rs. " + (int)seat.getPrice());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);

        JButton btnRemove = new JButton("X");
        btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnRemove.setForeground(new Color(248, 113, 113));
        btnRemove.setContentAreaFilled(false);
        btnRemove.setBorderPainted(false);
        btnRemove.setFocusPainted(false);
        btnRemove.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemove.addActionListener(e -> {
            seat.deselect();
            selectedSeats.remove(seat);
            for (SeatButton sb : seatButtons) {
                if (sb.getSeat() == seat) {
                    sb.updateTooltip();
                    sb.repaint();
                    break;
                }
            }
            updateCartState();
        });

        chip.add(lbl);
        chip.add(btnRemove);
        return chip;
    }

    private void clearAllSelections() {
        for (Seat s : selectedSeats) {
            s.deselect();
        }
        selectedSeats.clear();
        for (SeatButton sb : seatButtons) {
            sb.updateTooltip();
            sb.repaint();
        }
        updateCartState();
    }

    private void performAiAutoPick() {
        AISmartEngine.RecommendationType pref = (AISmartEngine.RecommendationType) aiRecommendCombo.getSelectedItem();
        if (pref == null) pref = AISmartEngine.RecommendationType.WINDOW_SCENIC;

        int count = (Integer) seatCountCombo.getSelectedItem();

        clearAllSelections();

        List<Seat> recommended = AISmartEngine.recommendSeats(seatList, pref, count);
        if (recommended.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No available seats matching: " + pref.getLabel(),
                    "AI Recommender",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Seat s : recommended) {
            s.setStatus(SeatStatus.SELECTED);
            selectedSeats.add(s);
        }

        for (SeatButton sb : seatButtons) {
            sb.updateTooltip();
            sb.repaint();
        }

        updateCartState();
    }

    private void showAiDemandDialog() {
        int avail = 0;
        for (Seat s : seatList) {
            if (s.isAvailable()) avail++;
        }
        AISmartEngine.DemandPrediction pred = AISmartEngine.analyzeDemand(seatList.size(), avail);

        String message = "<html><body style='font-family:Segoe UI, sans-serif; width:340px; padding:6px;'>" +
                "<h3 style='color:#3b82f6; margin-bottom:4px;'>[AI] Demand & Price Analytics</h3>" +
                "<hr style='border:1px solid #cbd5e1;'>" +
                "<b>Service:</b> " + trip.getVehicleName() + " (" + trip.getVehicleNumber() + ")<br>" +
                "<b>Occupancy Rate:</b> <span style='font-size:15px; color:#f59e0b;'><b>" + String.format("%.1f", pred.occupancyRate) + "%</b></span><br>" +
                "<b>Demand Status:</b> " + pred.demandLevel + "<br>" +
                "<b>Dynamic Pricing:</b> " + pred.priceTrend + "<br>" +
                "<b>Confirmation Probability:</b> <b style='color:#22c55e;'>" + pred.confirmationProbability + "% Instant Allocation</b><br><br>" +
                "<b>AI Travel Advisory:</b><br>" +
                "<i>" + pred.advice + "</i>" +
                "</body></html>";

        JOptionPane.showMessageDialog(this, new JLabel(message), "AI Demand Intelligence", JOptionPane.INFORMATION_MESSAGE);
    }
}
