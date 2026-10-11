package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = """
            SELECT d.*, u.full_name AS hod_name
            FROM departments d
            LEFT JOIN faculty f ON d.hod_faculty_id = f.id
            LEFT JOIN users u ON f.user_id = u.id
            ORDER BY d.name ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Department d = new Department();
                d.setId(rs.getInt("id"));
                d.setCode(rs.getString("code"));
                d.setName(rs.getString("name"));
                int hodId = rs.getInt("hod_faculty_id");
                if (!rs.wasNull()) {
                    d.setHodFacultyId(hodId);
                }
                d.setHodName(rs.getString("hod_name"));
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching departments: " + e.getMessage());
        }
        return list;
    }

    public Department getById(int id) {
        String sql = """
            SELECT d.*, u.full_name AS hod_name
            FROM departments d
            LEFT JOIN faculty f ON d.hod_faculty_id = f.id
            LEFT JOIN users u ON f.user_id = u.id
            WHERE d.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Department d = new Department();
                    d.setId(rs.getInt("id"));
                    d.setCode(rs.getString("code"));
                    d.setName(rs.getString("name"));
                    int hodId = rs.getInt("hod_faculty_id");
                    if (!rs.wasNull()) d.setHodFacultyId(hodId);
                    d.setHodName(rs.getString("hod_name"));
                    return d;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching department: " + e.getMessage());
        }
        return null;
    }

    public boolean createDepartment(String code, String name) {
        String sql = "INSERT INTO departments (code, name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.toUpperCase().trim());
            ps.setString(2, name.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating department: " + e.getMessage());
            return false;
        }
    }

    public boolean updateHod(int departmentId, Integer hodFacultyId) {
        String sql = "UPDATE departments SET hod_faculty_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hodFacultyId != null) {
                ps.setInt(1, hodFacultyId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, departmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating HOD: " + e.getMessage());
            return false;
        }
    }

    public record DepartmentStat(int id, String code, String name, String hodName, int facultyCount, int studentCount, int classCount) {}

    public List<DepartmentStat> getDepartmentStats() {
        List<DepartmentStat> list = new ArrayList<>();
        String sql = """
            SELECT d.id, d.code, d.name, u.full_name AS hod_name,
                   (SELECT COUNT(*) FROM faculty f WHERE f.department_id = d.id) AS fac_count,
                   (SELECT COUNT(*) FROM students s JOIN classes c ON s.class_id = c.id WHERE c.department_id = d.id) AS stu_count,
                   (SELECT COUNT(*) FROM classes c WHERE c.department_id = d.id) AS class_count
            FROM departments d
            LEFT JOIN faculty f_hod ON d.hod_faculty_id = f_hod.id
            LEFT JOIN users u ON f_hod.user_id = u.id
            ORDER BY d.name ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DepartmentStat(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("hod_name") != null ? rs.getString("hod_name") : "Not Appointed",
                        rs.getInt("fac_count"),
                        rs.getInt("stu_count"),
                        rs.getInt("class_count")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching department stats: " + e.getMessage());
        }
        return list;
    }

    public int getTotalDepartmentCount() {
        String sql = "SELECT COUNT(*) FROM departments";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error fetching total department count: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateDepartment(int id, String code, String name, Integer hodFacultyId) {
        String sql = "UPDATE departments SET code = ?, name = ?, hod_faculty_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.toUpperCase().trim());
            ps.setString(2, name.trim());
            if (hodFacultyId != null && hodFacultyId > 0) {
                ps.setInt(3, hodFacultyId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating department: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDepartment(int id) {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting department: " + e.getMessage());
            return false;
        }
    }
}
