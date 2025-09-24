package com.fadhli.automation;

import com.fadhli.automation.ui.MainWindow;
import com.fadhli.automation.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for BDD Test Runner
 * Desktop application using Swing GUI with MySQL database backend
 */
public class BddTestRunnerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(BddTestRunnerApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting BDD Test Runner Application...");
        
        try {
            // Set system properties for better UI experience
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Set Look and Feel to system default
            setLookAndFeel();
            
            // Initialize database
            initializeDatabase();
            
            // Register shutdown hook for cleanup
            DatabaseUtil.registerShutdownHook();
            
            // Launch GUI on Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                try {
                    MainWindow mainWindow = new MainWindow();
                    mainWindow.setVisible(true);
                    logger.info("BDD Test Runner Application started successfully");
                } catch (Exception e) {
                    logger.error("Failed to initialize main window", e);
                    showErrorDialog("Failed to start application", e.getMessage());
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to start BDD Test Runner Application", e);
            showErrorDialog("Application Startup Failed", e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Set Look and Feel to system default or fallback to cross-platform
     */
    private static void setLookAndFeel() {
        try {
            // Try to set system Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.debug("Using system Look and Feel: {}", UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            logger.warn("Failed to set system Look and Feel, using default", e);
            try {
                // Fallback to cross-platform Look and Feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                logger.warn("Failed to set cross-platform Look and Feel", ex);
            }
        }
    }
    
    /**
     * Initialize database connection and schema
     */
    private static void initializeDatabase() {
        try {
            logger.info("Initializing database connection...");
            DatabaseUtil dbUtil = DatabaseUtil.getInstance();
            
            // Test connection
            dbUtil.testConnection();
            
            // Initialize schema
            dbUtil.initializeSchema();
            
            logger.info("Database initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed. Please check MySQL server and configuration.", e);
        }
    }
    
    /**
     * Show error dialog to user
     * @param title dialog title
     * @param message error message
     */
    private static void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * Get application information
     * @return application info string
     */
    public static String getApplicationInfo() {
        return String.format(
            "BDD Test Runner v1.0.0%nJava Version: %s%nOS: %s %s%nArch: %s",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch")
        );
    }
}
