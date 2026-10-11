package com.acadexia.util;

import com.acadexia.dao.MarkDAO.StudentMarksEntryRow;
import com.acadexia.model.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility for generating professional institutional executive PDF reports for the Principal.
 * Produces audit-ready documents with official university letterhead, executive styling,
 * metrics summaries, formatted data tables, and Principal sign-off blocks.
 */
public final class PdfReportGenerator {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(17, 24, 39));
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(79, 70, 229));
    private static final Font FONT_META = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(107, 114, 128));
    private static final Font FONT_SEC_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(31, 41, 55));
    private static final Font FONT_TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_TD = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(31, 41, 55));
    private static final Font FONT_TD_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(17, 24, 39));
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(156, 163, 175));

    private static final Color COLOR_TH = new Color(49, 46, 129); // Deep Indigo
    private static final Color COLOR_ROW_ALT = new Color(243, 244, 246);
    private static final Color COLOR_BORDER = new Color(229, 231, 235);

    private PdfReportGenerator() {}

    private static Document createBaseDocument(FileOutputStream fos, String reportTitle) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        PdfWriter.getInstance(document, fos);
        document.open();

        // 1. Header Banner
        Paragraph header = new Paragraph();
        header.add(new Chunk("ACADEXIA INSTITUTE OF TECHNOLOGY & HIGHER EDUCATION\n", FONT_TITLE));
        header.add(new Chunk("OFFICE OF THE PRINCIPAL — EXECUTIVE ACADEMIC GOVERNANCE BOARD\n", FONT_SUBTITLE));
        header.add(new Chunk("Autonomous Institution Accredited with 'A++' Grade | ISO 9001:2015 Certified\n", FONT_META));
        document.add(header);

        // Divider
        Paragraph line = new Paragraph();
        line.add(new Chunk("____________________________________________________________________________________________________\n\n", FONT_META));
        document.add(line);

        // Report Title & Timestamp
        String nowStr = new SimpleDateFormat("dd MMMM yyyy, hh:mm a").format(new Date());
        Paragraph titleP = new Paragraph();
        titleP.add(new Chunk(reportTitle.toUpperCase() + "\n", FONT_SEC_HEADER));
        titleP.add(new Chunk("Generated on: " + nowStr + " | Reference: ACAD-PRIN-" + System.currentTimeMillis() % 1000000 + "\n\n", FONT_META));
        document.add(titleP);

        return document;
    }

    private static void addPrincipalSignOff(Document document, String summaryNotes) throws DocumentException {
        document.add(new Paragraph(" "));
        if (summaryNotes != null && !summaryNotes.isEmpty()) {
            PdfPTable noteBox = new PdfPTable(1);
            noteBox.setWidthPercentage(100);
            PdfPCell c = new PdfPCell(new Phrase("EXECUTIVE REMARKS & NOTES:\n" + summaryNotes, FONT_TD));
            c.setBackgroundColor(new Color(248, 250, 252));
            c.setBorderColor(new Color(203, 213, 225));
            c.setPadding(8);
            noteBox.addCell(c);
            document.add(noteBox);
            document.add(new Paragraph(" "));
        }

        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setWidths(new float[]{60, 40});

        PdfPCell left = new PdfPCell(new Phrase("Status: Officially Sanctioned & Recorded\nVerification Seal: ACADEXIA-EXECUTIVE-CONFIDENTIAL", FONT_FOOTER));
        left.setBorder(Rectangle.NO_BORDER);

        PdfPCell right = new PdfPCell(new Phrase("_____________________________________\nDr. K. S. Menon, Ph.D.\nPrincipal & Head of Institution\nAcadexia Institute of Technology", FONT_TD_BOLD));
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setBorder(Rectangle.NO_BORDER);

        signTable.addCell(left);
        signTable.addCell(right);
        document.add(signTable);
    }

    public static void generateInstitutionalPerformanceReport(File file, List<Student> students, List<Department> departments) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document doc = createBaseDocument(fos, "Institutional Academic Analytics & Performance Dossier");

            // Summary Stats Cards Table
            long distinctionCount = students.stream().filter(s -> s.getCurrentCgpa() >= 8.5).count();
            long atRiskCount = students.stream().filter(s -> s.getCurrentCgpa() < 5.0 || s.getAttendancePercentage() < 75.0).count();
            double avgCgpa = students.stream().mapToDouble(Student::getCurrentCgpa).average().orElse(0.0);
            double avgAtt = students.stream().mapToDouble(Student::getAttendancePercentage).average().orElse(0.0);

            PdfPTable statTable = new PdfPTable(4);
            statTable.setWidthPercentage(100);
            addStatCell(statTable, "TOTAL ENROLLMENT", String.valueOf(students.size()));
            addStatCell(statTable, "COLLEGE AVG CGPA", String.format("%.2f / 10.0", avgCgpa));
            addStatCell(statTable, "AVG ATTENDANCE", String.format("%.1f %%", avgAtt));
            addStatCell(statTable, "DISTINCTION COUNT", distinctionCount + " students");
            doc.add(statTable);
            doc.add(new Paragraph(" "));

            // Department Breakdown Table
            PdfPTable deptTable = new PdfPTable(5);
            deptTable.setWidthPercentage(100);
            deptTable.setWidths(new float[]{15, 35, 20, 15, 15});
            addTableHeader(deptTable, "Code", "Department", "Current HOD", "Enrolled", "Avg CGPA");

            int rowIdx = 0;
            for (Department d : departments) {
                List<Student> deptStudents = students.stream().filter(s -> d.getName().equalsIgnoreCase(s.getDepartmentName())).toList();
                double dAvg = deptStudents.stream().mapToDouble(Student::getCurrentCgpa).average().orElse(0.0);

                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(deptTable, d.getCode(), bg, false);
                addTableCell(deptTable, d.getName(), bg, true);
                addTableCell(deptTable, d.getHodName() != null ? d.getHodName() : "Unassigned", bg, false);
                addTableCell(deptTable, String.valueOf(deptStudents.size()), bg, false);
                addTableCell(deptTable, String.format("%.2f", dAvg), bg, false);
                rowIdx++;
            }
            doc.add(deptTable);
            doc.add(new Paragraph(" "));

            // Top Meritorious Students
            Paragraph topH = new Paragraph("TOP MERIT & ACADEMIC ACHIEVERS (TOP 10 CGPA)\n", FONT_SEC_HEADER);
            doc.add(topH);

            PdfPTable topTable = new PdfPTable(6);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{18, 10, 30, 20, 11, 11});
            addTableHeader(topTable, "Register No", "Roll", "Student Name", "Class", "Att %", "CGPA");

            List<Student> topStudents = students.stream()
                    .sorted((a, b) -> Double.compare(b.getCurrentCgpa(), a.getCurrentCgpa()))
                    .limit(10)
                    .toList();

            rowIdx = 0;
            for (Student s : topStudents) {
                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(topTable, s.getRegisterNumber(), bg, false);
                addTableCell(topTable, s.getRollNumber(), bg, false);
                addTableCell(topTable, s.getFullName(), bg, true);
                addTableCell(topTable, s.getClassName(), bg, false);
                addTableCell(topTable, String.format("%.1f%%", s.getAttendancePercentage()), bg, false);
                addTableCell(topTable, String.format("%.2f", s.getCurrentCgpa()), bg, true);
                rowIdx++;
            }
            doc.add(topTable);

            addPrincipalSignOff(doc, "Overall institutional academic progress is satisfactory. At-risk students (" + atRiskCount + ") must be assigned remedial mentor tutorials by respective CFAs.");
            doc.close();
        }
    }

    public static void generateClassResultReport(File file, String className, String subjectName, String examType,
                                                 List<StudentMarksEntryRow> rows, double maxMarks) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document doc = createBaseDocument(fos, "Examination Result & Mark Sheet Master Report");

            PdfPTable meta = new PdfPTable(4);
            meta.setWidthPercentage(100);
            addStatCell(meta, "CLASS / BATCH", className);
            addStatCell(meta, "SUBJECT / COURSE", subjectName);
            addStatCell(meta, "EXAM TYPE", examType);
            addStatCell(meta, "MAX MARKS", String.valueOf(maxMarks));
            doc.add(meta);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{16, 8, 28, 14, 12, 11, 11});
            addTableHeader(table, "Register No", "Roll", "Student Name", "Marks", "Percent", "Grade", "Status");

            int passCount = 0;
            int total = 0;
            double sumMarks = 0;

            int rowIdx = 0;
            for (StudentMarksEntryRow r : rows) {
                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(table, r.registerNumber(), bg, false);
                addTableCell(table, r.rollNumber(), bg, false);
                addTableCell(table, r.fullName(), bg, true);

                if (r.marksObtained() != null) {
                    double marks = r.marksObtained();
                    double pct = (marks / maxMarks) * 100.0;
                    sumMarks += marks;
                    total++;
                    boolean passed = pct >= 40.0;
                    if (passed) passCount++;

                    String grade = getGrade(pct);

                    addTableCell(table, String.format("%.1f / %.1f", marks, maxMarks), bg, false);
                    addTableCell(table, String.format("%.1f %%", pct), bg, false);
                    addTableCell(table, grade, bg, true);
                    addTableCell(table, passed ? "PASS" : "FAIL", bg, passed ? false : true);
                } else {
                    addTableCell(table, "ABSENT", bg, false);
                    addTableCell(table, "-", bg, false);
                    addTableCell(table, "F", bg, true);
                    addTableCell(table, "ABSENT", bg, true);
                }
                rowIdx++;
            }
            doc.add(table);
            doc.add(new Paragraph(" "));

            double passPct = total > 0 ? ((double) passCount / total) * 100.0 : 0.0;
            double classAvg = total > 0 ? (sumMarks / total) : 0.0;

            PdfPTable sumTable = new PdfPTable(3);
            sumTable.setWidthPercentage(100);
            addStatCell(sumTable, "CANDIDATES EVALUATED", String.valueOf(total));
            addStatCell(sumTable, "CLASS PASS PERCENTAGE", String.format("%.2f %%", passPct));
            addStatCell(sumTable, "CLASS AVERAGE MARK", String.format("%.2f / %.1f", classAvg, maxMarks));
            doc.add(sumTable);

            addPrincipalSignOff(doc, "Official examination marks verified and published by the Executive Office of the Principal.");
            doc.close();
        }
    }

    public static void generateDutyLeaveAuditReport(File file, List<DutyLeave> leaves) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document doc = createBaseDocument(fos, "Tier-3 Duty Leave Sanction Audit & Governance Register");

            long approved = leaves.stream().filter(l -> l.getStatus() == DutyLeave.Status.APPROVED).count();
            long pending = leaves.stream().filter(l -> l.getStatus() == DutyLeave.Status.PENDING_ADVISOR 
                    || l.getStatus() == DutyLeave.Status.RECOMMENDED_BY_ADVISOR 
                    || l.getStatus() == DutyLeave.Status.ENDORSED_BY_HOD).count();
            long rejected = leaves.stream().filter(l -> l.getStatus() == DutyLeave.Status.REJECTED_BY_ADVISOR 
                    || l.getStatus() == DutyLeave.Status.REJECTED_BY_HOD 
                    || l.getStatus() == DutyLeave.Status.REJECTED).count();

            PdfPTable stats = new PdfPTable(4);
            stats.setWidthPercentage(100);
            addStatCell(stats, "TOTAL APPLICATIONS", String.valueOf(leaves.size()));
            addStatCell(stats, "OFFICIALLY SANCTIONED", String.valueOf(approved));
            addStatCell(stats, "PENDING DECISION", String.valueOf(pending));
            addStatCell(stats, "DENIED / REJECTED", String.valueOf(rejected));
            doc.add(stats);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{8, 16, 18, 14, 8, 16, 10, 10});
            addTableHeader(table, "ID", "Student", "Register No", "Class", "Days", "Reason", "HOD Endorse", "Status");

            int rowIdx = 0;
            for (DutyLeave dl : leaves) {
                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(table, String.valueOf(dl.getId()), bg, false);
                addTableCell(table, dl.getStudentName(), bg, true);
                addTableCell(table, dl.getRegisterNumber(), bg, false);
                addTableCell(table, dl.getClassName(), bg, false);
                addTableCell(table, String.valueOf(dl.getTotalDays()), bg, false);
                addTableCell(table, dl.getReason(), bg, false);
                addTableCell(table, dl.getHodRemarks() != null ? dl.getHodRemarks() : "-", bg, false);
                addTableCell(table, dl.getStatus() != null ? dl.getStatus().getLabel() : "-", bg, true);
                rowIdx++;
            }
            doc.add(table);

            addPrincipalSignOff(doc, "All sanctioned duty leaves have been officially validated against supporting documentation and academic regulations.");
            doc.close();
        }
    }

    public static void generateGrievanceAuditReport(File file, List<Grievance> grievances) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document doc = createBaseDocument(fos, "Student Grievance Redressal Oversight & Adjudication Register");

            long resolved = grievances.stream().filter(g -> g.getStatus() == Grievance.Status.RESOLVED).count();
            long pending = grievances.stream().filter(g -> g.getStatus() == Grievance.Status.SUBMITTED || g.getStatus() == Grievance.Status.UNDER_REVIEW).count();

            PdfPTable stats = new PdfPTable(3);
            stats.setWidthPercentage(100);
            addStatCell(stats, "TOTAL GRIEVANCES", String.valueOf(grievances.size()));
            addStatCell(stats, "RESOLVED & CLOSED", String.valueOf(resolved));
            addStatCell(stats, "ACTIVE / UNDER REVIEW", String.valueOf(pending));
            doc.add(stats);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{8, 16, 16, 14, 20, 12, 14});
            addTableHeader(table, "ID", "Student", "Register No", "Category", "Title", "Status", "Resolved By");

            int rowIdx = 0;
            for (Grievance g : grievances) {
                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(table, String.valueOf(g.getId()), bg, false);
                addTableCell(table, g.getStudentName(), bg, true);
                addTableCell(table, g.getRegisterNumber(), bg, false);
                addTableCell(table, g.getCategory().getLabel(), bg, false);
                addTableCell(table, g.getTitle(), bg, false);
                addTableCell(table, g.getStatus().getLabel(), bg, true);
                addTableCell(table, g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "-", bg, false);
                rowIdx++;
            }
            doc.add(table);

            addPrincipalSignOff(doc, "Statutory grievance redressal hearings conducted in compliance with UGC/AICTE Grievance Redressal Regulations.");
            doc.close();
        }
    }

    public static void generateFacultyWorkloadReport(File file, List<Faculty> facultyList) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document doc = createBaseDocument(fos, "Institutional Faculty Workload & Governance Register");

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15, 25, 20, 20, 10, 10});
            addTableHeader(table, "Faculty ID", "Faculty Name", "Department", "Designation", "HOD", "CFA");

            int rowIdx = 0;
            for (Faculty f : facultyList) {
                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(table, f.getFacultyId(), bg, false);
                addTableCell(table, f.getFullName(), bg, true);
                addTableCell(table, f.getDepartmentName(), bg, false);
                addTableCell(table, f.getDesignation(), bg, false);
                addTableCell(table, f.isHod() ? "YES" : "NO", bg, f.isHod());
                addTableCell(table, f.isCfa() ? "YES" : "NO", bg, f.isCfa());
                rowIdx++;
            }
            doc.add(table);

            addPrincipalSignOff(doc, "Faculty assignments, HOD appointments, and academic workloads endorsed by the Office of the Principal.");
            doc.close();
        }
    }

    public static void generateAuditLogsReport(File file, List<AuditLog> logs) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document doc = createBaseDocument(fos, "Institutional System Security & Audit Trail Register");

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{18, 14, 12, 18, 12, 26});
            addTableHeader(table, "Timestamp", "User", "Role", "Action", "Severity", "Details");

            int rowIdx = 0;
            for (AuditLog l : logs) {
                Color bg = (rowIdx % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addTableCell(table, String.valueOf(l.getTimestamp()), bg, false);
                addTableCell(table, l.getUsername(), bg, true);
                addTableCell(table, l.getRole(), bg, false);
                addTableCell(table, l.getAction(), bg, false);
                addTableCell(table, l.getSeverity().name(), bg, l.getSeverity() == AuditLog.Severity.SECURITY_ALERT);
                addTableCell(table, l.getDetails() != null ? l.getDetails() : "-", bg, false);
                rowIdx++;
            }
            doc.add(table);

            addPrincipalSignOff(doc, "Executive security and compliance audit log generated for administrative audit archives.");
            doc.close();
        }
    }

    private static void addStatCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(248, 250, 252));
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(8);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label.toUpperCase() + "\n", FONT_META));
        p.add(new Chunk(value, FONT_SEC_HEADER));
        cell.addElement(p);

        table.addCell(cell);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FONT_TH));
            c.setBackgroundColor(COLOR_TH);
            c.setBorderColor(new Color(30, 27, 75));
            c.setPadding(6);
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c);
        }
    }

    private static void addTableCell(PdfPTable table, String text, Color bg, boolean bold) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "-", bold ? FONT_TD_BOLD : FONT_TD));
        c.setBackgroundColor(bg);
        c.setBorderColor(COLOR_BORDER);
        c.setPadding(5);
        table.addCell(c);
    }

    private static String getGrade(double pct) {
        if (pct >= 90) return "S";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        if (pct >= 40) return "E";
        return "F";
    }
}
