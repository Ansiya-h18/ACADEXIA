package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Mark;
import com.acadexia.model.ResultPublication;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultPublicationDAO {

    public List<ResultPublication> getAllPublications() {
        List<ResultPublication> list = new ArrayList<>();
        String sql = """
            SELECT rp.*,
                   c.name AS class_name,
                   s.code AS subject_code, s.name AS subject_name,
                   u_v.full_name AS verified_by_name,
                   u_p.full_name AS published_by_name
            FROM result_publications rp
            JOIN classes c ON rp.class_id = c.id
            JOIN subjects s ON rp.subject_id = s.id
            LEFT JOIN users u_v ON rp.verified_by_user_id = u_v.id
            LEFT JOIN users u_p ON rp.published_by_user_id = u_p.id
            ORDER BY rp.updated_at DESC, rp.id DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPublication(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all result publications: " + e.getMessage());
        }
        return list;
    }

    public ResultPublication getPublication(int classId, int subjectId, Mark.ExamType examType, String academicYear) {
        String sql = """
            SELECT rp.*,
                   c.name AS class_name,
                   s.code AS subject_code, s.name AS subject_name,
                   u_v.full_name AS verified_by_name,
                   u_p.full_name AS published_by_name
            FROM result_publications rp
            JOIN classes c ON rp.class_id = c.id
            JOIN subjects s ON rp.subject_id = s.id
            LEFT JOIN users u_v ON rp.verified_by_user_id = u_v.id
            LEFT JOIN users u_p ON rp.published_by_user_id = u_p.id
            WHERE rp.class_id = ? AND rp.subject_id = ? AND rp.exam_type = ? AND rp.academic_year = ?
            LIMIT 1
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, subjectId);
            ps.setString(3, examType.name());
            ps.setString(4, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPublication(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching publication status: " + e.getMessage());
        }
        return null;
    }

    public boolean recordOrUpdateStatus(int classId, int subjectId, Mark.ExamType examType,
                                        String academicYear, ResultPublication.PublicationStatus status,
                                        Integer userId, String remarks) {
        String sql = """
            INSERT INTO result_publications (class_id, subject_id, exam_type, academic_year, status, verified_by_user_id, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                status = VALUES(status),
                remarks = VALUES(remarks)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, subjectId);
            ps.setString(3, examType.name());
            ps.setString(4, academicYear);
            ps.setString(5, status.name());
            if (userId != null) {
                ps.setInt(6, userId);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording publication status: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyResults(int classId, int subjectId, Mark.ExamType examType, String academicYear, int verifiedByUserId) {
        String sql = """
            INSERT INTO result_publications (class_id, subject_id, exam_type, academic_year, status, verified_by_user_id, remarks)
            VALUES (?, ?, ?, ?, 'VERIFIED', ?, 'Verified by academic admin')
            ON DUPLICATE KEY UPDATE
                status = 'VERIFIED',
                verified_by_user_id = VALUES(verified_by_user_id),
                remarks = 'Verified by academic admin'
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, subjectId);
            ps.setString(3, examType.name());
            ps.setString(4, academicYear);
            ps.setInt(5, verifiedByUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error verifying marks results: " + e.getMessage());
            return false;
        }
    }

    public boolean publishResults(int classId, int subjectId, Mark.ExamType examType, String academicYear, int publishedByUserId) {
        String sql = """
            INSERT INTO result_publications (class_id, subject_id, exam_type, academic_year, status, published_by_user_id, published_at, remarks)
            VALUES (?, ?, ?, ?, 'PUBLISHED', ?, CURRENT_TIMESTAMP, 'Officially published for student & parent portals')
            ON DUPLICATE KEY UPDATE
                status = 'PUBLISHED',
                published_by_user_id = VALUES(published_by_user_id),
                published_at = CURRENT_TIMESTAMP,
                remarks = 'Officially published for student & parent portals'
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, subjectId);
            ps.setString(3, examType.name());
            ps.setString(4, academicYear);
            ps.setInt(5, publishedByUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error publishing results: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Integer> getPublicationStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("ENTERED", 0);
        stats.put("VERIFIED", 0);
        stats.put("PUBLISHED", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM result_publications GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching publication statistics: " + e.getMessage());
        }
        return stats;
    }

    private ResultPublication mapResultSetToPublication(ResultSet rs) throws SQLException {
        ResultPublication rp = new ResultPublication();
        rp.setId(rs.getInt("id"));
        rp.setClassId(rs.getInt("class_id"));
        rp.setClassName(rs.getString("class_name"));
        rp.setSubjectId(rs.getInt("subject_id"));
        rp.setSubjectCode(rs.getString("subject_code"));
        rp.setSubjectName(rs.getString("subject_name"));
        rp.setExamType(Mark.ExamType.valueOf(rs.getString("exam_type")));
        rp.setAcademicYear(rs.getString("academic_year"));
        rp.setStatus(ResultPublication.PublicationStatus.valueOf(rs.getString("status")));
        
        int vId = rs.getInt("verified_by_user_id");
        if (!rs.wasNull()) rp.setVerifiedByUserId(vId);
        rp.setVerifiedByName(rs.getString("verified_by_name"));

        int pId = rs.getInt("published_by_user_id");
        if (!rs.wasNull()) rp.setPublishedByUserId(pId);
        rp.setPublishedByName(rs.getString("published_by_name"));

        rp.setPublishedAt(rs.getTimestamp("published_at"));
        rp.setRemarks(rs.getString("remarks"));
        rp.setCreatedAt(rs.getTimestamp("created_at"));
        rp.setUpdatedAt(rs.getTimestamp("updated_at"));
        return rp;
    }
}
