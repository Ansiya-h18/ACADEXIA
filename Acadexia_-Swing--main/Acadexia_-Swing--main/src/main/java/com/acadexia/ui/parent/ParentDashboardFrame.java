package com.acadexia.ui.parent;

import com.acadexia.dao.*;
import com.acadexia.model.*;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Date;
import java.util.List;

/**
 * Modern Acadexia Parent Portal.
 * Features an executive university-management aesthetic strictly matching the Landing Page dark theme:
 * - Deep navy (#0B0F1A to #0F172A) canvas and elevated card panels (#161D31)
 * - Indigo accents (#6366F1), emerald successes, and warning badges
 * - Strict read-only access to linked child's results, attendance, assignments,
 *   daily classwork, faculty feedback, exam schedules, and institutional notifications.
 */
public class ParentDashboardFrame extends JFrame {

    // --- Dark Theme Color Palette matching LandingFrame ---
    private static final Color BG_DARK_TOP        = new Color(11, 15, 26);    // #0B0F1A Midnight Navy
    private static final Color BG_DARK_BOTTOM     = new Color(15, 23, 42);    // #0F172A Slate Navy
    private static final Color CARD_BG            = new Color(22, 29, 49);    // #161D31 Elevated Card
    private static final Color CARD_BG_ALT        = new Color(28, 36, 60);    // Alternate row
    private static final Color CARD_BORDER        = new Color(42, 54, 86);    // #2A3656
    private static final Color INDIGO_PRIMARY     = new Color(99, 102, 241);  // #6366F1
    private static final Color INDIGO_HOVER       = new Color(79, 70, 229);   // #4F46E5
    private static final Color PURPLE_ACCENT      = new Color(139, 92, 246);  // #8B5CF6
    private static final Color TEXT_HEAD          = new Color(248, 250, 252); // #F8FAFC
    private static final Color TEXT_BODY          = new Color(226, 232, 240); // #E2E8F0
    private static final Color TEXT_MUTED         = new Color(148, 163, 184); // #94A3B8
    private static final Color TEXT_DIM           = new Color(100, 116, 139); // #64748B
    private static final Color COLOR_STUDENT      = new Color(16, 185, 129);  // #10B981 Emerald
    private static final Color COLOR_WARN         = new Color(245, 158, 11);  // #F59E0B Amber
    private static final Color COLOR_DANGER       = new Color(239, 68, 68);   // #EF4444 Rose/Red
    private static final Color COLOR_INFO         = new Color(14, 165, 233);  // #0EA5E9 Sky Blue

    // DAOs
    private final ParentDAO parentDAO = new ParentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final MarkDAO markDAO = new MarkDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final DailyClassworkDAO classworkDAO = new DailyClassworkDAO();
    private final FacultyRemarkDAO remarkDAO = new FacultyRemarkDAO();
    private final ExamScheduleDAO examDAO = new ExamScheduleDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Active Data
    private Parent currentParent;
    private Student linkedChild;

    // UI Table Models
    private DefaultTableModel marksTableModel;
    private DefaultTableModel attSummaryTableModel;
    private DefaultTableModel attHistoryTableModel;
    private DefaultTableModel assignmentTableModel;
    private DefaultTableModel classworkTableModel;
    private DefaultTableModel feedbackTableModel;
    private DefaultTableModel examTableModel;
    private DefaultTableModel notifTableModel;

    // Filter controls & summary labels
    private JComboBox<String> cmbSemesterFilter;
    private JLabel lblMarksSummary;
    private JComboBox<String> cmbAssignmentStatusFilter;
    private List<Assignment> cachedAssignments;
    private List<Notification> cachedNotifications;
    private List<DailyClasswork> cachedClasswork;

    public ParentDashboardFrame() {
        this.currentParent = SessionManager.getInstance().getCurrentParent();
        this.linkedChild = SessionManager.getInstance().getLinkedChild();

        // Fallback safety if accessed without pre-cached session
        if (this.currentParent == null && SessionManager.getInstance().getCurrentUser() != null) {
            this.currentParent = parentDAO.getParentByUserId(SessionManager.getInstance().getCurrentUser().getId());
            if (this.currentParent != null && this.currentParent.getStudentId() > 0) {
                this.linkedChild = studentDAO.getById(this.currentParent.getStudentId());
                SessionManager.getInstance().setLinkedChild(this.linkedChild);
            }
        }

        setTitle("Acadexia — Parent & Guardian Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK_TOP);

        initUI();
        loadAllData();
    }

    private void initUI() {
        // Gradient root canvas
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK_TOP, 0, getHeight(), BG_DARK_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout());
        setContentPane(root);

        // Top Navigation Header
        root.add(createTopBar(), BorderLayout.NORTH);

        // Center Content Body
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 22, 16, 22));

        // Child Overview KPI Banner
        body.add(createChildOverviewBanner(), BorderLayout.NORTH);

        // Main Navigational Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(CARD_BG);
        tabs.setForeground(TEXT_BODY);

        tabs.addTab("📊 Results & Performance", createMarksTab());
        tabs.addTab("🕒 Attendance Tracker", createAttendanceTab());
        tabs.addTab("📁 Assignment Status", createAssignmentsTab());
        tabs.addTab("📖 Daily Classwork Status", createDailyClassworkTab());
        tabs.addTab("💬 Faculty Feedback", createFacultyFeedbackTab());
        tabs.addTab("📅 Examination Schedules", createExamSchedulesTab());
        tabs.addTab("🔔 Institutional Notifications", createNotificationsTab());
        tabs.addTab("👤 Child & Parent Profile", createProfileTab());

        body.add(tabs, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. TOP NAVIGATION BAR
    // =========================================================================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(15, 20, 35));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(12, 22, 12, 22)
        ));

        // Left Branding & Identity
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("ACADEXIA PARENT PORTAL");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(INDIGO_PRIMARY);

        String verText = (currentParent != null && currentParent.getVerificationStatus() == Parent.VerificationStatus.VERIFIED)
                ? "✓ Verified Guardian" : "Pending Verification";
        Color verBg = (currentParent != null && currentParent.getVerificationStatus() == Parent.VerificationStatus.VERIFIED)
                ? new Color(6, 78, 59) : new Color(120, 53, 15);
        Color verFg = (currentParent != null && currentParent.getVerificationStatus() == Parent.VerificationStatus.VERIFIED)
                ? COLOR_STUDENT : COLOR_WARN;

        JLabel badgeVerified = UIComponents.createBadge(verText, verBg, verFg);
        left.add(lblTitle);
        left.add(badgeVerified);

        if (linkedChild != null) {
            JLabel childPill = UIComponents.createBadge("Ward: " + linkedChild.getFullName() + " (" + linkedChild.getRegisterNumber() + ")",
                    new Color(49, 46, 129), TEXT_HEAD);
            left.add(childPill);
        }

        // Right User info & Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        String parentName = (currentParent != null) ? currentParent.getFullName() : "Parent";
        JLabel lblParent = new JLabel("Signed in as: " + parentName + " (" + (currentParent != null ? currentParent.getRelationship() : "Guardian") + ")");
        lblParent.setFont(UITheme.FONT_REGULAR);
        lblParent.setForeground(TEXT_MUTED);

        JButton btnLogout = UIComponents.createDangerButton("Log Out");
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().logout();
            new LandingFrame().setVisible(true);
            dispose();
        });

        right.add(lblParent);
        right.add(btnLogout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // 2. CHILD OVERVIEW KPI BANNER
    // =========================================================================
    private JPanel createChildOverviewBanner() {
        JPanel banner = new JPanel(new GridLayout(1, 4, 14, 14));
        banner.setOpaque(false);

        double attPct = (linkedChild != null) ? linkedChild.getAttendancePercentage() : 100.0;
        double sgpa = (linkedChild != null) ? linkedChild.getCurrentSgpa() : 0.0;
        double cgpa = (linkedChild != null) ? linkedChild.getCurrentCgpa() : 0.0;

        Color attColor = attPct >= 75.0 ? COLOR_STUDENT : COLOR_DANGER;
        String attSub = attPct >= 75.0 ? "Eligible for Examinations (≥75%)" : "⚠️ Shortage Alert (<75% min required)";

        banner.add(UIComponents.createStatCard("Child Attendance", String.format("%.1f%%", attPct), attSub, attColor));
        banner.add(UIComponents.createStatCard("Semester SGPA", String.format("%.2f", sgpa), "Current Semester Grade Point", INDIGO_PRIMARY));
        banner.add(UIComponents.createStatCard("Cumulative CGPA", String.format("%.2f", cgpa), "All-Semester Standing", PURPLE_ACCENT));

        String classAdvisor = (currentParent != null && currentParent.getStudentAdvisorName() != null)
                ? currentParent.getStudentAdvisorName() : "Faculty Advisor";
        String classDept = (linkedChild != null) ? linkedChild.getClassName() + " (" + linkedChild.getDepartmentName() + ")" : "Class";
        banner.add(UIComponents.createStatCard("Class & Advisor", classDept, "CFA: " + classAdvisor, COLOR_WARN));

        return banner;
    }

    // =========================================================================
    // 3. TAB: RESULTS & PERFORMANCE
    // =========================================================================
    private JPanel createMarksTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Filter Bar
        JPanel toolBar = new JPanel(new BorderLayout(10, 0));
        toolBar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblSem = new JLabel("Academic Semester:");
        lblSem.setFont(UITheme.FONT_BOLD);
        lblSem.setForeground(TEXT_BODY);

        String[] sems = {
                "Semester 1", "Semester 2", "Semester 3", "Semester 4",
                "Semester 5", "Semester 6", "Semester 7", "Semester 8",
                "All Semesters"
        };
        cmbSemesterFilter = new JComboBox<>(sems);
        cmbSemesterFilter.setFont(UITheme.FONT_REGULAR);
        cmbSemesterFilter.setSelectedIndex(4); // Default to Semester 5
        cmbSemesterFilter.addActionListener(e -> loadMarksData());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Marksheet");
        btnRefresh.addActionListener(e -> loadMarksData());

        left.add(lblSem);
        left.add(cmbSemesterFilter);
        left.add(btnRefresh);

        lblMarksSummary = new JLabel("Loading marks summary...");
        lblMarksSummary.setFont(UITheme.FONT_BOLD);
        lblMarksSummary.setForeground(COLOR_INFO);

        toolBar.add(left, BorderLayout.WEST);
        toolBar.add(lblMarksSummary, BorderLayout.EAST);

        // Results Table
        String[] cols = {"Semester", "Course Code", "Course Name", "Evaluation Type", "Marks Scored", "Max Marks", "Percentage", "Letter Grade", "Grade Point"};
        marksTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; } // Strict Read-Only
        };
        JTable table = createDarkTable(marksTableModel);

        p.add(toolBar, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadMarksData() {
        marksTableModel.setRowCount(0);
        if (linkedChild == null) return;

        int selectedIdx = cmbSemesterFilter.getSelectedIndex();
        List<Mark> list;
        if (selectedIdx >= 0 && selectedIdx <= 7) {
            list = markDAO.getMarksForStudentAndSemester(linkedChild.getId(), selectedIdx + 1);
        } else {
            list = markDAO.getMarksForStudent(linkedChild.getId());
        }

        double totalMarksObtained = 0;
        double totalMaxMarks = 0;

        for (Mark m : list) {
            marksTableModel.addRow(new Object[]{
                    "Sem " + m.getSemester(),
                    m.getSubjectCode(),
                    m.getSubjectName(),
                    m.getExamType().getLabel(),
                    m.getMarksObtained(),
                    m.getMaxMarks(),
                    String.format("%.1f%%", m.getPercentage()),
                    m.getGrade(),
                    m.getGradePoint()
            });
            totalMarksObtained += m.getMarksObtained();
            totalMaxMarks += m.getMaxMarks();
        }

        String semText = (selectedIdx >= 0 && selectedIdx <= 7) ? "Semester " + (selectedIdx + 1) : "All Semesters";
        double avgPct = totalMaxMarks > 0 ? (totalMarksObtained / totalMaxMarks) * 100.0 : 0.0;
        lblMarksSummary.setText(String.format("%s | Records: %d | Total: %.1f / %.1f (%.1f%%)",
                semText, list.size(), totalMarksObtained, totalMaxMarks, avgPct));
    }

    // =========================================================================
    // 4. TAB: ATTENDANCE TRACKER
    // =========================================================================
    private JPanel createAttendanceTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane attTabs = new JTabbedPane();
        attTabs.setFont(UITheme.FONT_REGULAR);

        // Subtab A: Subject-wise Summary
        JPanel pSummary = new JPanel(new BorderLayout(10, 10));
        pSummary.setOpaque(false);
        pSummary.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] colsSummary = {"Subject Code", "Subject Name", "Total Hours Conducted", "Hours Present", "Duty Leave Credit", "Hours Absent", "Attendance %", "Regulatory Status"};
        attSummaryTableModel = new DefaultTableModel(colsSummary, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblSummary = createDarkTable(attSummaryTableModel);

        JPanel summaryHeader = new JPanel(new BorderLayout());
        summaryHeader.setOpaque(false);
        JLabel lblAttNotice = new JLabel("<html><b>Subject-Wise Attendance Distribution:</b> Minimum 75% attendance is mandatory in each subject to sit for University Examinations.</html>");
        lblAttNotice.setFont(UITheme.FONT_REGULAR);
        lblAttNotice.setForeground(TEXT_BODY);

        JButton btnRefAtt = UIComponents.createSecondaryButton("🔄 Refresh Attendance");
        btnRefAtt.addActionListener(e -> loadAttendanceData());

        summaryHeader.add(lblAttNotice, BorderLayout.WEST);
        summaryHeader.add(btnRefAtt, BorderLayout.EAST);

        pSummary.add(summaryHeader, BorderLayout.NORTH);
        pSummary.add(new JScrollPane(tblSummary), BorderLayout.CENTER);

        // Subtab B: Chronological Daily Attendance Log
        JPanel pHistory = new JPanel(new BorderLayout(10, 10));
        pHistory.setOpaque(false);
        pHistory.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] colsHistory = {"Date", "Period / Hour", "Subject Code", "Subject Name", "Status", "Marked By Faculty", "Remarks"};
        attHistoryTableModel = new DefaultTableModel(colsHistory, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblHistory = createDarkTable(attHistoryTableModel);

        JLabel lblHistDesc = new JLabel("Live Date-Wise Clocked Attendance Log for Ward:");
        lblHistDesc.setFont(UITheme.FONT_BOLD);
        lblHistDesc.setForeground(TEXT_HEAD);

        pHistory.add(lblHistDesc, BorderLayout.NORTH);
        pHistory.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

        attTabs.addTab("📊 Subject-Wise Percentage", pSummary);
        attTabs.addTab("📅 Chronological Daily Attendance Log", pHistory);

        p.add(attTabs, BorderLayout.CENTER);
        return p;
    }

    private void loadAttendanceData() {
        attSummaryTableModel.setRowCount(0);
        attHistoryTableModel.setRowCount(0);
        if (linkedChild == null) return;

        // 1. Subject-wise summary
        List<AttendanceDAO.SubjectAttendanceSummary> sumList = attendanceDAO.getStudentSubjectWiseAttendance(linkedChild.getId());
        for (AttendanceDAO.SubjectAttendanceSummary s : sumList) {
            String status = s.percentage() >= 75.0 ? "Satisfactory" : "⚠️ SHORTAGE (<75%)";
            attSummaryTableModel.addRow(new Object[]{
                    s.subjectCode(), s.subjectName(), s.totalHours(), s.presentHours(),
                    s.dutyLeaveHours(), s.absentHours(), s.percentage() + "%", status
            });
        }

        // 2. Chronological History Log
        List<AttendanceDAO.StudentAttendanceRecord> histList = attendanceDAO.getStudentAttendanceHistory(linkedChild.getId());
        for (AttendanceDAO.StudentAttendanceRecord r : histList) {
            attHistoryTableModel.addRow(new Object[]{
                    r.date(),
                    "Hour " + r.hour(),
                    r.subjectCode(),
                    r.subjectName(),
                    r.status().name(),
                    r.facultyName(),
                    r.remarks() != null ? r.remarks() : "-"
            });
        }
    }

    // =========================================================================
    // 5. TAB: ASSIGNMENT STATUS
    // =========================================================================
    private JPanel createAssignmentsTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(TEXT_BODY);

        cmbAssignmentStatusFilter = new JComboBox<>(new String[]{"ALL", "SUBMITTED", "PENDING"});
        cmbAssignmentStatusFilter.addActionListener(e -> filterAssignments());

        JButton btnViewDetails = UIComponents.createPrimaryButton("👁️ View Assignment Details & Evaluator Feedback");
        btnViewDetails.addActionListener(e -> showAssignmentDetailModal());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadAssignmentsData());

        tool.add(lblFilter);
        tool.add(cmbAssignmentStatusFilter);
        tool.add(btnViewDetails);
        tool.add(btnRefresh);

        String[] cols = {"Assignment ID", "Course", "Title", "Faculty Instructor", "Due Date", "Max Marks", "Child Status", "Marks Awarded", "Submission Date"};
        assignmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createDarkTable(assignmentTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAssignmentsData() {
        if (linkedChild == null) return;
        cachedAssignments = assignmentDAO.getAssignmentsForClass(linkedChild.getClassId(), linkedChild.getId());
        filterAssignments();
    }

    private void filterAssignments() {
        assignmentTableModel.setRowCount(0);
        if (cachedAssignments == null) return;

        String filter = (String) cmbAssignmentStatusFilter.getSelectedItem();
        for (Assignment a : cachedAssignments) {
            boolean isSubmitted = a.isSubmittedByCurrentStudent();
            if ("SUBMITTED".equals(filter) && !isSubmitted) continue;
            if ("PENDING".equals(filter) && isSubmitted) continue;

            String statusDisplay = isSubmitted ? "✓ SUBMITTED" : "⏳ PENDING";
            String marksDisplay = (a.getMarksAwarded() != null) ? String.format("%.1f / %.1f", a.getMarksAwarded(), a.getMaxMarks()) : (isSubmitted ? "Pending Evaluation" : "Not Submitted");
            String subDateStr = (a.getSubmissionDate() != null) ? a.getSubmissionDate().toString() : "-";

            assignmentTableModel.addRow(new Object[]{
                    a.getId(),
                    a.getSubjectCode(),
                    a.getTitle(),
                    a.getFacultyName(),
                    a.getDueDate(),
                    a.getMaxMarks(),
                    statusDisplay,
                    marksDisplay,
                    subDateStr
            });
        }
    }

    private void showAssignmentDetailModal() {
        int row = assignmentTableModel.getRowCount() > 0 ? 0 : -1;
        if (row < 0 || cachedAssignments == null || cachedAssignments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assignment available to view.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Find selected or first
        Assignment a = cachedAssignments.get(0);

        JDialog dlg = new JDialog(this, "Assignment Brief & Evaluator Feedback — #" + a.getId(), true);
        dlg.setSize(620, 460);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new GridLayout(4, 2, 8, 8));
        header.setOpaque(false);
        header.add(new JLabel("Title:"));
        header.add(new JLabel(a.getTitle()));
        header.add(new JLabel("Course & Instructor:"));
        header.add(new JLabel(a.getSubjectCode() + " • " + a.getFacultyName()));
        header.add(new JLabel("Due Date:"));
        header.add(new JLabel(a.getDueDate().toString()));
        header.add(new JLabel("Submission Status:"));
        header.add(new JLabel(a.isSubmittedByCurrentStudent() ? "SUBMITTED" : "PENDING"));

        JTextArea txtDesc = new JTextArea(a.getDescription() != null ? a.getDescription() : "No extended description provided.");
        txtDesc.setEditable(false);
        txtDesc.setLineWrap(true);
        txtDesc.setBackground(new Color(15, 20, 35));
        txtDesc.setForeground(TEXT_BODY);

        JTextArea txtFeedback = new JTextArea(a.getFacultyFeedback() != null ? a.getFacultyFeedback() : "No written faculty feedback recorded yet.");
        txtFeedback.setEditable(false);
        txtFeedback.setLineWrap(true);
        txtFeedback.setBackground(new Color(15, 20, 35));
        txtFeedback.setForeground(COLOR_STUDENT);

        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        center.setOpaque(false);
        JPanel pDesc = new JPanel(new BorderLayout(4, 4));
        pDesc.setOpaque(false);
        pDesc.add(new JLabel("Assignment Brief:"), BorderLayout.NORTH);
        pDesc.add(new JScrollPane(txtDesc), BorderLayout.CENTER);

        JPanel pFb = new JPanel(new BorderLayout(4, 4));
        pFb.setOpaque(false);
        pFb.add(new JLabel("Faculty Evaluator Feedback:"), BorderLayout.NORTH);
        pFb.add(new JScrollPane(txtFeedback), BorderLayout.CENTER);

        center.add(pDesc);
        center.add(pFb);

        JButton btnClose = UIComponents.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        p.add(header, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btnClose, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // =========================================================================
    // 6. TAB: DAILY CLASSWORK STATUS
    // =========================================================================
    private JPanel createDailyClassworkTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JLabel lblDesc = new JLabel("📖 Real-Time Syllabus & Daily Classwork Log (Topics Taught & Homework Assigned):");
        lblDesc.setFont(UITheme.FONT_BOLD);
        lblDesc.setForeground(TEXT_HEAD);

        JButton btnViewTopic = UIComponents.createPrimaryButton("🔍 View Full Classwork Notes");
        btnViewTopic.addActionListener(e -> showClassworkDetailModal());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Classwork");
        btnRefresh.addActionListener(e -> loadDailyClassworkData());

        tool.add(lblDesc);
        tool.add(btnViewTopic);
        tool.add(btnRefresh);

        String[] cols = {"Date", "Period", "Course Code", "Course Title", "Faculty Instructor", "Topics Taught Today", "Homework / Assignments", "Status"};
        classworkTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createDarkTable(classworkTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadDailyClassworkData() {
        classworkTableModel.setRowCount(0);
        if (linkedChild == null) return;

        cachedClasswork = classworkDAO.getClassworkForClass(linkedChild.getClassId());
        for (DailyClasswork cw : cachedClasswork) {
            classworkTableModel.addRow(new Object[]{
                    cw.getDate(),
                    "Hour " + cw.getPeriodHour(),
                    cw.getSubjectCode(),
                    cw.getSubjectName(),
                    cw.getFacultyName(),
                    cw.getTopicCovered(),
                    cw.getHomeworkAssigned() != null ? cw.getHomeworkAssigned() : "None",
                    cw.getStatus().getLabel()
            });
        }
    }

    private void showClassworkDetailModal() {
        if (cachedClasswork == null || cachedClasswork.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classwork records available.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DailyClasswork cw = cachedClasswork.get(0);
        JDialog dlg = new JDialog(this, "Daily Classwork & Homework Log", true);
        dlg.setSize(580, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel head = new JPanel(new GridLayout(3, 2, 8, 8));
        head.setOpaque(false);
        head.add(new JLabel("Date & Period:"));
        head.add(new JLabel(cw.getDate() + " (Period " + cw.getPeriodHour() + ")"));
        head.add(new JLabel("Course:"));
        head.add(new JLabel(cw.getSubjectCode() + " - " + cw.getSubjectName()));
        head.add(new JLabel("Faculty Instructor:"));
        head.add(new JLabel(cw.getFacultyName()));

        JTextArea txtTopic = new JTextArea(cw.getTopicCovered());
        txtTopic.setEditable(false);
        txtTopic.setLineWrap(true);
        txtTopic.setBackground(new Color(15, 20, 35));
        txtTopic.setForeground(TEXT_BODY);

        JTextArea txtHw = new JTextArea(cw.getHomeworkAssigned() != null ? cw.getHomeworkAssigned() : "No homework assigned for this period.");
        txtHw.setEditable(false);
        txtHw.setLineWrap(true);
        txtHw.setBackground(new Color(15, 20, 35));
        txtHw.setForeground(COLOR_WARN);

        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        center.setOpaque(false);

        JPanel p1 = new JPanel(new BorderLayout(4, 4));
        p1.setOpaque(false);
        p1.add(new JLabel("Topic Covered:"), BorderLayout.NORTH);
        p1.add(new JScrollPane(txtTopic), BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(4, 4));
        p2.setOpaque(false);
        p2.add(new JLabel("Homework & Preparation Required:"), BorderLayout.NORTH);
        p2.add(new JScrollPane(txtHw), BorderLayout.CENTER);

        center.add(p1);
        center.add(p2);

        JButton btnClose = UIComponents.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        p.add(head, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btnClose, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // =========================================================================
    // 7. TAB: FACULTY FEEDBACK
    // =========================================================================
    private JPanel createFacultyFeedbackTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JLabel lblTitle = new JLabel("💬 Faculty Mentor Remarks, Behavioral Feedback & Recommendations for Ward:");
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(TEXT_HEAD);

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Feedback");
        btnRefresh.addActionListener(e -> loadFacultyFeedbackData());

        tool.add(lblTitle);
        tool.add(btnRefresh);

        String[] cols = {"Date Posted", "Faculty Instructor", "Designation", "Course", "Feedback Category", "Title / Note", "Faculty Remarks", "Action Recommended", "Rating"};
        feedbackTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createDarkTable(feedbackTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadFacultyFeedbackData() {
        feedbackTableModel.setRowCount(0);
        if (linkedChild == null) return;

        List<FacultyRemark> remarks = remarkDAO.getRemarksForStudent(linkedChild.getId());
        for (FacultyRemark r : remarks) {
            String courseText = r.getSubjectCode() != null ? r.getSubjectCode() : "General";
            String stars = r.getRating() != null ? (r.getRating() + " ★") : "-";

            feedbackTableModel.addRow(new Object[]{
                    r.getDatePosted(),
                    r.getFacultyName(),
                    r.getFacultyDesignation(),
                    courseText,
                    r.getCategory().getLabel(),
                    r.getTitle(),
                    r.getRemarks(),
                    r.getActionSuggested() != null ? r.getActionSuggested() : "None",
                    stars
            });
        }
    }

    // =========================================================================
    // 8. TAB: EXAMINATION SCHEDULES
    // =========================================================================
    private JPanel createExamSchedulesTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JLabel lblTitle = new JLabel("📅 Official Examination Schedule & Syllabus Portions for Ward's Batch:");
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(TEXT_HEAD);

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Schedules");
        btnRefresh.addActionListener(e -> loadExamSchedulesData());

        tool.add(lblTitle);
        tool.add(btnRefresh);

        String[] cols = {"Exam Name", "Course Code", "Course Title", "Exam Date", "Time Slot", "Hall / Venue", "Max Marks", "Portions / Syllabus Scope"};
        examTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createDarkTable(examTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadExamSchedulesData() {
        examTableModel.setRowCount(0);
        if (linkedChild == null) return;

        List<ExamSchedule> schedules = examDAO.getExamSchedulesForClass(linkedChild.getClassId());
        for (ExamSchedule es : schedules) {
            examTableModel.addRow(new Object[]{
                    es.getExamName(),
                    es.getSubjectCode(),
                    es.getSubjectName(),
                    es.getExamDate(),
                    es.getStartTime() + " - " + es.getEndTime(),
                    es.getRoomHall(),
                    es.getMaxMarks(),
                    es.getPortionsSyllabus() != null ? es.getPortionsSyllabus() : "Complete Syllabus"
            });
        }
    }

    // =========================================================================
    // 9. TAB: INSTITUTIONAL NOTIFICATIONS
    // =========================================================================
    private JPanel createNotificationsTab() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JLabel lblTitle = new JLabel("🔔 Institutional Circulars, Parent-Teacher Meeting Notices & Campus Alerts:");
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(TEXT_HEAD);

        JButton btnReadFull = UIComponents.createPrimaryButton("📖 Read Selected Notification");
        btnReadFull.addActionListener(e -> showNotificationDetailModal());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Notifications");
        btnRefresh.addActionListener(e -> loadNotificationsData());

        tool.add(lblTitle);
        tool.add(btnReadFull);
        tool.add(btnRefresh);

        String[] cols = {"Date", "Priority", "Category", "Circular Title", "Message Preview", "Issued By"};
        notifTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createDarkTable(notifTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadNotificationsData() {
        notifTableModel.setRowCount(0);
        Integer classId = (linkedChild != null) ? linkedChild.getClassId() : null;
        cachedNotifications = notificationDAO.getNotificationsForParent(classId);

        for (Notification n : cachedNotifications) {
            String preview = n.getMessage().length() > 80 ? (n.getMessage().substring(0, 80) + "...") : n.getMessage();
            notifTableModel.addRow(new Object[]{
                    n.getPublishDate(),
                    n.getPriority().getLabel(),
                    n.getCategory().getLabel(),
                    n.getTitle(),
                    preview,
                    n.getPostedByUserName() != null ? n.getPostedByUserName() : "Administration"
            });
        }
    }

    private void showNotificationDetailModal() {
        if (cachedNotifications == null || cachedNotifications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notifications available to view.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Notification n = cachedNotifications.get(0);

        JDialog dlg = new JDialog(this, "Institutional Announcement: " + n.getTitle(), true);
        dlg.setSize(600, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel(new GridLayout(3, 2, 8, 8));
        top.setOpaque(false);
        top.add(new JLabel("Publication Date:"));
        top.add(new JLabel(n.getPublishDate().toString()));
        top.add(new JLabel("Priority & Category:"));
        top.add(new JLabel(n.getPriority().getLabel() + " • " + n.getCategory().getLabel()));
        top.add(new JLabel("Issued By:"));
        top.add(new JLabel(n.getPostedByUserName() != null ? n.getPostedByUserName() : "Administration"));

        JTextArea txtMsg = new JTextArea(n.getMessage());
        txtMsg.setEditable(false);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMsg.setBackground(new Color(15, 20, 35));
        txtMsg.setForeground(TEXT_BODY);

        JButton btnClose = UIComponents.createPrimaryButton("Dismiss");
        btnClose.addActionListener(e -> dlg.dispose());

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(txtMsg), BorderLayout.CENTER);
        p.add(btnClose, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // =========================================================================
    // 10. TAB: CHILD & PARENT PROFILE
    // =========================================================================
    private JPanel createProfileTab() {
        JPanel p = new JPanel(new GridLayout(1, 2, 16, 16));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Left: Child Profile Card
        JPanel cardChild = UIComponents.createCardPanel();
        cardChild.setLayout(new BorderLayout(12, 12));
        cardChild.setBackground(CARD_BG);

        JLabel lblChildHeader = new JLabel("🎓 Linked Ward / Student Profile");
        lblChildHeader.setFont(UITheme.FONT_SECTION);
        lblChildHeader.setForeground(INDIGO_PRIMARY);
        cardChild.add(lblChildHeader, BorderLayout.NORTH);

        JPanel pChildFields = new JPanel(new GridLayout(8, 2, 10, 10));
        pChildFields.setOpaque(false);

        String sName = (linkedChild != null) ? linkedChild.getFullName() : "-";
        String sReg = (linkedChild != null) ? linkedChild.getRegisterNumber() : "-";
        String sRoll = (linkedChild != null) ? linkedChild.getRollNumber() : "-";
        String sClass = (linkedChild != null) ? linkedChild.getClassName() : "-";
        String sDept = (linkedChild != null) ? linkedChild.getDepartmentName() : "-";
        String sSem = (linkedChild != null) ? ("Semester " + linkedChild.getSemester()) : "-";
        String sYear = (linkedChild != null) ? String.valueOf(linkedChild.getAdmissionYear()) : "-";
        String sAdvisor = (currentParent != null && currentParent.getStudentAdvisorName() != null)
                ? currentParent.getStudentAdvisorName() : "Faculty Advisor";

        addField(pChildFields, "Full Student Name:", sName);
        addField(pChildFields, "University Register No:", sReg);
        addField(pChildFields, "Roll Number:", sRoll);
        addField(pChildFields, "Academic Class / Batch:", sClass);
        addField(pChildFields, "Department / School:", sDept);
        addField(pChildFields, "Current Semester:", sSem);
        addField(pChildFields, "Admission Year:", sYear);
        addField(pChildFields, "Class Faculty Advisor (CFA):", sAdvisor);

        cardChild.add(pChildFields, BorderLayout.CENTER);

        // Right: Parent Account & Verification Card
        JPanel cardParent = UIComponents.createCardPanel();
        cardParent.setLayout(new BorderLayout(12, 12));
        cardParent.setBackground(CARD_BG);

        JLabel lblParentHeader = new JLabel("🛡️ Parent Account & Verification Record");
        lblParentHeader.setFont(UITheme.FONT_SECTION);
        lblParentHeader.setForeground(COLOR_STUDENT);
        cardParent.add(lblParentHeader, BorderLayout.NORTH);

        JPanel pParentFields = new JPanel(new GridLayout(8, 2, 10, 10));
        pParentFields.setOpaque(false);

        String pName = (currentParent != null) ? currentParent.getFullName() : "-";
        String pUser = (currentParent != null) ? currentParent.getUsername() : "-";
        String pRel = (currentParent != null) ? currentParent.getRelationship() : "-";
        String pEmail = (currentParent != null && currentParent.getEmail() != null) ? currentParent.getEmail() : "-";
        String pPhone = (currentParent != null && currentParent.getPhone() != null) ? currentParent.getPhone() : "-";
        String pOcc = (currentParent != null && currentParent.getOccupation() != null) ? currentParent.getOccupation() : "Professional";
        String pStatus = (currentParent != null) ? currentParent.getVerificationStatus().getLabel() : "Pending";
        String pNotes = (currentParent != null && currentParent.getVerificationNotes() != null) ? currentParent.getVerificationNotes() : "Verified by Institution Admin";

        addField(pParentFields, "Guardian Name:", pName);
        addField(pParentFields, "Portal Username:", pUser);
        addField(pParentFields, "Relationship to Student:", pRel);
        addField(pParentFields, "Contact Email:", pEmail);
        addField(pParentFields, "Primary Phone:", pPhone);
        addField(pParentFields, "Occupation / Employer:", pOcc);
        addField(pParentFields, "Verification Status:", pStatus);
        addField(pParentFields, "Admin Verification Notes:", pNotes);

        cardParent.add(pParentFields, BorderLayout.CENTER);

        p.add(cardChild);
        p.add(cardParent);
        return p;
    }

    private void addField(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_REGULAR);
        val.setForeground(TEXT_BODY);

        panel.add(lbl);
        panel.add(val);
    }

    // =========================================================================
    // HELPER: DATA LOADER & TABLE FACTORY
    // =========================================================================
    private void loadAllData() {
        loadMarksData();
        loadAttendanceData();
        loadAssignmentsData();
        loadDailyClassworkData();
        loadFacultyFeedbackData();
        loadExamSchedulesData();
        loadNotificationsData();
    }

    private JTable createDarkTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(UITheme.FONT_REGULAR);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_BODY);

        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(15, 20, 35));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Custom zebra stripe cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(4, 12, 4, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : CARD_BG_ALT);
                } else {
                    c.setBackground(new Color(49, 46, 129));
                }

                // Status highlighting
                if (val != null) {
                    String str = val.toString();
                    if (str.contains("SHORTAGE") || str.contains("REJECTED")) {
                        c.setForeground(COLOR_DANGER);
                    } else if (str.contains("Satisfactory") || str.contains("SUBMITTED") || str.contains("VERIFIED") || str.contains("Completed")) {
                        c.setForeground(COLOR_STUDENT);
                    } else if (str.contains("PENDING")) {
                        c.setForeground(COLOR_WARN);
                    } else {
                        c.setForeground(TEXT_BODY);
                    }
                }
                return c;
            }
        });

        return table;
    }
}
