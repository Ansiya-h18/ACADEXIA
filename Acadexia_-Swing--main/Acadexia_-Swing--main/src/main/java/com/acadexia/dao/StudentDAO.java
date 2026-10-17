package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public Student getByUserId(int userId) {
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, 
                   COALESCE(c.department_id, u.department_id) AS department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            LEFT JOIN classes c ON s.class_id = c.id
            LEFT JOIN departments d ON d.id = COALESCE(c.department_id, u.department_id)
            WHERE s.user_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by user ID: " + e.getMessage());
        }
        return null;
    }

    public Student getById(int id) {
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, 
                   COALESCE(c.department_id, u.department_id) AS department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            LEFT JOIN classes c ON s.class_id = c.id
            LEFT JOIN departments d ON d.id = COALESCE(c.department_id, u.department_id)
            WHERE s.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by ID: " + e.getMessage());
        }
        return null;
    }

    public Student getByRegisterNumber(String registerNumber) {
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, 
                   COALESCE(c.department_id, u.department_id) AS department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            LEFT JOIN classes c ON s.class_id = c.id
            LEFT JOIN departments d ON d.id = COALESCE(c.department_id, u.department_id)
            WHERE s.register_number = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registerNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by register number: " + e.getMessage());
        }
        return null;
    }

    public List<Student> getStudentsByClass(int classId) {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, 
                   COALESCE(c.department_id, u.department_id) AS department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            JOIN classes c ON s.class_id = c.id
            LEFT JOIN departments d ON d.id = COALESCE(c.department_id, u.department_id)
            WHERE s.class_id = ?
            ORDER BY CAST(s.roll_number AS UNSIGNED), s.register_number
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students by class: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getStudentsByDepartment(int departmentId) {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, 
                   COALESCE(c.department_id, u.department_id) AS department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            LEFT JOIN classes c ON s.class_id = c.id
            JOIN departments d ON d.id = COALESCE(c.department_id, u.department_id)
            WHERE d.id = ?
            ORDER BY c.semester, CAST(s.roll_number AS UNSIGNED)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students by department: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, 
                   COALESCE(c.department_id, u.department_id) AS department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            LEFT JOIN classes c ON s.class_id = c.id
            LEFT JOIN departments d ON d.id = COALESCE(c.department_id, u.department_id)
            ORDER BY d.name, c.semester, CAST(s.roll_number AS UNSIGNED)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = mapResultSetToStudent(rs);
                enrichStudentMetrics(s, conn);
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        return list;
    }

    public int createStudent(Student s) throws SQLException {
        String sql = """
            INSERT INTO students (user_id, register_number, roll_number, class_id, admission_year, guardian_name, guardian_phone)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getRegisterNumber().toUpperCase().trim());
            ps.setString(3, s.getRollNumber().trim());
            ps.setInt(4, s.getClassId());
            ps.setInt(5, s.getAdmissionYear());
            ps.setString(6, s.getGuardianName());
            ps.setString(7, s.getGuardianPhone());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    s.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public int getTotalStudentCount() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error fetching total student count: " + e.getMessage());
        }
        return 0;
    }


    public String generateNextRegisterNumber(int admissionYear, String deptCode) {
        String cleanDept = (deptCode != null && !deptCode.trim().isEmpty()) ? deptCode.trim().toUpperCase() : "CS";
        if (cleanDept.equals("CSE")) cleanDept = "CS";
        String prefix = "REG" + admissionYear + cleanDept;
        String sql = "SELECT register_number FROM students WHERE register_number LIKE ? ORDER BY LENGTH(register_number) DESC, register_number DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastReg = rs.getString("register_number");
                    if (lastReg != null && lastReg.length() >= prefix.length() + 3) {
                        String numPart = lastReg.substring(prefix.length());
                        try {
                            int nextSeq = Integer.parseInt(numPart) + 1;
                            return prefix + String.format("%03d", nextSeq);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next register number: " + e.getMessage());
        }
        return prefix + "001";
    }

    public String getNextRollNumber(int classId) {
        String sql = "SELECT MAX(CAST(roll_number AS UNSIGNED)) FROM students WHERE class_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxRoll = rs.getInt(1);
                    return String.format("%02d", maxRoll + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching next roll number: " + e.getMessage());
        }
        return "01";
    }

    public boolean updateStudent(Student s) {
        String updateStudentSql = """
            UPDATE students 
            SET roll_number = ?, class_id = ?, admission_year = ?, guardian_name = ?, guardian_phone = ?
            WHERE id = ?
            """;
        String updateUserSql = "UPDATE users SET full_name = ?, email = ?, phone = ?, department_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psStu = conn.prepareStatement(updateStudentSql);
                 PreparedStatement psUser = conn.prepareStatement(updateUserSql)) {
                psStu.setString(1, s.getRollNumber().trim());
                psStu.setInt(2, s.getClassId());
                psStu.setInt(3, s.getAdmissionYear());
                psStu.setString(4, s.getGuardianName());
                psStu.setString(5, s.getGuardianPhone());
                psStu.setInt(6, s.getId());
                psStu.executeUpdate();

                psUser.setString(1, s.getFullName().trim());
                psUser.setString(2, s.getEmail() != null ? s.getEmail().trim() : null);
                psUser.setString(3, s.getPhone() != null ? s.getPhone().trim() : null);
                if (s.getDepartmentId() > 0) {
                    psUser.setInt(4, s.getDepartmentId());
                } else {
                    psUser.setNull(4, Types.INTEGER);
                }
                psUser.setInt(5, s.getUserId());
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
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM users WHERE id = (SELECT user_id FROM students WHERE id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }


    /**
     * Dynamically calculates the student's attendance percentage and SGPA/CGPA.
     */
    private void enrichStudentMetrics(Student s, Connection conn) {
        // Attendance calculation
        String attSql = """
            SELECT 
                COUNT(*) AS total_hours,
                SUM(CASE WHEN status IN ('PRESENT', 'DUTY_LEAVE') THEN 1 ELSE 0 END) AS attended_hours
            FROM attendance
            WHERE student_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(attSql)) {
            ps.setInt(1, s.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_hours");
                    int attended = rs.getInt("attended_hours");
                    s.setAttendancePercentage(total > 0 ? (attended * 100.0 / total) : 100.0);
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
            ps.setInt(1, s.getId());
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
                s.setCurrentSgpa(Math.round(sgpa * 100.0) / 100.0);
                s.setCurrentCgpa(s.getCurrentSgpa()); // Baseline CGPA
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

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setRegisterNumber(rs.getString("register_number"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setClassId(rs.getInt("class_id"));
        s.setAdmissionYear(rs.getInt("admission_year"));
        s.setGuardianName(rs.getString("guardian_name"));
        s.setGuardianPhone(rs.getString("guardian_phone"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setClassName(rs.getString("class_name") != null ? rs.getString("class_name") : "Assigned Class");
        s.setSemester(rs.getInt("semester"));
        s.setDepartmentId(rs.getInt("department_id"));
        s.setDepartmentName(rs.getString("department_name") != null ? rs.getString("department_name") : "-");
        return s;
    }
}
