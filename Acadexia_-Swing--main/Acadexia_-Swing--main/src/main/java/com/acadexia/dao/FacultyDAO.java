package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Faculty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {

    public Faculty getByUserId(int userId) {
        String sql = """
            SELECT f.*, u.full_name, u.email, u.phone, d.name AS department_name
            FROM faculty f
            JOIN users u ON f.user_id = u.id
            JOIN departments d ON f.department_id = d.id
            WHERE f.user_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Faculty f = mapResultSetToFaculty(rs);
                    enrichFacultyRoles(f, conn);
                    return f;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty by user ID: " + e.getMessage());
        }
        return null;
    }

    public Faculty getByFacultyId(String facultyId) {
        String sql = """
            SELECT f.*, u.full_name, u.email, u.phone, d.name AS department_name
            FROM faculty f
            JOIN users u ON f.user_id = u.id
            JOIN departments d ON f.department_id = d.id
            WHERE f.faculty_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, facultyId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Faculty f = mapResultSetToFaculty(rs);
                    enrichFacultyRoles(f, conn);
                    return f;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty by faculty ID: " + e.getMessage());
        }
        return null;
    }

    public List<Faculty> getAllFaculty() {
        List<Faculty> list = new ArrayList<>();
        String sql = """
            SELECT f.*, u.full_name, u.email, u.phone, d.name AS department_name
            FROM faculty f
            JOIN users u ON f.user_id = u.id
            JOIN departments d ON f.department_id = d.id
            ORDER BY u.full_name ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Faculty f = mapResultSetToFaculty(rs);
                enrichFacultyRoles(f, conn);
                list.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all faculty: " + e.getMessage());
        }
        return list;
    }

    public List<Faculty> getFacultyByDepartment(int departmentId) {
        List<Faculty> list = new ArrayList<>();
        String sql = """
            SELECT f.*, u.full_name, u.email, u.phone, d.name AS department_name
            FROM faculty f
            JOIN users u ON f.user_id = u.id
            JOIN departments d ON f.department_id = d.id
            WHERE f.department_id = ?
            ORDER BY u.full_name ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Faculty f = mapResultSetToFaculty(rs);
                    enrichFacultyRoles(f, conn);
                    list.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty by department: " + e.getMessage());
        }
        return list;
    }

    public int createFaculty(Faculty faculty) throws SQLException {
        String sql = """
            INSERT INTO faculty (user_id, faculty_id, designation, qualification, department_id, joining_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, faculty.getUserId());
            ps.setString(2, faculty.getFacultyId().toUpperCase().trim());
            ps.setString(3, faculty.getDesignation());
            ps.setString(4, faculty.getQualification());
            ps.setInt(5, faculty.getDepartmentId());
            ps.setDate(6, faculty.getJoiningDate());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    faculty.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Enriches the Faculty model dynamically with HOD status and CFA (Class Advisor) assignment.
     */
    private void enrichFacultyRoles(Faculty f, Connection conn) {
        // Check if HOD
        String hodSql = "SELECT COUNT(*) FROM departments WHERE hod_faculty_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(hodSql)) {
            ps.setInt(1, f.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    f.setHod(true);
                }
            }
        } catch (SQLException ignored) {}

        // Check if CFA for any class
        String cfaSql = "SELECT id, name, semester, section FROM classes WHERE advisor_faculty_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(cfaSql)) {
            ps.setInt(1, f.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    f.setCfa(true);
                    f.setAdvisedClassId(rs.getInt("id"));
                    f.setAdvisedClassName(rs.getString("name") + " S" + rs.getInt("semester") + " " + rs.getString("section"));
                }
            }
        } catch (SQLException ignored) {}
    }

    public Faculty getById(int id) {
        String sql = """
            SELECT f.*, u.full_name, u.email, u.phone, d.name AS department_name
            FROM faculty f
            JOIN users u ON f.user_id = u.id
            JOIN departments d ON f.department_id = d.id
            WHERE f.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Faculty f = mapResultSetToFaculty(rs);
                    enrichFacultyRoles(f, conn);
                    return f;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty by ID: " + e.getMessage());
        }
        return null;
    }

    public int getTotalFacultyCount() {
        String sql = "SELECT COUNT(*) FROM faculty";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error fetching total faculty count: " + e.getMessage());
        }
        return 0;
    }

    public String generateNextFacultyId(String deptCode) {
        String cleanDept = (deptCode != null && !deptCode.trim().isEmpty()) ? deptCode.trim().toUpperCase() : "CS";
        if (cleanDept.equals("CSE")) cleanDept = "CS";
        if (cleanDept.equals("MATH")) cleanDept = "MA";
        if (cleanDept.equals("ECE")) cleanDept = "EC";
        String prefix = "FAC-" + cleanDept + "-";
        String sql = "SELECT faculty_id FROM faculty WHERE faculty_id LIKE ? ORDER BY faculty_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("faculty_id");
                    if (lastId != null && lastId.length() >= prefix.length() + 3) {
                        String numPart = lastId.substring(prefix.length());
                        try {
                            int nextSeq = Integer.parseInt(numPart) + 1;
                            return prefix + String.format("%03d", nextSeq);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating next faculty ID: " + e.getMessage());
        }
        return prefix + "001";
    }

    public boolean updateFaculty(Faculty f) {
        String updateFacultySql = """
            UPDATE faculty 
            SET designation = ?, qualification = ?, department_id = ?, joining_date = ?
            WHERE id = ?
            """;
        String updateUserSql = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psFac = conn.prepareStatement(updateFacultySql);
                 PreparedStatement psUser = conn.prepareStatement(updateUserSql)) {
                psFac.setString(1, f.getDesignation());
                psFac.setString(2, f.getQualification());
                psFac.setInt(3, f.getDepartmentId());
                psFac.setDate(4, f.getJoiningDate());
                psFac.setInt(5, f.getId());
                psFac.executeUpdate();

                psUser.setString(1, f.getFullName().trim());
                psUser.setString(2, f.getEmail() != null ? f.getEmail().trim() : null);
                psUser.setString(3, f.getPhone() != null ? f.getPhone().trim() : null);
                psUser.setInt(4, f.getUserId());
                psUser.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error updating faculty: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFaculty(int facultyId) {
        String sql = "DELETE FROM users WHERE id = (SELECT user_id FROM faculty WHERE id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting faculty: " + e.getMessage());
            return false;
        }
    }

    private Faculty mapResultSetToFaculty(ResultSet rs) throws SQLException {
        Faculty f = new Faculty();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setFacultyId(rs.getString("faculty_id"));
        f.setDesignation(rs.getString("designation"));
        f.setQualification(rs.getString("qualification"));
        f.setDepartmentId(rs.getInt("department_id"));
        f.setJoiningDate(rs.getDate("joining_date"));
        f.setFullName(rs.getString("full_name"));
        f.setEmail(rs.getString("email"));
        f.setPhone(rs.getString("phone"));
        f.setDepartmentName(rs.getString("department_name"));
        return f;
    }
}
