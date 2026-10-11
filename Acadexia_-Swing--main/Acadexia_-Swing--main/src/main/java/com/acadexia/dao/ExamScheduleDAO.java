package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.ExamSchedule;
import com.acadexia.model.Mark;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamScheduleDAO {

    public List<ExamSchedule> getExamSchedulesForClass(int classId) {
        List<ExamSchedule> list = new ArrayList<>();
        String sql = """
            SELECT es.*,
                   c.name AS class_name,
                   s.code AS subject_code, s.name AS subject_name
            FROM exam_schedules es
            JOIN classes c ON es.class_id = c.id
            JOIN subjects s ON es.subject_id = s.id
            WHERE es.class_id = ?
            ORDER BY es.exam_date ASC, es.start_time ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSchedule(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching exam schedules for class: " + e.getMessage());
        }
        return list;
    }

    public boolean createExamSchedule(ExamSchedule es) {
        String sql = """
            INSERT INTO exam_schedules (class_id, subject_id, exam_type, exam_name, exam_date, start_time, end_time, room_hall, max_marks, portions_syllabus)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, es.getClassId());
            ps.setInt(2, es.getSubjectId());
            ps.setString(3, es.getExamType().name());
            ps.setString(4, es.getExamName());
            ps.setDate(5, es.getExamDate());
            ps.setString(6, es.getStartTime());
            ps.setString(7, es.getEndTime());
            ps.setString(8, es.getRoomHall());
            ps.setDouble(9, es.getMaxMarks());
            ps.setString(10, es.getPortionsSyllabus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating exam schedule: " + e.getMessage());
            return false;
        }
    }

    public List<ExamSchedule> getAllExamSchedules() {
        List<ExamSchedule> list = new ArrayList<>();
        String sql = """
            SELECT es.*,
                   c.name AS class_name,
                   s.code AS subject_code, s.name AS subject_name
            FROM exam_schedules es
            JOIN classes c ON es.class_id = c.id
            JOIN subjects s ON es.subject_id = s.id
            ORDER BY es.exam_date DESC, es.start_time ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSchedule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all exam schedules: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteExamSchedule(int id) {
        String sql = "DELETE FROM exam_schedules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting exam schedule: " + e.getMessage());
            return false;
        }
    }


    private ExamSchedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        ExamSchedule es = new ExamSchedule();
        es.setId(rs.getInt("id"));
        es.setClassId(rs.getInt("class_id"));
        es.setClassName(rs.getString("class_name"));
        es.setSubjectId(rs.getInt("subject_id"));
        es.setSubjectCode(rs.getString("subject_code"));
        es.setSubjectName(rs.getString("subject_name"));
        es.setExamType(Mark.ExamType.valueOf(rs.getString("exam_type")));
        es.setExamName(rs.getString("exam_name"));
        es.setExamDate(rs.getDate("exam_date"));
        es.setStartTime(rs.getString("start_time"));
        es.setEndTime(rs.getString("end_time"));
        es.setRoomHall(rs.getString("room_hall"));
        es.setMaxMarks(rs.getDouble("max_marks"));
        es.setPortionsSyllabus(rs.getString("portions_syllabus"));
        es.setCreatedAt(rs.getTimestamp("created_at"));
        return es;
    }
}
