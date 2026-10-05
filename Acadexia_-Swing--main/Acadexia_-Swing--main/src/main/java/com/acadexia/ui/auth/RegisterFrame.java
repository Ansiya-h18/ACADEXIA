package com.acadexia.ui.auth;

import com.acadexia.dao.UserDAO;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Multi-step registration dialog.
 *
 * Step 1 — Account Information  (username, email, password, role)
 * Step 2 — Personal Information (name, phone, gender)
 * Step 3 — Confirmation summary
 *
 * Writes to the users table only (student/faculty profile records
 * are created by the Admin in their respective management panels).
 */
public class RegisterFrame extends JDialog {

    // ── state ─────────────────────────────────────────────────────────────────
    private int currentStep = 1;
    private static final int TOTAL_STEPS = 3;

    // Step 1 fields
    private JTextField     txtUsername;
    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;
    private JComboBox<Role> cmbRole;

    // Step 2 fields
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JComboBox<String> cmbGender;

    // Navigation
    private JButton btnBack;
    private JButton btnNext;
    private JButton btnCancel;
    private JLabel  lblStepTitle;
    private JLabel  lblStepSub;
    private JLabel  lblStepBadge;
    private JPanel  contentArea;
    private CardLayout cardLayout;

    private final UserDAO userDAO = new UserDAO();

    // ── constructor ───────────────────────────────────────────────────────────
    public RegisterFrame(Frame owner) {
        super(owner, "Create Account — Acadexia", true);
        setSize(520, 600);
        setMinimumSize(new Dimension(480, 540));
        setLocationRelativeTo(owner);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG);
        initFields();
        buildUI();
    }

    private void initFields() {
        txtUsername = UIComponents.createTextField("Choose a username");
        txtEmail    = UIComponents.createTextField("your@email.com");
        txtPassword = UIComponents.createPasswordField("Minimum 6 characters");
        txtConfirm  = UIComponents.createPasswordField("Re-enter password");
        cmbRole     = UIComponents.createComboBox(new Role[]{Role.STUDENT, Role.FACULTY});

        txtFullName = UIComponents.createTextField("Your full name");
        txtPhone    = UIComponents.createTextField("10-digit phone number");
        cmbGender   = UIComponents.createComboBox(new String[]{"Prefer not to say", "Male", "Female", "Other"});

        // Apply security filters
        InputFilterFactory.applyIdentifierFilter(txtUsername, 30);
        InputFilterFactory.applyPasswordFilter(txtPassword, 50);
        InputFilterFactory.applyPasswordFilter(txtConfirm, 50);
        InputFilterFactory.applySqlSanitizer(txtFullName, 80, true);
        InputFilterFactory.applySqlSanitizer(txtEmail, 80, false);
        InputFilterFactory.applyNumericFilter(txtPhone, 15, false);
    }

    // ── UI assembly ───────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        // ── header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.SIDEBAR_BG);
        header.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel brand = new JLabel("ACADEXIA");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(UITheme.PRIMARY);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStepTitle = new JLabel("Account Information");
        lblStepTitle.setFont(UITheme.FONT_SECTION);
        lblStepTitle.setForeground(Color.WHITE);
        lblStepTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStepSub = new JLabel("Create your login credentials");
        lblStepSub.setFont(UITheme.FONT_REGULAR);
        lblStepSub.setForeground(new Color(0x94A3B8));
        lblStepSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(brand);
        headerLeft.add(Box.createVerticalStrut(10));
        headerLeft.add(lblStepTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblStepSub);

        lblStepBadge = new JLabel("Step 1 of " + TOTAL_STEPS);
        lblStepBadge.setFont(UITheme.FONT_SMALL);
        lblStepBadge.setForeground(new Color(0x64748B));

        header.add(headerLeft,   BorderLayout.CENTER);
        header.add(lblStepBadge, BorderLayout.EAST);

        // ── step indicator bar ─────────────────────────────────────────────────
        JPanel stepBar = buildStepBar();

        // ── content area (CardLayout) ──────────────────────────────────────────
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG);
        contentArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentArea.add(buildStep1(), "step1");
        contentArea.add(buildStep2(), "step2");
        contentArea.add(buildStep3(), "step3");

        // ── footer navigation ──────────────────────────────────────────────────
        JPanel footer = buildFooter();

        // ── compose ────────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.add(header,  BorderLayout.CENTER);
        top.add(stepBar, BorderLayout.SOUTH);

        add(top,         BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
        add(footer,      BorderLayout.SOUTH);
    }

    private JPanel buildStepBar() {
        JPanel bar = new JPanel(new GridLayout(1, TOTAL_STEPS, 2, 0));
        bar.setBackground(UITheme.SIDEBAR_BG);
        bar.setPreferredSize(new Dimension(0, 4));
        bar.putClientProperty("stepBar", true);

        for (int i = 1; i <= TOTAL_STEPS; i++) {
            final int step = i;
            JPanel seg = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(step <= currentStep ? UITheme.PRIMARY : new Color(0x334155));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            seg.setName("seg" + i);
            bar.add(seg);
        }
        return bar;
    }

    private JPanel buildStep1() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(28, 32, 20, 32));

        p.add(makeRow("Username",         txtUsername));
        p.add(Box.createVerticalStrut(14));
        p.add(makeRow("Email Address",    txtEmail));
        p.add(Box.createVerticalStrut(14));
        p.add(makeRow("Password",         txtPassword));
        p.add(Box.createVerticalStrut(14));
        p.add(makeRow("Confirm Password", txtConfirm));
        p.add(Box.createVerticalStrut(14));
        p.add(makeRow("Account Type",     cmbRole));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildStep2() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(28, 32, 20, 32));

        p.add(makeRow("Full Name", txtFullName));
        p.add(Box.createVerticalStrut(14));
        p.add(makeRow("Phone Number", txtPhone));
        p.add(Box.createVerticalStrut(14));
        p.add(makeRow("Gender", cmbGender));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildStep3() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(28, 32, 20, 32));
        p.setName("step3");
        // Content is rebuilt dynamically when user reaches step 3
        return p;
    }

    private void refreshStep3() {
        // Find the step3 panel and rebuild its summary
        JPanel p = null;
        for (Component c : contentArea.getComponents()) {
            if ("step3".equals(c.getName())) { p = (JPanel) c; break; }
        }
        if (p == null) return;
        p.removeAll();

        JLabel heading = UIComponents.createSectionLabel("Confirm your information");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(heading);
        p.add(Box.createVerticalStrut(16));

        addSummaryRow(p, "Username",    txtUsername.getText());
        addSummaryRow(p, "Email",       txtEmail.getText());
        addSummaryRow(p, "Role",        cmbRole.getSelectedItem().toString());
        addSummaryRow(p, "Full Name",   txtFullName.getText());
        addSummaryRow(p, "Phone",       txtPhone.getText());
        addSummaryRow(p, "Gender",      cmbGender.getSelectedItem().toString());

        p.add(Box.createVerticalStrut(18));

        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        note.setOpaque(false);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("<html><b>Note:</b> After account creation, contact your Admin to<br>" +
                "link your profile to a student/faculty record.</html>");
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        note.add(lbl);
        p.add(note);

        p.add(Box.createVerticalGlue());
        p.revalidate();
        p.repaint();
    }

    private void addSummaryRow(JPanel container, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UITheme.SURFACE);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(10, 14, 10, 14)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value.isEmpty() ? "—" : value);
        val.setFont(UITheme.FONT_REGULAR);
        val.setForeground(UITheme.TEXT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        container.add(row);
        container.add(Box.createVerticalStrut(1));
    }

    private JPanel makeRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = UIComponents.createFormLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        row.add(lbl);
        row.add(Box.createVerticalStrut(5));
        row.add(field);
        return row;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(14, 28, 14, 28)
        ));

        btnCancel = UIComponents.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        btnBack = UIComponents.createSecondaryButton("← Back");
        btnBack.setEnabled(false);
        btnBack.addActionListener(e -> goBack());

        btnNext = UIComponents.createPrimaryButton("Continue →");
        btnNext.addActionListener(e -> goNext());

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);
        leftBtns.add(btnCancel);
        leftBtns.add(btnBack);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnNext);

        footer.add(leftBtns,  BorderLayout.WEST);
        footer.add(rightBtns, BorderLayout.EAST);
        return footer;
    }

    // ── navigation logic ──────────────────────────────────────────────────────
    private void goNext() {
        if (!validateCurrentStep()) return;

        if (currentStep == TOTAL_STEPS) {
            // Final step — submit
            createAccount();
            return;
        }

        currentStep++;
        if (currentStep == TOTAL_STEPS) {
            refreshStep3();
            btnNext.setText("Create Account");
        }
        updateStepUI();
        cardLayout.show(contentArea, "step" + currentStep);
    }

    private void goBack() {
        if (currentStep <= 1) return;
        currentStep--;
        if (currentStep < TOTAL_STEPS) {
            btnNext.setText("Continue →");
        }
        updateStepUI();
        cardLayout.show(contentArea, "step" + currentStep);
    }

    private void updateStepUI() {
        btnBack.setEnabled(currentStep > 1);
        lblStepBadge.setText("Step " + currentStep + " of " + TOTAL_STEPS);

        switch (currentStep) {
            case 1 -> { lblStepTitle.setText("Account Information");  lblStepSub.setText("Create your login credentials"); }
            case 2 -> { lblStepTitle.setText("Personal Information"); lblStepSub.setText("Tell us a bit about yourself"); }
            case 3 -> { lblStepTitle.setText("Confirmation");         lblStepSub.setText("Review and create your account"); }
        }
        // Repaint step bar
        for (Component c : getContentPane().getComponents()) {
            repaintStepBar(c);
        }
    }

    private void repaintStepBar(Component c) {
        if (c instanceof JPanel p) {
            if (Boolean.TRUE.equals(p.getClientProperty("stepBar"))) {
                p.repaint();
                return;
            }
            for (Component child : p.getComponents()) repaintStepBar(child);
        }
    }

    // ── validation ────────────────────────────────────────────────────────────
    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1 -> { return validateStep1(); }
            case 2 -> { return validateStep2(); }
            default -> { return true; }
        }
    }

    private boolean validateStep1() {
        String u  = txtUsername.getText().trim();
        String em = txtEmail.getText().trim();
        String p  = new String(txtPassword.getPassword());
        String c  = new String(txtConfirm.getPassword());

        if (u.isEmpty() || em.isEmpty() || p.isEmpty() || c.isEmpty()) {
            UIComponents.showError(this, "All fields in this step are required.");
            return false;
        }
        if (u.length() < 3) {
            UIComponents.showError(this, "Username must be at least 3 characters.");
            return false;
        }
        if (!em.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            UIComponents.showError(this, "Please enter a valid email address.");
            return false;
        }
        if (p.length() < 6) {
            UIComponents.showError(this, "Password must be at least 6 characters.");
            return false;
        }
        if (!p.equals(c)) {
            UIComponents.showError(this, "Passwords do not match.");
            return false;
        }
        // Check username uniqueness
        if (userDAO.usernameExists(u)) {
            UIComponents.showError(this, "Username '" + u + "' is already taken. Please choose another.");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        String name  = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            UIComponents.showError(this, "Full name is required.");
            return false;
        }
        if (name.length() < 2) {
            UIComponents.showError(this, "Please enter a valid full name.");
            return false;
        }
        if (!phone.isEmpty() && !phone.matches("\\d{7,15}")) {
            UIComponents.showError(this, "Please enter a valid phone number (7–15 digits).");
            return false;
        }
        return true;
    }

    // ── account creation ──────────────────────────────────────────────────────
    private void createAccount() {
        btnNext.setEnabled(false);
        btnNext.setText("Creating…");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() throws Exception {
                User u = new User();
                u.setUsername(txtUsername.getText().trim());
                u.setFullName(txtFullName.getText().trim());
                u.setEmail(txtEmail.getText().trim());
                u.setPhone(txtPhone.getText().trim());
                u.setRole((Role) cmbRole.getSelectedItem());
                u.setActive(true);
                return userDAO.createUser(u, new String(txtPassword.getPassword()));
            }
            @Override protected void done() {
                btnNext.setEnabled(true);
                btnNext.setText("Create Account");
                try {
                    int newId = get();
                    if (newId > 0) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                                "<html><b>Account created successfully!</b><br><br>" +
                                "Your username: <b>" + txtUsername.getText().trim() + "</b><br>" +
                                "You can now sign in. Contact your Admin to link your profile.</html>",
                                "Account Created", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        UIComponents.showError(RegisterFrame.this,
                                "Could not create account. The username or email may already be in use.");
                    }
                } catch (Exception ex) {
                    UIComponents.showError(RegisterFrame.this,
                            "Registration failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
