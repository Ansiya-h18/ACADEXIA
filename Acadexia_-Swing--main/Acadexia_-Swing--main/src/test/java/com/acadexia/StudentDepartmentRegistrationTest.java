package com.acadexia;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.config.DatabaseSeeder;
import com.acadexia.dao.ClassDAO;
import com.acadexia.dao.DepartmentDAO;
import com.acadexia.dao.StudentDAO;
import com.acadexia.dao.UserDAO;
import com.acadexia.model.ClassBatch;
import com.acadexia.model.Department;
import com.acadexia.model.Role;
import com.acadexia.model.Student;
import com.acadexia.model.User;

import java.sql.Connection;
import java.util.List;

public class StudentDepartmentRegistrationTest {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("ACADEXIA - STUDENT REGISTRATION & DYNAMIC DEPARTMENT DROPDOWN TEST");
        System.out.println("======================================================================");

        try {
            // Step 1: Ensure DB is connected and seeded
            System.out.println("\n[1] Testing MySQL connection & department seeding...");
            try (Connection conn = DatabaseConnection.getConnection()) {
                DatabaseSeeder.seedDepartmentClassesIfEmpty(conn);
            }
            System.out.println("✓ Database connection active and class seeding verified.");

            // Step 2: Load departments dynamically from MySQL via JDBC
            System.out.println("\n[2] Loading all departments dynamically from MySQL 'departments' table...");
            DepartmentDAO deptDAO = new DepartmentDAO();
            List<Department> departments = deptDAO.getAllDepartments();
            System.out.println("✓ Total departments loaded dynamically: " + departments.size());
            for (Department d : departments) {
                System.out.println("   - ID: " + d.getId() + " | Code: " + d.getCode() + " | Name: " + d.getName());
            }

            if (departments.size() < 2) {
                throw new RuntimeException("FAILED: Expected multiple departments, found " + departments.size());
            }
            System.out.println("✓ Verification passed: Multiple departments dynamically loaded (not just CS).");

            // Step 3: Test registration for multiple departments
            ClassDAO classDAO = new ClassDAO();
            UserDAO userDAO = new UserDAO();
            StudentDAO studentDAO = new StudentDAO();

            int testYear = 2026;

            for (Department dept : departments) {
                System.out.println("\n[3] Testing registration for Department: " + dept.getCode() + " (" + dept.getName() + ")...");

                // Get or create class for this department
                List<ClassBatch> deptClasses = classDAO.getClassesByDepartment(dept.getId());
                if (deptClasses.isEmpty()) {
                    ClassBatch newCb = new ClassBatch();
                    newCb.setName("S1 " + dept.getCode());
                    newCb.setDepartmentId(dept.getId());
                    newCb.setSemester(1);
                    newCb.setAcademicYear(testYear + "-" + (testYear + 1));
                    newCb.setSection("A");
                    int newClassId = classDAO.createClass(newCb);
                    newCb.setId(newClassId);
                    deptClasses.add(newCb);
                    System.out.println("   -> Auto-provisioned class: " + newCb.getName() + " (ID: " + newClassId + ")");
                }
                ClassBatch targetClass = deptClasses.get(0);
                System.out.println("   -> Using Class Batch: " + targetClass.getName() + " (Class ID: " + targetClass.getId() + ")");

                // Generate register number
                String nextReg = studentDAO.generateNextRegisterNumber(testYear, dept.getCode());
                System.out.println("   -> Generated Official Reg No: " + nextReg);

                // Unique username for testing
                String username = "test.stu." + dept.getCode().toLowerCase() + "." + System.currentTimeMillis() % 10000;
                String fullName = "Test Student " + dept.getCode();

                User u = new User();
                u.setUsername(username);
                u.setFullName(fullName);
                u.setEmail(username + "@acadexia.edu");
                u.setPhone("9988776655");
                u.setDepartmentId(dept.getId()); // Saving selected department's correct ID
                u.setRole(Role.STUDENT);
                u.setActive(true);

                int userId = userDAO.createUser(u, "password123");
                if (userId <= 0) {
                    throw new RuntimeException("FAILED to create user for dept " + dept.getCode());
                }

                Student s = new Student();
                s.setUserId(userId);
                s.setRegisterNumber(nextReg);
                s.setRollNumber(studentDAO.getNextRollNumber(targetClass.getId()));
                s.setClassId(targetClass.getId());
                s.setAdmissionYear(testYear);
                s.setGuardianName("Guardian of " + fullName);
                s.setGuardianPhone("9988776655");

                int studentId = studentDAO.createStudent(s);
                if (studentId <= 0) {
                    throw new RuntimeException("FAILED to create student for dept " + dept.getCode());
                }
                System.out.println("   ✓ Successfully registered Student #" + studentId + " in " + dept.getCode());

                // Verify registered student record
                Student fetched = studentDAO.getById(studentId);
                if (fetched == null) {
                    throw new RuntimeException("FAILED: Could not fetch registered student #" + studentId);
                }
                if (fetched.getDepartmentId() != dept.getId()) {
                    throw new RuntimeException("FAILED: Expected department_id " + dept.getId() + ", but found " + fetched.getDepartmentId());
                }
                if (!dept.getName().equalsIgnoreCase(fetched.getDepartmentName())) {
                    throw new RuntimeException("FAILED: Expected department name '" + dept.getName() + "', but found '" + fetched.getDepartmentName() + "'");
                }
                System.out.println("   ✓ Verified: DB saved department_id=" + fetched.getDepartmentId() + " (" + fetched.getDepartmentName() + ")");
            }

            // Step 4: Verify Department Filtering in StudentDAO
            System.out.println("\n[4] Testing Department filtering across registered students...");
            List<Student> allStudents = studentDAO.getAllStudents();
            System.out.println("✓ Total students in database: " + allStudents.size());

            for (Department dept : departments) {
                long count = allStudents.stream().filter(st -> dept.getName().equalsIgnoreCase(st.getDepartmentName())).count();
                System.out.println("   - Department " + dept.getCode() + " (" + dept.getName() + "): " + count + " student(s)");
                if (count == 0) {
                    throw new RuntimeException("FAILED: Expected at least 1 student in " + dept.getCode());
                }
            }

            // Step 5: Test Student Editing with Department change
            System.out.println("\n[5] Testing Student Edit with Department Update...");
            Department firstDept = departments.get(0);
            Department secondDept = departments.get(1);

            List<Student> firstDeptStudents = studentDAO.getStudentsByDepartment(firstDept.getId());
            if (!firstDeptStudents.isEmpty()) {
                Student toEdit = firstDeptStudents.get(0);
                int oldDeptId = toEdit.getDepartmentId();
                List<ClassBatch> newDeptClasses = classDAO.getClassesByDepartment(secondDept.getId());
                toEdit.setClassId(newDeptClasses.get(0).getId());
                toEdit.setDepartmentId(secondDept.getId());
                boolean updated = studentDAO.updateStudent(toEdit);
                if (!updated) {
                    throw new RuntimeException("FAILED: Could not update student department!");
                }
                Student reFetched = studentDAO.getById(toEdit.getId());
                System.out.println("   ✓ Successfully edited student #" + toEdit.getId() + ": changed from Dept " + oldDeptId + " to Dept " + reFetched.getDepartmentId() + " (" + reFetched.getDepartmentName() + ")");
                
                // Revert back
                toEdit.setClassId(firstDeptStudents.get(0).getClassId());
                toEdit.setDepartmentId(firstDept.getId());
                studentDAO.updateStudent(toEdit);
            }

            System.out.println("\n======================================================================");
            System.out.println("ALL INTEGRATION TESTS PASSED SUCCESSFULLY! 100% VERIFIED.");
            System.out.println("======================================================================");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }
}
