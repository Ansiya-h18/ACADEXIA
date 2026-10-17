package com.acadexia.ui.auth;

import com.acadexia.dao.UserDAO;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.admin.AdminDashboardFrame;
import com.acadexia.ui.common.UITheme;
import com.acadexia.ui.faculty.FacultyDashboardFrame;
import com.acadexia.ui.parent.ParentDashboardFrame;
import com.acadexia.ui.principal.PrincipalDashboardFrame;
import com.acadexia.ui.student.StudentDashboardFrame;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Modern Acadexia Landing & Authentication Portal.
 * Features an executive university-management-system aesthetic:
 * - Rich dark navy and indigo/purple color palette
 * - High-DPI anti-aliased Java2D vector crests and academic illustrations
 * - Responsive floating login card with glowing focus states
 * - Password visibility toggle and smooth hover interactions
 * - Interactive quick-access persona selector for evaluation
 * - Non-blocking asynchronous credential verification
 */
public class LandingFrame extends JFrame {

    // --- Color System: Dark Navy + Electric Blue Hover Theme ---
    private static final Color BG_DARK_TOP        = new Color(8, 12, 22);     // #080C16 Deepest Midnight Navy
    private static final Color BG_DARK_BOTTOM     = new Color(13, 20, 38);    // #0D1426 Slate Navy Canvas
    private static final Color CARD_BG_TOP        = new Color(19, 29, 54);    // #131D36 Elevated Dark Blue Card
    private static final Color CARD_BG_BOTTOM     = new Color(14, 21, 40);    // #0E1528 Bottom Card
    private static final Color CARD_BORDER        = new Color(34, 52, 94);    // #22345E Card Border

    private static final Color INPUT_BG           = new Color(10, 15, 28);    // #0A0F1C Recessed Input BG
    private static final Color INPUT_BORDER       = new Color(28, 42, 74);    // #1C2A4A Default Input Border
    private static final Color INPUT_BORDER_FOCUS = new Color(0, 210, 255);   // #00D2FF Electric Cyan
    private static final Color INPUT_FOCUS_GLOW   = new Color(0, 210, 255, 55);

    private static final Color INDIGO_PRIMARY     = new Color(37, 99, 235);   // #2563EB Electric Blue
    private static final Color INDIGO_HOVER       = new Color(59, 130, 246);  // #3B82F6 Bright Blue
    private static final Color PURPLE_ACCENT      = new Color(0, 210, 255);   // #00D2FF Cyan Accent
    private static final Color PURPLE_DEEP        = new Color(2, 132, 199);   // #0284C7 Deep Sky Blue

    private static final Color TEXT_HEAD          = new Color(248, 250, 252); // #F8FAFC
    private static final Color TEXT_BODY          = new Color(226, 232, 240); // #E2E8F0
    private static final Color TEXT_MUTED         = new Color(148, 163, 184); // #94A3B8
    private static final Color TEXT_DIM           = new Color(100, 116, 139); // #64748B

    // Role Accent Colors
    private static final Color COLOR_STUDENT      = new Color(16, 185, 129);  // #10B981 Emerald
    private static final Color COLOR_CFA          = new Color(14, 165, 233);  // #0EA5E9 Sky Blue
    private static final Color COLOR_HOD_CSE      = new Color(99, 102, 241);  // #6366F1 Indigo
    private static final Color COLOR_HOD_MATH     = new Color(168, 85, 247);  // #A855F7 Purple
    private static final Color COLOR_PRINCIPAL    = new Color(245, 158, 11);  // #F59E0B Amber
    private static final Color COLOR_ADMIN        = new Color(244, 63, 94);   // #F43F5E Rose
    private static final Color COLOR_PARENT       = new Color(234, 88, 12);   // #EA580C Amber Orange

    // Fields & State
    private final JTextField txtIdentifier = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final NoticeBanner noticeBanner = new NoticeBanner();
    private final GradientButton btnLogin = new GradientButton("Sign In to Portal");
    private final UserDAO userDAO = new UserDAO();

    private boolean passwordRevealed = false;
    private EyeToggleButton btnTogglePassword;
    private Timer pulseTimer;
    private float pulsePhase = 0f;

    public LandingFrame() {
        setTitle("Acadexia — Higher Education ERP & Campus Governance");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 715);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setIconImage(createAcademicCrest(64));

        initUI();
        startPulseAnimation();
    }

    private void initUI() {
        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        // Responsive 2-column split (Hero left 53%, Login card right 47%)
        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        contentGrid.setBorder(new EmptyBorder(20, 36, 20, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left: Academic Hero Panel
        gbc.gridx = 0;
        gbc.weightx = 0.53;
        contentGrid.add(createHeroPanel(), gbc);

        // Right: Login Card Wrapper
        gbc.gridx = 1;
        gbc.weightx = 0.47;
        contentGrid.add(createLoginCardWrapper(), gbc);

        root.add(contentGrid, BorderLayout.CENTER);
    }

    // =========================================================================
    // HERO SECTION (Left)
    // =========================================================================

    private JPanel createHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout(0, 14));
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(8, 12, 8, 22));

        // 1. Top Branding Header
        JPanel brandHeader = new JPanel(new BorderLayout(14, 0));
        brandHeader.setOpaque(false);

        JLabel crestLabel = new JLabel(new ImageIcon(createAcademicCrest(54)));
        brandHeader.add(crestLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JPanel namePillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        namePillRow.setOpaque(false);
        namePillRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrand = new JLabel("ACADEXIA");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblBrand.setForeground(TEXT_HEAD);

        JLabel enterprisePill = createPillBadge("ENTERPRISE ERP", INDIGO_PRIMARY, new Color(99, 102, 241, 35));

        namePillRow.add(lblBrand);
        namePillRow.add(enterprisePill);

        JLabel lblMotto = new JLabel("Higher Education ERP & Campus Governance System");
        lblMotto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMotto.setForeground(TEXT_MUTED);
        lblMotto.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(namePillRow);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblMotto);

        brandHeader.add(titleBlock, BorderLayout.CENTER);

        // 2. Center Content: Headline & 4 Feature Tiles
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel headlines = new JPanel();
        headlines.setOpaque(false);
        headlines.setLayout(new BoxLayout(headlines, BoxLayout.Y_AXIS));

        JLabel lblHeadline = new JLabel("<html>Empowering Academic Excellence<br>&amp; Institutional Governance</html>");
        lblHeadline.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblHeadline.setForeground(TEXT_HEAD);
        lblHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("A unified, role-governed desktop ecosystem for students, faculty, and leadership.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        headlines.add(lblHeadline);
        headlines.add(Box.createVerticalStrut(4));
        headlines.add(lblDesc);

        centerPanel.add(headlines, BorderLayout.NORTH);

        // 4 Modern Feature Tiles
        JPanel featuresGrid = new JPanel(new GridLayout(4, 1, 0, 8));
        featuresGrid.setOpaque(false);

        featuresGrid.add(new FeatureTile(
                FeatureTile.ICON_STUDENT,
                COLOR_STUDENT,
                "Student Lifecycle & Academic Analytics",
                "<html>Attendance tracking, internal SGPA/CGPA scores<br>and tier-based duty leave management.</html>"
        ));

        featuresGrid.add(new FeatureTile(
                FeatureTile.ICON_FACULTY,
                COLOR_CFA,
                "Multi-Role Faculty & Mentor Workspace",
                "<html>Subject attendance registers, continuous assessments<br>and CFA mentor advisory monitoring.</html>"
        ));

        featuresGrid.add(new FeatureTile(
                FeatureTile.ICON_PRINCIPAL,
                COLOR_PRINCIPAL,
                "Executive Governance & Principal Sanctions",
                "<html>Tier-3 institutional duty leave approvals, HOD chairs<br>and campus-wide performance analytics.</html>"
        ));

        featuresGrid.add(new FeatureTile(
                FeatureTile.ICON_SECURITY,
                PURPLE_ACCENT,
                "Zero-Trust Architecture & Forensic Audit",
                "<html>256-bit BCrypt cryptography, parameter-bound queries<br>and tamper-evident forensic event logs.</html>"
        ));

        centerPanel.add(featuresGrid, BorderLayout.CENTER);

        // 3. Bottom Campus Health Status Bar
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(30, 42, 68), 10, 1),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JPanel statusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusLeft.setOpaque(false);

        PulsingDot dot = new PulsingDot();
        JLabel lblStatus = new JLabel("Campus Core Engine Online  •  Encrypted Session Active");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(203, 213, 225));

        statusLeft.add(dot);
        statusLeft.add(lblStatus);

        JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        statusRight.setOpaque(false);
        statusRight.add(createPillBadge("v1.0.0", TEXT_DIM, new Color(30, 41, 59)));
        statusRight.add(createPillBadge("SECURE", COLOR_STUDENT, new Color(16, 185, 129, 30)));

        statusBar.add(statusLeft, BorderLayout.WEST);
        statusBar.add(statusRight, BorderLayout.EAST);

        hero.add(brandHeader, BorderLayout.NORTH);
        hero.add(centerPanel, BorderLayout.CENTER);
        hero.add(statusBar, BorderLayout.SOUTH);

        return hero;
    }

    // =========================================================================
    // LOGIN CARD SECTION (Right)
    // =========================================================================

    private JPanel createLoginCardWrapper() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(4, 4, 4, 4));

        LoginCard card = new LoginCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 26, 24, 26));

        // 1. Card Header
        JPanel cardHeader = new JPanel();
        cardHeader.setOpaque(false);
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        cardHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel overline = new JLabel("PORTAL AUTHENTICATION");
        overline.setFont(new Font("Segoe UI", Font.BOLD, 10));
        overline.setForeground(new Color(165, 180, 252));
        overline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Institutional Sign In");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_HEAD);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Access your role-governed academic portal workspace.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardHeader.add(overline);
        cardHeader.add(Box.createVerticalStrut(4));
        cardHeader.add(lblTitle);
        cardHeader.add(Box.createVerticalStrut(3));
        cardHeader.add(lblSubtitle);

        // 2. Input Fields
        JLabel lblUser = new JLabel("Institutional ID / Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(TEXT_BODY);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Document filters for SQL injection mitigation and character control
        InputFilterFactory.applyLoginIdentifierFilter(txtIdentifier, 60);
        InputFilterFactory.applyPasswordFilter(txtPassword, 50);

        txtIdentifier.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. parent.rohan, REG2024CS001, FAC-CS-001, admin");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your institutional password");

        ModernInputField userFieldWrap = new ModernInputField(ModernInputField.ICON_USER, txtIdentifier, null);
        userFieldWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(TEXT_BODY);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnTogglePassword = new EyeToggleButton();
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());

        ModernInputField passFieldWrap = new ModernInputField(ModernInputField.ICON_LOCK, txtPassword, btnTogglePassword);
        passFieldWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Enter key listeners
        txtIdentifier.addActionListener(e -> {
            if (txtPassword.getPassword().length == 0) {
                txtPassword.requestFocusInWindow();
            } else {
                performLogin();
            }
        });
        txtPassword.addActionListener(e -> performLogin());

        // 3. Notice Banner (Error / Authenticating Feedback)
        noticeBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 4. Primary Sign In Button
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());

        // 5. Quick Access Personas (Modern 2x3 Grid)
        JPanel personaSection = new JPanel();
        personaSection.setOpaque(false);
        personaSection.setLayout(new BoxLayout(personaSection, BoxLayout.Y_AXIS));
        personaSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel personaHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        personaHeader.setOpaque(false);
        personaHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblPersonaTitle = new JLabel("QUICK ACCESS DEMO PERSONAS");
        lblPersonaTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPersonaTitle.setForeground(new Color(148, 163, 184));
        personaHeader.add(lblPersonaTitle);

        JPanel personaGrid = new JPanel(new GridLayout(4, 2, 8, 5));
        personaGrid.setOpaque(false);
        personaGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        personaGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 162));

        personaGrid.add(createPersonaChip("STUDENT", COLOR_STUDENT, "Rohan V.", "REG2024CS001", "student123"));
        personaGrid.add(createPersonaChip("PARENT", COLOR_PARENT, "Mr. S. Das", "parent.rohan", "parent123"));
        personaGrid.add(createPersonaChip("CFA", COLOR_CFA, "Prof. Arun", "FAC-CS-002", "faculty123"));
        personaGrid.add(createPersonaChip("HOD CSE", COLOR_HOD_CSE, "Dr. Rajesh", "FAC-CS-001", "faculty123"));
        personaGrid.add(createPersonaChip("HOD MATH", COLOR_HOD_MATH, "Dr. Sarah", "FAC-MA-001", "faculty123"));
        personaGrid.add(createPersonaChip("PRINCIPAL", COLOR_PRINCIPAL, "Dr. Menon", "principal", "principal123"));
        personaGrid.add(createPersonaChip("ADMIN", COLOR_ADMIN, "Sys Admin", "admin", "admin123"));

        personaSection.add(personaHeader);
        personaSection.add(Box.createVerticalStrut(6));
        personaSection.add(personaGrid);

        // 6. Security Footer with painted vector lock (Aligned to LEFT with centered content inside)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        footerPanel.setOpaque(false);
        footerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JComponent miniLock = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(12, 12);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEXT_DIM);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawArc(3, 1, 6, 6, 0, 180);
                g2.fillRoundRect(1, 4, 10, 7, 2, 2);
                g2.dispose();
            }
        };

        JLabel lblFooter = new JLabel("End-to-End BCrypt Encryption  •  Role-Governed Access");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(TEXT_DIM);

        footerPanel.add(miniLock);
        footerPanel.add(lblFooter);

        // Assembly
        card.add(cardHeader);
        card.add(Box.createVerticalStrut(12));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(4));
        card.add(userFieldWrap);
        card.add(Box.createVerticalStrut(10));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(4));
        card.add(passFieldWrap);
        card.add(Box.createVerticalStrut(6));
        card.add(noticeBanner);
        card.add(Box.createVerticalStrut(8));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(personaSection);
        card.add(Box.createVerticalStrut(12));
        card.add(footerPanel);

        wrapper.add(card);
        return wrapper;
    }

    // =========================================================================
    // AUTHENTICATION LOGIC (Preserving 100% Functionality & Role Routing)
    // =========================================================================

    private void performLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            noticeBanner.showError("Please enter both identifier and password.");
            return;
        }

        noticeBanner.showInfo("Verifying credentials with institutional directory...");
        setFormEnabled(false);

        // Run database query asynchronously to keep UI buttery smooth
        new Thread(() -> {
            try {
                User user = userDAO.authenticate(identifier, password);
                SwingUtilities.invokeLater(() -> {
                    if (user != null) {
                        noticeBanner.hideNotice();
                        SessionManager.getInstance().login(user);
                        openRoleDashboard(user);
                        dispose(); // Close landing portal
                    } else {
                        setFormEnabled(true);
                        noticeBanner.showError("Invalid credentials or account inactive.");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setFormEnabled(true);
                    noticeBanner.showError("Directory service error. Check MySQL database connection.");
                });
            }
        }).start();
    }

    private void setFormEnabled(boolean enabled) {
        txtIdentifier.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        btnLogin.setText(enabled ? "Sign In to Portal" : "Authenticating...");
    }

    private void openRoleDashboard(User user) {
        Role role = user.getRole();
        switch (role) {
            case STUDENT -> {
                StudentDashboardFrame frame = new StudentDashboardFrame();
                frame.setVisible(true);
            }
            case FACULTY -> {
                FacultyDashboardFrame frame = new FacultyDashboardFrame();
                frame.setVisible(true);
            }
            case PRINCIPAL -> {
                PrincipalDashboardFrame frame = new PrincipalDashboardFrame();
                frame.setVisible(true);
            }
            case ADMIN -> {
                AdminDashboardFrame frame = new AdminDashboardFrame();
                frame.setVisible(true);
            }
            case PARENT -> {
                ParentDashboardFrame frame = new ParentDashboardFrame();
                frame.setVisible(true);
            }
        }
    }

    private void togglePasswordVisibility() {
        passwordRevealed = !passwordRevealed;
        txtPassword.setEchoChar(passwordRevealed ? (char) 0 : '•');
        btnTogglePassword.setRevealed(passwordRevealed);
        txtPassword.requestFocusInWindow();
    }

    private JPanel createPersonaChip(String badgeText, Color badgeColor, String name, String id, String pass) {
        PersonaChip chip = new PersonaChip(badgeText, badgeColor, name, id);
        chip.addActionListener(e -> {
            txtIdentifier.setText(id);
            txtPassword.setText(pass);
            noticeBanner.hideNotice();
            btnLogin.requestFocusInWindow();
        });
        return chip;
    }

    private JLabel createPillBadge(String text, Color fg, Color bg) {
        JLabel pill = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(fg);
        pill.setBorder(new EmptyBorder(3, 8, 3, 8));
        pill.setOpaque(false);
        return pill;
    }

    private void startPulseAnimation() {
        pulseTimer = new Timer(40, e -> {
            pulsePhase = (float) ((Math.sin(System.currentTimeMillis() / 320.0) + 1.0) / 2.0);
            repaint();
        });
        pulseTimer.start();
    }

    @Override
    public void dispose() {
        if (pulseTimer != null && pulseTimer.isRunning()) {
            pulseTimer.stop();
        }
        super.dispose();
    }

    // =========================================================================
    // VECTOR GRAPHICS & CUSTOM COMPONENTS
    // =========================================================================

    /**
     * Generates a high-DPI academic shield crest with open book and wisdom star.
     */
    public static BufferedImage createAcademicCrest(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float s = size / 48f;

        // Shield geometry
        Path2D.Double shield = new Path2D.Double();
        shield.moveTo(6 * s, 6 * s);
        shield.lineTo(42 * s, 6 * s);
        shield.quadTo(42 * s, 28 * s, 24 * s, 44 * s);
        shield.quadTo(6 * s, 28 * s, 6 * s, 6 * s);
        shield.closePath();

        // Indigo to Violet gradient
        GradientPaint gp = new GradientPaint(6 * s, 6 * s, new Color(99, 102, 241), 42 * s, 44 * s, new Color(139, 92, 246));
        g2.setPaint(gp);
        g2.fill(shield);

        // Crisp inner stroke
        g2.setStroke(new BasicStroke(1.5f * s));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.draw(shield);

        // Open book
        float cx = 24 * s;
        float cy = 25 * s;
        Path2D.Double bookLeft = new Path2D.Double();
        bookLeft.moveTo(cx - 1 * s, cy + 3 * s);
        bookLeft.quadTo(cx - 7 * s, cy + 1 * s, cx - 12 * s, cy + 3 * s);
        bookLeft.lineTo(cx - 12 * s, cy + 10 * s);
        bookLeft.quadTo(cx - 7 * s, cy + 8 * s, cx - 1 * s, cy + 10 * s);
        bookLeft.closePath();

        Path2D.Double bookRight = new Path2D.Double();
        bookRight.moveTo(cx + 1 * s, cy + 3 * s);
        bookRight.quadTo(cx + 7 * s, cy + 1 * s, cx + 12 * s, cy + 3 * s);
        bookRight.lineTo(cx + 12 * s, cy + 10 * s);
        bookRight.quadTo(cx + 7 * s, cy + 8 * s, cx + 1 * s, cy + 10 * s);
        bookRight.closePath();

        g2.setColor(Color.WHITE);
        g2.fill(bookLeft);
        g2.fill(bookRight);

        // Radiant star of knowledge
        Path2D.Double star = new Path2D.Double();
        star.moveTo(cx, cy - 8 * s);
        star.lineTo(cx + 2.5f * s, cy - 4.5f * s);
        star.lineTo(cx + 6.5f * s, cy - 3 * s);
        star.lineTo(cx + 2.5f * s, cy - 1.5f * s);
        star.lineTo(cx, cy + 2.5f * s);
        star.lineTo(cx - 2.5f * s, cy - 1.5f * s);
        star.lineTo(cx - 6.5f * s, cy - 3 * s);
        star.lineTo(cx - 2.5f * s, cy - 4.5f * s);
        star.closePath();

        g2.setColor(new Color(253, 224, 71)); // Golden amber
        g2.fill(star);

        g2.dispose();
        return img;
    }

    /**
     * Canvas background with deep navy gradient, subtle ambient radial glows, and tech micro-dot matrix.
     */
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Diagonal navy gradient
            GradientPaint gp = new GradientPaint(0, 0, BG_DARK_TOP, w, h, BG_DARK_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // 2. Top-left indigo ambient glow
            int r1 = 450;
            RadialGradientPaint heroGlow = new RadialGradientPaint(
                    new Point2D.Float(100, 100),
                    r1,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{new Color(79, 70, 229, 38), new Color(99, 102, 241, 14), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(heroGlow);
            g2.fillOval(100 - r1, 100 - r1, r1 * 2, r1 * 2);

            // 3. Bottom-right purple ambient glow
            int r2 = 380;
            RadialGradientPaint cardGlow = new RadialGradientPaint(
                    new Point2D.Float(w - 120, h - 120),
                    r2,
                    new float[]{0f, 0.7f, 1f},
                    new Color[]{new Color(139, 92, 246, 26), new Color(124, 58, 237, 8), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(cardGlow);
            g2.fillOval((w - 120) - r2, (h - 120) - r2, r2 * 2, r2 * 2);

            // 4. Subtle micro-dot matrix pattern
            g2.setColor(new Color(255, 255, 255, 7));
            int step = 32;
            for (int x = 16; x < w; x += step) {
                for (int y = 16; y < h; y += step) {
                    g2.fillRect(x, y, 1, 1);
                }
            }

            g2.dispose();
        }
    }

    /**
     * Floating login card panel with dual-tone gradient fill, rounded corners, and glowing border.
     */
    private static class LoginCard extends JPanel {
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(454, d.width), d.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 20;

            // Background gradient
            GradientPaint gp = new GradientPaint(0, 0, CARD_BG_TOP, 0, h, CARD_BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Subtle top highlight border
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Inner subtle top edge light
            g2.setColor(new Color(255, 255, 255, 22));
            g2.drawLine(arc / 2, 1, w - arc / 2, 1);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Interactive feature tile on the hero section with vector icon badge and hover highlight.
     */
    private static class FeatureTile extends JPanel {
        public static final int ICON_STUDENT = 1;
        public static final int ICON_FACULTY = 2;
        public static final int ICON_PRINCIPAL = 3;
        public static final int ICON_SECURITY = 4;

        private final int iconType;
        private final Color accent;
        private boolean isHovered = false;

        public FeatureTile(int iconType, Color accent, String title, String desc) {
            this.iconType = iconType;
            this.accent = accent;
            setOpaque(false);
            setLayout(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            // Left icon badge
            JComponent iconBadge = new JComponent() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(36, 36);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Badge circle background
                    Color badgeBg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), isHovered ? 45 : 28);
                    g2.setColor(badgeBg);
                    g2.fillRoundRect(0, 0, 36, 36, 12, 12);

                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, 35, 35, 12, 12);

                    // Draw vector icon
                    int cx = 18;
                    int cy = 18;
                    g2.setColor(accent);

                    switch (iconType) {
                        case ICON_STUDENT -> {
                            // Mortarboard cap
                            Path2D.Double cap = new Path2D.Double();
                            cap.moveTo(cx, cy - 6);
                            cap.lineTo(cx + 10, cy - 1);
                            cap.lineTo(cx, cy + 4);
                            cap.lineTo(cx - 10, cy - 1);
                            cap.closePath();
                            g2.fill(cap);
                            g2.fillArc(cx - 5, cy, 10, 6, 180, 180);
                            g2.setStroke(new BasicStroke(1.2f));
                            g2.drawLine(cx, cy - 1, cx + 9, cy + 6);
                        }
                        case ICON_FACULTY -> {
                            // Lecture podium / chalkboard
                            g2.setStroke(new BasicStroke(1.4f));
                            g2.drawRoundRect(cx - 8, cy - 8, 16, 11, 3, 3);
                            g2.drawLine(cx - 5, cy - 5, cx + 5, cy - 5);
                            g2.drawLine(cx - 5, cy - 2, cx + 2, cy - 2);
                            g2.drawLine(cx, cy + 3, cx, cy + 8);
                            g2.drawLine(cx - 4, cy + 8, cx + 4, cy + 8);
                        }
                        case ICON_PRINCIPAL -> {
                            // Executive neoclassical pillars
                            Path2D.Double roof = new Path2D.Double();
                            roof.moveTo(cx, cy - 8);
                            roof.lineTo(cx - 9, cy - 3);
                            roof.lineTo(cx + 9, cy - 3);
                            roof.closePath();
                            g2.fill(roof);
                            g2.fillRect(cx - 8, cy - 2, 16, 2);
                            g2.fillRect(cx - 7, cy + 1, 3, 6);
                            g2.fillRect(cx - 1, cy + 1, 3, 6);
                            g2.fillRect(cx + 5, cy + 1, 3, 6);
                            g2.fillRect(cx - 9, cy + 8, 18, 2);
                        }
                        case ICON_SECURITY -> {
                            // Shield with checkmark
                            Path2D.Double sh = new Path2D.Double();
                            sh.moveTo(cx - 7, cy - 7);
                            sh.lineTo(cx + 7, cy - 7);
                            sh.quadTo(cx + 7, cy + 2, cx, cy + 8);
                            sh.quadTo(cx - 7, cy + 2, cx - 7, cy - 7);
                            sh.closePath();
                            g2.setStroke(new BasicStroke(1.4f));
                            g2.draw(sh);
                            g2.drawLine(cx - 3, cy, cx - 1, cy + 2);
                            g2.drawLine(cx - 1, cy + 2, cx + 4, cy - 3);
                        }
                    }

                    g2.dispose();
                }
            };

            // Text block with explicit left-alignment so labels expand to full width
            JPanel textBlock = new JPanel();
            textBlock.setOpaque(false);
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

            JLabel lblT = new JLabel(title);
            lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblT.setForeground(TEXT_HEAD);
            lblT.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblD = new JLabel(desc);
            lblD.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblD.setForeground(TEXT_MUTED);
            lblD.setAlignmentX(Component.LEFT_ALIGNMENT);

            textBlock.add(lblT);
            textBlock.add(Box.createVerticalStrut(2));
            textBlock.add(lblD);

            add(iconBadge, BorderLayout.WEST);
            add(textBlock, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 12;

            // Background
            Color bg = isHovered ? new Color(24, 32, 54) : new Color(17, 23, 38);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Border
            Color border = isHovered ? INDIGO_PRIMARY : new Color(32, 42, 68);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Modern input field container featuring leading vector icon, focus glow ring, and optional trailing button.
     */
    private static class ModernInputField extends JPanel {
        public static final int ICON_USER = 1;
        public static final int ICON_LOCK = 2;

        private boolean hasFocus = false;

        public ModernInputField(int iconType, JComponent field, JComponent trailingComponent) {
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(3, 10, 3, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(320, 40));

            // Leading Icon
            JComponent leadingIcon = new JComponent() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(20, 20);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(hasFocus ? INDIGO_PRIMARY : new Color(129, 140, 248));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    if (iconType == ICON_USER) {
                        g2.fillOval(cx - 4, cy - 7, 8, 8);
                        g2.fillArc(cx - 7, cy + 2, 14, 12, 0, 180);
                    } else if (iconType == ICON_LOCK) {
                        g2.setStroke(new BasicStroke(1.6f));
                        g2.drawArc(cx - 4, cy - 7, 8, 8, 0, 180);
                        g2.fillRoundRect(cx - 6, cy - 1, 12, 9, 3, 3);
                        g2.setColor(INPUT_BG);
                        g2.fillOval(cx - 1, cy + 2, 2, 2);
                    }
                    g2.dispose();
                }
            };

            // Configure text field styles
            field.setOpaque(false);
            field.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setForeground(TEXT_HEAD);
            if (field instanceof JTextField tf) {
                tf.setCaretColor(TEXT_HEAD);
            }

            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    hasFocus = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    hasFocus = false;
                    repaint();
                }
            });

            add(leadingIcon, BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
            if (trailingComponent != null) {
                add(trailingComponent, BorderLayout.EAST);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            // Recessed background
            g2.setColor(INPUT_BG);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (hasFocus) {
                // Focus glow ring
                g2.setColor(INPUT_FOCUS_GLOW);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

                g2.setColor(INPUT_BORDER_FOCUS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            } else {
                // Default subtle border
                g2.setColor(INPUT_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Eye toggle button for password visibility.
     */
    private static class EyeToggleButton extends JButton {
        private boolean revealed = false;
        private boolean isHovered = false;

        public EyeToggleButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(28, 28));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setRevealed(boolean revealed) {
            this.revealed = revealed;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isHovered ? INDIGO_PRIMARY : TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.4f));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            // Eye outline
            Path2D.Double eye = new Path2D.Double();
            eye.moveTo(cx - 7, cy);
            eye.quadTo(cx, cy - 6, cx + 7, cy);
            eye.quadTo(cx, cy + 6, cx - 7, cy);
            eye.closePath();
            g2.draw(eye);

            if (revealed) {
                // Visible pupil
                g2.fillOval(cx - 2, cy - 2, 5, 5);
            } else {
                // Slashed eye indicating hidden
                g2.fillOval(cx - 2, cy - 2, 4, 4);
                g2.drawLine(cx - 6, cy + 5, cx + 6, cy - 5);
            }

            g2.dispose();
        }
    }

    /**
     * Primary action button with modern Indigo-Violet gradient and hover lighting.
     */
    private static class GradientButton extends JButton {
        private boolean isHovered = false;
        private boolean isPressed = false;

        public GradientButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            if (!isEnabled()) {
                g2.setColor(new Color(45, 45, 70));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            } else {
                Color c1, c2;
                if (isPressed) {
                    c1 = new Color(30, 64, 175);
                    c2 = new Color(29, 78, 216);
                } else if (isHovered) {
                    c1 = new Color(59, 130, 246);
                    c2 = new Color(0, 210, 255);
                } else {
                    c1 = new Color(37, 99, 235);
                    c2 = new Color(29, 78, 216);
                }

                GradientPaint gp = new GradientPaint(0, 0, c1, w, 0, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Subtle inner top highlight
                g2.setColor(new Color(255, 255, 255, 45));
                g2.drawLine(arc / 2, 1, w - arc / 2, 1);

                // Glowing Electric Cyan border on hover
                if (isHovered) {
                    g2.setColor(new Color(0, 210, 255, 180));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                }
            }

            // Draw button text and vector arrow
            FontMetrics fm = g2.getFontMetrics(getFont());
            String text = getText();
            int textW = fm.stringWidth(text);
            int totalW = textW + 18;
            int startX = (w - totalW) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setFont(getFont());
            g2.setColor(getForeground());
            g2.drawString(text, startX, textY);

            // Vector right arrow icon
            if (isEnabled()) {
                int ax = startX + textW + 8;
                int ay = h / 2;
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(ax, ay, ax + 8, ay);
                g2.drawLine(ax + 5, ay - 3, ax + 8, ay);
                g2.drawLine(ax + 5, ay + 3, ax + 8, ay);
            }

            g2.dispose();
        }
    }

    /**
     * Compact, elegant persona chip for one-click demo credentials.
     */
    private static class PersonaChip extends JPanel {
        private final Color roleColor;
        private boolean isHovered = false;
        private ActionListener actionListener;

        public PersonaChip(String roleTag, Color roleColor, String name, String subtitle) {
            this.roleColor = roleColor;
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(5, 7, 5, 7));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Left mini pill
            JLabel lblTag = new JLabel(roleTag, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(roleColor.getRed(), roleColor.getGreen(), roleColor.getBlue(), 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblTag.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblTag.setForeground(roleColor);
            lblTag.setBorder(new EmptyBorder(2, 5, 2, 5));

            // Text block
            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblName.setForeground(TEXT_HEAD);
            lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblSub.setForeground(TEXT_MUTED);
            lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(lblName);
            textPanel.add(lblSub);

            add(lblTag, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (actionListener != null) {
                        actionListener.actionPerformed(new ActionEvent(PersonaChip.this, ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            });
        }

        public void addActionListener(ActionListener l) {
            this.actionListener = l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 8;

            g2.setColor(isHovered ? new Color(24, 32, 54) : new Color(14, 19, 32));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(isHovered ? roleColor : new Color(30, 40, 64));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Polished alert notice banner for status/error feedback with vector icons.
     */
    private static class NoticeBanner extends JPanel {
        private final JLabel lblText = new JLabel(" ");
        private boolean isVisibleNotice = false;
        private boolean isError = false;

        public NoticeBanner() {
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

            // Leading vector warning/spinner icon
            JComponent badgeIcon = new JComponent() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(16, 16);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    if (!isVisibleNotice) return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    if (isError) {
                        // Warning triangle
                        Path2D.Double tri = new Path2D.Double();
                        tri.moveTo(cx, cy - 6);
                        tri.lineTo(cx + 6, cy + 5);
                        tri.lineTo(cx - 6, cy + 5);
                        tri.closePath();
                        g2.setColor(new Color(239, 68, 68));
                        g2.fill(tri);
                        g2.setColor(Color.WHITE);
                        g2.fillRect(cx - 1, cy - 2, 2, 4);
                        g2.fillRect(cx - 1, cy + 3, 2, 1);
                    } else {
                        // Processing dot ring
                        g2.setColor(INDIGO_PRIMARY);
                        g2.setStroke(new BasicStroke(1.8f));
                        g2.drawArc(cx - 5, cy - 5, 10, 10, 0, 270);
                    }
                    g2.dispose();
                }
            };

            lblText.setFont(new Font("Segoe UI", Font.BOLD, 11));
            add(badgeIcon, BorderLayout.WEST);
            add(lblText, BorderLayout.CENTER);
            setVisible(false);
        }

        public void showError(String msg) {
            isError = true;
            isVisibleNotice = true;
            lblText.setText(msg);
            lblText.setForeground(new Color(252, 165, 165));
            setVisible(true);
            revalidate();
            repaint();
        }

        public void showInfo(String msg) {
            isError = false;
            isVisibleNotice = true;
            lblText.setText(msg);
            lblText.setForeground(new Color(199, 210, 254));
            setVisible(true);
            revalidate();
            repaint();
        }

        public void hideNotice() {
            isVisibleNotice = false;
            lblText.setText(" ");
            setVisible(false);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!isVisibleNotice) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 8;

            if (isError) {
                g2.setColor(new Color(58, 20, 28));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(new Color(239, 68, 68));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            } else {
                g2.setColor(new Color(30, 27, 75));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(INDIGO_PRIMARY);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Pulsing emerald dot showing live campus gateway connectivity.
     */
    private class PulsingDot extends JComponent {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(16, 16);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            // Fading outer ring
            int maxRadius = 7;
            int haloR = (int) (4 + pulsePhase * maxRadius);
            int haloAlpha = (int) ((1f - pulsePhase) * 160);
            g2.setColor(new Color(16, 185, 129, Math.max(0, haloAlpha)));
            g2.fillOval(cx - haloR, cy - haloR, haloR * 2, haloR * 2);

            // Solid inner dot
            g2.setColor(COLOR_STUDENT);
            g2.fillOval(cx - 3, cy - 3, 7, 7);

            g2.dispose();
        }
    }

    /**
     * Generic rounded border helper.
     */
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int arc;
        private final int thickness;

        public RoundedBorder(Color color, int arc, int thickness) {
            this.color = color;
            this.arc = arc;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }
    }
}
