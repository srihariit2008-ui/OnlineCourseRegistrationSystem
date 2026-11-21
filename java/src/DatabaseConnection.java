package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages JDBC Database Connectivity to MySQL.
 * Demonstrates: Java Database Connectivity (JDBC), Exception Handling,
 * Configuration Management, and Resource Management.
 * Syllabus Reference: Java Programming Unit 4 (Database Connection).
 */
public class DatabaseConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/online_course_registration?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    private static String dbDriver = "com.mysql.cj.jdbc.Driver";

    private static boolean driverLoaded = false;
    private static Boolean connectionAvailable = null;

    static {
        loadConfiguration();
        try {
            Class.forName(dbDriver);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] MySQL JDBC Driver (" + dbDriver + ") not found in classpath.");
            System.err.println("       For live MySQL access, include 'mysql-connector-j-*.jar'.");
            driverLoaded = false;
        }
    }

    /**
     * Loads database credentials from db.properties or environment variables.
     */
    public static void loadConfiguration() {
        Properties props = new Properties();
        File propFile = new File("src/db.properties");
        if (!propFile.exists()) {
            propFile = new File("db.properties");
        }

        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                props.load(input);
                if (props.getProperty("db.url") != null) dbUrl = props.getProperty("db.url").trim();
                if (props.getProperty("db.user") != null) dbUser = props.getProperty("db.user").trim();
                if (props.getProperty("db.password") != null) dbPassword = props.getProperty("db.password").trim();
                if (props.getProperty("db.driver") != null) dbDriver = props.getProperty("db.driver").trim();
            } catch (Exception e) {
                System.err.println("[WARN] Could not read db.properties file. Using default values.");
            }
        }

        // Allow environment variable overrides
        if (System.getenv("DB_URL") != null) dbUrl = System.getenv("DB_URL");
        if (System.getenv("DB_USER") != null) dbUser = System.getenv("DB_USER");
        if (System.getenv("DB_PASSWORD") != null) dbPassword = System.getenv("DB_PASSWORD");
    }

    /**
     * Obtains a new connection to MySQL database.
     * @return java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName(dbDriver);
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found on classpath: " + dbDriver, e);
            }
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests if MySQL database server is online and reachable.
     */
    public static boolean isDatabaseAvailable() {
        try (Connection conn = getConnection()) {
            connectionAvailable = (conn != null && !conn.isClosed());
            return connectionAvailable;
        } catch (Exception e) {
            connectionAvailable = false;
            return false;
        }
    }

    /**
     * Safely closes JDBC resources to avoid resource leaks.
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void closeAll(ResultSet rs, Statement stmt, Connection conn) {
        closeQuietly(rs);
        closeQuietly(stmt);
        closeQuietly(conn);
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }
}
