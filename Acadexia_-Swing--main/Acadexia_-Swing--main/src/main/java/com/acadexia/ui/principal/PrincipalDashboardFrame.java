package com.acadexia.ui.principal;

import com.acadexia.dao.*;
import com.acadexia.model.*;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.common.MainFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Principal dashboard — extends MainFrame.
 *
 * Sidebar navigation:
 *   Overview · Duty Leave (Tier-3) · Departments · Analytics · Grievances
 *
 * All DAO calls preserved exactly from the original.
 */
public class PrincipalDashboardFrame extends MainFrame {

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final DutyLeaveDAO   dutyLeaveDAO   = new DutyLeaveDAO();
    private final DepartmentDAO  departmentDAO  = new DepartmentDAO();
    private final FacultyDAO     facultyDAO     = new FacultyDAO();
    private final StudentDAO     studentDAO     = new StudentDAO();
    private final GrievanceDAO   grievanceDAO   = new GrievanceDAO();

    // ── table models ──────────────────────────────────────────────────────────
    private DefaultTableModel leaveModel;
    private DefaultTableModel deptModel;
    private DefaultTableModel analyticsModel;
    private DefaultTableModel grievModel;

    // ── live data for processing ──────────────────────────────────────────────
    private List<DutyLeave>  pendingLeaves;
    private List<Grievance>  allGrievances;

    // ── constructor ───────────────────────────────────────────────────────────
    public PrincipalDashboardFrame() {
        super("Acadexia – Principal's Office", 1260, 780);

        addNavItem("🏠", "Overview",        "overview",   buildOverviewPanel());
        addNavItem("📋", "Duty Leaves",     "dutyleave",  buildDutyLeavePanel());
        addNavItem("🏛️", "Departments",     "departments",buildDepartmentsPanel());
        addNavItem("📊", "Analytics",       "analytics",  buildAnalyticsPanel());
        addNavItem("⚖️", "Grievances",      "grievances", buildGrievancesPanel());

        showDefaultPanel();
        SwingUtilities.invokeLater(this::loadAllData);
    }

    // =========================================================================
    // OVERVIEW
    // =========================================================================
    private JPanel buildOverviewPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 24));

        User user = SessionManager.getInstance().getCurrentUser();
        String principalName = user != null ? user.getFullName() : "Principal";

        // Greeting
        JPanel greeting = new JPanel(new BorderLayout(0, 4));
        greeting.setOpaque(false);

        JLabel lblHi = new JLabel("Welcome, " + principalName.split(" ")[0] + " 👋");
        lblHi.setFont(UITheme.FONT_TITLE);
        lblHi.setForeground(UITheme.TEXT);

        JLabel lblRole = new JLabel("Office of the Principal  ·  Executive Academic Governance");
        lblRole.setFont(UITheme.FONT_REGULAR);
        lblRole.setForeground(UITheme.TEXT_MUTED);

        greeting.add(lblHi,   BorderLayout.NORTH);
        greeting.add(lblRole, BorderLayout.SOUTH);

        // KPI stat cards — load counts live
        List<Student>    students    = studentDAO.getAllStudents();
        List<Faculty>    facultyList = facultyDAO.getAllFaculty();
        List<Department> depts       = departmentDAO.getAllDepartments();
        List<DutyLeave>  pending     = dutyLeaveDAO.getPendingLeavesForPrincipal();

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(UIComponents.createStatCard("Total Students",    String.valueOf(students.size()),    "Enrolled students",        UITheme.PRIMARY));
        cards.add(UIComponents.createStatCard("Faculty",           String.valueOf(facultyList.size()), "Teaching staff",           UITheme.INFO));
        cards.add(UIComponents.createStatCard("Departments",       String.valueOf(depts.size()),       "Academic departments",     UITheme.SUCCESS));
        cards.add(UIComponents.createStatCard("Pending Sanctions", String.valueOf(pending.size()),     "Duty leaves awaiting you", pending.isEmpty() ? UITheme.SUCCESS : UITheme.DANGER));

        // Summary card
        JPanel summaryCard = UIComponents.createCardPanel();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));

        JLabel lblTitle = UIComponents.createSectionLabel("Executive Dashboard");
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html>" +
                "As Principal you have supreme authority over:<br>" +
                "• <b>Tier-3 Duty Leave Sanctions</b> — final approval after CFA and HOD review<br>" +
                "• <b>HOD Appointments</b> — assign or change Heads of Department<br>" +
                "• <b>Analytics</b> — institution-wide attendance and GPA monitoring<br>" +
                "• <b>Grievance Redressal</b> — resolve student complaints and disputes" +
                "</html>");
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        summaryCard.add(lblTitle);
        summaryCard.add(Box.createVerticalStrut(10));
        summaryCard.add(lblDesc);
        summaryCard.add(Box.createVerticalStrut(20));

        // Quick actions row
        JPanel qaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qaRow.setOpaque(false);
        qaRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnLeave   = UIComponents.createPrimaryButton("📋  Review Duty Leaves");
        JButton btnDepts   = UIComponents.createSecondaryButton("🏛️  Departments & HOD");
        JButton btnAnalyt  = UIComponents.createSecondaryButton("📊  Analytics");
        JButton btnGriev   = UIComponents.createSecondaryButton("⚖️  Grievances");

        btnLeave .addActionListener(e -> showPanel("dutyleave"));
        btnDepts .addActionListener(e -> showPanel("departments"));
        btnAnalyt.addActionListener(e -> showPanel("analytics"));
        btnGriev .addActionListener(e -> showPanel("grievances"));

        qaRow.add(btnLeave);
        qaRow.add(btnDepts);
        qaRow.add(btnAnalyt);
        qaRow.add(btnGriev);
        summaryCard.add(qaRow);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(cards);
        body.add(Box.createVerticalStrut(24));
        body.add(summaryCard);

        page.add(greeting, BorderLayout.NORTH);
        page.add(body,     BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // DUTY LEAVE — TIER-3 SANCTIONS
    // =========================================================================
    private JPanel buildDutyLeavePanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Duty Leave — Tier-3 Sanctions",
                "Applications endorsed by HOD awaiting your final approval or rejection."),
                BorderLayout.NORTH);

        JButton btnApprove = UIComponents.createSuccessButton("✅  Final Sanction — Approve");
        JButton btnReject  = UIComponents.createDangerButton("❌  Deny / Reject");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");

        btnApprove.addActionListener(e -> processDutyLeave(true));
        btnReject .addActionListener(e -> processDutyLeave(false));
        btnRefresh.addActionListener(e -> loadDutyLeaves());

        String[] cols = {
            "App ID", "Student", "Reg No", "Class", "Department",
            "Reason", "Days", "Dates", "Proof", "CFA Remarks", "HOD Remarks"
        };
        leaveModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(leaveModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(50);
        table.getColumnModel().getColumn(7).setPreferredWidth(170);
        table.getColumnModel().getColumn(8).setPreferredWidth(110);
        table.getColumnModel().getColumn(9).setPreferredWidth(130);
        table.getColumnModel().getColumn(10).setPreferredWidth(130);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnApprove, btnReject, btnRefresh), BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadDutyLeaves() {
        if (leaveModel == null) return;
        leaveModel.setRowCount(0);
        pendingLeaves = dutyLeaveDAO.getPendingLeavesForPrincipal();
        for (DutyLeave dl : pendingLeaves) {
            leaveModel.addRow(new Object[]{
                dl.getId(),
                dl.getStudentName(),
                dl.getRegisterNumber(),
                dl.getClassName(),
                dl.getDepartmentName(),
                dl.getReason(),
                dl.getTotalDays(),
                dl.getStartDate() + " → " + dl.getEndDate(),
                dl.getProofDetails(),
                dl.getAdvisorRemarks()  != null ? dl.getAdvisorRemarks()  : "—",
                dl.getHodRemarks()      != null ? dl.getHodRemarks()      : "—"
            });
        }
    }

    private void processDutyLeave(boolean approve) {
        if (pendingLeaves == null || pendingLeaves.isEmpty()) {
            UIComponents.showError(this, "No pending duty leaves to process.");
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                "Enter Application ID to " + (approve ? "APPROVE" : "REJECT") + ":",
                approve ? "Approve Duty Leave" : "Reject Duty Leave",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int appId = Integer.parseInt(input.trim());
            DutyLeave target = pendingLeaves.stream()
                    .filter(l -> l.getId() == appId).findFirst().orElse(null);
            if (target == null) {
                UIComponents.showError(this, "Application ID not found in pending list.");
                return;
            }
            String remarks = JOptionPane.showInputDialog(this,
                    "Enter Principal Remarks:",
                    approve ? "Duty Leave Approved by Principal" : "Rejected by Principal");
            if (remarks == null) remarks = approve ? "Approved" : "Rejected";

            String principalUser = SessionManager.getInstance().getCurrentUser().getUsername();
            boolean ok = dutyLeaveDAO.updatePrincipalDecision(appId, approve, remarks, principalUser);
            if (ok) {
                loadDutyLeaves();
                UIComponents.showSuccessToast(this,
                        "Application #" + appId + (approve ? " approved." : " rejected."));
            }
        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid Application ID.");
        }
    }

    // =========================================================================
    // DEPARTMENTS & HOD GOVERNANCE
    // =========================================================================
    private JPanel buildDepartmentsPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Departments & HOD Governance",
                "View departments and appoint or change Heads of Department."),
                BorderLayout.NORTH);

        JButton btnAssignHod = UIComponents.createPrimaryButton("👔  Appoint / Change HOD");
        JButton btnRefresh   = UIComponents.createSecondaryButton("↻  Refresh");

        btnAssignHod.addActionListener(e -> showAssignHodDialog());
        btnRefresh  .addActionListener(e -> loadDepartments());

        String[] cols = {"Dept ID", "Code", "Department Name", "Current HOD"};
        deptModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(deptModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnAssignHod, btnRefresh), BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadDepartments() {
        if (deptModel == null) return;
        deptModel.setRowCount(0);
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            deptModel.addRow(new Object[]{
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getHodName() != null ? d.getHodName() : "⚠  Unassigned"
            });
        }
    }

    private void showAssignHodDialog() {
        List<Department> depts      = departmentDAO.getAllDepartments();
        List<Faculty>    facList    = facultyDAO.getAllFaculty();

        if (depts.isEmpty() || facList.isEmpty()) {
            UIComponents.showError(this, "No departments or faculty available.");
            return;
        }

        JDialog dlg = new JDialog(this, "Appoint Head of Department", true);
        dlg.setSize(460, 260);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.SURFACE);
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JComboBox<Department> cmbDept = UIComponents.createComboBox(depts.toArray(new Department[0]));
        JComboBox<Faculty>    cmbFac  = UIComponents.createComboBox(facList.toArray(new Faculty[0]));

        addFRow(form, "Department",          cmbDept);
        form.add(Box.createVerticalStrut(14));
        addFRow(form, "Appoint as HOD",      cmbFac);

        JPanel footer = buildDlgFooter(dlg, () -> {
            Department selDept = (Department) cmbDept.getSelectedItem();
            Faculty    selFac  = (Faculty)    cmbFac.getSelectedItem();
            if (selDept == null || selFac == null) return;
            departmentDAO.updateHod(selDept.getId(), selFac.getId());
            loadDepartments();
            dlg.dispose();
            UIComponents.showSuccessToast(this,
                    selFac.getFullName() + " appointed as HOD of " + selDept.getName());
        });

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // ACADEMIC ANALYTICS
    // =========================================================================
    private JPanel buildAnalyticsPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Academic Analytics",
                "College-wide student performance, attendance, and GPA overview."),
                BorderLayout.NORTH);

        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnRefresh.addActionListener(e -> loadAnalytics());

        // Summary stat row above the table
        List<Student> allStudents = studentDAO.getAllStudents();
        long shortage  = allStudents.stream().filter(s -> s.getAttendancePercentage() < 75.0).count();
        double avgCgpa = allStudents.stream()
                .mapToDouble(Student::getCurrentCgpa).average().orElse(0.0);
        long above8    = allStudents.stream().filter(s -> s.getCurrentCgpa() >= 8.0).count();

        JPanel summaryCards = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryCards.setOpaque(false);
        summaryCards.add(UIComponents.createStatCard("Total Students",     String.valueOf(allStudents.size()), "All departments",         UITheme.PRIMARY));
        summaryCards.add(UIComponents.createStatCard("Attendance Shortage",String.valueOf(shortage),           "Below 75% threshold",     shortage > 0 ? UITheme.DANGER : UITheme.SUCCESS));
        summaryCards.add(UIComponents.createStatCard("Average CGPA",       String.format("%.2f", avgCgpa),     above8 + " students ≥ 8.0",UITheme.INFO));

        String[] cols = {
            "Reg No", "Roll", "Name", "Class", "Department",
            "Semester", "Attendance %", "SGPA", "CGPA"
        };
        analyticsModel = new DefaultTableModel(cols, 0);
        JTable table   = UIComponents.createStyledTable(analyticsModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(summaryCards, BorderLayout.NORTH);

        JPanel tableWrapper = new JPanel(new BorderLayout(0, 8));
        tableWrapper.setOpaque(false);
        tableWrapper.add(UIComponents.createToolbar(btnRefresh), BorderLayout.NORTH);
        tableWrapper.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        center.add(tableWrapper, BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadAnalytics() {
        if (analyticsModel == null) return;
        analyticsModel.setRowCount(0);
        List<Student> students = studentDAO.getAllStudents();
        for (Student s : students) {
            analyticsModel.addRow(new Object[]{
                s.getRegisterNumber(),
                s.getRollNumber(),
                s.getFullName(),
                s.getClassName(),
                s.getDepartmentName(),
                s.getSemester(),
                s.getAttendancePercentage() + "%",
                s.getCurrentSgpa(),
                s.getCurrentCgpa()
            });
        }
    }

    // =========================================================================
    // GRIEVANCE REDRESSAL
    // =========================================================================
    private JPanel buildGrievancesPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Grievance Redressal",
                "Review and adjudicate student grievances at the institutional level."),
                BorderLayout.NORTH);

        JButton btnResolve = UIComponents.createPrimaryButton("⚖️  Adjudicate Selected");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");

        btnResolve.addActionListener(e -> showResolveGrievanceDialog());
        btnRefresh.addActionListener(e -> loadGrievances());

        String[] cols = {"ID", "Student", "Reg No", "Category", "Title", "Status", "Resolution", "Resolved By"};
        grievModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(grievModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnResolve, btnRefresh), BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadGrievances() {
        if (grievModel == null) return;
        grievModel.setRowCount(0);
        allGrievances = grievanceDAO.getAllGrievances();
        for (Grievance g : allGrievances) {
            grievModel.addRow(new Object[]{
                g.getId(),
                g.getStudentName(),
                g.getRegisterNumber(),
                g.getCategory().getLabel(),
                g.getTitle(),
                g.getStatus().getLabel(),
                g.getResolution()         != null ? g.getResolution()         : "Pending",
                g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "—"
            });
        }
    }

    private void showResolveGrievanceDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Grievance ID to adjudicate:", "Adjudicate Grievance", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int gid = Integer.parseInt(input.trim());

            JDialog dlg = new JDialog(this, "Adjudicate Grievance #" + gid, true);
            dlg.setSize(460, 300);
            dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(UITheme.BG);
            dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(UITheme.SURFACE);
            form.setBorder(new EmptyBorder(24, 28, 16, 28));

            JComboBox<Grievance.Status> cmbStatus = UIComponents.createComboBox(
                    new Grievance.Status[]{
                        Grievance.Status.RESOLVED,
                        Grievance.Status.UNDER_REVIEW,
                        Grievance.Status.DISMISSED
                    });

            JTextArea txtRes = new JTextArea(4, 30);
            txtRes.setFont(UITheme.FONT_REGULAR);
            txtRes.setLineWrap(true);
            txtRes.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UITheme.BORDER, 1),
                    new EmptyBorder(8, 10, 8, 10)));

            addFRow(form, "New Status", cmbStatus);
            form.add(Box.createVerticalStrut(12));
            JLabel lblRes = UIComponents.createFormLabel("Resolution Notes");
            lblRes.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(lblRes);
            form.add(Box.createVerticalStrut(4));
            JScrollPane resSp = new JScrollPane(txtRes);
            resSp.setAlignmentX(Component.LEFT_ALIGNMENT);
            resSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            form.add(resSp);

            JPanel footer = buildDlgFooter(dlg, () -> {
                User u = SessionManager.getInstance().getCurrentUser();
                grievanceDAO.updateResolution(
                        gid,
                        (Grievance.Status) cmbStatus.getSelectedItem(),
                        txtRes.getText().trim(),
                        u.getId(),
                        u.getUsername());
                loadGrievances();
                dlg.dispose();
                UIComponents.showSuccessToast(this, "Grievance #" + gid + " status updated.");
            });

            dlg.add(form,   BorderLayout.CENTER);
            dlg.add(footer, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid Grievance ID.");
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void addFRow(JPanel panel, String labelText, JComponent field) {
        JLabel lbl = UIComponents.createFormLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
    }

    private JPanel buildDlgFooter(JDialog dlg, Runnable onSubmit) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        footer.setBackground(UITheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnCancel = UIComponents.createSecondaryButton("Cancel");
        JButton btnSubmit = UIComponents.createPrimaryButton("Confirm");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSubmit.addActionListener(e -> onSubmit.run());
        footer.add(btnCancel);
        footer.add(btnSubmit);
        return footer;
    }

    private void loadAllData() {
        loadDutyLeaves();
        loadDepartments();
        loadAnalytics();
        loadGrievances();
    }
}
