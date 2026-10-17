package com.acadexia.ui.charts;

import com.acadexia.ui.common.UITheme;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;

import java.awt.*;

/**
 * Dark Navy Chart Theme for JFreeChart.
 * Enforces crisp Segoe UI typography, deep midnight canvas backgrounds,
 * translucent gridlines, and vibrant electric blue / cyan / emerald / violet color palettes.
 */
public final class AcademicChartTheme {

    private static final StandardChartTheme THEME;

    static {
        THEME = (StandardChartTheme) StandardChartTheme.createDarknessTheme();

        // Canvas & Plot Backgrounds
        THEME.setChartBackgroundPaint(UITheme.CARD_BG);
        THEME.setPlotBackgroundPaint(new Color(13, 20, 38));

        // Gridlines & Outlines
        THEME.setDomainGridlinePaint(new Color(34, 52, 94, 160));
        THEME.setRangeGridlinePaint(new Color(34, 52, 94, 160));
        THEME.setPlotOutlinePaint(UITheme.CARD_BORDER);

        // Typography Colors
        THEME.setTitlePaint(UITheme.TEXT_PRIMARY);
        THEME.setSubtitlePaint(UITheme.TEXT_SECONDARY);
        THEME.setAxisLabelPaint(UITheme.TEXT_SECONDARY);
        THEME.setTickLabelPaint(UITheme.TEXT_MUTED);
        THEME.setItemLabelPaint(Color.WHITE);
        THEME.setShadowVisible(false);

        // Fonts
        THEME.setExtraLargeFont(new Font("Segoe UI", Font.BOLD, 15));
        THEME.setLargeFont(new Font("Segoe UI", Font.BOLD, 13));
        THEME.setRegularFont(new Font("Segoe UI", Font.PLAIN, 12));
        THEME.setSmallFont(new Font("Segoe UI", Font.PLAIN, 10));

        // Curated Vibrant Palette
        Paint[] colors = new Paint[]{
                new Color(0, 210, 255),    // Neon Cyan
                new Color(59, 130, 246),   // Electric Blue
                new Color(16, 185, 129),   // Emerald
                new Color(168, 85, 247),   // Violet / Purple
                new Color(245, 158, 11),   // Amber
                new Color(239, 68, 68),    // Coral Red
                new Color(14, 165, 233)    // Sky Blue
        };

        THEME.setDrawingSupplier(new DefaultDrawingSupplier(
                colors,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));
    }

    private AcademicChartTheme() {}

    /**
     * Applies the Dark Navy theme to the specified JFreeChart instance.
     */
    public static void apply(JFreeChart chart) {
        if (chart != null) {
            THEME.apply(chart);
            chart.setAntiAlias(true);
            chart.setTextAntiAlias(true);
            chart.setBackgroundPaint(UITheme.CARD_BG);
            if (chart.getPlot() != null) {
                chart.getPlot().setBackgroundPaint(new Color(13, 20, 38));
                chart.getPlot().setOutlinePaint(UITheme.CARD_BORDER);
            }
        }
    }
}
