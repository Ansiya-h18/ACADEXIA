package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.AcademicEvent;
import com.acadexia.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcademicCalendarDAO {

    public List<AcademicEvent> getAllEvents() {
        List<AcademicEvent> list = new ArrayList<>();
        String sql = """
            SELECT ac.*, u.full_name AS created_by_name
            FROM academic_calendar ac
            LEFT JOIN users u ON ac.created_by_user_id = u.id
            ORDER BY ac.start_date ASC, ac.id ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching academic calendar events: " + e.getMessage());
        }
        return list;
    }

    public List<AcademicEvent> getEventsByType(AcademicEvent.EventType type) {
        List<AcademicEvent> list = new ArrayList<>();
        String sql = """
            SELECT ac.*, u.full_name AS created_by_name
            FROM academic_calendar ac
            LEFT JOIN users u ON ac.created_by_user_id = u.id
            WHERE ac.event_type = ?
            ORDER BY ac.start_date ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching academic events by type: " + e.getMessage());
        }
        return list;
    }

    public boolean createEvent(AcademicEvent event, String authorUsername) {
        String sql = """
            INSERT INTO academic_calendar (event_title, event_type, start_date, end_date, description, target_audience, semester_applicable, created_by_user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventTitle());
            ps.setString(2, event.getEventType().name());
            ps.setDate(3, event.getStartDate());
            ps.setDate(4, event.getEndDate());
            ps.setString(5, event.getDescription());
            ps.setString(6, event.getTargetAudience());
            ps.setString(7, event.getSemesterApplicable());
            if (event.getCreatedByUserId() != null) {
                ps.setInt(8, event.getCreatedByUserId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(event.getCreatedByUserId(), authorUsername, "PRINCIPAL",
                        "CALENDAR_EVENT_SCHEDULED", "ACADEMIC_CALENDAR", event.getEventTitle(),
                        "Date: " + event.getStartDate() + " | Type: " + event.getEventType().name(), AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error scheduling academic event: " + e.getMessage());
            return false;
        }
    }

    public boolean updateEvent(AcademicEvent event, String authorUsername) {
        String sql = """
            UPDATE academic_calendar
            SET event_title = ?, event_type = ?, start_date = ?, end_date = ?, description = ?, target_audience = ?, semester_applicable = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventTitle());
            ps.setString(2, event.getEventType().name());
            ps.setDate(3, event.getStartDate());
            ps.setDate(4, event.getEndDate());
            ps.setString(5, event.getDescription());
            ps.setString(6, event.getTargetAudience());
            ps.setString(7, event.getSemesterApplicable());
            ps.setInt(8, event.getId());
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(event.getCreatedByUserId(), authorUsername, "PRINCIPAL",
                        "CALENDAR_EVENT_UPDATED", "ACADEMIC_CALENDAR", String.valueOf(event.getId()),
                        "Updated: " + event.getEventTitle(), AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating academic event: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEvent(int id, String authorUsername, Integer authorUserId) {
        String sql = "DELETE FROM academic_calendar WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(authorUserId, authorUsername, "PRINCIPAL",
                        "CALENDAR_EVENT_DELETED", "ACADEMIC_CALENDAR", String.valueOf(id),
                        "Deleted academic calendar event ID " + id, AuditLog.Severity.WARNING);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting academic calendar event: " + e.getMessage());
            return false;
        }
    }

    private AcademicEvent mapResultSetToEvent(ResultSet rs) throws SQLException {
        AcademicEvent ae = new AcademicEvent();
        ae.setId(rs.getInt("id"));
        ae.setEventTitle(rs.getString("event_title"));
        ae.setEventType(AcademicEvent.EventType.valueOf(rs.getString("event_type")));
        ae.setStartDate(rs.getDate("start_date"));
        ae.setEndDate(rs.getDate("end_date"));
        ae.setDescription(rs.getString("description"));
        ae.setTargetAudience(rs.getString("target_audience"));
        ae.setSemesterApplicable(rs.getString("semester_applicable"));
        int uid = rs.getInt("created_by_user_id");
        if (!rs.wasNull()) {
            ae.setCreatedByUserId(uid);
            ae.setCreatedByUserName(rs.getString("created_by_name"));
        }
        ae.setCreatedAt(rs.getTimestamp("created_at"));
        return ae;
    }
}
