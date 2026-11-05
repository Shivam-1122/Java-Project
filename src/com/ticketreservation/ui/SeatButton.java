package com.ticketreservation.ui;

import com.ticketreservation.model.Seat;
import com.ticketreservation.model.SeatStatus;
import com.ticketreservation.model.SeatType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern, high-fidelity custom JButton matrix seat cell.
 * Features realistic armchair graphics, instant selection responsiveness,
 * and live color status (Emerald Green = Available, Royal Blue = Selected, Crimson Red = Occupied, Violet = Ladies).
 */
public class SeatButton extends JButton {
    private final Seat seat;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private SeatSelectionListener selectionListener;

    public interface SeatSelectionListener {
        void onSeatToggled(Seat seat, boolean isSelected);
    }

    public SeatButton(Seat seat) {
        this.seat = seat;
        initComponent();
    }

    private void initComponent() {
        setPreferredSize(new Dimension(68, 64));
        setMinimumSize(new Dimension(60, 56));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(seat.isOccupied() ? Cursor.getDefaultCursor() : new Cursor(Cursor.HAND_CURSOR));

        updateTooltip();

        // 1. Primary Action Listener (Guaranteed in Swing across all LookAndFeels)
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSeatClick();
            }
        });

        // 2. Mouse events for hover and immediate press animation
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!seat.isOccupied()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!seat.isOccupied()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void handleSeatClick() {
        if (seat.isOccupied()) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(
                    this,
                    "[OCCUPIED] Seat " + seat.getSeatNumber() + " is already OCCUPIED by another passenger.\n" +
                            (seat.getBookedByPnr() != null ? "Booking Reference: " + seat.getBookedByPnr() : ""),
                    "Seat Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Toggle selection
        if (seat.getStatus() == SeatStatus.SELECTED) {
            seat.deselect();
            if (selectionListener != null) {
                selectionListener.onSeatToggled(seat, false);
            }
        } else {
            seat.setStatus(SeatStatus.SELECTED);
            if (selectionListener != null) {
                selectionListener.onSeatToggled(seat, true);
            }
        }

        updateTooltip();
        repaint();
    }

    public void setSelectionListener(SeatSelectionListener listener) {
        this.selectionListener = listener;
    }

    public Seat getSeat() {
        return seat;
    }

    public void updateTooltip() {
        String statusText;
        String colorHex;
        if (seat.getStatus() == SeatStatus.AVAILABLE) {
            statusText = "[AVAILABLE] (Click to Select)";
            colorHex = "#22C55E";
        } else if (seat.getStatus() == SeatStatus.SELECTED) {
            statusText = "[SELECTED] (In Your Cart)";
            colorHex = "#38BDF8";
        } else if (seat.getStatus() == SeatStatus.OCCUPIED) {
            statusText = "[OCCUPIED] (Booked)";
            colorHex = "#EF4444";
        } else {
            statusText = "[LADIES RESERVED]";
            colorHex = "#C084FC";
        }

        String tooltipHtml = "<html><body style='font-family:Segoe UI, sans-serif; padding:8px; background-color:#0f172a; color:#f8fafc; border:1px solid #334155;'>" +
                "<div style='font-size:14px; font-weight:bold; color:" + colorHex + ";'>Seat " + seat.getSeatNumber() + " (" + seat.getSeatType().getLabel() + ")</div>" +
                "<div style='margin-top:4px;'>Status: <b style='color:" + colorHex + ";'>" + statusText + "</b></div>" +
                "<div>Fare: <b style='color:#F59E0B; font-size:13px;'>Rs. " + (int)seat.getPrice() + "</b></div>" +
                "<div>Location: " + seat.getDeckOrCoach() + "</div>" +
                (seat.getBookedByPnr() != null ? "<div style='color:#94a3b8; font-size:11px; margin-top:2px;'>Ref: " + seat.getBookedByPnr() + "</div>" : "") +
                "</body></html>";
        setToolTipText(tooltipHtml);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Color determination
        Color seatFill;
        Color headrestFill;
        Color strokeColor;
        Color textColor = Color.WHITE;

        switch (seat.getStatus()) {
            case AVAILABLE:
                seatFill = isHovered ? new Color(34, 197, 94) : new Color(22, 163, 74);
                headrestFill = isHovered ? new Color(74, 222, 128) : new Color(34, 197, 94);
                strokeColor = new Color(21, 128, 61);
                break;
            case SELECTED:
                seatFill = new Color(37, 99, 235);
                headrestFill = new Color(96, 165, 250);
                strokeColor = new Color(245, 158, 11); // Golden glow for selected
                break;
            case OCCUPIED:
                seatFill = new Color(185, 28, 28);
                headrestFill = new Color(220, 38, 38);
                strokeColor = new Color(127, 29, 29);
                textColor = new Color(254, 202, 202);
                break;
            case LADIES_ONLY:
                seatFill = isHovered ? new Color(168, 85, 247) : new Color(147, 51, 234);
                headrestFill = new Color(192, 132, 252);
                strokeColor = new Color(107, 33, 168);
                break;
            default:
                seatFill = Color.GRAY;
                headrestFill = Color.LIGHT_GRAY;
                strokeColor = Color.DARK_GRAY;
        }

        if (isPressed) {
            seatFill = seatFill.darker();
        }

        // Draw outer glow if selected or hovered
        if (seat.isSelected()) {
            g2.setColor(new Color(59, 130, 246, 90));
            g2.fillRoundRect(1, 1, w - 2, h - 2, 14, 14);
        } else if (isHovered && !seat.isOccupied()) {
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRoundRect(1, 1, w - 2, h - 2, 14, 14);
        }

        // 1. Armrests (left and right)
        g2.setColor(strokeColor.darker());
        g2.fillRoundRect(2, h / 3, 5, h / 2, 3, 3);
        g2.fillRoundRect(w - 7, h / 3, 5, h / 2, 3, 3);

        // 2. Headrest (top cushion)
        Shape headrest = new RoundRectangle2D.Float(10, 3, w - 20, 11, 8, 8);
        g2.setColor(headrestFill);
        g2.fill(headrest);
        g2.setColor(strokeColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(headrest);

        // 3. Backrest & Seat Cushion Body
        Shape mainSeat = new RoundRectangle2D.Float(6, 12, w - 12, h - 16, 10, 10);
        g2.setColor(seatFill);
        g2.fill(mainSeat);

        // Inner cushion contour highlight
        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillRoundRect(10, 15, w - 20, 12, 6, 6);

        // Border stroke
        g2.setColor(strokeColor);
        g2.setStroke(new BasicStroke(seat.isSelected() ? 2.5f : 1.2f));
        g2.draw(mainSeat);

        // 4. Center Seat Number Text
        g2.setColor(textColor);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        String seatNum = seat.getSeatNumber();
        int textX = (w - fm.stringWidth(seatNum)) / 2;
        int textY = (h / 2) + 7;
        g2.drawString(seatNum, textX, textY);

        // 5. Bottom Label / Status Glyph
        if (seat.isSelected()) {
            // Draw checkmark badge
            g2.setColor(new Color(245, 158, 11)); // Amber
            g2.fillOval(w - 18, 5, 14, 14);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(w - 15, 12, w - 12, 15);
            g2.drawLine(w - 12, 15, w - 7, 8);
        } else if (seat.isOccupied()) {
            // Draw lock / cross icon on top right
            g2.setColor(new Color(239, 68, 68));
            g2.fillOval(w - 17, 5, 13, 13);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawLine(w - 14, 8, w - 7, 15);
            g2.drawLine(w - 7, 8, w - 14, 15);
        } else if (seat.getStatus() == SeatStatus.LADIES_ONLY) {
            g2.setColor(new Color(168, 85, 247));
            g2.fillOval(w - 17, 5, 13, 13);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.drawString("L", w - 13, 15);
        }

        // Subtitle badge at bottom (LB, UB, WIN, AIS)
        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        FontMetrics fmSub = g2.getFontMetrics();
        String typeLabel = getShortTypeLabel(seat.getSeatType());
        int subX = (w - fmSub.stringWidth(typeLabel)) / 2;
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawString(typeLabel, subX, h - 7);

        g2.dispose();
    }

    private String getShortTypeLabel(SeatType type) {
        switch (type) {
            case LOWER_BERTH: return "Lower";
            case MIDDLE_BERTH: return "Middle";
            case UPPER_BERTH: return "Upper";
            case SIDE_LOWER: return "Side Low";
            case SIDE_UPPER: return "Side Up";
            case WINDOW: return "Window";
            case AISLE: return "Aisle";
            default: return "Seat";
        }
    }
}
