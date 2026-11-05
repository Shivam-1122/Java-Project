package com.ticketreservation;

import com.ticketreservation.database.DatabaseManager;
import com.ticketreservation.ui.MainApplicationFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for the Bus/Train Ticket Reservation System.
 */
public class Main {
    public static void main(String[] args) {
        // Enable anti-aliasing for text and shapes
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Try FlatLaf Dark theme for ultra-modern look
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize Database Manager in background or synchronously
        System.out.println(">> Starting Bus/Train Ticket Reservation System...");
        DatabaseManager.getInstance();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainApplicationFrame frame = new MainApplicationFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch application: " + e.getMessage(),
                        "Fatal Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
