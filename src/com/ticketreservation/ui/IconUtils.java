package com.ticketreservation.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Utility to generate crisp, high-DPI vector icons for the reservation system.
 */
public class IconUtils {

    public static Icon createTrainIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            // Train body
            g2.fillRoundRect(2, 2, w - 4, h - 7, 8, 8);
            // Windshield
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(5, 5, w - 10, h / 3, 4, 4);
            // Headlights
            g2.setColor(Color.YELLOW);
            g2.fillOval(5, h - 10, 4, 4);
            g2.fillOval(w - 9, h - 10, 4, 4);
            // Tracks / wheels
            g2.setColor(color.darker());
            g2.fillRect(4, h - 4, 4, 3);
            g2.fillRect(w - 8, h - 4, 4, 3);
        });
    }

    public static Icon createBusIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            // Bus body
            g2.fillRoundRect(2, 2, w - 4, h - 6, 6, 6);
            // Windshield
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRect(5, 5, w - 10, h / 3);
            // Wheels
            g2.setColor(new Color(15, 23, 42));
            g2.fillOval(4, h - 6, 5, 5);
            g2.fillOval(w - 9, h - 6, 5, 5);
            // Headlights
            g2.setColor(Color.ORANGE);
            g2.fillRect(3, h - 9, 2, 2);
            g2.fillRect(w - 5, h - 9, 2, 2);
        });
    }

    public static Icon createSeatIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            // Backrest
            g2.fillRoundRect(4, 2, w - 8, h - 9, 6, 6);
            // Cushion
            g2.fillRoundRect(2, h - 9, w - 4, 7, 4, 4);
            // Left armrest
            g2.fillRect(1, h / 2, 2, 5);
            // Right armrest
            g2.fillRect(w - 3, h / 2, 2, 5);
        });
    }

    public static Icon createSteeringIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(3, 3, w - 6, h - 6);
            g2.fillOval(w / 2 - 2, h / 2 - 2, 4, 4);
            g2.drawLine(w / 2, 3, w / 2, h - 3);
            g2.drawLine(3, h / 2, w - 3, h / 2);
        });
    }

    public static Icon createCheckIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Float();
            path.moveTo(w * 0.2, h * 0.5);
            path.lineTo(w * 0.45, h * 0.75);
            path.lineTo(w * 0.82, h * 0.25);
            g2.draw(path);
        });
    }

    public static Icon createLockIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            // Shackle
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawArc(w / 2 - 4, 2, 8, 8, 0, 180);
            // Body
            g2.fillRoundRect(w / 2 - 6, 7, 12, 9, 3, 3);
            // Keyhole
            g2.setColor(Color.WHITE);
            g2.fillOval(w / 2 - 1, 9, 2, 2);
            g2.fillRect(w / 2 - 1, 10, 2, 3);
        });
    }

    public static Icon createSparkleIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            Path2D p = new Path2D.Float();
            p.moveTo(w / 2.0, 1);
            p.quadTo(w / 2.0, h / 2.0, w - 1, h / 2.0);
            p.quadTo(w / 2.0, h / 2.0, w / 2.0, h - 1);
            p.quadTo(w / 2.0, h / 2.0, 1, h / 2.0);
            p.quadTo(w / 2.0, h / 2.0, w / 2.0, 1);
            p.closePath();
            g2.fill(p);
        });
    }

    private interface IconPainter {
        void paint(Graphics2D g2, int width, int height);
    }

    private static class VectorIcon implements Icon {
        private final int width;
        private final int height;
        private final IconPainter painter;

        public VectorIcon(int width, int height, IconPainter painter) {
            this.width = width;
            this.height = height;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            painter.paint(g2, width, height);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }
    }
}
