-- Acadexia Database Schema
-- Compatible with MySQL 8.0+ and 9.0+

CREATE TABLE IF NOT EXISTS departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    hod_faculty_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'FACULTY', 'PRINCIPAL', 'ADMIN', 'PARENT') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    department_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS faculty (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    faculty_id VARCHAR(50) NOT NULL UNIQUE,
    designation VARCHAR(100) NOT NULL DEFAULT 'Assistant Professor',
    qualification VARCHAR(100) NULL,
    department_id INT NOT NULL,
    joining_date DATE NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- Link departments HOD foreign key to faculty
ALTER TABLE departments 
    ADD CONSTRAINT fk_dept_hod FOREIGN KEY (hod_faculty_id) 
    REFERENCES faculty(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS classes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    department_id INT NOT NULL,
    semester INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    section VARCHAR(10) NOT NULL DEFAULT 'A',
    advisor_faculty_id INT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    FOREIGN KEY (advisor_faculty_id) REFERENCES faculty(id) ON DELETE SET NULL,
    UNIQUE KEY uq_class (department_id, semester, section, academic_year)
);

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    register_number VARCHAR(50) NOT NULL UNIQUE,
    roll_number VARCHAR(20) NOT NULL,
    class_id INT NOT NULL,
    admission_year INT NOT NULL,
    guardian_name VARCHAR(100) NULL,
    guardian_phone VARCHAR(20) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    semester INT NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    max_internal_marks INT NOT NULL DEFAULT 50,
    max_external_marks INT NOT NULL DEFAULT 100,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subject_assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_id INT NOT NULL,
    faculty_id INT NOT NULL,
    class_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    UNIQUE KEY uq_subject_class_fac (subject_id, faculty_id, class_id, academic_year)
);

CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    class_id INT NOT NULL,
    date DATE NOT NULL,
    hour INT NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'DUTY_LEAVE') NOT NULL DEFAULT 'PRESENT',
    marked_by_faculty_id INT NOT NULL,
    remarks VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by_faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    UNIQUE KEY uq_att_slot (student_id, subject_id, date, hour)
);

CREATE TABLE IF NOT EXISTS marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    exam_type ENUM('SERIES_1', 'SERIES_2', 'ASSIGNMENT', 'INTERNAL', 'SEMESTER_EXAM') NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL,
    exam_date DATE NULL,
    recorded_by_faculty_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by_faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    UNIQUE KEY uq_marks_entry (student_id, subject_id, exam_type)
);

CREATE TABLE IF NOT EXISTS assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NULL,
    subject_id INT NOT NULL,
    class_id INT NOT NULL,
    faculty_id INT NOT NULL,
    due_date DATE NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    assignment_id INT NOT NULL,
    student_id INT NOT NULL,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submission_text TEXT NULL,
    file_name VARCHAR(255) NULL,
    status ENUM('SUBMITTED', 'PENDING', 'GRADED', 'LATE') NOT NULL DEFAULT 'SUBMITTED',
    marks_obtained DECIMAL(5,2) NULL,
    faculty_feedback TEXT NULL,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_assignment (assignment_id, student_id)
);

CREATE TABLE IF NOT EXISTS duty_leaves (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL DEFAULT 1,
    proof_details TEXT NULL,
    status ENUM('PENDING_ADVISOR', 'RECOMMENDED_BY_ADVISOR', 'REJECTED_BY_ADVISOR', 
                'ENDORSED_BY_HOD', 'REJECTED_BY_HOD', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING_ADVISOR',
    advisor_remarks VARCHAR(255) NULL,
    hod_remarks VARCHAR(255) NULL,
    principal_remarks VARCHAR(255) NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    faculty_id INT NOT NULL,
    subject_id INT NOT NULL,
    rating_teaching INT NOT NULL CHECK (rating_teaching BETWEEN 1 AND 5),
    rating_punctuality INT NOT NULL CHECK (rating_punctuality BETWEEN 1 AND 5),
    rating_clarity INT NOT NULL CHECK (rating_clarity BETWEEN 1 AND 5),
    comments TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_feedback (student_id, faculty_id, subject_id)
);

CREATE TABLE IF NOT EXISTS grievances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    category ENUM('ACADEMIC', 'EXAMINATION', 'ATTENDANCE', 'FACILITY', 'HARASSMENT', 'OTHER') NOT NULL,
    description TEXT NOT NULL,
    status ENUM('SUBMITTED', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'SUBMITTED',
    resolution TEXT NULL,
    resolved_by_user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS timetable (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    faculty_id INT NOT NULL,
    subject_id INT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY') NOT NULL,
    period_slot INT NOT NULL CHECK (period_slot BETWEEN 1 AND 7),
    room_number VARCHAR(50) NOT NULL,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY uq_class_slot (class_id, day_of_week, period_slot)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NULL,
    entity_id VARCHAR(50) NULL,
    details TEXT NULL,
    ip_address VARCHAR(45) DEFAULT '127.0.0.1',
    severity ENUM('INFO', 'WARNING', 'SECURITY_ALERT', 'ERROR') NOT NULL DEFAULT 'INFO',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS parents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    student_id INT NOT NULL,
    relationship VARCHAR(50) NOT NULL DEFAULT 'Parent',
    occupation VARCHAR(100) NULL,
    address TEXT NULL,
    emergency_contact VARCHAR(20) NULL,
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    verified_by_user_id INT NULL,
    verification_date TIMESTAMP NULL,
    verification_notes VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS daily_classwork (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    subject_id INT NOT NULL,
    faculty_id INT NOT NULL,
    date DATE NOT NULL,
    period_hour INT NOT NULL DEFAULT 1,
    topic_covered VARCHAR(255) NOT NULL,
    homework_assigned TEXT NULL,
    reference_materials TEXT NULL,
    status ENUM('COMPLETED', 'IN_PROGRESS', 'POSTPONED') NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS faculty_remarks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    faculty_id INT NOT NULL,
    subject_id INT NULL,
    category ENUM('ACADEMIC_PROGRESS', 'CLASS_CONDUCT', 'ATTENDANCE_CONCERN', 'EXCELLENCE', 'GENERAL') NOT NULL DEFAULT 'ACADEMIC_PROGRESS',
    title VARCHAR(150) NOT NULL,
    remarks TEXT NOT NULL,
    action_suggested TEXT NULL,
    rating INT NULL,
    date_posted DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS exam_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    subject_id INT NOT NULL,
    exam_type ENUM('SERIES_1', 'SERIES_2', 'INTERNAL', 'SEMESTER_EXAM', 'MODEL_EXAM') NOT NULL,
    exam_name VARCHAR(100) NOT NULL,
    exam_date DATE NOT NULL,
    start_time VARCHAR(20) NOT NULL,
    end_time VARCHAR(20) NOT NULL,
    room_hall VARCHAR(50) NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL DEFAULT 50.00,
    portions_syllabus TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    target_audience ENUM('ALL', 'PARENTS', 'STUDENTS', 'FACULTY') NOT NULL DEFAULT 'ALL',
    target_class_id INT NULL,
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL',
    category ENUM('EXAMINATION', 'ATTENDANCE_ALERT', 'PARENT_MEETING', 'ACADEMIC', 'FEES', 'GENERAL') NOT NULL DEFAULT 'GENERAL',
    posted_by_user_id INT NULL,
    publish_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (posted_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS result_publications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    subject_id INT NOT NULL,
    exam_type ENUM('SERIES_1', 'SERIES_2', 'ASSIGNMENT', 'INTERNAL', 'SEMESTER_EXAM') NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    status ENUM('ENTERED', 'VERIFIED', 'PUBLISHED') NOT NULL DEFAULT 'ENTERED',
    verified_by_user_id INT NULL,
    published_by_user_id INT NULL,
    published_at TIMESTAMP NULL,
    remarks VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (published_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uq_pub (class_id, subject_id, exam_type, academic_year)
);

CREATE TABLE IF NOT EXISTS announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category ENUM('GENERAL', 'ACADEMIC', 'EXAMINATION', 'EVENT', 'URGENT', 'CIRCULAR') NOT NULL DEFAULT 'GENERAL',
    target_audience ENUM('ALL', 'FACULTY', 'STUDENTS', 'PARENTS') NOT NULL DEFAULT 'ALL',
    pinned BOOLEAN DEFAULT FALSE,
    posted_by_user_id INT NULL,
    posted_date DATE NOT NULL,
    expiry_date DATE NULL,
    attachment_note VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS academic_calendar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_title VARCHAR(200) NOT NULL,
    event_type ENUM('EXAM', 'HOLIDAY', 'ACADEMIC_MILESTONE', 'MEETING', 'CULTURAL_SPORTS', 'DEADLINE') NOT NULL DEFAULT 'ACADEMIC_MILESTONE',
    start_date DATE NOT NULL,
    end_date DATE NULL,
    description TEXT NULL,
    target_audience ENUM('ALL', 'FACULTY', 'STUDENTS', 'PARENTS') NOT NULL DEFAULT 'ALL',
    semester_applicable VARCHAR(50) NULL,
    created_by_user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);


