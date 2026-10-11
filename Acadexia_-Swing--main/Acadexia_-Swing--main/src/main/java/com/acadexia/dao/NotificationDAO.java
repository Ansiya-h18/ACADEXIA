package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public List<Notification> getNotificationsForParent(Integer classId) {
        List<Notification> list = new ArrayList<>();
        String sql = """
            SELECT n.*,
                   u.full_name AS posted_by_name,
                   c.name AS target_class_name
            FROM notifications n
            LEFT JOIN users u ON n.posted_by_user_id = u.id
            LEFT JOIN classes c ON n.target_class_id = c.id
            WHERE n.is_active = TRUE
              AND n.target_audience IN ('ALL', 'PARENTS')
              AND (n.target_class_id IS NULL OR n.target_class_id = ?)
            ORDER BY n.publish_date DESC, n.created_at DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (classId != null) {
                ps.setInt(1, classId);
            } else {
                ps.setInt(1, -1);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications for parent: " + e.getMessage());
        }
        return list;
    }

    public List<Notification> getAllNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = """
            SELECT n.*,
                   u.full_name AS posted_by_name,
                   c.name AS target_class_name
            FROM notifications n
            LEFT JOIN users u ON n.posted_by_user_id = u.id
            LEFT JOIN classes c ON n.target_class_id = c.id
            ORDER BY n.publish_date DESC, n.created_at DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all notifications: " + e.getMessage());
        }
        return list;
    }

    public boolean createNotification(Notification n) {
        String sql = """
            INSERT INTO notifications (title, message, target_audience, target_class_id, priority, category, posted_by_user_id, publish_date, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getMessage());
            ps.setString(3, n.getTargetAudience());
            if (n.getTargetClassId() != null && n.getTargetClassId() > 0) {
                ps.setInt(4, n.getTargetClassId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, n.getPriority().name());
            ps.setString(6, n.getCategory().name());
            if (n.getPostedByUserId() != null) {
                ps.setInt(7, n.getPostedByUserId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setDate(8, n.getPublishDate());
            ps.setBoolean(9, n.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteNotification(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleNotificationStatus(int id, boolean active) {
        String sql = "UPDATE notifications SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error toggling notification status: " + e.getMessage());
            return false;
        }
    }


    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setTargetAudience(rs.getString("target_audience"));
        int cId = rs.getInt("target_class_id");
        if (!rs.wasNull()) {
            n.setTargetClassId(cId);
            n.setTargetClassName(rs.getString("target_class_name"));
        }
        n.setPriority(Notification.Priority.valueOf(rs.getString("priority")));
        n.setCategory(Notification.Category.valueOf(rs.getString("category")));
        int uId = rs.getInt("posted_by_user_id");
        if (!rs.wasNull()) {
            n.setPostedByUserId(uId);
            n.setPostedByUserName(rs.getString("posted_by_name"));
        }
        n.setPublishDate(rs.getDate("publish_date"));
        n.setActive(rs.getBoolean("is_active"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}
