package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Parent;
import com.acadexia.model.Role;
import com.acadexia.security.SecurityUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParentDAO {

    public Parent getParentByUserId(int userId) {
        String sql = """
            SELECT p.*,
                   u.username, u.full_name AS parent_name, u.email AS parent_email, 
                   u.phone AS parent_phone, u.is_active AS user_active,
                   stu.id AS stu_id, stu.register_number, stu.roll_number, stu.class_id,
                   stu_user.full_name AS student_name,
                   c.name AS class_name, c.semester,
                   d.name AS department_name,
                   fac_user.full_name AS advisor_name
            FROM parents p
            JOIN users u ON p.user_id = u.id
            LEFT JOIN students stu ON p.student_id = stu.id
            LEFT JOIN users stu_user ON stu.user_id = stu_user.id
            LEFT JOIN classes c ON stu.class_id = c.id
            LEFT JOIN departments d ON c.department_id = d.id
            LEFT JOIN faculty fac ON c.advisor_faculty_id = fac.id
            LEFT JOIN users fac_user ON fac.user_id = fac_user.id
            WHERE p.user_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Parent parent = mapResultSetToParent(rs);
                    enrichStudentMetrics(parent, conn);
                    return parent;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching parent by user ID: " + e.getMessage());
        }
        return null;
    }

    public Parent getParentById(int parentId) {
        String sql = """
            SELECT p.*,
                   u.username, u.full_name AS parent_name, u.email AS parent_email, 
                   u.phone AS parent_phone, u.is_active AS user_active,
                   stu.id AS stu_id, stu.register_number, stu.roll_number, stu.class_id,
                   stu_user.full_name AS student_name,
                   c.name AS class_name, c.semester,
                   d.name AS department_name,
                   fac_user.full_name AS advisor_name
            FROM parents p
            JOIN users u ON p.user_id = u.id
            LEFT JOIN students stu ON p.student_id = stu.id
            LEFT JOIN users stu_user ON stu.user_id = stu_user.id
            LEFT JOIN classes c ON stu.class_id = c.id
            LEFT JOIN departments d ON c.department_id = d.id
            LEFT JOIN faculty fac ON c.advisor_faculty_id = fac.id
            LEFT JOIN users fac_user ON fac.user_id = fac_user.id
            WHERE p.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Parent parent = mapResultSetToParent(rs);
                    enrichStudentMetrics(parent, conn);
                    return parent;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching parent by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Parent> getAllParents() {
        List<Parent> list = new ArrayList<>();
        String sql = """
            SELECT p.*,
                   u.username, u.full_name AS parent_name, u.email AS parent_email, 
                   u.phone AS parent_phone, u.is_active AS user_active,
                   stu.id AS stu_id, stu.register_number, stu.roll_number, stu.class_id,
                   stu_user.full_name AS student_name,
                   c.name AS class_name, c.semester,
                   d.name AS department_name,
                   fac_user.full_name AS advisor_name
            FROM parents p
            JOIN users u ON p.user_id = u.id
            LEFT JOIN students stu ON p.student_id = stu.id
            LEFT JOIN users stu_user ON stu.user_id = stu_user.id
            LEFT JOIN classes c ON stu.class_id = c.id
            LEFT JOIN departments d ON c.department_id = d.id
            LEFT JOIN faculty fac ON c.advisor_faculty_id = fac.id
            LEFT JOIN users fac_user ON fac.user_id = fac_user.id
            ORDER BY p.created_at DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Parent parent = mapResultSetToParent(rs);
                enrichStudentMetrics(parent, conn);
                list.add(parent);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all parents: " + e.getMessage());
        }
        return list;
    }

    public int createParent(Parent parent, String plainPassword) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into users table
            String userSql = """
                INSERT INTO users (username, password_hash, role, full_name, email, phone, is_active)
                VALUES (?, ?, 'PARENT', ?, ?, ?, ?)
                """;
            int generatedUserId;
            try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, parent.getUsername().trim());
                psUser.setString(2, SecurityUtils.hashPassword(plainPassword));
                psUser.setString(3, parent.getFullName().trim());
                psUser.setString(4, parent.getEmail() != null ? parent.getEmail().trim() : null);
                psUser.setString(5, parent.getPhone() != null ? parent.getPhone().trim() : null);
                psUser.setBoolean(6, parent.isActive());
                psUser.executeUpdate();

                try (ResultSet rsUser = psUser.getGeneratedKeys()) {
                    if (rsUser.next()) {
                        generatedUserId = rsUser.getInt(1);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            // 2. Insert into parents table
            String parentSql = """
                INSERT INTO parents (user_id, student_id, relationship, occupation, address, emergency_contact, 
                                     verification_status, verified_by_user_id, verification_date, verification_notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            int generatedParentId;
            try (PreparedStatement psParent = conn.prepareStatement(parentSql, Statement.RETURN_GENERATED_KEYS)) {
                psParent.setInt(1, generatedUserId);
                psParent.setInt(2, parent.getStudentId());
                psParent.setString(3, parent.getRelationship() != null ? parent.getRelationship() : "Parent");
                psParent.setString(4, parent.getOccupation());
                psParent.setString(5, parent.getAddress());
                psParent.setString(6, parent.getEmergencyContact());
                psParent.setString(7, parent.getVerificationStatus() != null ? parent.getVerificationStatus().name() : "PENDING");
                if (parent.getVerifiedByUserId() != null) {
                    psParent.setInt(8, parent.getVerifiedByUserId());
                    psParent.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
                } else {
                    psParent.setNull(8, Types.INTEGER);
                    psParent.setNull(9, Types.TIMESTAMP);
                }
                psParent.setString(10, parent.getVerificationNotes());
                psParent.executeUpdate();

                try (ResultSet rsParent = psParent.getGeneratedKeys()) {
                    if (rsParent.next()) {
                        generatedParentId = rsParent.getInt(1);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            conn.commit();

            AuditLogDAO.log(generatedUserId, parent.getUsername(), "ADMIN",
                    "PARENT_ACCOUNT_PROVISIONED", "PARENT", String.valueOf(generatedParentId),
                    "Created parent account linked to student ID " + parent.getStudentId(), AuditLog.Severity.INFO);

            parent.setId(generatedParentId);
            parent.setUserId(generatedUserId);
            return generatedParentId;

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateVerificationStatus(int parentId, Parent.VerificationStatus status, int verifiedByUserId, String notes) {
        String sql = """
            UPDATE parents 
            SET verification_status = ?, verified_by_user_id = ?, verification_date = CURRENT_TIMESTAMP, verification_notes = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, verifiedByUserId);
            ps.setString(3, notes);
            ps.setInt(4, parentId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                AuditLogDAO.log(verifiedByUserId, "ADMIN", "ADMIN",
                        "PARENT_VERIFICATION_UPDATED", "PARENT", String.valueOf(parentId),
                        "Verification status set to " + status.name() + ". Notes: " + notes, AuditLog.Severity.INFO);
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("Error updating parent verification: " + e.getMessage());
            return false;
        }
    }

    public boolean linkParentToStudent(int parentId, int studentId, String relationship) {
        String sql = "UPDATE parents SET student_id = ?, relationship = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, relationship != null ? relationship : "Parent");
            ps.setInt(3, parentId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                AuditLogDAO.log(null, "ADMIN", "ADMIN",
                        "PARENT_STUDENT_LINK_MODIFIED", "PARENT", String.valueOf(parentId),
                        "Linked parent ID " + parentId + " to student ID " + studentId, AuditLog.Severity.INFO);
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("Error linking parent to student: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleParentStatus(int userId, boolean active) {
        UserDAO userDAO = new UserDAO();
        return userDAO.updateUserStatus(userId, active);
    }

    private Parent mapResultSetToParent(ResultSet rs) throws SQLException {
        Parent p = new Parent();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setStudentId(rs.getInt("student_id"));
        p.setRelationship(rs.getString("relationship"));
        p.setOccupation(rs.getString("occupation"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        String vStatus = rs.getString("verification_status");
        p.setVerificationStatus(vStatus != null ? Parent.VerificationStatus.valueOf(vStatus) : Parent.VerificationStatus.PENDING);
        int verifier = rs.getInt("verified_by_user_id");
        if (!rs.wasNull()) {
            p.setVerifiedByUserId(verifier);
        }
        p.setVerificationDate(rs.getTimestamp("verification_date"));
        p.setVerificationNotes(rs.getString("verification_notes"));
        p.setCreatedAt(rs.getTimestamp("created_at"));

        p.setUsername(rs.getString("username"));
        p.setFullName(rs.getString("parent_name"));
        p.setEmail(rs.getString("parent_email"));
        p.setPhone(rs.getString("parent_phone"));
        p.setActive(rs.getBoolean("user_active"));

        p.setStudentFullName(rs.getString("student_name") != null ? rs.getString("student_name") : "Linked Student");
        p.setStudentRegisterNumber(rs.getString("register_number") != null ? rs.getString("register_number") : "-");
        p.setStudentRollNumber(rs.getString("roll_number") != null ? rs.getString("roll_number") : "-");
        p.setStudentClassId(rs.getInt("class_id"));
        p.setStudentClassName(rs.getString("class_name") != null ? rs.getString("class_name") : "Assigned Class");
        p.setStudentDepartmentName(rs.getString("department_name") != null ? rs.getString("department_name") : "-");
        p.setStudentSemester(rs.getInt("semester"));
        p.setStudentAdvisorName(rs.getString("advisor_name") != null ? rs.getString("advisor_name") : "Not Assigned");

        return p;
    }

    private void enrichStudentMetrics(Parent p, Connection conn) {
        int stuId = p.getStudentId();
        if (stuId <= 0) return;

        // Attendance calculation
        String attSql = """
            SELECT 
                COUNT(*) AS total_hours,
                SUM(CASE WHEN status IN ('PRESENT', 'DUTY_LEAVE') THEN 1 ELSE 0 END) AS attended_hours
            FROM attendance
            WHERE student_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(attSql)) {
            ps.setInt(1, stuId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_hours");
                    int attended = rs.getInt("attended_hours");
                    p.setStudentAttendancePercentage(total > 0 ? (attended * 100.0 / total) : 100.0);
                }
            }
        } catch (SQLException ignored) {}

        // SGPA calculation from marks
        String sgpaSql = """
            SELECT m.marks_obtained, m.max_marks, sub.credits
            FROM marks m
            JOIN subjects sub ON m.subject_id = sub.id
            WHERE m.student_id = ? AND m.exam_type = 'INTERNAL'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sgpaSql)) {
            ps.setInt(1, stuId);
            try (ResultSet rs = ps.executeQuery()) {
                double totalCredits = 0;
                double weightedPoints = 0;
                while (rs.next()) {
                    double marks = rs.getDouble("marks_obtained");
                    double max = rs.getDouble("max_marks");
                    int credits = rs.getInt("credits");
                    double pct = max > 0 ? (marks / max) * 100.0 : 0.0;
                    double gp = calculateGradePoint(pct);
                    weightedPoints += gp * credits;
                    totalCredits += credits;
                }
                double sgpa = totalCredits > 0 ? (weightedPoints / totalCredits) : 0.0;
                p.setStudentSgpa(Math.round(sgpa * 100.0) / 100.0);
                p.setStudentCgpa(p.getStudentSgpa());
            }
        } catch (SQLException ignored) {}
    }

    private double calculateGradePoint(double pct) {
        if (pct >= 90) return 10.0;
        if (pct >= 85) return 9.0;
        if (pct >= 80) return 8.5;
        if (pct >= 75) return 8.0;
        if (pct >= 70) return 7.5;
        if (pct >= 65) return 7.0;
        if (pct >= 60) return 6.5;
        if (pct >= 50) return 6.0;
        if (pct >= 40) return 5.5;
        return 0.0;
    }
}
