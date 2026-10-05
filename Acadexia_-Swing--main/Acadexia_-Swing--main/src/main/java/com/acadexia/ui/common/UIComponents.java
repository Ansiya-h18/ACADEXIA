package com.acadexia.ui.common;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable Swing component factory for Acadexia.
 *
 * All UI components are created here so the entire application has a
 * consistent look. No other class should create raw JButton / JTextField
 * without using these factory methods.
 */
public final class UIComponents {

    private UIComponents() {}

    // =========================================================================
    // BUTTONS
    // =========================================================================

    /**
     * Primary filled button — blue background, white text.
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, UITheme.PRIMARY, UITheme.PRIMARY_DARK, Color.WHITE);
    }

    /**
     * Secondary outlined button — white background, blue border/text.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(UITheme.PRIMARY_LIGHT);
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.PRIMARY_LIGHT);
                } else {
                    g2.setColor(UITheme.SURFACE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BTN_ARC, UITheme.BTN_ARC);
                g2.setColor(UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.BTN_ARC, UITheme.BTN_ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BUTTON);
        btn.setForeground(UITheme.PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(UITheme.PAD_BTN));
        return btn;
    }

    /**
     * Danger button — red background.
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, UITheme.DANGER, new Color(0xB91C1C), Color.WHITE);
    }

    /**
     * Success button — green background.
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, UITheme.SUCCESS, new Color(0x15803D), Color.WHITE);
    }

    /**
     * Warning button — amber background.
     */
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, UITheme.WARNING, new Color(0xD97706), Color.WHITE);
    }

    /**
     * Ghost/text button — no background, colored text only.
     */
    public static JButton createGhostButton(String text, Color textColor) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BUTTON);
        btn.setForeground(textColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 4, 6, 4));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setForeground(UITheme.PRIMARY_DARK);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setForeground(textColor);
            }
        });
        return btn;
    }

    private static JButton createStyledButton(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BTN_ARC, UITheme.BTN_ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BUTTON);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(UITheme.PAD_BTN));
        return btn;
    }

    // =========================================================================
    // TEXT FIELDS
    // =========================================================================

    /**
     * Modern styled text field with border and padding.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        styleInputField(field, placeholder);
        return field;
    }

    /**
     * Modern styled password field with optional show/hide toggle.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        styleInputField(field, placeholder);
        return field;
    }

    /**
     * Combo box styled to match the text fields.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setFont(UITheme.FONT_REGULAR);
        box.setBackground(UITheme.SURFACE);
        box.setForeground(UITheme.TEXT);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return box;
    }

    private static void styleInputField(JTextComponent field, String placeholder) {
        field.setFont(UITheme.FONT_REGULAR);
        field.setBackground(UITheme.SURFACE);
        field.setForeground(UITheme.TEXT);
        field.setCaretColor(UITheme.PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Focus border color change
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UITheme.BORDER_FOCUS, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UITheme.BORDER, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        // Placeholder hint
        if (placeholder != null && !placeholder.isEmpty()) {
            field.putClientProperty("JTextField.placeholderText", placeholder);
        }
    }

    // =========================================================================
    // PANELS & CARDS
    // =========================================================================

    /**
     * White card panel with rounded border and shadow-like border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(UITheme.PAD_CARD)
        ));
        return panel;
    }

    /**
     * Page content panel — grey background, generous padding.
     */
    public static JPanel createPagePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(UITheme.PAD_PAGE));
        return panel;
    }

    /**
     * KPI / Statistic summary card with colored left-border accent.
     *
     * @param title       Small uppercase label at top (e.g. "TOTAL STUDENTS")
     * @param value       Large number / value (e.g. "1,248")
     * @param subtitle    Small description below value
     * @param accentColor Left border + value color
     */
    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Colored left accent bar
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor != null ? accentColor : UITheme.PRIMARY);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
                g2.dispose();
            }
        };
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lblTitle = new JLabel(title != null ? title.toUpperCase() : "");
        lblTitle.setFont(UITheme.FONT_STAT_LABEL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        JLabel lblValue = new JLabel(value != null ? value : "-");
        lblValue.setFont(UITheme.FONT_STAT_NUM);
        lblValue.setForeground(accentColor != null ? accentColor : UITheme.TEXT);

        JLabel lblSub = new JLabel(subtitle != null ? subtitle : "");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Section header with title and subtitle.
     */
    public static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(2, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(UITheme.FONT_SUBTITLE);
            lblSub.setForeground(UITheme.TEXT_MUTED);
            header.add(lblSub, BorderLayout.SOUTH);
        }

        header.add(lblTitle, BorderLayout.NORTH);
        return header;
    }

    /**
     * Inline section title with colored left bar — for use inside cards.
     */
    public static JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_HEADING);
        lbl.setForeground(UITheme.TEXT);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    /**
     * Form field label (bold, dark).
     */
    public static JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT);
        return lbl;
    }

    // =========================================================================
    // TABLE
    // =========================================================================

    /**
     * Modern styled JTable. Non-editable by default.
     * Use DefaultTableModel as the model for editable tables (marks entry etc.)
     * — pass a custom model in that case.
     */
    public static JTable createStyledTable(TableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? UITheme.TABLE_ROW_EVEN : UITheme.TABLE_ROW_ODD);
                    c.setForeground(UITheme.TEXT);
                } else {
                    c.setBackground(UITheme.TABLE_SEL);
                    c.setForeground(UITheme.TABLE_SEL_TEXT);
                }
                return c;
            }
        };

        table.setFont(UITheme.FONT_TABLE);
        table.setRowHeight(UITheme.TABLE_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(UITheme.TABLE_ROW_EVEN);
        table.setForeground(UITheme.TEXT);
        table.setFillsViewportHeight(true);
        table.setGridColor(UITheme.BORDER);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_TABLE_HEADER);
        header.setBackground(UITheme.TABLE_HEADER_BG);
        header.setForeground(UITheme.TEXT_MUTED);
        header.setReorderingAllowed(false);
        header.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, UITheme.TABLE_HEADER_H));

        // Cell padding renderer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(new EmptyBorder(4, 14, 4, 14));
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);

        // Center-align numeric column renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(4, 14, 4, 14));
                return this;
            }
        };
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);
        table.setDefaultRenderer(Float.class, centerRenderer);

        return table;
    }

    /**
     * Wraps a table in a scroll pane with a clean border.
     */
    public static JScrollPane createTableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        sp.setBackground(UITheme.SURFACE);
        sp.getViewport().setBackground(UITheme.SURFACE);
        return sp;
    }

    // =========================================================================
    // BADGES & CHIPS
    // =========================================================================

    /**
     * Status pill / badge component with rounded background.
     */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setOpaque(false);
        return badge;
    }

    /** Pre-styled green "Active" badge */
    public static JLabel createActiveBadge() {
        return createBadge("● Active", UITheme.SUCCESS_LIGHT, UITheme.SUCCESS);
    }

    /** Pre-styled red "Inactive" badge */
    public static JLabel createInactiveBadge() {
        return createBadge("● Inactive", UITheme.DANGER_LIGHT, UITheme.DANGER);
    }

    /** Pre-styled amber "Pending" badge */
    public static JLabel createPendingBadge() {
        return createBadge("● Pending", UITheme.WARNING_LIGHT, UITheme.WARNING);
    }

    // =========================================================================
    // DIVIDER
    // =========================================================================

    /**
     * Thin horizontal divider line.
     */
    public static JSeparator createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setBackground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // =========================================================================
    // EMPTY STATE
    // =========================================================================

    /**
     * Panel to show when a table / list has no records.
     */
    public static JPanel createEmptyState(String icon, String title, String description) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(new EmptyBorder(48, 48, 48, 48));

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADING);
        lblTitle.setForeground(UITheme.TEXT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><center>" + description + "</center></html>", SwingConstants.CENTER);
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblIcon);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblDesc);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // =========================================================================
    // TOAST NOTIFICATIONS
    // =========================================================================

    /**
     * Shows a non-blocking success toast notification at the bottom of the parent frame.
     */
    public static void showSuccessToast(JFrame parent, String message) {
        showToast(parent, message, UITheme.SUCCESS, UITheme.SUCCESS_LIGHT);
    }

    /**
     * Shows a non-blocking error toast notification at the bottom of the parent frame.
     */
    public static void showErrorToast(JFrame parent, String message) {
        showToast(parent, message, UITheme.DANGER, UITheme.DANGER_LIGHT);
    }

    /**
     * Shows a non-blocking info toast notification at the bottom of the parent frame.
     */
    public static void showInfoToast(JFrame parent, String message) {
        showToast(parent, message, UITheme.PRIMARY, UITheme.PRIMARY_LIGHT);
    }

    private static void showToast(JFrame parent, String message, Color borderColor, Color bgColor) {
        JWindow toast = new JWindow(parent);
        toast.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panel.setBackground(bgColor);
        panel.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel lbl = new JLabel(message);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(borderColor);
        panel.add(lbl);

        toast.add(panel, BorderLayout.CENTER);
        toast.pack();

        // Position at bottom-center of parent
        Point loc = parent.getLocationOnScreen();
        Dimension pSize = parent.getSize();
        int x = loc.x + (pSize.width - toast.getWidth()) / 2;
        int y = loc.y + pSize.height - toast.getHeight() - 48;
        toast.setLocation(x, y);
        toast.setVisible(true);

        // Auto-dismiss after 2.5s
        Timer timer = new Timer(2500, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    // =========================================================================
    // TOOLBAR PANEL
    // =========================================================================

    /**
     * Toolbar panel for tables — grey background, left-aligned buttons.
     */
    public static JPanel createToolbar(Component... components) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(UITheme.BG);
        for (Component c : components) {
            toolbar.add(c);
        }
        return toolbar;
    }

    /**
     * Toolbar panel with right-aligned actions.
     */
    public static JPanel createToolbarWithActions(Component left, Component right) {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UITheme.BG);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
        if (left != null) toolbar.add(left, BorderLayout.WEST);
        if (right != null) toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    // =========================================================================
    // SEARCH FIELD
    // =========================================================================

    /**
     * Styled search field with magnifier placeholder.
     */
    public static JTextField createSearchField() {
        JTextField field = createTextField("🔍  Search...");
        field.setPreferredSize(new Dimension(220, 34));
        field.setMaximumSize(new Dimension(280, 34));
        return field;
    }

    // =========================================================================
    // DIALOG HELPERS
    // =========================================================================

    /**
     * Styled confirmation dialog for destructive actions.
     * Returns true if user confirmed.
     */
    public static boolean showDeleteConfirmation(Component parent, String entityName) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                "<html><b>Delete " + entityName + "?</b><br><br>" +
                "This action cannot be undone. All related records<br>" +
                "will also be removed.</html>",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Styled success message dialog.
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Styled error message dialog.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

}
