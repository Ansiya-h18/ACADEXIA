package com.acadexia.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

/**
 * Database connection factory for MySQL.
 * Each call to getConnection() returns a FRESH connection that callers
 * are responsible for closing (safe to use in try-with-resources in DAOs).
 * 
 * initialize() is called once at startup to create the DB schema.
 */
public final class DatabaseConnection {

    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    // Connection parameters cached after properties are loaded
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    static {
        loadProperties();
    }

    private DatabaseConnection() {}

    private static void loadProperties() {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                // Default fallback configuration
                properties.setProperty("db.host", "localhost");
                properties.setProperty("db.port", "3306");
                properties.setProperty("db.name", "acadexia_db");
                properties.setProperty("db.user", "root");
                properties.setProperty("db.password", "");
                properties.setProperty("db.useSSL", "false");
                properties.setProperty("db.allowPublicKeyRetrieval", "true");
                properties.setProperty("db.serverTimezone", "UTC");
                properties.setProperty("db.autoInit", "true");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load db.properties, using fallback defaults: " + e.getMessage());
        }

        // Cache connection parameters
        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String dbName = properties.getProperty("db.name", "acadexia_db");
        dbUser = properties.getProperty("db.user", "root");
        dbPass = properties.getProperty("db.password", "");
        dbUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true",
                host, port, dbName
        );
    }

    /**
     * Initializes the MySQL database and executes schema.sql once at startup.
     * This method is idempotent and thread-safe.
     */
    public static synchronized void initialize() throws SQLException {
        if (initialized) return;

        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String dbName = properties.getProperty("db.name", "acadexia_db");

        // Step 1: Connect to MySQL server without database to ensure database exists
        String serverUrl = String.format(
                "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port
        );

        try (Connection serverConn = DriverManager.getConnection(serverUrl, dbUser, dbPass);
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        }

        // Step 2: Run schema.sql if tables do not exist
        if (Boolean.parseBoolean(properties.getProperty("db.autoInit", "true"))) {
            executeSchemaScript();
        }

        // Step 3: Ensure role column includes PARENT enum value in existing databases
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE users MODIFY COLUMN role ENUM('STUDENT', 'FACULTY', 'PRINCIPAL', 'ADMIN', 'PARENT') NOT NULL");
        } catch (SQLException ex) {
            // Ignored if already aligned
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             Statement stmt = conn.createStatement()) {
            String pubTableSql = """
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
                )
                """;
            stmt.executeUpdate(pubTableSql);

            String announcementTableSql = """
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
                )
                """;
            stmt.executeUpdate(announcementTableSql);

            String calendarTableSql = """
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
                )
                """;
            stmt.executeUpdate(calendarTableSql);
        } catch (SQLException ex) {
            // Ignored if already created
        }

        initialized = true;

        // Step 4: Auto seed starter records & parent portal records if empty
        try {
            DatabaseSeeder.seedIfEmpty();
        } catch (Exception e) {
            System.err.println("Notice during automatic seeding: " + e.getMessage());
        }
    }

    /**
     * Returns a FRESH database connection. The caller is responsible for closing it.
     * Safe to use in try-with-resources blocks.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    /**
     * Executes the SQL statements from schema.sql using a fresh dedicated connection.
     */
    private static void executeSchemaScript() {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                System.err.println("schema.sql not found in resources!");
                return;
            }

            String host = properties.getProperty("db.host", "localhost");
            String port = properties.getProperty("db.port", "3306");
            String dbName = properties.getProperty("db.name", "acadexia_db");
            String schemaUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, dbName
            );

            try (Connection schemaConn = DriverManager.getConnection(schemaUrl, dbUser, dbPass);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Statement stmt = schemaConn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sqlBuilder.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        // Remove trailing semicolon
                        sql = sql.substring(0, sql.length() - 1);
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            // If constraint or table already exists, log and proceed
                            if (!ex.getMessage().contains("already exists") && !ex.getMessage().contains("Duplicate foreign key")) {
                                System.err.println("Notice executing schema statement: " + ex.getMessage());
                            }
                        }
                        sqlBuilder.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing schema script: " + e.getMessage());
        }
    }

    /**
     * Tests whether the database is currently reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
