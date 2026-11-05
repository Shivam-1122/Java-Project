package com.ticketreservation.ui;

import com.ticketreservation.database.DatabaseManager;
import com.ticketreservation.model.Booking;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel to look up, manage, inspect and cancel reservations.
 * Cancelling a ticket immediately frees up the seats on the live matrix!
 */
public class MyBookingsPanel extends JPanel {
    private JTextField tfSearchPnr;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private List<Booking> currentBookings;
    private JLabel lblStatusCount;

    private java.util.function.Consumer<Booking> onViewTicketAction;

    public MyBookingsPanel() {
        initUI();
    }

    public void setOnViewTicketAction(java.util.function.Consumer<Booking> onViewTicketAction) {
        this.onViewTicketAction = onViewTicketAction;
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        // 1. Top Search & Filter Bar
        add(createTopSearchBar(), BorderLayout.NORTH);

        // 2. Center Table
        add(createBookingsTableScrollPane(), BorderLayout.CENTER);

        // 3. Bottom Action Bar
        add(createBottomActionBar(), BorderLayout.SOUTH);

        refreshBookingsList();
    }

    private JPanel createTopSearchBar() {
        JPanel bar = Theme.createCardPanel();
        bar.setLayout(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel title = new JLabel("Manage Bookings & PNR Tracking");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_LIGHT);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        tfSearchPnr = new JTextField(14);
        tfSearchPnr.setFont(Theme.FONT_REGULAR);
        tfSearchPnr.setToolTipText("Enter PNR (e.g. PNR-928174) or Mobile number");

        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(Theme.FONT_BOLD);
        btnSearch.setBackground(Theme.PRIMARY_BLUE);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> searchSpecificBooking());

        JButton btnRefresh = new JButton("Show All");
        btnRefresh.setFont(Theme.FONT_REGULAR);
        btnRefresh.setBackground(new Color(51, 65, 85));
        btnRefresh.setForeground(Theme.TEXT_LIGHT);
        btnRefresh.addActionListener(e -> refreshBookingsList());

        right.add(new JLabel("PNR / Phone:"));
        right.add(tfSearchPnr);
        right.add(btnSearch);
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane createBookingsTableScrollPane() {
        String[] columnNames = {"PNR", "Type", "Vehicle Name", "Route", "Date", "Seats", "Total Fare", "Status", "Contact"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setFont(Theme.FONT_REGULAR);
        bookingsTable.setRowHeight(32);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.getTableHeader().setFont(Theme.FONT_BOLD);
        bookingsTable.getTableHeader().setBackground(new Color(30, 41, 59));
        bookingsTable.getTableHeader().setForeground(Theme.TEXT_LIGHT);
        bookingsTable.setBackground(new Color(15, 23, 42));
        bookingsTable.setForeground(Theme.TEXT_LIGHT);
        bookingsTable.setGridColor(Theme.BORDER_COLOR);

        // Custom renderer for Status column
        bookingsTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = String.valueOf(value);
                if ("CONFIRMED".equalsIgnoreCase(status)) {
                    setForeground(Theme.SEAT_AVAILABLE);
                    setFont(Theme.FONT_BOLD);
                } else {
                    setForeground(Theme.SEAT_OCCUPIED);
                    setFont(Theme.FONT_BOLD);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        return scrollPane;
    }

    private JPanel createBottomActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        lblStatusCount = new JLabel("Total Bookings: 0");
        lblStatusCount.setForeground(Theme.TEXT_MUTED);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnViewTicket = new JButton("View Full E-Ticket");
        btnViewTicket.setFont(Theme.FONT_BOLD);
        btnViewTicket.setBackground(Theme.PRIMARY_BLUE);
        btnViewTicket.setForeground(Color.WHITE);
        btnViewTicket.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewTicket.addActionListener(e -> viewSelectedTicket());

        JButton btnCancel = new JButton("Cancel Booking & Free Seats");
        btnCancel.setFont(Theme.FONT_BOLD);
        btnCancel.setBackground(Theme.SEAT_OCCUPIED);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> cancelSelectedBooking());

        right.add(btnViewTicket);
        right.add(btnCancel);

        bar.add(lblStatusCount, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    public void refreshBookingsList() {
        currentBookings = DatabaseManager.getInstance().getAllBookings();
        renderTableRows(currentBookings);
    }

    private void renderTableRows(List<Booking> list) {
        tableModel.setRowCount(0);
        for (Booking b : list) {
            tableModel.addRow(new Object[]{
                    b.getPnr(),
                    b.getVehicleType().getDisplayName(),
                    b.getVehicleName() + " (" + b.getVehicleNumber() + ")",
                    b.getRouteName(),
                    b.getJourneyDate(),
                    b.getSeatNumbersSummary(),
                    "Rs. " + (int)b.getTotalFare(),
                    b.getStatus(),
                    b.getContactPhone()
            });
        }
        lblStatusCount.setText("Total Bookings in Database: " + list.size());
    }

    private void searchSpecificBooking() {
        String query = tfSearchPnr.getText().trim();
        if (query.isEmpty()) {
            refreshBookingsList();
            return;
        }

        Booking found = DatabaseManager.getInstance().getBookingByPnr(query);
        if (found != null) {
            currentBookings = List.of(found);
            renderTableRows(currentBookings);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No booking found matching PNR / Phone: " + query,
                    "Booking Not Found",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewSelectedTicket() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view its e-ticket.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking b = currentBookings.get(selectedRow);
        if (onViewTicketAction != null) {
            onViewTicketAction.accept(b);
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking b = currentBookings.get(selectedRow);
        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "This booking has already been CANCELLED and refunded.", "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double refundAmount = b.getTotalFare() * 0.90; // 90% refund
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking: " + b.getPnr() + "?\n\n" +
                        "Vehicle: " + b.getVehicleName() + "\n" +
                        "Allocated Seats to Release: " + b.getSeatNumbersSummary() + "\n" +
                        "Original Fare: Rs. " + (int)b.getTotalFare() + "\n" +
                        "Estimated Refund (90%): Rs. " + (int)refundAmount + "\n\n" +
                        "Notice: The seats will immediately become AVAILABLE (GREEN) on the live seat map!",
                "Confirm Ticket Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = DatabaseManager.getInstance().cancelBooking(b.getPnr());
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Booking " + b.getPnr() + " has been successfully CANCELLED!\n" +
                                "Seats " + b.getSeatNumbersSummary() + " have been released back to Available status.\n" +
                                "Refund of Rs. " + (int)refundAmount + " credited to original payment method.",
                        "Cancellation Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshBookingsList();
            } else {
                JOptionPane.showMessageDialog(this, "Error cancelling booking from database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
