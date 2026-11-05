package com.ticketreservation.ui;

import com.ticketreservation.database.DatabaseManager;
import com.ticketreservation.model.Route;
import com.ticketreservation.model.Trip;
import com.ticketreservation.model.VehicleType;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

/**
 * Modern Search & Booking Schedule Browser with rich cards, amenities badges, and vector icons.
 */
public class SearchAndFilterPanel extends JPanel {
    private JRadioButton rbTrain;
    private JRadioButton rbBus;
    private JRadioButton rbAll;
    private JComboBox<String> cbOrigin;
    private JComboBox<String> cbDestination;
    private JComboBox<String> cbDate;

    private JPanel tripListContainer;
    private java.util.function.Consumer<Trip> onTripSelectedListener;

    public SearchAndFilterPanel() {
        initUI();
    }

    public void setOnTripSelectedListener(java.util.function.Consumer<Trip> listener) {
        this.onTripSelectedListener = listener;
    }

    private void initUI() {
        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Search Bar Card
        add(createSearchFormCard(), BorderLayout.NORTH);

        // 2. Results List
        tripListContainer = new JPanel();
        tripListContainer.setLayout(new BoxLayout(tripListContainer, BoxLayout.Y_AXIS));
        tripListContainer.setBackground(Theme.BG_DARK);

        JScrollPane scrollPane = new JScrollPane(tripListContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(scrollPane, BorderLayout.CENTER);

        performSearch();
    }

    private JPanel createSearchFormCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(14, 14));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        topBar.setOpaque(false);

        JLabel lblMode = new JLabel("Transport Mode:");
        lblMode.setFont(Theme.FONT_BOLD);
        lblMode.setForeground(Theme.TEXT_LIGHT);

        rbAll = new JRadioButton("All Services", true);
        rbTrain = new JRadioButton("Train Reservations");
        rbBus = new JRadioButton("Bus Reservations");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbAll);
        bg.add(rbTrain);
        bg.add(rbBus);

        for (JRadioButton rb : new JRadioButton[]{rbAll, rbTrain, rbBus}) {
            rb.setFont(Theme.FONT_BOLD);
            rb.setForeground(Theme.TEXT_LIGHT);
            rb.setOpaque(false);
            rb.setFocusPainted(false);
            rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            rb.addActionListener(e -> performSearch());
        }

        topBar.add(lblMode);
        topBar.add(rbAll);
        topBar.add(rbTrain);
        topBar.add(rbBus);

        // Center Inputs Grid
        JPanel inputs = new JPanel(new GridBagLayout());
        inputs.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Route> routes = DatabaseManager.getInstance().getAllRoutes();
        Vector<String> origins = new Vector<>();
        origins.add("All Stations");
        Vector<String> destinations = new Vector<>();
        destinations.add("All Stations");

        for (Route r : routes) {
            if (!origins.contains(r.getOrigin())) origins.add(r.getOrigin());
            if (!destinations.contains(r.getDestination())) destinations.add(r.getDestination());
        }

        cbOrigin = new JComboBox<>(origins);
        cbDestination = new JComboBox<>(destinations);
        cbOrigin.setPreferredSize(new Dimension(210, 36));
        cbDestination.setPreferredSize(new Dimension(210, 36));

        JButton btnSwap = new JButton("<>");
        btnSwap.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSwap.setBackground(new Color(51, 65, 85));
        btnSwap.setForeground(Color.WHITE);
        btnSwap.setToolTipText("Swap Origin and Destination");
        btnSwap.setFocusPainted(false);
        btnSwap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSwap.addActionListener(e -> {
            Object orig = cbOrigin.getSelectedItem();
            cbOrigin.setSelectedItem(cbDestination.getSelectedItem());
            cbDestination.setSelectedItem(orig);
            performSearch();
        });

        Vector<String> dates = new Vector<>();
        LocalDate today = LocalDate.now();
        dates.add("Today (" + today + ")");
        dates.add("Tomorrow (" + today.plusDays(1) + ")");
        dates.add("Weekend (" + today.plusDays(3) + ")");
        cbDate = new JComboBox<>(dates);
        cbDate.setPreferredSize(new Dimension(190, 36));

        JButton btnSearch = new JButton("Find Schedules >>");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(Theme.PRIMARY_BLUE);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(170, 36));
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> performSearch());

        // Labels row
        gbc.gridx = 0; gbc.gridy = 0;
        inputs.add(createHeaderLabel("Departure Station / City:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        inputs.add(new JLabel(""), gbc);
        gbc.gridx = 2; gbc.gridy = 0;
        inputs.add(createHeaderLabel("Destination Station / City:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0;
        inputs.add(createHeaderLabel("Travel Date:"), gbc);
        gbc.gridx = 4; gbc.gridy = 0;
        inputs.add(new JLabel(""), gbc);

        // Inputs row
        gbc.gridx = 0; gbc.gridy = 1;
        inputs.add(cbOrigin, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        inputs.add(btnSwap, gbc);
        gbc.gridx = 2; gbc.gridy = 1;
        inputs.add(cbDestination, gbc);
        gbc.gridx = 3; gbc.gridy = 1;
        inputs.add(cbDate, gbc);
        gbc.gridx = 4; gbc.gridy = 1;
        inputs.add(btnSearch, gbc);

        card.add(topBar, BorderLayout.NORTH);
        card.add(inputs, BorderLayout.CENTER);
        return card;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    public void performSearch() {
        VehicleType vType = null;
        if (rbTrain.isSelected()) vType = VehicleType.TRAIN;
        else if (rbBus.isSelected()) vType = VehicleType.BUS;

        String origin = (String) cbOrigin.getSelectedItem();
        String destination = (String) cbDestination.getSelectedItem();

        List<Trip> trips = DatabaseManager.getInstance().searchTrips(vType, origin, destination);

        tripListContainer.removeAll();

        if (trips.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Theme.CARD_BG);
            emptyPanel.setBorder(new EmptyBorder(40, 20, 40, 20));
            JLabel emptyMsg = new JLabel("No matching bus or train trips found. Please reset filters or select 'All Stations'.");
            emptyMsg.setFont(Theme.FONT_SUBTITLE);
            emptyMsg.setForeground(Theme.TEXT_MUTED);
            emptyPanel.add(emptyMsg);
            tripListContainer.add(emptyPanel);
        } else {
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            header.setOpaque(false);
            JLabel lblH = new JLabel("Found " + trips.size() + " Available Departures (Click any service to view seat matrix):");
            lblH.setFont(Theme.FONT_SUBTITLE);
            lblH.setForeground(Theme.TEXT_LIGHT);
            header.add(lblH);
            tripListContainer.add(header);
            tripListContainer.add(Box.createRigidArea(new Dimension(0, 10)));

            for (Trip trip : trips) {
                tripListContainer.add(createModernTripCard(trip));
                tripListContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        tripListContainer.revalidate();
        tripListContainer.repaint();
    }

    private JPanel createModernTripCard(Trip trip) {
        JPanel card = new JPanel(new BorderLayout(16, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // 1. Left: Vehicle Branding & Amenities
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleLine.setOpaque(false);

        Icon vIcon = trip.getVehicleType() == VehicleType.TRAIN ?
                IconUtils.createTrainIcon(24, Theme.PRIMARY_BLUE) :
                IconUtils.createBusIcon(24, Theme.ACCENT_AMBER);
        JLabel iconLbl = new JLabel(vIcon);

        JLabel nameLbl = new JLabel(trip.getVehicleName() + " (" + trip.getVehicleNumber() + ")");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        nameLbl.setForeground(Theme.TEXT_LIGHT);

        JLabel badgeClass = new JLabel(" " + trip.getTravelClass() + " ");
        badgeClass.setFont(Theme.FONT_SMALL);
        badgeClass.setForeground(Color.WHITE);
        badgeClass.setOpaque(true);
        badgeClass.setBackground(new Color(67, 56, 202));
        badgeClass.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));

        titleLine.add(iconLbl);
        titleLine.add(nameLbl);
        titleLine.add(badgeClass);

        // Amenities chips
        JPanel amenities = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        amenities.setOpaque(false);
        amenities.add(createAmenityChip("High-Speed"));
        amenities.add(createAmenityChip("Free Wi-Fi"));
        amenities.add(createAmenityChip("Catering"));
        amenities.add(createAmenityChip("Rating: " + trip.getRating() + " / 5.0"));

        left.add(titleLine);
        left.add(Box.createRigidArea(new Dimension(0, 6)));
        left.add(amenities);

        // 2. Center: Timeline (Departure -> Duration -> Arrival)
        JPanel timeline = new JPanel(new GridLayout(2, 1, 4, 4));
        timeline.setOpaque(false);

        JLabel timing = new JLabel("Dep: " + trip.getDepartureTime() + "   ----- (" +
                trip.getRoute().getEstimatedDuration() + ") ----->   Arr: " + trip.getArrivalTime());
        timing.setFont(new Font("Segoe UI", Font.BOLD, 15));
        timing.setForeground(new Color(56, 189, 248)); // Light sky blue

        JLabel stations = new JLabel(trip.getRoute().getOrigin() + "   ->   " + trip.getRoute().getDestination() +
                " (" + trip.getRoute().getDistanceKm() + " km)");
        stations.setFont(Theme.FONT_REGULAR);
        stations.setForeground(Theme.TEXT_MUTED);

        timeline.add(timing);
        timeline.add(stations);

        // 3. Right: Live Available Seats Pill + Fare + Large Action Button
        JPanel right = new JPanel(new GridLayout(3, 1, 4, 4));
        right.setOpaque(false);

        JLabel fare = new JLabel("Rs. " + (int)trip.getBaseFare() + " / seat", SwingConstants.RIGHT);
        fare.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fare.setForeground(Theme.ACCENT_AMBER);

        JLabel avail = new JLabel("Available: " + trip.getAvailableSeats() + " / " + trip.getTotalSeats() + " Seats", SwingConstants.RIGHT);
        avail.setFont(Theme.FONT_BOLD);
        avail.setForeground(Theme.SEAT_AVAILABLE);

        JButton btnSelect = new JButton("Select Seats & View Matrix >>");
        btnSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSelect.setBackground(Theme.PRIMARY_BLUE);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFocusPainted(false);
        btnSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSelect.addActionListener(e -> {
            if (onTripSelectedListener != null) {
                onTripSelectedListener.accept(trip);
            }
        });

        right.add(fare);
        right.add(avail);
        right.add(btnSelect);

        card.add(left, BorderLayout.WEST);
        card.add(timeline, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private JLabel createAmenityChip(String text) {
        JLabel chip = new JLabel(" " + text + " ");
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chip.setForeground(new Color(203, 213, 225));
        chip.setOpaque(true);
        chip.setBackground(new Color(30, 41, 59));
        chip.setBorder(new LineBorder(new Color(71, 85, 105), 1, true));
        return chip;
    }
}
