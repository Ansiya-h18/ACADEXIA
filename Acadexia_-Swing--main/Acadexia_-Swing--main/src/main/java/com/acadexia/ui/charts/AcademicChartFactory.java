package com.acadexia.ui.charts;

import com.acadexia.ui.common.UITheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Factory for creating eye-catching interactive JFreeChart visualizations
 * integrated seamlessly with Java Swing panels and the Dark Navy Hover Theme.
 */
public final class AcademicChartFactory {

    private AcademicChartFactory() {}

    /**
     * Creates an interactive Department-wise Results Bar Chart.
     */
    public static JFreeChart createDepartmentResultsBarChart(String title, Map<String, Double> deptValues, String valueAxisTitle) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (deptValues != null) {
            for (Map.Entry<String, Double> entry : deptValues.entrySet()) {
                dataset.addValue(entry.getValue(), "Department", entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Department",
                valueAxisTitle != null ? valueAxisTitle : "Average CGPA / Pass %",
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
        renderer.setMaximumBarWidth(0.12);
        renderer.setItemMargin(0.04);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(Color.WHITE);

        // Electric Blue to Cyan gradient on bars
        GradientPaint barPaint = new GradientPaint(0, 0, UITheme.CYAN_ACCENT, 0, 300, UITheme.PRIMARY);
        renderer.setSeriesPaint(0, barPaint);

        return chart;
    }

    /**
     * Creates an interactive Pass/Fail & Standing Donut/Ring Chart with percentages.
     */
    public static JFreeChart createPassFailDonutChart(String title, double pass, double fail, double withheld) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (pass > 0) dataset.setValue("Passed", pass);
        if (fail > 0) dataset.setValue("Failed / At Risk", fail);
        if (withheld > 0) dataset.setValue("Withheld / Incomplete", withheld);

        JFreeChart chart = ChartFactory.createRingChart(
                title,
                dataset,
                true,
                true,
                false
        );

        AcademicChartTheme.apply(chart);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.38);
        plot.setSeparatorsVisible(true);
        plot.setSeparatorPaint(new Color(10, 14, 26));

        // Labels
        plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(new Color(19, 29, 54, 220));
        plot.setLabelOutlinePaint(UITheme.CARD_BORDER);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

        // Colors
        plot.setSectionPaint("Passed", UITheme.SUCCESS);
        plot.setSectionPaint("Failed / At Risk", UITheme.DANGER);
        plot.setSectionPaint("Withheld / Incomplete", UITheme.WARNING);

        return chart;
    }

    /**
     * Creates an interactive Student Performance Bar Chart for subject marks.
     */
    public static JFreeChart createStudentPerformanceChart(String title, Map<String, Double> subjectMarks) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (subjectMarks != null) {
            for (Map.Entry<String, Double> entry : subjectMarks.entrySet()) {
                dataset.addValue(entry.getValue(), "Marks", entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Course Subject",
                "Marks Obtained (%)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        AcademicChartTheme.apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0.0, 100.0);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.12);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(Color.WHITE);

        // Gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(56, 189, 248), 0, 300, new Color(99, 102, 241));
        renderer.setSeriesPaint(0, gp);

        return chart;
    }

    /**
     * Creates an interactive Semester Comparison Line Graph showing SGPA progression.
     */
    public static JFreeChart createSemesterTrendLineChart(String title, Map<String, Double> semesterSgpa) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (semesterSgpa != null) {
            for (Map.Entry<String, Double> entry : semesterSgpa.entrySet()) {
                dataset.addValue(entry.getValue(), "SGPA", entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                title,
                "Semester",
                "SGPA (Scale 10.0)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        AcademicChartTheme.apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getRangeAxis().setRange(0.0, 10.0);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setSeriesPaint(0, UITheme.CYAN_ACCENT);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(Color.WHITE);

        return chart;
    }

    /**
     * Creates an interactive Overall Academic Trends Multi-Series Chart.
     */
    public static JFreeChart createAcademicTrendsChart(String title, String[] semesters, Map<String, double[]> seriesData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (seriesData != null && semesters != null) {
            for (Map.Entry<String, double[]> entry : seriesData.entrySet()) {
                String seriesName = entry.getKey();
                double[] values = entry.getValue();
                for (int i = 0; i < values.length && i < semesters.length; i++) {
                    dataset.addValue(values[i], seriesName, semesters[i]);
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                title,
                "Academic Term",
                "Average Grade / Performance",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        AcademicChartTheme.apply(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);

        Color[] seriesColors = new Color[]{
                UITheme.CYAN_ACCENT,
                UITheme.SUCCESS,
                UITheme.ACCENT_PURPLE,
                UITheme.WARNING,
                UITheme.INFO
        };

        for (int i = 0; i < seriesColors.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
            renderer.setSeriesStroke(i, new BasicStroke(2.5f));
        }

        return chart;
    }

    /**
     * Wraps a JFreeChart into a sleek, responsive ChartPanel configured for Swing.
     */
    public static ChartPanel createChartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart, true);
        panel.setMouseWheelEnabled(true);
        panel.setDisplayToolTips(true);
        panel.setBackground(UITheme.CARD_BG);
        panel.setOpaque(true);
        panel.setMinimumDrawWidth(100);
        panel.setMinimumDrawHeight(100);
        panel.setMaximumDrawWidth(3000);
        panel.setMaximumDrawHeight(2000);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        return panel;
    }
}
