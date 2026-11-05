package com.ticketreservation.ui;

import com.ticketreservation.database.DatabaseManager;
import com.ticketreservation.model.Booking;
import com.ticketreservation.model.Passenger;
import com.ticketreservation.model.Seat;
import com.ticketreservation.model.Trip;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Passenger Details entry panel with dynamic forms per seat, promo codes and fare calculation.
 */
public class PassengerDetailsPanel extends JPanel {
    private final Trip trip;
    private final List<Seat> selectedSeats;

    private final List<JTextField> nameFields = new ArrayList<>();
    private final List<JSpinner> ageSpinners = new ArrayList<>();
    private final List<JComboBox<String>> genderCombos = new ArrayList<>();
    private final List<JComboBox<String>> berthPrefCombos = new ArrayList<>();

    private JTextField tfEmail;
    private JTextField tfPhone;
    private JTextField tfPromo;
    private JLabel lblDiscount;
    private JLabel lblTotalPayable;

    private double discountAmount = 0.0;
    private double baseTotal = 0.0;
    private double taxAmount = 0.0;
    private double totalPayable = 0.0;

    private Runnable onBackAction;
    private java.util.function.Consumer<Booking> onBookingConfirmed;

    public PassengerDetailsPanel(Trip trip, List<Seat> selectedSeats) {
        this.trip = trip;
        this.selectedSeats = selectedSeats;
        initUI();
    }

    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    public void setOnBookingConfirmed(java.util.function.Consumer<Booking> onBookingConfirmed) {
        this.onBookingConfirmed = onBookingConfirmed;
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // 1. Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center: Scrollable Passenger List + Contact Details
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setBackground(Theme.BG_DARK);

        centerContainer.add(createPassengerFormsCard());
        centerContainer.add(Box.createRigidArea(new Dimension(0, 14)));
        centerContainer.add(createContactInfoCard());

        JScrollPane scrollPane = new JScrollPane(centerContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // 3. Right: Fare Summary Card
        add(createFareSummaryCard(), BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Passenger Details & Booking Checkout");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel sub = new JLabel(trip.getVehicleName() + " (" + trip.getVehicleNumber() + ") | " +
                trip.getRoute().getRouteName() + " | Selected Seats: " + selectedSeats.size());
        sub.setFont(Theme.FONT_REGULAR);
        sub.setForeground(Theme.TEXT_MUTED);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createPassengerFormsCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Traveler Information (" + selectedSeats.size() + " Passenger" + (selectedSeats.size() > 1 ? "s" : "") + ")");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        nameFields.clear();
        ageSpinners.clear();
        genderCombos.clear();
        berthPrefCombos.clear();

        for (int i = 0; i < selectedSeats.size(); i++) {
            Seat seat = selectedSeats.get(i);
            JPanel rowPanel = new JPanel(new GridBagLayout());
            rowPanel.setOpaque(false);
            rowPanel.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 6, 4, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Seat Badge
            JLabel seatBadge = new JLabel(" Seat " + seat.getSeatNumber() + " [" + seat.getSeatType().getLabel() + "] ");
            seatBadge.setFont(Theme.FONT_BOLD);
            seatBadge.setForeground(Color.WHITE);
            seatBadge.setOpaque(true);
            seatBadge.setBackground(Theme.SEAT_SELECTED);
            seatBadge.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));

            JTextField tfName = new JTextField("Passenger " + (i + 1), 14);
            tfName.setFont(Theme.FONT_REGULAR);

            JSpinner spAge = new JSpinner(new SpinnerNumberModel(28 + i * 4, 1, 100, 1));
            spAge.setPreferredSize(new Dimension(60, 28));

            JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
            JComboBox<String> cbBerth = new JComboBox<>(new String[]{"No Preference", "Lower Berth", "Middle Berth", "Upper Berth", "Window Side"});

            nameFields.add(tfName);
            ageSpinners.add(spAge);
            genderCombos.add(cbGender);
            berthPrefCombos.add(cbBerth);

            // Row layout
            gbc.gridx = 0; gbc.gridy = 0;
            rowPanel.add(new JLabel("Assigned Seat:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0;
            rowPanel.add(new JLabel("Full Legal Name:"), gbc);
            gbc.gridx = 2; gbc.gridy = 0;
            rowPanel.add(new JLabel("Age:"), gbc);
            gbc.gridx = 3; gbc.gridy = 0;
            rowPanel.add(new JLabel("Gender:"), gbc);
            gbc.gridx = 4; gbc.gridy = 0;
            rowPanel.add(new JLabel("Preference:"), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            rowPanel.add(seatBadge, gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            rowPanel.add(tfName, gbc);
            gbc.gridx = 2; gbc.gridy = 1;
            rowPanel.add(spAge, gbc);
            gbc.gridx = 3; gbc.gridy = 1;
            rowPanel.add(cbGender, gbc);
            gbc.gridx = 4; gbc.gridy = 1;
            rowPanel.add(cbBerth, gbc);

            card.add(rowPanel);
            card.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        return card;
    }

    private JPanel createContactInfoCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Contact Details (For E-Ticket SMS & Email Delivery)");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        tfEmail = new JTextField("traveler@example.com", 20);
        tfPhone = new JTextField("+91 98765 12340", 15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblEmail = new JLabel("Email Address:");
        lblEmail.setForeground(Theme.TEXT_MUTED);
        card.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JLabel lblPhone = new JLabel("Mobile Number:");
        lblPhone.setForeground(Theme.TEXT_MUTED);
        card.add(lblPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        card.add(tfEmail, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        card.add(tfPhone, gbc);

        return card;
    }

    private JPanel createFareSummaryCard() {
        JPanel card = Theme.createCardPanel();
        card.setPreferredSize(new Dimension(320, 420));
        card.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel title = new JLabel("Fare Summary");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        baseTotal = 0;
        for (Seat s : selectedSeats) {
            baseTotal += s.getPrice();
        }
        taxAmount = baseTotal * 0.05; // 5% GST
        recalculateFare();

        content.add(createFareRow("Base Fare (" + selectedSeats.size() + " seats):", "Rs. " + (int)baseTotal));
        content.add(createFareRow("Reservation / Station Fee:", "Rs. 40"));
        content.add(createFareRow("GST / Taxes (5%):", "Rs. " + (int)taxAmount));

        // Promo code row
        JPanel promoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        promoPanel.setOpaque(false);
        tfPromo = new JTextField("SAVE10", 8);
        JButton btnApply = new JButton("Apply");
        btnApply.setFont(Theme.FONT_SMALL);
        btnApply.setBackground(Theme.PRIMARY_BLUE);
        btnApply.setForeground(Color.WHITE);
        btnApply.addActionListener(e -> applyPromoCode());

        promoPanel.add(new JLabel("Promo: "));
        promoPanel.add(tfPromo);
        promoPanel.add(btnApply);

        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(promoPanel);

        lblDiscount = new JLabel("Discount Applied: -Rs. 0");
        lblDiscount.setFont(Theme.FONT_BOLD);
        lblDiscount.setForeground(Theme.SEAT_AVAILABLE);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(lblDiscount);

        content.add(Box.createRigidArea(new Dimension(0, 10)));
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_COLOR);
        content.add(sep);
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        lblTotalPayable = new JLabel("Total: Rs. " + (int)totalPayable);
        lblTotalPayable.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalPayable.setForeground(Theme.ACCENT_AMBER);
        content.add(lblTotalPayable);

        card.add(content, BorderLayout.CENTER);

        // Bottom Action buttons
        JPanel actions = new JPanel(new GridLayout(2, 1, 6, 6));
        actions.setOpaque(false);

        JButton btnConfirm = new JButton("Pay & Confirm Booking >>");
        btnConfirm.setFont(Theme.FONT_BOLD);
        btnConfirm.setBackground(Theme.SEAT_AVAILABLE);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> processConfirmation());

        JButton btnBack = new JButton("< Back to Seat Map");
        btnBack.setFont(Theme.FONT_REGULAR);
        btnBack.setBackground(new Color(51, 65, 85));
        btnBack.setForeground(Theme.TEXT_LIGHT);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });

        actions.add(btnConfirm);
        actions.add(btnBack);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFareRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(300, 24));

        JLabel l = new JLabel(label);
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_BOLD);
        v.setForeground(Theme.TEXT_LIGHT);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private void recalculateFare() {
        totalPayable = Math.max(0, (baseTotal + 40 + taxAmount) - discountAmount);
        if (lblTotalPayable != null) {
            lblTotalPayable.setText("Total: Rs. " + (int)totalPayable);
        }
    }

    private void applyPromoCode() {
        String code = tfPromo.getText().trim().toUpperCase();
        if (code.equals("SAVE10")) {
            discountAmount = (baseTotal * 0.10);
            lblDiscount.setText("Discount Applied (10%): -Rs. " + (int)discountAmount);
            JOptionPane.showMessageDialog(this, "Promo code 'SAVE10' applied! You saved Rs. " + (int)discountAmount, "Promo Applied", JOptionPane.INFORMATION_MESSAGE);
        } else if (code.equals("DIWALI20") || code.equals("FESTIVE20")) {
            discountAmount = (baseTotal * 0.20);
            lblDiscount.setText("Discount Applied (20%): -Rs. " + (int)discountAmount);
            JOptionPane.showMessageDialog(this, "Festival offer applied! 20% discount applied: -Rs. " + (int)discountAmount, "Promo Applied", JOptionPane.INFORMATION_MESSAGE);
        } else {
            discountAmount = 0.0;
            lblDiscount.setText("Invalid or Expired Code");
            JOptionPane.showMessageDialog(this, "Invalid promo code. Try 'SAVE10' or 'DIWALI20'.", "Invalid Code", JOptionPane.WARNING_MESSAGE);
        }
        recalculateFare();
    }

    private void processConfirmation() {
        // Validate
        for (int i = 0; i < nameFields.size(); i++) {
            if (nameFields.get(i).getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter legal name for Passenger " + (i + 1), "Missing Name", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        if (email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide contact email and mobile number.", "Missing Contact", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate PNR
        Random rand = new Random();
        int randomDigits = 100000 + rand.nextInt(900000);
        String pnr = "PNR-" + randomDigits;

        Booking booking = new Booking(
                pnr,
                trip.getId(),
                trip.getVehicleType(),
                trip.getVehicleName(),
                trip.getVehicleNumber(),
                trip.getTravelClass(),
                trip.getRoute().getRouteName(),
                trip.getJourneyDate().toString(),
                trip.getDepartureTime().toString(),
                trip.getArrivalTime().toString(),
                email,
                phone,
                totalPayable,
                "CONFIRMED",
                LocalDateTime.now()
        );

        for (int i = 0; i < selectedSeats.size(); i++) {
            Seat seat = selectedSeats.get(i);
            String name = nameFields.get(i).getText().trim();
            int age = (Integer) ageSpinners.get(i).getValue();
            String gender = (String) genderCombos.get(i).getSelectedItem();
            String pref = (String) berthPrefCombos.get(i).getSelectedItem();

            Passenger p = new Passenger(name, age, gender, seat.getSeatNumber(), pref, seat.getPrice());
            p.setPnr(pnr);
            booking.addPassenger(p);
        }

        // Save in Database transactionally
        boolean success = DatabaseManager.getInstance().bookSeats(booking);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "[SUCCESS] Payment Successful & Booking Confirmed!\n" +
                            "Your PNR is: " + pnr + "\n" +
                            "Seats " + booking.getSeatNumbersSummary() + " are now locked in the live matrix.",
                    "Booking Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            if (onBookingConfirmed != null) {
                onBookingConfirmed.accept(booking);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Booking failed! One or more seats might have just been booked by another passenger. Please return to the seat matrix.",
                    "Booking Conflict",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
