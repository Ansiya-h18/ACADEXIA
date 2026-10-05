package com.acadexia.ui.auth;

import com.acadexia.dao.UserDAO;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.admin.AdminDashboardFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;
import com.acadexia.ui.faculty.FacultyDashboardFrame;
import com.acadexia.ui.principal.PrincipalDashboardFrame;
import com.acadexia.ui.student.StudentDashboardFrame;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern login screen for Acadexia.
 *
 * Layout: two-column split
 *   LEFT  — brand panel (logo, name, tagline, feature highlights)
 *   RIGHT — login card (fields, sign-in button, register link)
 *
 * All authentication logic is unchanged from original.
 * Security filters (InputFilterFactory) are preserved.
 */
public class LandingFrame extends JFrame {

    // ── fields ───────────────────────────────────────────────────────────────
    private final JTextField     txtIdentifier = new JTextField(20);
    private final JPasswordField txtPassword   = new JPasswordField(20);
    private final JLabel         lblError      = new JLabel(" ");
    private final JButton        btnTogglePass = new JButton("👁");
    private final JButton        btnLogin      = UIComponents.createPrimaryButton("Sign In");
    private boolean passwordVisible = false;

    private final UserDAO userDAO = new UserDAO();

    // ── constructor ──────────────────────────────────────────────────────────
    public LandingFrame() {
        setTitle("Acadexia – Student Result Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 660);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(UITheme.BG);

        // Apply strict SQL-injection filters (unchanged from original)
        InputFilterFactory.applyIdentifierFilter(txtIdentifier, 50);
        InputFilterFactory.applyPasswordFilter(txtPassword, 50);

        buildUI();
    }

    // ── UI construction ──────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildBrandPanel(), buildLoginCard());
        split.setDividerLocation(460);
        split.setDividerSize(0);
        split.setBorder(null);
        split.setResizeWeight(0.44);

        add(split, BorderLayout.CENTER);
    }

    // ── LEFT: brand panel ────────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // Gradient background: dark navy → slightly lighter navy
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UITheme.SIDEBAR_BG,
                        getWidth(), getHeight(), new Color(0x1E3A5F));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(52, 44, 52, 44));

        // ── top: logo + name ─────────────────────────────────────────────────
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Circular logo placeholder
        JPanel logoCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, 56, 56);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String letter = "A";
                int x = (56 - fm.stringWidth(letter)) / 2;
                int y = (56 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(letter, x, y);
                g2.dispose();
            }
        };
        logoCircle.setPreferredSize(new Dimension(56, 56));
        logoCircle.setMaximumSize(new Dimension(56, 56));
        logoCircle.setOpaque(false);
        logoCircle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrand = new JLabel("ACADEXIA");
        lblBrand.setFont(UITheme.FONT_BRAND);
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTagline = new JLabel("Student Result Management System");
        lblTagline.setFont(UITheme.FONT_SUBTITLE);
        lblTagline.setForeground(new Color(0x94A3B8));
        lblTagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSection.add(logoCircle);
        topSection.add(Box.createVerticalStrut(16));
        topSection.add(lblBrand);
        topSection.add(Box.createVerticalStrut(6));
        topSection.add(lblTagline);

        // ── middle: feature highlights ────────────────────────────────────────
        JPanel features = new JPanel();
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        features.setOpaque(false);
        features.setBorder(new EmptyBorder(32, 0, 0, 0));

        addFeature(features, "🎓", "Student Lifecycle",
                "Results, internals, SGPA/CGPA, attendance & feedback");
        features.add(Box.createVerticalStrut(20));
        addFeature(features, "👨‍🏫", "Faculty Portal",
                "Attendance register, marks entry, assignments & timetable");
        features.add(Box.createVerticalStrut(20));
        addFeature(features, "🏛️", "HOD & Principal",
                "Department governance, duty leave sanctions & analytics");
        features.add(Box.createVerticalStrut(20));
        addFeature(features, "🛡️", "Secure & Audited",
                "SQL injection protection, BCrypt passwords, audit logs");

        // ── bottom: version badge ─────────────────────────────────────────────
        JLabel lblVersion = new JLabel("v1.0  •  Java Swing  •  MySQL");
        lblVersion.setFont(UITheme.FONT_SMALL);
        lblVersion.setForeground(new Color(0x475569));
        lblVersion.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(topSection, BorderLayout.NORTH);
        panel.add(features,   BorderLayout.CENTER);
        panel.add(lblVersion, BorderLayout.SOUTH);

        return panel;
    }

    private void addFeature(JPanel container, String icon, String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblIcon.setVerticalAlignment(SwingConstants.TOP);
        lblIcon.setBorder(new EmptyBorder(2, 0, 0, 0));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(UITheme.FONT_BOLD);
        lblT.setForeground(Color.WHITE);
        lblT.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblD = new JLabel("<html>" + desc + "</html>");
        lblD.setFont(UITheme.FONT_SMALL);
        lblD.setForeground(new Color(0x94A3B8));
        lblD.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(lblT);
        text.add(Box.createVerticalStrut(2));
        text.add(lblD);

        row.add(lblIcon, BorderLayout.WEST);
        row.add(text,    BorderLayout.CENTER);

        container.add(row);
    }

    // ── RIGHT: login card ────────────────────────────────────────────────────
    private JPanel buildLoginCard() {
        // Outer wrapper — grey background
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG);

        // The white card itself
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Drop shadow effect
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, UITheme.CARD_ARC + 4, UITheme.CARD_ARC + 4);
                // White card
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, UITheme.CARD_ARC, UITheme.CARD_ARC);
                // Border
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, UITheme.CARD_ARC, UITheme.CARD_ARC);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(40, 42, 40, 42));
        card.setPreferredSize(new Dimension(400, 520));
        card.setMaximumSize(new Dimension(420, 560));

        // ── header ────────────────────────────────────────────────────────────
        JLabel lblWelcome = new JLabel("Welcome back");
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.TEXT);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Sign in to continue to Acadexia");
        lblSub.setFont(UITheme.FONT_REGULAR);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── username / id field ───────────────────────────────────────────────
        JLabel lblUser = UIComponents.createFormLabel("Username / Register No.");
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleLoginField(txtIdentifier);
        txtIdentifier.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtIdentifier.putClientProperty("JTextField.placeholderText", "e.g. admin, REG2024CS001");

        // ── password field ────────────────────────────────────────────────────
        JLabel lblPass = UIComponents.createFormLabel("Password");
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field + show/hide toggle in a layered panel
        JPanel passRow = buildPasswordRow();
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // ── error label ───────────────────────────────────────────────────────
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── sign in button ────────────────────────────────────────────────────
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());   // Enter key

        // ── demo credentials hint ─────────────────────────────────────────────
        JPanel hintPanel = buildDemoHint();
        hintPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── register link ─────────────────────────────────────────────────────
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        registerRow.setOpaque(false);
        registerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNoAcc = new JLabel("Don't have an account?  ");
        lblNoAcc.setFont(UITheme.FONT_REGULAR);
        lblNoAcc.setForeground(UITheme.TEXT_MUTED);

        JButton btnRegister = UIComponents.createGhostButton("Register here", UITheme.PRIMARY);
        btnRegister.setFont(UITheme.FONT_BOLD);
        btnRegister.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
        });

        registerRow.add(lblNoAcc);
        registerRow.add(btnRegister);

        // ── assemble card ─────────────────────────────────────────────────────
        card.add(lblWelcome);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtIdentifier);
        card.add(Box.createVerticalStrut(16));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(passRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblError);
        card.add(Box.createVerticalStrut(18));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(20));
        card.add(UIComponents.createDivider());
        card.add(Box.createVerticalStrut(16));
        card.add(hintPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(registerRow);

        wrapper.add(card);
        return wrapper;
    }

    /** Password field + eye toggle button in a BorderLayout row. */
    private JPanel buildPasswordRow() {
        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setOpaque(false);

        styleLoginField(txtPassword);

        btnTogglePass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnTogglePass.setContentAreaFilled(false);
        btnTogglePass.setBorderPainted(false);
        btnTogglePass.setFocusPainted(false);
        btnTogglePass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTogglePass.setToolTipText("Show / hide password");
        btnTogglePass.setPreferredSize(new Dimension(38, 38));
        btnTogglePass.addActionListener(e -> togglePasswordVisibility());

        // Wrap toggle in a panel that matches the field border style
        JPanel toggleWrapper = new JPanel(new BorderLayout());
        toggleWrapper.setBackground(UITheme.SURFACE);
        toggleWrapper.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 1, UITheme.BORDER),
                new EmptyBorder(0, 0, 0, 4)
        ));
        toggleWrapper.add(btnTogglePass, BorderLayout.CENTER);

        row.add(txtPassword,    BorderLayout.CENTER);
        row.add(toggleWrapper,  BorderLayout.EAST);
        return row;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePass.setText("🙈");
        } else {
            txtPassword.setEchoChar('•');
            btnTogglePass.setText("👁");
        }
    }

    /** Small collapsible hint with demo credentials. */
    private JPanel buildDemoHint() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblHint = new JLabel("Demo credentials (for testing)");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);
        lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Quick-fill chip buttons in a wrap panel
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);

        chips.add(createChip("Admin",     "admin",         "admin123"));
        chips.add(createChip("Student",   "REG2024CS001",  "student123"));
        chips.add(createChip("Faculty",   "FAC-CS-001",    "faculty123"));
        chips.add(createChip("Principal", "principal",     "principal123"));

        panel.add(lblHint);
        panel.add(Box.createVerticalStrut(6));
        panel.add(chips);
        return panel;
    }

    private JButton createChip(String label, String id, String pass) {
        JButton chip = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? UITheme.PRIMARY_LIGHT
                        : new Color(0xF1F5F9));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(UITheme.FONT_SMALL);
        chip.setForeground(UITheme.TEXT_MUTED);
        chip.setContentAreaFilled(false);
        chip.setBorderPainted(false);
        chip.setFocusPainted(false);
        chip.setOpaque(false);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setBorder(new EmptyBorder(5, 12, 5, 12));
        chip.setToolTipText(id + " / " + pass);
        chip.addActionListener(e -> {
            txtIdentifier.setText(id);
            txtPassword.setText(pass);
            lblError.setText(" ");
        });
        return chip;
    }

    private void styleLoginField(JTextComponent field) {
        field.setFont(UITheme.FONT_REGULAR);
        field.setBackground(UITheme.SURFACE);
        field.setForeground(UITheme.TEXT);
        field.setCaretColor(UITheme.PRIMARY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, false),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UITheme.BORDER_FOCUS, 1, false),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UITheme.BORDER, 1, false),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    // ── authentication logic (UNCHANGED from original) ───────────────────────
    private void performLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password   = new String(txtPassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both ID and password.");
            return;
        }

        lblError.setForeground(UITheme.TEXT_MUTED);
        lblError.setText("Authenticating…");
        btnLogin.setEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() {
                return userDAO.authenticate(identifier, password);
            }
            @Override protected void done() {
                btnLogin.setEnabled(true);
                try {
                    User user = get();
                    if (user != null) {
                        lblError.setText(" ");
                        SessionManager.getInstance().login(user);
                        openRoleDashboard(user);
                        dispose();
                    } else {
                        lblError.setForeground(UITheme.DANGER);
                        lblError.setText("Invalid credentials or account inactive.");
                    }
                } catch (Exception ex) {
                    lblError.setForeground(UITheme.DANGER);
                    lblError.setText("Login error. Please try again.");
                }
            }
        };
        worker.execute();
    }

    private void openRoleDashboard(User user) {
        Role role = user.getRole();
        switch (role) {
            case STUDENT   -> new StudentDashboardFrame().setVisible(true);
            case FACULTY   -> new FacultyDashboardFrame().setVisible(true);
            case PRINCIPAL -> new PrincipalDashboardFrame().setVisible(true);
            case ADMIN     -> new AdminDashboardFrame().setVisible(true);
        }
    }
}
