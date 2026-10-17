package com.acadexia.ui.principal.charts;

import com.acadexia.ui.charts.AcademicChartFactory;
import com.acadexia.ui.charts.AcademicChartTheme;
import com.acadexia.ui.common.UITheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modern JFreeChart-powered Donut / Ring Chart for institutional distribution.
 * Features customizable section depths, anti-aliased geometry, and interactive tooltips.
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
    private final DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    private final RingPlot plot;

    public ModernDonutChart(String chartTitle, String centerSubtitle) {
        this.chartTitle = chartTitle;
        this.centerSubtitle = centerSubtitle;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(360, 240));

        chart = ChartFactory.createRingChart(
                chartTitle,
                dataset,
                true,
                true,
                false
        );
        AcademicChartTheme.apply(chart);

        plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.38);
        plot.setSeparatorsVisible(true);
        plot.setSeparatorPaint(new Color(10, 14, 26));
        plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 10));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(new Color(19, 29, 54, 220));
        plot.setLabelOutlinePaint(UITheme.CARD_BORDER);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

        chartPanel = AcademicChartFactory.createChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
    }

    public synchronized void setData(List<Segment> newSegments) {
        dataset.clear();
        if (newSegments != null) {
            for (Segment s : newSegments) {
                dataset.setValue(s.getLabel(), s.getValue());
                if (s.getColor() != null) {
                    plot.setSectionPaint(s.getLabel(), s.getColor());
                }
            }
        }
        revalidate();
        repaint();
    }
}
