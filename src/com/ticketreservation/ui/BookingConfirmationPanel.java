package com.ticketreservation.ui;

import com.ticketreservation.model.Booking;
import com.ticketreservation.model.Passenger;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * E-Ticket Confirmation & Boarding Pass presentation panel.
 */
public class BookingConfirmationPanel extends JPanel {
    private final Booking booking;
    private Runnable onNewBookingAction;
    private Runnable onViewAllAction;

    public BookingConfirmationPanel(Booking booking) {
        this.booking = booking;
        initUI();
    }

    public void setOnNewBookingAction(Runnable onNewBookingAction) {
        this.onNewBookingAction = onNewBookingAction;
    }

    public void setOnViewAllAction(Runnable onViewAllAction) {
        this.onViewAllAction = onViewAllAction;
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // Center: Scrollable Boarding Pass Card
        JScrollPane scrollPane = new JScrollPane(createBoardingPassCard());
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Bottom: Action buttons
        add(createActionsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createBoardingPassCard() {
        JPanel card = new JPanel(new BorderLayout(14, 14));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(59, 130, 246), 2, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // 1. Ticket Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);

        Icon vIcon = booking.getVehicleType() == com.ticketreservation.model.VehicleType.TRAIN ?
                IconUtils.createTrainIcon(26, Theme.PRIMARY_BLUE) :
                IconUtils.createBusIcon(26, Theme.ACCENT_AMBER);
        logoPanel.add(new JLabel(vIcon));

        JLabel logo = new JLabel("OFFICIAL E-RESERVATION SLIP (ERS)");
        logo.setFont(Theme.FONT_TITLE);
        logo.setForeground(Theme.TEXT_LIGHT);
        logoPanel.add(logo);

        JPanel pnrBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnrBadge.setOpaque(false);

        JLabel lblPnr = new JLabel(" PNR: " + booking.getPnr() + " ");
        lblPnr.setFont(new Font("Consolas", Font.BOLD, 18));
        lblPnr.setForeground(Color.WHITE);
        lblPnr.setOpaque(true);
        lblPnr.setBackground(new Color(37, 99, 235));
        lblPnr.setBorder(new LineBorder(Color.WHITE, 1, true));

        JLabel lblStatus = new JLabel(" " + booking.getStatus() + " ");
        lblStatus.setFont(Theme.FONT_BOLD);
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(booking.getStatus().equals("CONFIRMED") ? Theme.SEAT_AVAILABLE : Theme.SEAT_OCCUPIED);
        lblStatus.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));

        pnrBadge.add(lblPnr);
        pnrBadge.add(lblStatus);

        topBar.add(logoPanel, BorderLayout.WEST);
        topBar.add(pnrBadge, BorderLayout.EAST);

        // 2. Journey Details Grid
        JPanel journeyGrid = new JPanel(new GridLayout(3, 2, 12, 10));
        journeyGrid.setOpaque(false);
        journeyGrid.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        journeyGrid.add(createField("Vehicle:", booking.getVehicleName() + " (" + booking.getVehicleNumber() + ")"));
        journeyGrid.add(createField("Travel Class:", booking.getTravelClass()));
        journeyGrid.add(createField("Route:", booking.getRouteName()));
        journeyGrid.add(createField("Journey Date:", booking.getJourneyDate()));
        journeyGrid.add(createField("Departure Time:", booking.getDepartureTime()));
        journeyGrid.add(createField("Arrival Time:", booking.getArrivalTime()));

        // 3. Passenger Table
        JPanel psgSection = new JPanel(new BorderLayout(6, 6));
        psgSection.setOpaque(false);

        JLabel psgTitle = new JLabel("Passenger Information & Allocated Seats:");
        psgTitle.setFont(Theme.FONT_SUBTITLE);
        psgTitle.setForeground(Theme.TEXT_LIGHT);
        psgSection.add(psgTitle, BorderLayout.NORTH);

        String[] cols = {"#", "Passenger Name", "Age", "Gender", "Seat Number", "Preference", "Status"};
        Object[][] data = new Object[booking.getPassengers().size()][7];
        for (int i = 0; i < booking.getPassengers().size(); i++) {
            Passenger p = booking.getPassengers().get(i);
            data[i][0] = i + 1;
            data[i][1] = p.getName();
            data[i][2] = p.getAge();
            data[i][3] = p.getGender();
            data[i][4] = p.getSeatNumber();
            data[i][5] = p.getBerthPreference();
            data[i][6] = "CONFIRMED";
        }

        JTable table = new JTable(data, cols);
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(26);
        table.getTableHeader().setFont(Theme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(30, 41, 59));
        table.getTableHeader().setForeground(Theme.TEXT_LIGHT);
        table.setBackground(new Color(15, 23, 42));
        table.setForeground(Theme.TEXT_LIGHT);
        table.setGridColor(Theme.BORDER_COLOR);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(650, 110));
        tableScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        psgSection.add(tableScroll, BorderLayout.CENTER);

        // 4. Contact & Total Fare Bar
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setOpaque(false);

        JLabel contact = new JLabel("Delivered to: " + booking.getContactEmail() + " | " + booking.getContactPhone());
        contact.setForeground(Theme.TEXT_MUTED);

        JLabel fare = new JLabel("Total Paid: Rs. " + (int)booking.getTotalFare());
        fare.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fare.setForeground(Theme.ACCENT_AMBER);

        footerBar.add(contact, BorderLayout.WEST);
        footerBar.add(fare, BorderLayout.EAST);

        // Combine into Card
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        mainContent.add(topBar);
        mainContent.add(Box.createRigidArea(new Dimension(0, 14)));
        mainContent.add(journeyGrid);
        mainContent.add(Box.createRigidArea(new Dimension(0, 14)));
        mainContent.add(psgSection);
        mainContent.add(Box.createRigidArea(new Dimension(0, 14)));
        mainContent.add(footerBar);

        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createField(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 1, 1));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_BOLD);
        v.setForeground(Theme.TEXT_LIGHT);

        p.add(l);
        p.add(v);
        return p;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panel.setOpaque(false);

        JButton btnSave = new JButton("Save E-Ticket (.txt)");
        btnSave.setFont(Theme.FONT_BOLD);
        btnSave.setBackground(new Color(51, 65, 85));
        btnSave.setForeground(Theme.TEXT_LIGHT);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveTicketToFile());

        JButton btnManage = new JButton("View My Bookings");
        btnManage.setFont(Theme.FONT_BOLD);
        btnManage.setBackground(new Color(79, 70, 229));
        btnManage.setForeground(Color.WHITE);
        btnManage.setFocusPainted(false);
        btnManage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnManage.addActionListener(e -> {
            if (onViewAllAction != null) onViewAllAction.run();
        });

        JButton btnNew = new JButton("Book Another Journey >>");
        btnNew.setFont(Theme.FONT_BOLD);
        btnNew.setBackground(Theme.PRIMARY_BLUE);
        btnNew.setForeground(Color.WHITE);
        btnNew.setFocusPainted(false);
        btnNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNew.addActionListener(e -> {
            if (onNewBookingAction != null) onNewBookingAction.run();
        });

        panel.add(btnSave);
        panel.add(btnManage);
        panel.add(btnNew);
        return panel;
    }

    private void saveTicketToFile() {
        String fileName = "Ticket_" + booking.getPnr() + ".txt";
        File file = new File(fileName);

        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("         OFFICIAL ELECTRONIC RESERVATION SLIP (ERS)              \n");
        sb.append("=================================================================\n");
        sb.append("PNR NUMBER     : ").append(booking.getPnr()).append("\n");
        sb.append("BOOKING STATUS : ").append(booking.getStatus()).append("\n");
        sb.append("VEHICLE TYPE   : ").append(booking.getVehicleType().getDisplayName()).append("\n");
        sb.append("SERVICE NAME   : ").append(booking.getVehicleName()).append(" (").append(booking.getVehicleNumber()).append(")\n");
        sb.append("TRAVEL CLASS   : ").append(booking.getTravelClass()).append("\n");
        sb.append("ROUTE          : ").append(booking.getRouteName()).append("\n");
        sb.append("JOURNEY DATE   : ").append(booking.getJourneyDate()).append("\n");
        sb.append("DEPARTURE      : ").append(booking.getDepartureTime()).append("\n");
        sb.append("ARRIVAL        : ").append(booking.getArrivalTime()).append("\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("PASSENGER DETAILS:\n");
        int idx = 1;
        for (Passenger p : booking.getPassengers()) {
            sb.append("  ").append(idx++).append(". ")
                    .append(p.getName()).append(" | Age: ").append(p.getAge())
                    .append(" | ").append(p.getGender())
                    .append(" | Allocated Seat: ").append(p.getSeatNumber())
                    .append(" | Fare: Rs. ").append((int)p.getFare()).append("\n");
        }
        sb.append("-----------------------------------------------------------------\n");
        sb.append("TOTAL FARE PAID: Rs. ").append((int)booking.getTotalFare()).append("\n");
        sb.append("CONTACT EMAIL  : ").append(booking.getContactEmail()).append("\n");
        sb.append("CONTACT PHONE  : ").append(booking.getContactPhone()).append("\n");
        sb.append("BOOKING TIME   : ").append(booking.getBookingTimestamp()).append("\n");
        sb.append("=================================================================\n");
        sb.append("Thank you for booking with us. Have a safe and comfortable journey!\n");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sb.toString());
            JOptionPane.showMessageDialog(this,
                    "E-Ticket saved successfully to:\n" + file.getAbsolutePath(),
                    "Ticket Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save ticket: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
