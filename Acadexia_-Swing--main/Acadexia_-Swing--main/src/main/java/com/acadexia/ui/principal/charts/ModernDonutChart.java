package com.acadexia.ui.principal.charts;

import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern anti-aliased Java2D Donut Chart for standing distribution and performance categorization.
 * Renders center aggregate count, colored sectors, and side legends.
 */
public class ModernDonutChart extends JPanel {

    public static class Segment {
        private final String label;
        private final double value;
        private final Color color;

        public Segment(String label, double value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }

        public String getLabel() { return label; }
        public double getValue() { return value; }
        public Color getColor() { return color; }
    }

    private final String chartTitle;
    private final String centerSubtitle;
    private final List<Segment> segments = new ArrayList<>();

    public ModernDonutChart(String chartTitle, String centerSubtitle) {
        this.chartTitle = chartTitle;
        this.centerSubtitle = centerSubtitle;
        setBackground(UITheme.CARD_BG);
        setPreferredSize(new Dimension(360, 240));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    public synchronized void setData(List<Segment> newSegments) {
        this.segments.clear();
        if (newSegments != null) {
            this.segments.addAll(newSegments);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card background
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g2.setColor(UITheme.CARD_BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Chart Title
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(chartTitle, 16, 24);

        double total = segments.stream().mapToDouble(Segment::getValue).sum();

        if (total <= 0) {
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("No segment distribution data", 16, h / 2);
            g2.dispose();
            return;
        }

        int donutSize = Math.min(140, Math.min(w / 2, h - 55));
        int donutX = 20;
        int donutY = 40 + (h - 40 - donutSize) / 2;

        // Draw Arcs
        double startAngle = 90.0;
        for (Segment s : segments) {
            double angle = (s.getValue() / total) * 360.0;
            g2.setColor(s.getColor());
            g2.fill(new Arc2D.Double(donutX, donutY, donutSize, donutSize, startAngle, -angle, Arc2D.PIE));
            startAngle -= angle;
        }

        // Inner Cutout (Donut hole)
        int innerSize = (int) (donutSize * 0.62);
        int innerX = donutX + (donutSize - innerSize) / 2;
        int innerY = donutY + (donutSize - innerSize) / 2;

        g2.setColor(UITheme.CARD_BG);
        g2.fill(new Ellipse2D.Double(innerX, innerY, innerSize, innerSize));

        // Center Total Text
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        String totalStr = String.valueOf((long) total);
        g2.drawString(totalStr, innerX + (innerSize - fm.stringWidth(totalStr)) / 2, innerY + (innerSize / 2) + 2);

        g2.setColor(UITheme.TEXT_MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        fm = g2.getFontMetrics();
        g2.drawString(centerSubtitle, innerX + (innerSize - fm.stringWidth(centerSubtitle)) / 2, innerY + (innerSize / 2) + 16);

        // Side Legend
        int legendX = donutX + donutSize + 22;
        int legendY = 48;
        int legendSlot = Math.min(26, Math.max(18, (h - 60) / Math.max(1, segments.size())));

        for (Segment s : segments) {
            // Color dot
            g2.setColor(s.getColor());
            g2.fillOval(legendX, legendY + 2, 9, 9);

            // Label & Percentage
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            double pct = (s.getValue() / total) * 100.0;
            String text = String.format("%s (%.0f%% - %d)", s.getLabel(), pct, (long) s.getValue());
            g2.drawString(text, legendX + 15, legendY + 11);

            legendY += legendSlot;
        }

        g2.dispose();
    }
}
