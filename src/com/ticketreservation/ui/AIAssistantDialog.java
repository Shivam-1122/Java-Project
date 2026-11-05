package com.ticketreservation.ui;

import com.ticketreservation.ai.AISmartEngine;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Interactive AI Travel Assistant dialog with quick inquiry chips and intelligent responses.
 */
public class AIAssistantDialog extends JDialog {
    private JTextPane chatPane;
    private JTextField tfInput;
    private final StringBuilder conversationHistory = new StringBuilder();

    public AIAssistantDialog(Frame owner) {
        super(owner, "AI Travel & Booking Assistant", false);
        initUI();
    }

    private void initUI() {
        setSize(580, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Theme.BG_DARK);

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD_BG);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("AI Travel Assistant (Powered by Smart Booking Engine)");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_LIGHT);

        JLabel sub = new JLabel("Ask anything about luggage policies, seat comfort, refund charges, or MySQL database setup.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // 2. Center: Chat Log
        chatPane = new JTextPane();
        chatPane.setContentType("text/html");
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(15, 23, 42));
        chatPane.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));

        add(scrollPane, BorderLayout.CENTER);

        // 3. Quick Chips Panel + Input Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(8, 12, 12, 12));

        // Quick Suggestion Chips
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chipsPanel.setOpaque(false);

        addChip(chipsPanel, "Luggage Limits", "What is the baggage limit for trains and buses?");
        addChip(chipsPanel, "Refund Rules", "What are the ticket cancellation and refund charges?");
        addChip(chipsPanel, "Best Seats", "Which seat is recommended for senior citizens or couples?");
        addChip(chipsPanel, "Meals & Catering", "Are meals provided on Rajdhani and buses?");
        addChip(chipsPanel, "MySQL Setup", "How do I connect this app to MySQL Workbench?");

        southPanel.add(chipsPanel, BorderLayout.NORTH);

        // Message Input
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setOpaque(false);

        tfInput = new JTextField();
        tfInput.setFont(Theme.FONT_REGULAR);
        tfInput.setPreferredSize(new Dimension(380, 36));
        tfInput.addActionListener(e -> processMessage());

        JButton btnSend = new JButton("Ask AI >>");
        btnSend.setFont(Theme.FONT_BOLD);
        btnSend.setBackground(Theme.PRIMARY_BLUE);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> processMessage());

        inputBar.add(tfInput, BorderLayout.CENTER);
        inputBar.add(btnSend, BorderLayout.EAST);

        southPanel.add(inputBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // Initial Greeting
        appendMessage("AI Assistant", AISmartEngine.getTravelAssistantResponse(""), false);
    }

    private void addChip(JPanel container, String chipLabel, String queryText) {
        JButton btn = new JButton(chipLabel);
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(new Color(30, 41, 59));
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(71, 85, 105), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            tfInput.setText(queryText);
            processMessage();
        });
        container.add(btn);
    }

    private void processMessage() {
        String query = tfInput.getText().trim();
        if (query.isEmpty()) return;

        appendMessage("You", query, true);
        tfInput.setText("");

        // Get AI response
        String aiAnswer = AISmartEngine.getTravelAssistantResponse(query);
        appendMessage("AI Assistant", aiAnswer, false);
    }

    private void appendMessage(String sender, String content, boolean isUser) {
        String formatted = content.replace("\n", "<br>")
                .replace("**", "<b>")
                .replace("\u2022", "&bull;");

        String bubbleColor = isUser ? "#1e3a8a" : "#1e293b";
        String senderColor = isUser ? "#60a5fa" : "#34d399";

        conversationHistory.append("<div style='margin-bottom:12px; font-family:Segoe UI, sans-serif;'>")
                .append("<span style='font-size:11px; font-weight:bold; color:").append(senderColor).append(";'>")
                .append(sender).append("</span>")
                .append("<div style='margin-top:4px; padding:10px 14px; background-color:").append(bubbleColor)
                .append("; border-radius:8px; color:#f8fafc; font-size:12px; line-height:1.4;'>")
                .append(formatted)
                .append("</div></div>");

        chatPane.setText("<html><body style='padding:10px; background-color:#0f172a;'>" +
                conversationHistory + "</body></html>");
        chatPane.setCaretPosition(chatPane.getDocument().getLength());
    }
}
