# Acadexia — Student Result Management System

> **Tech Stack**: Java 21+, Java Swing (`javax.swing.*`), FlatLaf Light Theme, MySQL (Port 3306), Maven  
> **Status**: ✅ Fully redesigned, compiled, and ready to run  
> **Last Updated**: 2026-10-05  
> **UI Mandate**: 100% Native Desktop Java Swing. No web wrappers or external frontends.

---

## 📌 Project Overview

Acadexia is a professional desktop College Management System built with **Java Swing + FlatLaf**.  
It provides role-based access for four roles:

| Role | Dashboard | Key Features |
|---|---|---|
| **Student** | Student Portal | Results, attendance, duty leave, assignments, grievances, feedback |
| **Faculty** | Faculty Portal | Attendance register, marks entry, assignments, CFA + HOD panels |
| **Principal** | Principal's Office | Tier-3 duty leave, HOD appointments, analytics, grievances |
| **Admin** | System Admin | User provisioning, audit logs, system status |

---

## 🗺️ Architecture

```
src/main/java/com/acadexia/
├── config/         DatabaseConnection.java, DatabaseSeeder.java
├── dao/            14 DAO classes (StudentDAO, FacultyDAO, MarkDAO, …)
├── model/          16 model classes (Student, Faculty, Mark, …)
├── security/       SessionManager, InputFilterFactory, SqlSanitizerFilter
└── ui/
    ├── auth/       LandingFrame.java, RegisterFrame.java
    ├── common/     MainFrame.java, UITheme.java, UIComponents.java
    ├── admin/      AdminDashboardFrame.java
    ├── student/    StudentDashboardFrame.java
    ├── faculty/    FacultyDashboardFrame.java
    └── principal/  PrincipalDashboardFrame.java
```

---

## 🎨 UI Redesign — 2026-10-05

A complete modern UI/UX transformation was applied.  
**Zero database schema changes. Zero DAO changes. All business logic preserved.**

### What changed

#### Design System
- **`UITheme.java`** — Switched from `FlatDarkLaf` to `FlatLightLaf`. Full professional academic color palette (`#2563EB` primary blue, `#F8FAFC` background, white surfaces). Centralized fonts (Segoe UI), spacing constants, and table sizing. Legacy aliases kept so no existing code broke.
- **`UIComponents.java`** — Complete rewrite of the component factory: modern rounded buttons with hover paint, styled text fields with focus border, rounded stat cards with colored left accent bar, striped tables, toast notifications, empty states, toolbar helpers, delete confirmation dialogs.

#### Login & Registration
- **`LandingFrame.java`** — Two-column split: dark gradient brand panel (logo, features) + white login card (clean fields, show/hide password toggle, demo chip buttons, SwingWorker async auth).
- **`RegisterFrame.java`** *(new)* — 3-step registration wizard: Account Info → Personal Info → Confirm. Step progress bar, per-step validation, async account creation via SwingWorker.

#### Navigation Shell
- **`MainFrame.java`** *(new)* — Base frame that all 4 dashboards extend. Provides:
  - Left sidebar (230px dark navy) with logo, user profile area, nav buttons, logout
  - Top bar (60px white) with page title + role badge
  - `CardLayout` content area — panels swap on nav click
  - `SidebarNavButton` inner class with active/hover paint

#### Dashboards (all extend `MainFrame`)
- **`AdminDashboardFrame`** — 4 panels: Overview (stats + quick actions), User Accounts, Audit Logs, System Status
- **`StudentDashboardFrame`** — 8 panels: Overview (greeting + KPI cards), Results, Attendance, Duty Leave, Timetable, Assignments, Grievances, Feedback
- **`FacultyDashboardFrame`** — Overview + Teaching (tabbed: attendance/marks/assignments/feedback/timetable) + conditional CFA panel + conditional HOD panel
- **`PrincipalDashboardFrame`** — 5 panels: Overview, Duty Leave Tier-3, Departments+HOD, Analytics (with summary stats), Grievances

#### Minor DAO addition
- **`UserDAO.usernameExists()`** — added for registration duplicate check (additive, no existing code changed)

### What was NOT changed
- `DatabaseConnection.java` — untouched
- `DatabaseSeeder.java` — untouched
- All 14 DAO classes — untouched (except `UserDAO` additive method)
- All 16 model classes — untouched
- `SessionManager`, `InputFilterFactory`, `SqlSanitizerFilter` — untouched
- `schema.sql`, `db.properties` — untouched
- `Main.java` — untouched

---

## 🚀 How to Run

### Prerequisites
1. **Java 21+** installed
2. **Maven 3.6+** installed
3. **MySQL 8.x / 9.x** running on `localhost:3306`
4. Edit `src/main/resources/db.properties` with your MySQL password

### Run
```bash
mvn compile
mvn exec:java
```

The app will:
1. Auto-create `acadexia_db` database
2. Auto-run `schema.sql` to create all tables
3. Auto-seed demo users and data
4. Open the login screen

### Demo Credentials

| Role | Username / ID | Password |
|---|---|---|
| System Admin | `admin` | `admin123` |
| Student | `REG2024CS001` | `student123` |
| Faculty (HOD CSE) | `FAC-CS-001` | `faculty123` |
| Faculty (CFA) | `FAC-CS-002` | `faculty123` |
| Principal | `principal` | `principal123` |

---

## 🔒 Security Notes

1. **PreparedStatement everywhere** — no string-concatenated SQL in any DAO
2. **BCrypt password hashing** — via `jbcrypt` library
3. **DocumentFilter suite** — blocks SQL injection at keystroke level in all input fields
4. **Audit log trail** — all significant actions logged to `audit_logs` table
5. **Session management** — `SessionManager` singleton, cleared on logout

---

## 📋 Implementation Checklist

### Original Features (all preserved)
- [x] MySQL auto-init + seeder
- [x] Multi-identifier login (register no. / faculty ID / username)
- [x] Student marksheet, SGPA/CGPA
- [x] Subject-wise attendance with shortage warning
- [x] 3-tier duty leave pipeline (CFA → HOD → Principal)
- [x] Faculty attendance register
- [x] Faculty marks entry (Series 1/2, internal, semester)
- [x] Assignment post + submission + grading
- [x] Anonymous faculty feedback
- [x] Grievance filing + resolution
- [x] HOD: assign CFA, assign subject teachers
- [x] Principal: HOD appointments, duty leave sanctions, analytics
- [x] Admin: user provisioning, audit logs, system status

### New UI Features (added in redesign)
- [x] Modern light theme (FlatLightLaf)
- [x] Sidebar navigation shell (all roles)
- [x] Gradient brand panel on login
- [x] Show/hide password toggle
- [x] 3-step registration wizard with validation
- [x] KPI stat cards with accent bars
- [x] Toast notifications (success/error/info)
- [x] SwingWorker async login (non-blocking UI)
- [x] User profile area in sidebar
- [x] Logout confirmation dialog
- [x] Consistent dialog footers (Cancel + Submit)
- [x] Striped table rows
- [x] Focus border on input fields
- [x] Role badge in top bar

---

## 📝 Change Log

| Date | Summary |
|---|---|
| 2026-10-04 | Project initialized. Full feature set built on FlatDarkLaf. 44 source files. `mvn compile` clean. |
| 2026-10-05 | **Full modern UI/UX redesign.** FlatLightLaf, MainFrame shell, new login/register, all dashboards redesigned. 46 source files. `mvn compile` clean — 0 errors, 0 warnings. |
