package com.acadexia.ui.principal.charts;

import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern anti-aliased Java2D Bar Chart for institutional analytics.
 * Renders smooth pill bars, value callouts, gradients, and labels.
 */
public class ModernBarChart extends JPanel {

    public static class BarItem {
        private final String label;
        private final double value;
        private final String displayValue;
        private final Color color1;
        private final Color color2;

        public BarItem(String label, double value, String displayValue, Color color1, Color color2) {
            this.label = label;
            this.value = value;
            this.displayValue = displayValue;
            this.color1 = color1;
            this.color2 = color2 != null ? color2 : color1;
        }

        public String getLabel() { return label; }
        public double getValue() { return value; }
        public String getDisplayValue() { return displayValue; }
        public Color getColor1() { return color1; }
        public Color getColor2() { return color2; }
    }

    private final String chartTitle;
    private final List<BarItem> items = new ArrayList<>();
    private double maxValue = 10.0;

    public ModernBarChart(String chartTitle) {
        this.chartTitle = chartTitle;
        setBackground(UITheme.CARD_BG);
        setPreferredSize(new Dimension(360, 240));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    public synchronized void setData(List<BarItem> newItems, double max) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.maxValue = max > 0 ? max : 1.0;
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

        // Background card rounded rect
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g2.setColor(UITheme.CARD_BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Chart Title
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(chartTitle, 16, 24);

        if (items.isEmpty()) {
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("No analytical data available to plot", 16, h / 2);
            g2.dispose();
            return;
        }

        int startY = 46;
        int availableHeight = h - startY - 14;
        int barSlot = Math.max(26, availableHeight / items.size());
        int labelWidth = 100;
        int valueWidth = 55;
        int chartAreaWidth = Math.max(50, w - labelWidth - valueWidth - 36);

        int curY = startY;
        for (BarItem item : items) {
            // Label
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String lbl = item.getLabel();
            if (fm.stringWidth(lbl) > labelWidth - 8) {
                lbl = lbl.substring(0, Math.min(lbl.length(), 10)) + "..";
            }
            g2.drawString(lbl, 16, curY + 14);

            // Bar track
            int trackX = 16 + labelWidth;
            int barHeight = Math.max(10, barSlot - 14);
            int barY = curY + 3;

            g2.setColor(new Color(24, 24, 38));
            g2.fillRoundRect(trackX, barY, chartAreaWidth, barHeight, 6, 6);

            // Active Bar with gradient
            double ratio = Math.max(0.0, Math.min(1.0, item.getValue() / maxValue));
            int barWidth = (int) (chartAreaWidth * ratio);
            if (barWidth > 0) {
                GradientPaint gp = new GradientPaint(trackX, barY, item.getColor1(), trackX + barWidth, barY, item.getColor2());
                g2.setPaint(gp);
                g2.fillRoundRect(trackX, barY, barWidth, barHeight, 6, 6);
            }

            // Value text
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(item.getDisplayValue(), trackX + chartAreaWidth + 10, curY + 14);

            curY += barSlot;
        }

        g2.dispose();
    }
}
