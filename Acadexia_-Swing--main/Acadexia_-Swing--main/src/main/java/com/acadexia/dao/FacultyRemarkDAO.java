package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.FacultyRemark;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacultyRemarkDAO {

    public List<FacultyRemark> getRemarksForStudent(int studentId) {
        List<FacultyRemark> list = new ArrayList<>();
        String sql = """
            SELECT fr.*,
                   u_stu.full_name AS student_name, stu.register_number,
                   u_fac.full_name AS faculty_name, f.designation AS faculty_designation,
                   s.code AS subject_code, s.name AS subject_name
            FROM faculty_remarks fr
            JOIN students stu ON fr.student_id = stu.id
            JOIN users u_stu ON stu.user_id = u_stu.id
            JOIN faculty f ON fr.faculty_id = f.id
            JOIN users u_fac ON f.user_id = u_fac.id
            LEFT JOIN subjects s ON fr.subject_id = s.id
            WHERE fr.student_id = ?
            ORDER BY fr.date_posted DESC, fr.created_at DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRemark(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty remarks for student: " + e.getMessage());
        }
        return list;
    }

    public List<FacultyRemark> getRemarksForFaculty(int facultyId) {
        List<FacultyRemark> list = new ArrayList<>();
        String sql = """
            SELECT fr.*,
                   u_stu.full_name AS student_name, stu.register_number,
                   u_fac.full_name AS faculty_name, f.designation AS faculty_designation,
                   s.code AS subject_code, s.name AS subject_name
            FROM faculty_remarks fr
            JOIN students stu ON fr.student_id = stu.id
            JOIN users u_stu ON stu.user_id = u_stu.id
            JOIN faculty f ON fr.faculty_id = f.id
            JOIN users u_fac ON f.user_id = u_fac.id
            LEFT JOIN subjects s ON fr.subject_id = s.id
            WHERE fr.faculty_id = ?
            ORDER BY fr.date_posted DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRemark(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty remarks: " + e.getMessage());
        }
        return list;
    }

    public boolean createRemark(FacultyRemark remark) {
        String sql = """
            INSERT INTO faculty_remarks (student_id, faculty_id, subject_id, category, title, remarks, action_suggested, rating, date_posted)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, remark.getStudentId());
            ps.setInt(2, remark.getFacultyId());
            if (remark.getSubjectId() != null && remark.getSubjectId() > 0) {
                ps.setInt(3, remark.getSubjectId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, remark.getCategory().name());
            ps.setString(5, remark.getTitle());
            ps.setString(6, remark.getRemarks());
            ps.setString(7, remark.getActionSuggested());
            if (remark.getRating() != null) {
                ps.setInt(8, remark.getRating());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setDate(9, remark.getDatePosted());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating faculty remark: " + e.getMessage());
            return false;
        }
    }

    private FacultyRemark mapResultSetToRemark(ResultSet rs) throws SQLException {
        FacultyRemark r = new FacultyRemark();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setStudentName(rs.getString("student_name"));
        r.setStudentRegisterNo(rs.getString("register_number"));
        r.setFacultyId(rs.getInt("faculty_id"));
        r.setFacultyName(rs.getString("faculty_name"));
        r.setFacultyDesignation(rs.getString("faculty_designation"));
        int subId = rs.getInt("subject_id");
        if (!rs.wasNull()) {
            r.setSubjectId(subId);
            r.setSubjectCode(rs.getString("subject_code"));
            r.setSubjectName(rs.getString("subject_name"));
        }
        r.setCategory(FacultyRemark.Category.valueOf(rs.getString("category")));
        r.setTitle(rs.getString("title"));
        r.setRemarks(rs.getString("remarks"));
        r.setActionSuggested(rs.getString("action_suggested"));
        int rat = rs.getInt("rating");
        if (!rs.wasNull()) {
            r.setRating(rat);
        }
        r.setDatePosted(rs.getDate("date_posted"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
