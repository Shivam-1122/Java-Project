package com.ticketreservation.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern design system, curated color palette, typography and UI tokens.
 */
public class Theme {
    // Curated Harmonious Palette
    public static final Color BG_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color CARD_BG = new Color(30, 41, 59);       // Slate 800
    public static final Color CARD_BG_LIGHT = new Color(248, 250, 252);
    public static final Color PANEL_HEADER_BG = new Color(24, 33, 47);
    public static final Color BORDER_COLOR = new Color(51, 65, 85);   // Slate 700
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);

    // Primary Accents
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);  // Royal Blue
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color ACCENT_INDIGO = new Color(99, 102, 241);
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);

    // Seat Status Colors (Requirement: Live Occupied/Available color status)
    public static final Color SEAT_AVAILABLE = new Color(34, 197, 94);     // Emerald 500
    public static final Color SEAT_AVAILABLE_HOVER = new Color(22, 163, 74);
    public static final Color SEAT_SELECTED = new Color(59, 130, 246);      // Electric Blue
    public static final Color SEAT_SELECTED_HOVER = new Color(29, 78, 216);
    public static final Color SEAT_OCCUPIED = new Color(239, 68, 68);      // Crimson Red
    public static final Color SEAT_OCCUPIED_BORDER = new Color(185, 28, 28);
    public static final Color SEAT_LADIES = new Color(168, 85, 247);       // Purple 500
    public static final Color SEAT_LADIES_HOVER = new Color(147, 51, 234);

    // Text Colors
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color TEXT_DARK = new Color(15, 23, 42);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_CODE = new Font("Consolas", Font.BOLD, 13);

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static Border createRoundedBorder(Color color, int radius) {
        return new LineBorder(color, 1, true);
    }
}
