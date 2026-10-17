package com.acadexia.ui.principal.charts;

import com.acadexia.ui.charts.AcademicChartFactory;
import com.acadexia.ui.charts.AcademicChartTheme;
import com.acadexia.ui.common.UITheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modern JFreeChart-powered Bar Chart for institutional analytics.
 * Renders anti-aliased electric-blue gradient bars, interactive tooltips, and labels.
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
    private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private final JFreeChart chart;
    private final ChartPanel chartPanel;

    public ModernBarChart(String chartTitle) {
        this.chartTitle = chartTitle;
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(360, 240));

        chart = ChartFactory.createBarChart(
                chartTitle,
                null,
                null,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        AcademicChartTheme.apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.14);
        renderer.setItemMargin(0.04);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(Color.WHITE);
        renderer.setSeriesPaint(0, new GradientPaint(0, 0, UITheme.CYAN_ACCENT, 0, 250, UITheme.PRIMARY));

        chartPanel = AcademicChartFactory.createChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
    }

    public synchronized void setData(List<BarItem> newItems, double max) {
        dataset.clear();
        if (newItems != null) {
            for (BarItem item : newItems) {
                dataset.addValue(item.getValue(), "Metric", item.getLabel());
            }
        }
        CategoryPlot plot = chart.getCategoryPlot();
        if (max > 0) {
            plot.getRangeAxis().setRange(0.0, max * 1.15);
        }
        revalidate();
        repaint();
    }
}
