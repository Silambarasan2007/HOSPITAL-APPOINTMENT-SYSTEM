package hospital.db;

import java.sql.*;
import java.util.Properties;
import java.io.*;

/**
 * DBConnection — singleton JDBC connection manager.
 *
 * Configuration is read from db.properties (in project root).
 * Falls back to defaults if the file is missing.
 *
 * Usage:
 *   Connection conn = DBConnection.getConnection();
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    // ─── defaults (overridden by db.properties) ───────────────
    private static String URL      = "jdbc:mysql://localhost:3306/medicare_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String USER     = "root";
    private static String PASSWORD = "";

    private static Connection connection = null;

    static {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL driver not found. Add mysql-connector-j.jar to classpath.");
        }
    }

    // ─── Load db.properties ───────────────────────────────────
    private static void loadConfig() {
        Properties props = new Properties();
        // Try loading from working directory first
        try (InputStream is = new FileInputStream(CONFIG_FILE)) {
            props.load(is);
            URL      = props.getProperty("db.url",      URL).trim();
            USER     = props.getProperty("db.user",     USER).trim();
            PASSWORD = props.getProperty("db.password", PASSWORD).trim();
            System.out.println("[DBConnection] Loaded config from " + CONFIG_FILE);
        } catch (IOException ignored) {
            // Try parent directory
            try (InputStream is = new FileInputStream("../" + CONFIG_FILE)) {
                props.load(is);
                URL      = props.getProperty("db.url",      URL).trim();
                USER     = props.getProperty("db.user",     USER).trim();
                PASSWORD = props.getProperty("db.password", PASSWORD).trim();
                System.out.println("[DBConnection] Loaded config from ../" + CONFIG_FILE);
            } catch (IOException e2) {
                // Try classpath
                try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                    if (is != null) {
                        props.load(is);
                        URL      = props.getProperty("db.url",      URL).trim();
                        USER     = props.getProperty("db.user",     USER).trim();
                        PASSWORD = props.getProperty("db.password", PASSWORD).trim();
                    }
                } catch (IOException e3) {
                    System.out.println("[DBConnection] Using default connection settings.");
                }
            }
        }
    }

    // ─── Get Connection ───────────────────────────────────────
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DBConnection] Connected to MySQL successfully.");
        }
        return connection;
    }

    // ─── Test connection ──────────────────────────────────────
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
            return false;
        }
    }

    // ─── Close quietly ────────────────────────────────────────
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBConnection] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
        }
    }

    // ─── Utility: close resources ─────────────────────────────
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception ignored) {}
            }
        }
    }
}
