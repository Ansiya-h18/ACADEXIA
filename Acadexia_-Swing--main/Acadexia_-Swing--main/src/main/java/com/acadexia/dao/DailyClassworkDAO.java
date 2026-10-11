package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.DailyClasswork;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DailyClassworkDAO {

    public List<DailyClasswork> getClassworkForClass(int classId) {
        List<DailyClasswork> list = new ArrayList<>();
        String sql = """
            SELECT cw.*,
                   c.name AS class_name,
                   s.code AS subject_code, s.name AS subject_name,
                   u.full_name AS faculty_name
            FROM daily_classwork cw
            JOIN classes c ON cw.class_id = c.id
            JOIN subjects s ON cw.subject_id = s.id
            JOIN faculty f ON cw.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            WHERE cw.class_id = ?
            ORDER BY cw.date DESC, cw.period_hour ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClasswork(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching classwork for class: " + e.getMessage());
        }
        return list;
    }

    public List<DailyClasswork> getClassworkForClassAndDate(int classId, Date date) {
        List<DailyClasswork> list = new ArrayList<>();
        String sql = """
            SELECT cw.*,
                   c.name AS class_name,
                   s.code AS subject_code, s.name AS subject_name,
                   u.full_name AS faculty_name
            FROM daily_classwork cw
            JOIN classes c ON cw.class_id = c.id
            JOIN subjects s ON cw.subject_id = s.id
            JOIN faculty f ON cw.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            WHERE cw.class_id = ? AND cw.date = ?
            ORDER BY cw.period_hour ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setDate(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClasswork(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching classwork for class and date: " + e.getMessage());
        }
        return list;
    }

    public boolean createClasswork(DailyClasswork cw) {
        String sql = """
            INSERT INTO daily_classwork (class_id, subject_id, faculty_id, date, period_hour, topic_covered, homework_assigned, reference_materials, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cw.getClassId());
            ps.setInt(2, cw.getSubjectId());
            ps.setInt(3, cw.getFacultyId());
            ps.setDate(4, cw.getDate());
            ps.setInt(5, cw.getPeriodHour());
            ps.setString(6, cw.getTopicCovered());
            ps.setString(7, cw.getHomeworkAssigned());
            ps.setString(8, cw.getReferenceMaterials());
            ps.setString(9, cw.getStatus().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording daily classwork: " + e.getMessage());
            return false;
        }
    }

    private DailyClasswork mapResultSetToClasswork(ResultSet rs) throws SQLException {
        DailyClasswork cw = new DailyClasswork();
        cw.setId(rs.getInt("id"));
        cw.setClassId(rs.getInt("class_id"));
        cw.setClassName(rs.getString("class_name"));
        cw.setSubjectId(rs.getInt("subject_id"));
        cw.setSubjectCode(rs.getString("subject_code"));
        cw.setSubjectName(rs.getString("subject_name"));
        cw.setFacultyId(rs.getInt("faculty_id"));
        cw.setFacultyName(rs.getString("faculty_name"));
        cw.setDate(rs.getDate("date"));
        cw.setPeriodHour(rs.getInt("period_hour"));
        cw.setTopicCovered(rs.getString("topic_covered"));
        cw.setHomeworkAssigned(rs.getString("homework_assigned"));
        cw.setReferenceMaterials(rs.getString("reference_materials"));
        cw.setStatus(DailyClasswork.Status.valueOf(rs.getString("status")));
        cw.setCreatedAt(rs.getTimestamp("created_at"));
        return cw;
    }
}
