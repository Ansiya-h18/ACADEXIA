package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Announcement;
import com.acadexia.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u.full_name AS posted_by_name
            FROM announcements a
            LEFT JOIN users u ON a.posted_by_user_id = u.id
            ORDER BY a.pinned DESC, a.posted_date DESC, a.id DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAnnouncement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching announcements: " + e.getMessage());
        }
        return list;
    }

    public List<Announcement> getAnnouncementsForAudience(String audience) {
        List<Announcement> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u.full_name AS posted_by_name
            FROM announcements a
            LEFT JOIN users u ON a.posted_by_user_id = u.id
            WHERE a.target_audience = 'ALL' OR a.target_audience = ?
            ORDER BY a.pinned DESC, a.posted_date DESC, a.id DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, audience);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAnnouncement(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching announcements for audience: " + e.getMessage());
        }
        return list;
    }

    public boolean createAnnouncement(Announcement a, String authorUsername) {
        String sql = """
            INSERT INTO announcements (title, content, category, target_audience, pinned, posted_by_user_id, posted_date, expiry_date, attachment_note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory().name());
            ps.setString(4, a.getTargetAudience());
            ps.setBoolean(5, a.isPinned());
            if (a.getPostedByUserId() != null) {
                ps.setInt(6, a.getPostedByUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setDate(7, a.getPostedDate());
            ps.setDate(8, a.getExpiryDate());
            ps.setString(9, a.getAttachmentNote());
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(a.getPostedByUserId(), authorUsername, "PRINCIPAL",
                        "ANNOUNCEMENT_PUBLISHED", "ANNOUNCEMENT", a.getTitle(),
                        "Target: " + a.getTargetAudience() + " | Category: " + a.getCategory().name(), AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error creating announcement: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAnnouncement(Announcement a, String authorUsername) {
        String sql = """
            UPDATE announcements
            SET title = ?, content = ?, category = ?, target_audience = ?, pinned = ?, expiry_date = ?, attachment_note = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory().name());
            ps.setString(4, a.getTargetAudience());
            ps.setBoolean(5, a.isPinned());
            ps.setDate(6, a.getExpiryDate());
            ps.setString(7, a.getAttachmentNote());
            ps.setInt(8, a.getId());
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(a.getPostedByUserId(), authorUsername, "PRINCIPAL",
                        "ANNOUNCEMENT_UPDATED", "ANNOUNCEMENT", String.valueOf(a.getId()),
                        "Updated circular: " + a.getTitle(), AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating announcement: " + e.getMessage());
            return false;
        }
    }

    public boolean togglePin(int id, boolean pinned) {
        String sql = "UPDATE announcements SET pinned = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, pinned);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error toggling announcement pin: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAnnouncement(int id, String authorUsername, Integer authorUserId) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(authorUserId, authorUsername, "PRINCIPAL",
                        "ANNOUNCEMENT_DELETED", "ANNOUNCEMENT", String.valueOf(id),
                        "Removed circular ID " + id, AuditLog.Severity.WARNING);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting announcement: " + e.getMessage());
            return false;
        }
    }

    private Announcement mapResultSetToAnnouncement(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setCategory(Announcement.Category.valueOf(rs.getString("category")));
        a.setTargetAudience(rs.getString("target_audience"));
        a.setPinned(rs.getBoolean("pinned"));
        int uid = rs.getInt("posted_by_user_id");
        if (!rs.wasNull()) {
            a.setPostedByUserId(uid);
            a.setPostedByUserName(rs.getString("posted_by_name"));
        }
        a.setPostedDate(rs.getDate("posted_date"));
        a.setExpiryDate(rs.getDate("expiry_date"));
        a.setAttachmentNote(rs.getString("attachment_note"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
