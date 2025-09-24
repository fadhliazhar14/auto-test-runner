package com.fadhli.automation.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database utility class for connection management using HikariCP connection pool
 * Implements singleton pattern for single connection pool instance
 */
public class DatabaseUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    
    private static DatabaseUtil instance;
    private static HikariDataSource dataSource;
    
    // Database configuration constants
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/bdd_test_runner?useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "password";
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final int DEFAULT_MINIMUM_IDLE = 5;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final long DEFAULT_IDLE_TIMEOUT = 600000; // 10 minutes
    private static final long DEFAULT_MAX_LIFETIME = 1800000; // 30 minutes
    
    /**
     * Private constructor for singleton pattern
     */
    private DatabaseUtil() {
        initializeDataSource();
    }
    
    /**
     * Get singleton instance of DatabaseUtil
     * @return DatabaseUtil instance
     */
    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }
    
    /**
     * Initialize HikariCP data source with configuration
     */
    private void initializeDataSource() {
        try {
            Properties props = loadDatabaseProperties();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", DEFAULT_DB_URL));
            config.setUsername(props.getProperty("db.username", DEFAULT_DB_USERNAME));
            config.setPassword(props.getProperty("db.password", DEFAULT_DB_PASSWORD));
            config.setDriverClassName(props.getProperty("db.driver", DEFAULT_DB_DRIVER));
            
            // Connection pool settings
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.max_size", String.valueOf(DEFAULT_MAXIMUM_POOL_SIZE))));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.min_idle", String.valueOf(DEFAULT_MINIMUM_IDLE))));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connection_timeout", String.valueOf(DEFAULT_CONNECTION_TIMEOUT))));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idle_timeout", String.valueOf(DEFAULT_IDLE_TIMEOUT))));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.max_lifetime", String.valueOf(DEFAULT_MAX_LIFETIME))));
            
            // Additional HikariCP settings
            config.setPoolName("BDDTestRunnerPool");
            config.setLeakDetectionThreshold(60000); // 1 minute
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            logger.info("Database connection pool initialized successfully");
            logger.info("Database URL: {}", maskPassword(config.getJdbcUrl()));
            
            // Test connection
            testConnection();
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Load database properties from configuration file
     * @return Properties object with database configuration
     */
    private Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
                logger.info("Database properties loaded from database.properties");
            } else {
                logger.info("database.properties not found, using default configuration");
            }
        } catch (IOException e) {
            logger.warn("Failed to load database.properties, using default configuration", e);
        }
        
        return props;
    }
    
    /**
     * Get database connection from pool
     * @return Connection instance
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Test database connection
     * @throws SQLException if connection test fails
     */
    public void testConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (connection.isValid(5)) {
                logger.info("Database connection test successful");
            } else {
                throw new SQLException("Database connection test failed");
            }
        }
    }
    
    /**
     * Execute database schema initialization script
     * @throws SQLException if schema initialization fails
     */
    public void initializeSchema() throws SQLException {
        logger.info("Initializing database schema...");
        
        String[] createTableStatements = {
            // Create scenario table
            """
            CREATE TABLE IF NOT EXISTS scenario (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                feature_name VARCHAR(255) NOT NULL,
                scenario_name VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY unique_scenario (feature_name, scenario_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """,
            
            // Create step table
            """
            CREATE TABLE IF NOT EXISTS step (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                scenario_id BIGINT NOT NULL,
                step_type VARCHAR(10) NOT NULL,
                step_text TEXT NOT NULL,
                step_order INT NOT NULL,
                FOREIGN KEY (scenario_id) REFERENCES scenario(id) ON DELETE CASCADE,
                INDEX idx_scenario_order (scenario_id, step_order)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """,
            
            // Create execution_result table
            """
            CREATE TABLE IF NOT EXISTS execution_result (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                scenario_id BIGINT NOT NULL,
                status VARCHAR(20) NOT NULL,
                log_path VARCHAR(500),
                screenshot_path VARCHAR(500),
                error_message TEXT,
                execution_time_ms BIGINT DEFAULT 0,
                executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (scenario_id) REFERENCES scenario(id) ON DELETE CASCADE,
                INDEX idx_scenario_executed (scenario_id, executed_at DESC)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """
        };
        
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                for (String sql : createTableStatements) {
                    statement.executeUpdate(sql);
                    logger.debug("Executed: {}", sql.replaceAll("\\s+", " ").trim());
                }
                
                connection.commit();
                logger.info("Database schema initialized successfully");
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Close database connection pool
     */
    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
    
    /**
     * Get connection pool statistics
     * @return formatted string with pool statistics
     */
    public String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "DataSource not initialized";
    }
    
    /**
     * Check if data source is initialized and healthy
     * @return true if data source is ready
     */
    public boolean isHealthy() {
        try {
            return dataSource != null && !dataSource.isClosed() && dataSource.getConnection().isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Mask password in database URL for logging
     * @param url database URL
     * @return URL with masked password
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^&;]*", "password=***");
    }
    
    /**
     * Shutdown hook to close data source on application exit
     */
    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.closeDataSource();
            }
        }));
    }
}
