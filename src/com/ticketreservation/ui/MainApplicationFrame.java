package com.ticketreservation.ui;

import com.ticketreservation.database.DatabaseManager;
import com.ticketreservation.model.Booking;
import com.ticketreservation.model.Seat;
import com.ticketreservation.model.Trip;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main application window coordinating navigation, views, database state and AI tools.
 */
public class MainApplicationFrame extends JFrame {
    private static final String VIEW_SEARCH = "SEARCH";
    private static final String VIEW_SEATS = "SEATS";
    private static final String VIEW_PASSENGERS = "PASSENGERS";
    private static final String VIEW_TICKET = "TICKET";
    private static final String VIEW_MY_BOOKINGS = "MY_BOOKINGS";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    private SearchAndFilterPanel searchPanel;
    private SeatMatrixPanel seatMatrixPanel;
    private PassengerDetailsPanel passengerPanel;
    private BookingConfirmationPanel confirmationPanel;
    private MyBookingsPanel myBookingsPanel;

    private JLabel lblDbStatusBadge;
    private JLabel lblClock;

    private Trip currentSelectedTrip;

    public MainApplicationFrame() {
        super("Bus & Train Ticket Reservation System | Java AWT + Swing | JDBC | MySQL Workbench | AI Tools");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 820);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);

        // Sub Header Stepper Bar
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(createTopNavBar(), BorderLayout.NORTH);
        topContainer.add(createStepperBar(), BorderLayout.SOUTH);

        root.add(topContainer, BorderLayout.NORTH);

        // 2. Central Card View Stack
        setupCardViews();
        root.add(cardsPanel, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        root.add(createBottomStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);

        // Start clock timer
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
        updateClock();
    }

    private JLabel step1Label;
    private JLabel step2Label;
    private JLabel step3Label;
    private JLabel step4Label;

    private JPanel createStepperBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        bar.setBackground(new Color(20, 29, 44));
        bar.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, false),
                new EmptyBorder(4, 10, 4, 10)
        ));

        step1Label = createStepBadge("1. Select Schedule", true);
        step2Label = createStepBadge("2. Choose Seats (Matrix)", false);
        step3Label = createStepBadge("3. Passenger Info", false);
        step4Label = createStepBadge("4. Boarding Pass", false);

        bar.add(step1Label);
        bar.add(new JLabel("->"));
        bar.add(step2Label);
        bar.add(new JLabel("->"));
        bar.add(step3Label);
        bar.add(new JLabel("->"));
        bar.add(step4Label);

        return bar;
    }

    private JLabel createStepBadge(String text, boolean active) {
        JLabel l = new JLabel(" " + text + " ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setOpaque(true);
        updateStepStyle(l, active);
        l.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        return l;
    }

    private void updateStepStyle(JLabel l, boolean active) {
        if (active) {
            l.setBackground(Theme.PRIMARY_BLUE);
            l.setForeground(Color.WHITE);
        } else {
            l.setBackground(new Color(30, 41, 59));
            l.setForeground(Theme.TEXT_MUTED);
        }
    }

    private void updateStepperForView(String viewName) {
        if (step1Label == null) return;
        updateStepStyle(step1Label, VIEW_SEARCH.equals(viewName));
        updateStepStyle(step2Label, VIEW_SEATS.equals(viewName));
        updateStepStyle(step3Label, VIEW_PASSENGERS.equals(viewName));
        updateStepStyle(step4Label, VIEW_TICKET.equals(viewName));
    }

    private JPanel createTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout(10, 10));
        nav.setBackground(Theme.PANEL_HEADER_BG);
        nav.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Left Branding
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);

        JLabel logo = new JLabel(IconUtils.createTrainIcon(28, Theme.PRIMARY_BLUE));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 0));
        titleBlock.setOpaque(false);

        JLabel appTitle = new JLabel("JourneyPass Express");
        appTitle.setFont(Theme.FONT_TITLE);
        appTitle.setForeground(Theme.TEXT_LIGHT);

        JLabel appSub = new JLabel("Bus & Train Ticket Reservation System | JDBC & MySQL Workbench");
        appSub.setFont(Theme.FONT_SMALL);
        appSub.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(appTitle);
        titleBlock.add(appSub);

        brand.add(logo);
        brand.add(titleBlock);

        // Right Navigation Buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtons.setOpaque(false);

        JButton btnSearch = createNavButton("Search Schedules", () -> showView(VIEW_SEARCH));
        JButton btnMyBookings = createNavButton("My Bookings & PNR", () -> {
            myBookingsPanel.refreshBookingsList();
            showView(VIEW_MY_BOOKINGS);
        });

        JButton btnAi = new JButton("AI Travel Assistant");
        btnAi.setFont(Theme.FONT_BOLD);
        btnAi.setBackground(new Color(99, 102, 241)); // Indigo
        btnAi.setForeground(Color.WHITE);
        btnAi.setFocusPainted(false);
        btnAi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAi.addActionListener(e -> new AIAssistantDialog(this).setVisible(true));

        // DB Status Badge
        lblDbStatusBadge = new JLabel();
        lblDbStatusBadge.setFont(Theme.FONT_BOLD);
        lblDbStatusBadge.setOpaque(true);
        lblDbStatusBadge.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        updateDbStatusBadge();

        navButtons.add(btnSearch);
        navButtons.add(btnMyBookings);
        navButtons.add(btnAi);
        navButtons.add(lblDbStatusBadge);

        nav.add(brand, BorderLayout.WEST);
        nav.add(navButtons, BorderLayout.EAST);
        return nav;
    }

    private JButton createNavButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BOLD);
        btn.setBackground(Theme.CARD_BG);
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void setupCardViews() {
        cardsPanel.setBackground(Theme.BG_DARK);

        // 1. Search Panel
        searchPanel = new SearchAndFilterPanel();
        searchPanel.setOnTripSelectedListener(this::openSeatMatrixForTrip);
        cardsPanel.add(searchPanel, VIEW_SEARCH);

        // 2. My Bookings Panel
        myBookingsPanel = new MyBookingsPanel();
        myBookingsPanel.setOnViewTicketAction(this::showConfirmationTicket);
        cardsPanel.add(myBookingsPanel, VIEW_MY_BOOKINGS);

        showView(VIEW_SEARCH);
    }

    private void openSeatMatrixForTrip(Trip trip) {
        this.currentSelectedTrip = trip;
        List<Seat> seats = DatabaseManager.getInstance().getSeatsForTrip(trip.getId());

        seatMatrixPanel = new SeatMatrixPanel(trip, seats);
        seatMatrixPanel.setOnBackAction(() -> showView(VIEW_SEARCH));
        seatMatrixPanel.setOnProceedAction(selectedSeats -> openPassengerDetails(trip, selectedSeats));

        cardsPanel.add(seatMatrixPanel, VIEW_SEATS);
        showView(VIEW_SEATS);
    }

    private void openPassengerDetails(Trip trip, List<Seat> selectedSeats) {
        passengerPanel = new PassengerDetailsPanel(trip, selectedSeats);
        passengerPanel.setOnBackAction(() -> showView(VIEW_SEATS));
        passengerPanel.setOnBookingConfirmed(booking -> {
            showConfirmationTicket(booking);
            // Refresh search panel in background
            searchPanel.performSearch();
        });

        cardsPanel.add(passengerPanel, VIEW_PASSENGERS);
        showView(VIEW_PASSENGERS);
    }

    private void showConfirmationTicket(Booking booking) {
        confirmationPanel = new BookingConfirmationPanel(booking);
        confirmationPanel.setOnNewBookingAction(() -> {
            searchPanel.performSearch();
            showView(VIEW_SEARCH);
        });
        confirmationPanel.setOnViewAllAction(() -> {
            myBookingsPanel.refreshBookingsList();
            showView(VIEW_MY_BOOKINGS);
        });

        cardsPanel.add(confirmationPanel, VIEW_TICKET);
        showView(VIEW_TICKET);
    }

    private void showView(String viewName) {
        cardLayout.show(cardsPanel, viewName);
        updateStepperForView(viewName);
    }

    private JPanel createBottomStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(new Color(15, 23, 42));
        status.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, false),
                new EmptyBorder(6, 16, 6, 16)
        ));

        JLabel info = new JLabel("* Interactive Seat Matrix: Green = Available | Blue = Selected | Red = Occupied | Purple = Ladies Reserved");
        info.setFont(Theme.FONT_SMALL);
        info.setForeground(Theme.TEXT_MUTED);

        lblClock = new JLabel();
        lblClock.setFont(Theme.FONT_SMALL);
        lblClock.setForeground(Theme.TEXT_MUTED);

        status.add(info, BorderLayout.WEST);
        status.add(lblClock, BorderLayout.EAST);
        return status;
    }

    private void updateClock() {
        lblClock.setText("Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | HH:mm:ss")));
    }

    private void updateDbStatusBadge() {
        String msg = DatabaseManager.getInstance().getConnectionStatusMessage();
        if (msg.contains("MySQL")) {
            lblDbStatusBadge.setText(" MySQL Connected ");
            lblDbStatusBadge.setBackground(new Color(20, 83, 45));
            lblDbStatusBadge.setForeground(Color.WHITE);
            lblDbStatusBadge.setToolTipText(msg);
        } else {
            lblDbStatusBadge.setText(" SQLite Mode ");
            lblDbStatusBadge.setBackground(new Color(30, 58, 138));
            lblDbStatusBadge.setForeground(Color.WHITE);
            lblDbStatusBadge.setToolTipText(msg);
        }
    }
}
