package com.acadexia.config;

import com.acadexia.dao.*;
import com.acadexia.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Automatically seeds the database with initial institutional demo data
 * (Principal, Admin, HODs, CFA, Subject Faculty, Students, Classes, Subjects, and Cross-Department assignments)
 * if the users table is currently empty.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {}

    public static void seedIfEmpty() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int userCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) userCount = rs.getInt(1);
            }

            if (userCount > 0) {
                seedParentsAndExtendedModulesIfEmpty(conn);
                seedAnnouncementsIfEmpty(conn);
                seedAcademicCalendarIfEmpty(conn);
                return; // Database already seeded
            }

            System.out.println("🌱 Initializing Acadexia starter database records...");

            UserDAO userDAO = new UserDAO();
            DepartmentDAO deptDAO = new DepartmentDAO();
            FacultyDAO facDAO = new FacultyDAO();

            // 1. Create Departments
            deptDAO.createDepartment("CSE", "Computer Science & Engineering");
            deptDAO.createDepartment("MATH", "Mathematics & Basic Sciences");
            deptDAO.createDepartment("ECE", "Electronics & Communication Engineering");

            Department cseDept = deptDAO.getAllDepartments().stream().filter(d -> d.getCode().equals("CSE")).findFirst().orElse(null);
            Department mathDept = deptDAO.getAllDepartments().stream().filter(d -> d.getCode().equals("MATH")).findFirst().orElse(null);

            int cseDeptId = cseDept != null ? cseDept.getId() : 1;
            int mathDeptId = mathDept != null ? mathDept.getId() : 2;

            // 2. Create Principal
            User principalUser = new User();
            principalUser.setUsername("principal");
            principalUser.setFullName("Dr. K. S. Menon");
            principalUser.setEmail("principal@acadexia.edu");
            principalUser.setPhone("9876543210");
            principalUser.setRole(Role.PRINCIPAL);
            principalUser.setActive(true);
            userDAO.createUser(principalUser, "principal123");

            // 3. Create System Admin
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setFullName("IT Systems Administrator");
            adminUser.setEmail("admin@acadexia.edu");
            adminUser.setPhone("9876543211");
            adminUser.setRole(Role.ADMIN);
            adminUser.setActive(true);
            userDAO.createUser(adminUser, "admin123");

            // 4. Create Faculty 1: CSE HOD (Dr. Rajesh Sharma)
            User fac1User = new User();
            fac1User.setUsername("rajesh.cse");
            fac1User.setFullName("Dr. Rajesh Sharma");
            fac1User.setEmail("rajesh.cse@acadexia.edu");
            fac1User.setPhone("9876543212");
            fac1User.setDepartmentId(cseDeptId);
            fac1User.setRole(Role.FACULTY);
            fac1User.setActive(true);
            int fac1UserId = userDAO.createUser(fac1User, "faculty123");

            Faculty fac1 = new Faculty();
            fac1.setUserId(fac1UserId);
            fac1.setFacultyId("FAC-CS-001");
            fac1.setDesignation("Professor & HOD");
            fac1.setQualification("Ph.D. in Computer Science");
            fac1.setDepartmentId(cseDeptId);
            fac1.setJoiningDate(Date.valueOf("2015-06-15"));
            int fac1Id = facDAO.createFaculty(fac1);
            deptDAO.updateHod(cseDeptId, fac1Id);

            // 5. Create Faculty 2: Math HOD (Dr. Sarah Varghese) - Teaches cross-departmentally!
            User fac2User = new User();
            fac2User.setUsername("sarah.math");
            fac2User.setFullName("Dr. Sarah Varghese");
            fac2User.setEmail("sarah.math@acadexia.edu");
            fac2User.setPhone("9876543213");
            fac2User.setDepartmentId(mathDeptId);
            fac2User.setRole(Role.FACULTY);
            fac2User.setActive(true);
            int fac2UserId = userDAO.createUser(fac2User, "faculty123");

            Faculty fac2 = new Faculty();
            fac2.setUserId(fac2UserId);
            fac2.setFacultyId("FAC-MA-001");
            fac2.setDesignation("Professor & HOD (Math)");
            fac2.setQualification("Ph.D. in Applied Mathematics");
            fac2.setDepartmentId(mathDeptId);
            fac2.setJoiningDate(Date.valueOf("2016-08-01"));
            int fac2Id = facDAO.createFaculty(fac2);
            deptDAO.updateHod(mathDeptId, fac2Id);

            // 6. Create Faculty 3: Class Advisor / CFA for S5 CSE A (Prof. Arun Nair)
            User fac3User = new User();
            fac3User.setUsername("arun.cse");
            fac3User.setFullName("Prof. Arun Nair");
            fac3User.setEmail("arun.cse@acadexia.edu");
            fac3User.setPhone("9876543214");
            fac3User.setDepartmentId(cseDeptId);
            fac3User.setRole(Role.FACULTY);
            fac3User.setActive(true);
            int fac3UserId = userDAO.createUser(fac3User, "faculty123");

            Faculty fac3 = new Faculty();
            fac3.setUserId(fac3UserId);
            fac3.setFacultyId("FAC-CS-002");
            fac3.setDesignation("Assistant Professor & CFA");
            fac3.setQualification("M.Tech in Software Engineering");
            fac3.setDepartmentId(cseDeptId);
            fac3.setJoiningDate(Date.valueOf("2019-07-10"));
            int fac3Id = facDAO.createFaculty(fac3);

            // 7. Create Faculty 4: Subject Faculty (Prof. Priya Menon)
            User fac4User = new User();
            fac4User.setUsername("priya.cse");
            fac4User.setFullName("Prof. Priya Menon");
            fac4User.setEmail("priya.cse@acadexia.edu");
            fac4User.setPhone("9876543215");
            fac4User.setDepartmentId(cseDeptId);
            fac4User.setRole(Role.FACULTY);
            fac4User.setActive(true);
            int fac4UserId = userDAO.createUser(fac4User, "faculty123");

            Faculty fac4 = new Faculty();
            fac4.setUserId(fac4UserId);
            fac4.setFacultyId("FAC-CS-003");
            fac4.setDesignation("Assistant Professor");
            fac4.setQualification("M.Tech in Cybersecurity");
            fac4.setDepartmentId(cseDeptId);
            fac4.setJoiningDate(Date.valueOf("2021-01-18"));
            int fac4Id = facDAO.createFaculty(fac4);

            // 8. Create Class Batch: S5 CSE A
            int classId = 1;
            String insertClassSql = """
                INSERT INTO classes (name, department_id, semester, academic_year, section, advisor_faculty_id)
                VALUES ('S5 CSE', ?, 5, '2024-2025', 'A', ?)
                """;
            try (PreparedStatement ps = conn.prepareStatement(insertClassSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cseDeptId);
                ps.setInt(2, fac3Id); // Advised by Prof. Arun Nair
                ps.executeUpdate();
                try (ResultSet crs = ps.getGeneratedKeys()) {
                    if (crs.next()) classId = crs.getInt(1);
                }
            }

            // 9. Create Students
            createStudentHelper(conn, userDAO, "rohan.das", "Rohan Das", "REG2024CS001", "01", classId, 2022, "Mr. S. Das", "9811122233");
            createStudentHelper(conn, userDAO, "ananya.sen", "Ananya Sen", "REG2024CS002", "02", classId, 2022, "Mrs. R. Sen", "9811122234");
            createStudentHelper(conn, userDAO, "kevin.paul", "Kevin Paul", "REG2024CS003", "03", classId, 2022, "Mr. J. Paul", "9811122235");

            // 10. Create Subjects (Semester 4 and Semester 5)
            int subSem4_1 = createSubjectHelper(conn, "CS401", "Data Structures & Algorithms", cseDeptId, 4, 4);
            int subSem4_2 = createSubjectHelper(conn, "MA201", "Linear Algebra & Complex Analysis", mathDeptId, 4, 4);
            int sub1Id = createSubjectHelper(conn, "CS501", "Design & Analysis of Algorithms", cseDeptId, 5, 4);
            int sub2Id = createSubjectHelper(conn, "CS502", "Database Management Systems", cseDeptId, 5, 4);
            int sub3Id = createSubjectHelper(conn, "MA301", "Discrete Mathematical Structures", mathDeptId, 5, 4); // Math Dept!
            int sub4Id = createSubjectHelper(conn, "CS503", "Operating System Concepts", cseDeptId, 5, 3);

            // 11. Assign Teachers to Subjects for S5 CSE A (demonstrating cross-department assignment)
            assignTeacherHelper(conn, sub1Id, fac1Id, classId, "2024-2025"); // Algorithms taught by CSE HOD
            assignTeacherHelper(conn, sub2Id, fac3Id, classId, "2024-2025"); // DBMS taught by CFA
            assignTeacherHelper(conn, sub3Id, fac2Id, classId, "2024-2025"); // Discrete Math taught by Math HOD!
            assignTeacherHelper(conn, sub4Id, fac4Id, classId, "2024-2025"); // OS taught by Subject Faculty

            // 12. Seed 7-Period Timetable for S5 CSE A (Monday to Friday)
            seedFullClassTimetable(conn, classId, fac1Id, fac2Id, fac3Id, fac4Id, sub1Id, sub2Id, sub3Id, sub4Id);

            // 13. Seed Starter Marks & Attendance
            seedInitialMarksAndAttendance(conn, classId, fac1Id, subSem4_1, subSem4_2, sub1Id, sub2Id, sub3Id, sub4Id);

            // 14. Seed Sample Duty Leave Application (at Tier 1 awaiting CFA recommendation)
            seedSampleDutyLeave(conn, 1);

            // 15. Seed Parent Demo Accounts and Extended Modules
            seedParentsAndExtendedModulesIfEmpty(conn);
            seedAnnouncementsIfEmpty(conn);
            seedAcademicCalendarIfEmpty(conn);

            AuditLogDAO.log(adminUser.getId(), "SYSTEM", "ADMIN", "DATABASE_SEEDED", "SYSTEM", null,
                    "Default college hierarchy, departments, faculty, and sample batch seeded successfully.", AuditLog.Severity.INFO);

            System.out.println("✅ Acadexia starter database successfully seeded!");

        } catch (Exception e) {
            System.err.println("Database seeding error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void createStudentHelper(Connection conn, UserDAO userDAO, String username, String fullName,
                                            String registerNo, String rollNo, int classId, int admYear,
                                            String gName, String gPhone) throws SQLException {
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(username + "@acadexia.edu");
        u.setPhone("98000" + rollNo + "123");
        u.setRole(Role.STUDENT);
        u.setActive(true);
        int uid = userDAO.createUser(u, "student123");

        String sql = """
            INSERT INTO students (user_id, register_number, roll_number, class_id, admission_year, guardian_name, guardian_phone)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uid);
            ps.setString(2, registerNo);
            ps.setString(3, rollNo);
            ps.setInt(4, classId);
            ps.setInt(5, admYear);
            ps.setString(6, gName);
            ps.setString(7, gPhone);
            ps.executeUpdate();
        }
    }

    private static int createSubjectHelper(Connection conn, String code, String name, int deptId, int sem, int credits) throws SQLException {
        String sql = "INSERT INTO subjects (code, name, department_id, semester, credits) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setInt(3, deptId);
            ps.setInt(4, sem);
            ps.setInt(5, credits);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static void assignTeacherHelper(Connection conn, int subjectId, int facultyId, int classId, String yr) throws SQLException {
        String sql = "INSERT INTO subject_assignments (subject_id, faculty_id, class_id, academic_year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, facultyId);
            ps.setInt(3, classId);
            ps.setString(4, yr);
            ps.executeUpdate();
        }
    }

    private static void seedTimetable(Connection conn, int classId, int facultyId, int subjectId, String day, int period, String room) throws SQLException {
        String sql = """
            INSERT INTO timetable (class_id, faculty_id, subject_id, day_of_week, period_slot, room_number) 
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE faculty_id = VALUES(faculty_id), subject_id = VALUES(subject_id), room_number = VALUES(room_number)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, facultyId);
            ps.setInt(3, subjectId);
            ps.setString(4, day);
            ps.setInt(5, period);
            ps.setString(6, room);
            ps.executeUpdate();
        }
    }

    private static void seedFullClassTimetable(Connection conn, int classId, int fac1, int fac2, int fac3, int fac4,
                                               int sub1, int sub2, int sub3, int sub4) throws SQLException {
        // Monday (7 periods)
        seedTimetable(conn, classId, fac1, sub1, "MONDAY", 1, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "MONDAY", 2, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "MONDAY", 3, "LH-301");
        seedTimetable(conn, classId, fac4, sub4, "MONDAY", 4, "LH-301");
        seedTimetable(conn, classId, fac1, sub1, "MONDAY", 5, "LAB-1");
        seedTimetable(conn, classId, fac3, sub2, "MONDAY", 6, "LAB-1");
        seedTimetable(conn, classId, fac4, sub4, "MONDAY", 7, "LH-301");

        // Tuesday (7 periods)
        seedTimetable(conn, classId, fac3, sub2, "TUESDAY", 1, "LH-301");
        seedTimetable(conn, classId, fac1, sub1, "TUESDAY", 2, "LH-301");
        seedTimetable(conn, classId, fac4, sub4, "TUESDAY", 3, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "TUESDAY", 4, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "TUESDAY", 5, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "TUESDAY", 6, "LH-301");
        seedTimetable(conn, classId, fac1, sub1, "TUESDAY", 7, "LH-301");

        // Wednesday (7 periods)
        seedTimetable(conn, classId, fac2, sub3, "WEDNESDAY", 1, "LH-301");
        seedTimetable(conn, classId, fac4, sub4, "WEDNESDAY", 2, "LH-301");
        seedTimetable(conn, classId, fac1, sub1, "WEDNESDAY", 3, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "WEDNESDAY", 4, "LH-301");
        seedTimetable(conn, classId, fac4, sub4, "WEDNESDAY", 5, "LAB-2");
        seedTimetable(conn, classId, fac1, sub1, "WEDNESDAY", 6, "LAB-2");
        seedTimetable(conn, classId, fac2, sub3, "WEDNESDAY", 7, "LH-301");

        // Thursday (7 periods)
        seedTimetable(conn, classId, fac4, sub4, "THURSDAY", 1, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "THURSDAY", 2, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "THURSDAY", 3, "LH-301");
        seedTimetable(conn, classId, fac1, sub1, "THURSDAY", 4, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "THURSDAY", 5, "LH-301");
        seedTimetable(conn, classId, fac4, sub4, "THURSDAY", 6, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "THURSDAY", 7, "LH-301");

        // Friday (7 periods)
        seedTimetable(conn, classId, fac1, sub1, "FRIDAY", 1, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "FRIDAY", 2, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "FRIDAY", 3, "LH-301");
        seedTimetable(conn, classId, fac4, sub4, "FRIDAY", 4, "LH-301");
        seedTimetable(conn, classId, fac1, sub1, "FRIDAY", 5, "LH-301");
        seedTimetable(conn, classId, fac3, sub2, "FRIDAY", 6, "LH-301");
        seedTimetable(conn, classId, fac2, sub3, "FRIDAY", 7, "LH-301");
    }

    private static void seedInitialMarksAndAttendance(Connection conn, int classId, int fac1Id, int subSem4_1, int subSem4_2, int s1, int s2, int s3, int s4) {
        try {
            // Attendance for past 5 days
            String[] dates = {"2026-09-28", "2026-09-29", "2026-09-30", "2026-10-01", "2026-10-02"};
            String attSql = "INSERT IGNORE INTO attendance (student_id, subject_id, class_id, date, hour, status, marked_by_faculty_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(attSql)) {
                for (int stuId = 1; stuId <= 3; stuId++) {
                    for (String d : dates) {
                        for (int h = 1; h <= 3; h++) {
                            ps.setInt(1, stuId);
                            ps.setInt(2, s1);
                            ps.setInt(3, classId);
                            ps.setDate(4, Date.valueOf(d));
                            ps.setInt(5, h);
                            ps.setString(6, (stuId == 3 && h == 2) ? "ABSENT" : "PRESENT");
                            ps.setInt(7, fac1Id);
                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();
            }

            // Series 1 Marks
            String markSql = "INSERT IGNORE INTO marks (student_id, subject_id, exam_type, marks_obtained, max_marks, exam_date, recorded_by_faculty_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(markSql)) {
                // Rohan - Sem 4
                ps.setInt(1, 1); ps.setInt(2, subSem4_1); ps.setString(3, "INTERNAL"); ps.setDouble(4, 45.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-05-15")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, subSem4_2); ps.setString(3, "INTERNAL"); ps.setDouble(4, 48.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-05-15")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, subSem4_1); ps.setString(3, "SEMESTER_EXAM"); ps.setDouble(4, 88.0); ps.setDouble(5, 100.0); ps.setDate(6, Date.valueOf("2026-06-10")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, subSem4_2); ps.setString(3, "SEMESTER_EXAM"); ps.setDouble(4, 92.0); ps.setDouble(5, 100.0); ps.setDate(6, Date.valueOf("2026-06-12")); ps.setInt(7, fac1Id); ps.addBatch();

                // Rohan - Sem 5
                ps.setInt(1, 1); ps.setInt(2, s1); ps.setString(3, "SERIES_1"); ps.setDouble(4, 46.5); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-20")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s2); ps.setString(3, "SERIES_1"); ps.setDouble(4, 44.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-21")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s3); ps.setString(3, "SERIES_1"); ps.setDouble(4, 48.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-22")); ps.setInt(7, fac1Id); ps.addBatch();
                // Consolidated Internals
                ps.setInt(1, 1); ps.setInt(2, s1); ps.setString(3, "INTERNAL"); ps.setDouble(4, 47.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s2); ps.setString(3, "INTERNAL"); ps.setDouble(4, 45.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s3); ps.setString(3, "INTERNAL"); ps.setDouble(4, 49.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();

                // Ananya
                ps.setInt(1, 2); ps.setInt(2, s1); ps.setString(3, "INTERNAL"); ps.setDouble(4, 42.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 2); ps.setInt(2, s2); ps.setString(3, "INTERNAL"); ps.setDouble(4, 43.5); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 2); ps.setInt(2, s3); ps.setString(3, "INTERNAL"); ps.setDouble(4, 46.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();

                ps.executeBatch();
            }

            // Seed an assignment
            String assignSql = """
                INSERT IGNORE INTO assignments (id, title, description, subject_id, class_id, faculty_id, due_date, max_marks)
                VALUES (1, 'Dynamic Programming Problem Set', 'Implement Knapsack, Matrix Chain Multiplication, and LCS in Java', ?, ?, ?, '2026-10-15', 10.0)
                """;
            try (PreparedStatement ps = conn.prepareStatement(assignSql)) {
                ps.setInt(1, s1);
                ps.setInt(2, classId);
                ps.setInt(3, fac1Id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            System.err.println("Seed marks error: " + e.getMessage());
        }
    }

    private static void seedSampleDutyLeave(Connection conn, int studentId) {
        String sql = """
            INSERT IGNORE INTO duty_leaves (id, student_id, reason, start_date, end_date, total_days, proof_details, status)
            VALUES (1, ?, 'National Level Hackathon Participation at IIT Madras', '2026-10-10', '2026-10-12', 3, 'Official Invitation Letter Ref: IITM/HACK/2026/410', 'PENDING_ADVISOR')
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public static void seedParentsAndExtendedModulesIfEmpty(Connection conn) {
        try {
            int parentCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM parents")) {
                if (rs.next()) parentCount = rs.getInt(1);
            } catch (SQLException e) {
                // Table might not exist yet if script hasn't run
                return;
            }

            if (parentCount > 0) {
                return;
            }

            System.out.println("🌱 Seeding Parent portal demo records & extended modules...");
            ParentDAO parentDAO = new ParentDAO();
            StudentDAO studentDAO = new StudentDAO();

            // Find Rohan Das
            Student rohan = studentDAO.getByRegisterNumber("REG2024CS001");
            int rohanId = rohan != null ? rohan.getId() : 1;
            int classId = rohan != null ? rohan.getClassId() : 1;

            // 1. Seed Parent 1: Mr. Suresh Das (Father of Rohan Das)
            Parent p1 = new Parent();
            p1.setUsername("parent.rohan");
            p1.setFullName("Mr. Suresh Das");
            p1.setEmail("suresh.das@gmail.com");
            p1.setPhone("9811122233");
            p1.setStudentId(rohanId);
            p1.setRelationship("Father");
            p1.setOccupation("Chief Civil Engineer, PWD");
            p1.setAddress("402, Palm Crest Residences, MG Road");
            p1.setEmergencyContact("+91 98111 22233");
            p1.setVerificationStatus(Parent.VerificationStatus.VERIFIED);
            p1.setVerifiedByUserId(2); // Verified by Admin
            p1.setVerificationNotes("Physical verification completed with Aadhaar and Student Birth Certificate.");
            p1.setActive(true);
            parentDAO.createParent(p1, "parent123");

            // 2. Seed Parent 2: Mrs. Rekha Sen (Mother of Ananya Sen)
            Student ananya = studentDAO.getByRegisterNumber("REG2024CS002");
            int ananyaId = ananya != null ? ananya.getId() : 2;
            Parent p2 = new Parent();
            p2.setUsername("parent.ananya");
            p2.setFullName("Mrs. Rekha Sen");
            p2.setEmail("rekha.sen@gmail.com");
            p2.setPhone("9811122234");
            p2.setStudentId(ananyaId);
            p2.setRelationship("Mother");
            p2.setOccupation("Senior Software Architect, Infosys");
            p2.setAddress("Villa 12, Green Meadows, Infopark Road");
            p2.setEmergencyContact("+91 98111 22234");
            p2.setVerificationStatus(Parent.VerificationStatus.VERIFIED);
            p2.setVerifiedByUserId(2);
            p2.setVerificationNotes("Verified by Academic Dean via online portal submission.");
            p2.setActive(true);
            parentDAO.createParent(p2, "parent123");

            // 3. Seed Daily Classwork
            DailyClassworkDAO classworkDAO = new DailyClassworkDAO();
            seedClassworkHelper(conn, classworkDAO, classId);

            // 4. Seed Faculty Mentoring Remarks for Rohan
            FacultyRemarkDAO remarkDAO = new FacultyRemarkDAO();
            seedFacultyRemarksHelper(conn, remarkDAO, rohanId);

            // 5. Seed Exam Schedules
            ExamScheduleDAO examDAO = new ExamScheduleDAO();
            seedExamSchedulesHelper(conn, examDAO, classId);

            // 6. Seed Notifications
            NotificationDAO notifDAO = new NotificationDAO();
            seedNotificationsHelper(notifDAO, classId);

            System.out.println("✅ Parent portal demo accounts and modules successfully seeded!");

        } catch (Exception e) {
            System.err.println("Notice seeding parent demo records: " + e.getMessage());
        }
    }

    private static void seedClassworkHelper(Connection conn, DailyClassworkDAO dao, int classId) {
        try {
            List<Integer> subIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM subjects ORDER BY id ASC")) {
                while (rs.next()) subIds.add(rs.getInt(1));
            }
            if (subIds.isEmpty()) return;

            List<Integer> facIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM faculty ORDER BY id ASC")) {
                while (rs.next()) facIds.add(rs.getInt(1));
            }
            if (facIds.isEmpty()) return;

            int s1 = subIds.get(0);
            int s2 = subIds.size() > 1 ? subIds.get(1) : s1;
            int s3 = subIds.size() > 2 ? subIds.get(2) : s1;
            int f1 = facIds.get(0);
            int f2 = facIds.size() > 1 ? facIds.get(1) : f1;

            DailyClasswork cw1 = new DailyClasswork();
            cw1.setClassId(classId);
            cw1.setSubjectId(s1);
            cw1.setFacultyId(f1);
            cw1.setDate(Date.valueOf("2026-10-06"));
            cw1.setPeriodHour(1);
            cw1.setTopicCovered("Dynamic Programming: Matrix Chain Multiplication & optimal parenthesization subproblem formulation");
            cw1.setHomeworkAssigned("Solve exercise problems 15.2-1 to 15.2-4 from CLRS algorithm textbook.");
            cw1.setReferenceMaterials("Lecture Notes Chapter 15; CLRS Slide Deck pp. 12-25.");
            cw1.setStatus(DailyClasswork.Status.COMPLETED);
            dao.createClasswork(cw1);

            DailyClasswork cw2 = new DailyClasswork();
            cw2.setClassId(classId);
            cw2.setSubjectId(s2);
            cw2.setFacultyId(f2);
            cw2.setDate(Date.valueOf("2026-10-06"));
            cw2.setPeriodHour(2);
            cw2.setTopicCovered("Graph Theory: Planar graphs, Euler's formula V - E + R = 2, and Kuratowski's theorem");
            cw2.setHomeworkAssigned("Prove non-planarity of complete graph K5 and bipartite graph K3,3.");
            cw2.setReferenceMaterials("Rosen Discrete Mathematics Chapter 10.7.");
            cw2.setStatus(DailyClasswork.Status.COMPLETED);
            dao.createClasswork(cw2);

            DailyClasswork cw3 = new DailyClasswork();
            cw3.setClassId(classId);
            cw3.setSubjectId(s1);
            cw3.setFacultyId(f1);
            cw3.setDate(Date.valueOf("2026-10-07"));
            cw3.setPeriodHour(3);
            cw3.setTopicCovered("Database Indexing: B+ Tree search operations, node structure, and order properties");
            cw3.setHomeworkAssigned("Draw step-by-step tree structure when keys 15, 25, 35, 45, 55 are inserted into empty B+ tree with order p=3.");
            cw3.setReferenceMaterials("Silberschatz Database System Concepts Chapter 14.");
            cw3.setStatus(DailyClasswork.Status.COMPLETED);
            dao.createClasswork(cw3);

            DailyClasswork cw4 = new DailyClasswork();
            cw4.setClassId(classId);
            cw4.setSubjectId(s3);
            cw4.setFacultyId(f2);
            cw4.setDate(Date.valueOf("2026-10-08"));
            cw4.setPeriodHour(1);
            cw4.setTopicCovered("Virtual Memory: Demand Paging, page fault service pipeline, and effective access time (EAT)");
            cw4.setHomeworkAssigned("Calculate EAT given memory access time 100ns and page fault overhead 10ms with 0.1% fault probability.");
            cw4.setReferenceMaterials("Galvin Operating System Concepts Chapter 10.");
            cw4.setStatus(DailyClasswork.Status.COMPLETED);
            dao.createClasswork(cw4);

            DailyClasswork cw5 = new DailyClasswork();
            cw5.setClassId(classId);
            cw5.setSubjectId(s1);
            cw5.setFacultyId(f1);
            cw5.setDate(Date.valueOf("2026-10-09"));
            cw5.setPeriodHour(1);
            cw5.setTopicCovered("Greedy Strategy vs DP: Optimal subproblems, 0-1 Knapsack vs Fractional Knapsack proof");
            cw5.setHomeworkAssigned("Implement greedy fractional knapsack algorithm in Java and test on edge testcases.");
            cw5.setReferenceMaterials("Lecture Notes Chapter 16; GitHub starter repo.");
            cw5.setStatus(DailyClasswork.Status.COMPLETED);
            dao.createClasswork(cw5);

            DailyClasswork cw6 = new DailyClasswork();
            cw6.setClassId(classId);
            cw6.setSubjectId(s2);
            cw6.setFacultyId(f2);
            cw6.setDate(Date.valueOf("2026-10-10"));
            cw6.setPeriodHour(2);
            cw6.setTopicCovered("Transaction Management: ACID properties, concurrent execution anomalies, and conflict serializability testing");
            cw6.setHomeworkAssigned("Construct precedence graph for Schedule S1 and determine if it is conflict serializable.");
            cw6.setReferenceMaterials("Silberschatz Chapter 17.");
            cw6.setStatus(DailyClasswork.Status.COMPLETED);
            dao.createClasswork(cw6);

        } catch (Exception e) {
            System.err.println("Notice seeding classwork: " + e.getMessage());
        }
    }

    private static void seedFacultyRemarksHelper(Connection conn, FacultyRemarkDAO dao, int studentId) {
        try {
            List<Integer> subIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM subjects ORDER BY id ASC")) {
                while (rs.next()) subIds.add(rs.getInt(1));
            }
            List<Integer> facIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM faculty ORDER BY id ASC")) {
                while (rs.next()) facIds.add(rs.getInt(1));
            }
            if (facIds.isEmpty()) return;

            int s1 = subIds.isEmpty() ? 1 : subIds.get(0);
            int s2 = subIds.size() > 1 ? subIds.get(1) : s1;
            int f1 = facIds.get(0);
            int f2 = facIds.size() > 1 ? facIds.get(1) : f1;

            FacultyRemark r1 = new FacultyRemark();
            r1.setStudentId(studentId);
            r1.setFacultyId(f1);
            r1.setSubjectId(s1);
            r1.setCategory(FacultyRemark.Category.ACADEMIC_PROGRESS);
            r1.setTitle("Exceptional Algorithmic Problem Solving Skills");
            r1.setRemarks("Rohan consistently demonstrates deep understanding of algorithm complexity, dynamic programming, and greedy optimization. Participates actively in lectures.");
            r1.setActionSuggested("Encouraged to register for the ACM ICPC competitive programming contest and college hackathon team.");
            r1.setRating(5);
            r1.setDatePosted(Date.valueOf("2026-10-05"));
            dao.createRemark(r1);

            FacultyRemark r2 = new FacultyRemark();
            r2.setStudentId(studentId);
            r2.setFacultyId(f2);
            r2.setSubjectId(s2);
            r2.setCategory(FacultyRemark.Category.GENERAL);
            r2.setTitle("Class Advisor (CFA) Mentorship Review");
            r2.setRemarks("Maintains above 92% attendance, exemplary discipline, and courteous demeanor with faculty and peers. Very sincere in coursework.");
            r2.setActionSuggested("Continue maintaining current disciplined routine and start preparation for upcoming Series-2 tests.");
            r2.setRating(5);
            r2.setDatePosted(Date.valueOf("2026-10-08"));
            dao.createRemark(r2);

            FacultyRemark r3 = new FacultyRemark();
            r3.setStudentId(studentId);
            r3.setFacultyId(f1);
            r3.setSubjectId(s1);
            r3.setCategory(FacultyRemark.Category.CLASS_CONDUCT);
            r3.setTitle("Operating Systems Practical Lab Performance");
            r3.setRemarks("Successfully submitted all CPU scheduling and multithreading lab implementations well in advance. Shows great teamwork during laboratory experiments.");
            r3.setActionSuggested("Recommended to explore advanced distributed systems elective for next semester.");
            r3.setRating(4);
            r3.setDatePosted(Date.valueOf("2026-10-02"));
            dao.createRemark(r3);

        } catch (Exception e) {
            System.err.println("Notice seeding faculty remarks: " + e.getMessage());
        }
    }

    private static void seedExamSchedulesHelper(Connection conn, ExamScheduleDAO dao, int classId) {
        try {
            List<Integer> subIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM subjects ORDER BY id ASC")) {
                while (rs.next()) subIds.add(rs.getInt(1));
            }
            if (subIds.isEmpty()) return;

            int s1 = subIds.get(0);
            int s2 = subIds.size() > 1 ? subIds.get(1) : s1;

            ExamSchedule e1 = new ExamSchedule();
            e1.setClassId(classId);
            e1.setSubjectId(s1);
            e1.setExamType(Mark.ExamType.SERIES_2);
            e1.setExamName("Semester 5 Series-2 Internal Examination");
            e1.setExamDate(Date.valueOf("2026-10-20"));
            e1.setStartTime("09:30 AM");
            e1.setEndTime("11:30 AM");
            e1.setRoomHall("Main Block LH-301");
            e1.setMaxMarks(50.0);
            e1.setPortionsSyllabus("Modules 1, 2, and 3: Divide & Conquer, Greedy Strategies, Dynamic Programming (Matrix Chain, 0/1 Knapsack, LCS)");
            dao.createExamSchedule(e1);

            ExamSchedule e2 = new ExamSchedule();
            e2.setClassId(classId);
            e2.setSubjectId(s2);
            e2.setExamType(Mark.ExamType.SERIES_2);
            e2.setExamName("Semester 5 Series-2 Internal Examination");
            e2.setExamDate(Date.valueOf("2026-10-22"));
            e2.setStartTime("09:30 AM");
            e2.setEndTime("11:30 AM");
            e2.setRoomHall("Main Block LH-301");
            e2.setMaxMarks(50.0);
            e2.setPortionsSyllabus("Modules 1, 2, and 3: Relational Algebra, Complex SQL Queries, Functional Dependencies, Normalization (1NF to BCNF)");
            dao.createExamSchedule(e2);

            ExamSchedule e3 = new ExamSchedule();
            e3.setClassId(classId);
            e3.setSubjectId(5); // MA301
            e3.setExamType(Mark.ExamType.SERIES_2);
            e3.setExamName("Semester 5 Series-2 Internal Examination");
            e3.setExamDate(Date.valueOf("2026-10-24"));
            e3.setStartTime("09:30 AM");
            e3.setEndTime("11:30 AM");
            e3.setRoomHall("Main Block LH-301");
            e3.setMaxMarks(50.0);
            e3.setPortionsSyllabus("Modules 1, 2, and 3: Propositional Logic, Predicate Calculus, Recurrence Relations, Planar Graphs, Tree Traversals");
            dao.createExamSchedule(e3);

            ExamSchedule e4 = new ExamSchedule();
            e4.setClassId(classId);
            e4.setSubjectId(6); // CS503
            e4.setExamType(Mark.ExamType.SERIES_2);
            e4.setExamName("Semester 5 Series-2 Internal Examination");
            e4.setExamDate(Date.valueOf("2026-10-26"));
            e4.setStartTime("09:30 AM");
            e4.setEndTime("11:30 AM");
            e4.setRoomHall("Main Block LH-301");
            e4.setMaxMarks(50.0);
            e4.setPortionsSyllabus("Modules 1, 2, and 3: Process Management, Threads, CPU Scheduling Algorithms, Synchronization Primitives, Deadlocks");
            dao.createExamSchedule(e4);

        } catch (Exception e) {
            System.err.println("Notice seeding exam schedules: " + e.getMessage());
        }
    }

    private static void seedNotificationsHelper(NotificationDAO dao, int classId) {
        try {
            Notification n1 = new Notification();
            n1.setTitle("S5 Series-2 Internal Examinations Timetable Announced");
            n1.setMessage("Dear Parents and Guardians, the institutional timetable for Semester 5 Series-2 Internal Examinations has been formally published. Tests commence on October 20, 2026. Hall tickets will be issued subject to fulfillment of minimum 75% attendance criteria.");
            n1.setTargetAudience("PARENTS");
            n1.setTargetClassId(classId);
            n1.setPriority(Notification.Priority.HIGH);
            n1.setCategory(Notification.Category.EXAMINATION);
            n1.setPostedByUserId(2); // Admin
            n1.setPublishDate(Date.valueOf("2026-10-08"));
            dao.createNotification(n1);

            Notification n2 = new Notification();
            n2.setTitle("Mid-Semester Parent-Teacher-Mentor Consultation (PTM)");
            n2.setMessage("You are cordially invited to attend the S5 Parent-Teacher Consultation Meeting on Saturday, October 31, 2026 from 10:00 AM to 01:00 PM in the College Auditorium. Class Faculty Advisors (CFA) and subject faculty will be present for one-on-one progress discussions regarding academic standing and attendance.");
            n2.setTargetAudience("PARENTS");
            n2.setTargetClassId(classId);
            n2.setPriority(Notification.Priority.NORMAL);
            n2.setCategory(Notification.Category.PARENT_MEETING);
            n2.setPostedByUserId(2);
            n2.setPublishDate(Date.valueOf("2026-10-09"));
            dao.createNotification(n2);

            Notification n3 = new Notification();
            n3.setTitle("Mandatory University Attendance Advisory (75% Minimum Requirement)");
            n3.setMessage("Institutional Circular regarding University Regulations: A minimum of 75% attendance in each registered subject is statutory to register for the university end-semester examinations. Parents are urged to review the attendance tracker tab on this portal regularly.");
            n3.setTargetAudience("PARENTS");
            n3.setPriority(Notification.Priority.URGENT);
            n3.setCategory(Notification.Category.ATTENDANCE_ALERT);
            n3.setPostedByUserId(2);
            n3.setPublishDate(Date.valueOf("2026-10-05"));
            dao.createNotification(n3);

            Notification n4 = new Notification();
            n4.setTitle("Campus Career Guidance & Higher Studies Orientation Seminar");
            n4.setMessage("A special orientation session on Campus Recruitment, GATE preparation, and higher education abroad will be held on October 18, 2026 at 05:00 PM via live stream. Parents and students are welcome to participate.");
            n4.setTargetAudience("ALL");
            n4.setPriority(Notification.Priority.LOW);
            n4.setCategory(Notification.Category.ACADEMIC);
            n4.setPostedByUserId(2);
            n4.setPublishDate(Date.valueOf("2026-10-10"));
            dao.createNotification(n4);

        } catch (Exception e) {
            System.err.println("Notice seeding notifications: " + e.getMessage());
        }
    }

    public static void seedAnnouncementsIfEmpty(Connection conn) {
        try {
            int count = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM announcements")) {
                if (rs.next()) count = rs.getInt(1);
            } catch (SQLException ex) {
                return;
            }
            if (count > 0) return;

            AnnouncementDAO dao = new AnnouncementDAO();

            Announcement a1 = new Announcement();
            a1.setTitle("Executive Order: Mandatory 75% Attendance Compliance for Examination Hall Ticket Issuance");
            a1.setContent("Pursuant to Academic Regulation Section 4.2, all enrolled undergraduate students must maintain a minimum aggregate attendance of 75% across all registered subjects to qualify for End-Semester Examinations. Students with attendance between 65%-74% due to verified medical emergencies must submit condonation requests to CFA before October 25, 2026.");
            a1.setCategory(Announcement.Category.CIRCULAR);
            a1.setTargetAudience("ALL");
            a1.setPinned(true);
            a1.setPostedByUserId(1); // Principal
            a1.setPostedDate(Date.valueOf("2026-10-05"));
            a1.setExpiryDate(Date.valueOf("2026-11-30"));
            a1.setAttachmentNote("Office of the Principal Directive No. ACAD/2026/OCT-01");
            dao.createAnnouncement(a1, "principal");

            Announcement a2 = new Announcement();
            a2.setTitle("Series-2 Continuous Internal Assessment Schedule & Portions Consolidation");
            a2.setContent("The Series-2 Internal Examinations for Semester 5 are scheduled from October 20 to October 25, 2026. All faculty members must ensure completion of 80% syllabus modules and post question paper blueprints to the Exam Cell by October 15, 2026. Students are advised to review the Exam Scheduling tab.");
            a2.setCategory(Announcement.Category.EXAMINATION);
            a2.setTargetAudience("ALL");
            a2.setPinned(true);
            a2.setPostedByUserId(1);
            a2.setPostedDate(Date.valueOf("2026-10-08"));
            a2.setExpiryDate(Date.valueOf("2026-10-26"));
            a2.setAttachmentNote("Exam Cell Notification EC/S5/2026/02");
            dao.createAnnouncement(a2, "principal");

            Announcement a3 = new Announcement();
            a3.setTitle("Faculty Research Grant Sponsorship & Conference Travel Subsidy (Q3/Q4)");
            a3.setContent("Applications are invited from full-time teaching faculty for institutional research publication subsidies and IEEE/ACM international conference registration grants. Eligible faculty should forward proposals through respective Department Heads to the Principal's Secretariat by November 10, 2026.");
            a3.setCategory(Announcement.Category.ACADEMIC);
            a3.setTargetAudience("FACULTY");
            a3.setPinned(false);
            a3.setPostedByUserId(1);
            a3.setPostedDate(Date.valueOf("2026-10-09"));
            a3.setExpiryDate(Date.valueOf("2026-11-15"));
            a3.setAttachmentNote("Dean Academic Research Cell Guidelines");
            dao.createAnnouncement(a3, "principal");

            Announcement a4 = new Announcement();
            a4.setTitle("Inter-Collegiate Innovation Hackathon & Tech Symposium: TechnoVision 2026");
            a4.setContent("Acadexia Institute of Technology will host TechnoVision 2026 on November 18-19, 2026. Student innovators from CSE, ECE, and allied branches are invited to register projects in AI/ML, Cloud Computing, and IoT tracks. Prizes worth INR 1,50,000 to be won.");
            a4.setCategory(Announcement.Category.EVENT);
            a4.setTargetAudience("STUDENTS");
            a4.setPinned(false);
            a4.setPostedByUserId(1);
            a4.setPostedDate(Date.valueOf("2026-10-10"));
            a4.setExpiryDate(Date.valueOf("2026-11-20"));
            a4.setAttachmentNote("TechnoVision 2026 Brochure & Registration Form");
            dao.createAnnouncement(a4, "principal");

            Announcement a5 = new Announcement();
            a5.setTitle("Zero Tolerance Advisory: Anti-Ragging and Student Grievance Redressal Vigilance");
            a5.setContent("The College Anti-Ragging Squad and Internal Grievance Redressal Cell maintain 24/7 vigil across campus facilities and hostels. Any violation or harassment complaint can be lodged anonymously or directly via the Student Portal Grievance module for immediate adjudication.");
            a5.setCategory(Announcement.Category.URGENT);
            a5.setTargetAudience("ALL");
            a5.setPinned(false);
            a5.setPostedByUserId(1);
            a5.setPostedDate(Date.valueOf("2026-10-04"));
            a5.setExpiryDate(Date.valueOf("2026-12-31"));
            a5.setAttachmentNote("Statutory UGC & AICTE Anti-Ragging Cell Notice");
            dao.createAnnouncement(a5, "principal");

        } catch (Exception e) {
            System.err.println("Notice seeding announcements: " + e.getMessage());
        }
    }

    public static void seedAcademicCalendarIfEmpty(Connection conn) {
        try {
            int count = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM academic_calendar")) {
                if (rs.next()) count = rs.getInt(1);
            } catch (SQLException ex) {
                return;
            }
            if (count > 0) return;

            AcademicCalendarDAO dao = new AcademicCalendarDAO();

            AcademicEvent e1 = new AcademicEvent();
            e1.setEventTitle("S5 Series-2 Continuous Internal Assessment (Theory & Practical)");
            e1.setEventType(AcademicEvent.EventType.EXAM);
            e1.setStartDate(Date.valueOf("2026-10-20"));
            e1.setEndDate(Date.valueOf("2026-10-25"));
            e1.setDescription("Institutional 2nd series internal examinations across all engineering departments.");
            e1.setTargetAudience("ALL");
            e1.setSemesterApplicable("Semester 5");
            e1.setCreatedByUserId(1);
            dao.createEvent(e1, "principal");

            AcademicEvent e2 = new AcademicEvent();
            e2.setEventTitle("Last Date for Faculty Continuous Assessment Marks Entry (Series-2)");
            e2.setEventType(AcademicEvent.EventType.DEADLINE);
            e2.setStartDate(Date.valueOf("2026-10-28"));
            e2.setEndDate(Date.valueOf("2026-10-28"));
            e2.setDescription("Statutory deadline for all subject faculties to enter and verify marks in Acadexia portal.");
            e2.setTargetAudience("FACULTY");
            e2.setSemesterApplicable("All Semesters");
            e2.setCreatedByUserId(1);
            dao.createEvent(e2, "principal");

            AcademicEvent e3 = new AcademicEvent();
            e3.setEventTitle("Academic Senate & HOD Executive Performance Review Meeting");
            e3.setEventType(AcademicEvent.EventType.MEETING);
            e3.setStartDate(Date.valueOf("2026-10-29"));
            e3.setEndDate(Date.valueOf("2026-10-29"));
            e3.setDescription("Quarterly departmental governance, student progress, and accreditation review chaired by Principal.");
            e3.setTargetAudience("FACULTY");
            e3.setSemesterApplicable("Institutional");
            e3.setCreatedByUserId(1);
            dao.createEvent(e3, "principal");

            AcademicEvent e4 = new AcademicEvent();
            e4.setEventTitle("Mid-Semester Parent-Teacher Consultation Conference (PTM)");
            e4.setEventType(AcademicEvent.EventType.MEETING);
            e4.setStartDate(Date.valueOf("2026-10-31"));
            e4.setEndDate(Date.valueOf("2026-10-31"));
            e4.setDescription("Direct interaction between parents, Class Faculty Advisors (CFAs), and HODs in Main Auditorium.");
            e4.setTargetAudience("PARENTS");
            e4.setSemesterApplicable("All Semesters");
            e4.setCreatedByUserId(1);
            dao.createEvent(e4, "principal");

            AcademicEvent e5 = new AcademicEvent();
            e5.setEventTitle("Public Holiday: Deepavali (Festival of Lights)");
            e5.setEventType(AcademicEvent.EventType.HOLIDAY);
            e5.setStartDate(Date.valueOf("2026-11-01"));
            e5.setEndDate(Date.valueOf("2026-11-02"));
            e5.setDescription("College and administrative offices closed on account of Deepavali holiday.");
            e5.setTargetAudience("ALL");
            e5.setSemesterApplicable("All");
            e5.setCreatedByUserId(1);
            dao.createEvent(e5, "principal");

            AcademicEvent e6 = new AcademicEvent();
            e6.setEventTitle("Official Principal Release & Publication of Series-2 Results");
            e6.setEventType(AcademicEvent.EventType.DEADLINE);
            e6.setStartDate(Date.valueOf("2026-11-05"));
            e6.setEndDate(Date.valueOf("2026-11-05"));
            e6.setDescription("Executive approval by Principal releasing results to student and parent dashboards.");
            e6.setTargetAudience("ALL");
            e6.setSemesterApplicable("Semester 5");
            e6.setCreatedByUserId(1);
            dao.createEvent(e6, "principal");

            AcademicEvent e7 = new AcademicEvent();
            e7.setEventTitle("Annual Inter-Collegiate Sports & Athletic Meet 2026");
            e7.setEventType(AcademicEvent.EventType.CULTURAL_SPORTS);
            e7.setStartDate(Date.valueOf("2026-11-12"));
            e7.setEndDate(Date.valueOf("2026-11-14"));
            e7.setDescription("Track & field events, inter-department tournament trophies, and sports day celebration.");
            e7.setTargetAudience("ALL");
            e7.setSemesterApplicable("All");
            e7.setCreatedByUserId(1);
            dao.createEvent(e7, "principal");

        } catch (Exception e) {
            System.err.println("Notice seeding academic calendar: " + e.getMessage());
        }
    }
}


