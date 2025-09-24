package com.fadhli.automation.ui;

import com.fadhli.automation.executor.CucumberTestEngine;
import com.fadhli.automation.model.ExecutionResult;
import com.fadhli.automation.model.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for displaying test execution progress and results
 * Shows real-time log output and execution status
 */
public class ExecutionConsoleDialog extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutionConsoleDialog.class);
    
    // UI Components
    private JLabel scenarioInfoLabel;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JTextArea logTextArea;
    private JScrollPane logScrollPane;
    private JButton closeButton;
    private JButton viewReportButton;
    private JButton viewScreenshotButton;
    
    // Execution data
    private final Scenario scenario;
    private ExecutionResult executionResult;
    private boolean executionCompleted = false;
    private CucumberTestEngine executionEngine;
    private Thread executionThread;
    private Thread timeoutWatcherThread;
    
    // Execution timeout (in milliseconds) - 5 minutes max per scenario
    private static final long EXECUTION_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    
    /**
     * Constructor
     * @param parent parent window
     * @param scenario scenario to execute
     */
    public ExecutionConsoleDialog(Window parent, Scenario scenario) {
        super(parent, "Test Execution Console", ModalityType.MODELESS);
        
        this.scenario = scenario;
        
        initializeDialog();
        setupComponents();
        setupEventHandlers();
        
        setLocationRelativeTo(parent);
        
        logger.debug("Execution console dialog initialized for scenario: {}", scenario.getScenarioName());
    }
    
    /**
     * Initialize dialog properties
     */
    private void initializeDialog() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setResizable(true);
    }
    
    /**
     * Setup UI components
     */
    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header panel with scenario info and status
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with log output
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Create header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Scenario info
        scenarioInfoLabel = new JLabel(String.format(
            "<html><b>Feature:</b> %s<br><b>Scenario:</b> %s</html>",
            scenario.getFeatureName(),
            scenario.getScenarioName()
        ));
        scenarioInfoLabel.setFont(scenarioInfoLabel.getFont().deriveFont(14f));
        panel.add(scenarioInfoLabel, BorderLayout.NORTH);
        
        // Status and progress
        JPanel statusPanel = new JPanel(new BorderLayout(10, 5));
        
        statusLabel = new JLabel("Ready to execute");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Waiting to start...");
        statusPanel.add(progressBar, BorderLayout.CENTER);
        
        panel.add(statusPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create center panel with log output
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Log text area
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        logTextArea.setBackground(Color.BLACK);
        logTextArea.setForeground(Color.GREEN);
        logTextArea.setCaretColor(Color.GREEN);
        
        // Scroll pane
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Execution Log"));
        
        panel.add(logScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // View Report button (initially disabled)
        viewReportButton = new JButton("View Report");
        viewReportButton.setIcon(createIcon("report"));
        viewReportButton.setEnabled(false);
        viewReportButton.setToolTipText("View detailed execution report");
        
        // View Screenshot button (initially disabled)
        viewScreenshotButton = new JButton("View Screenshot");
        viewScreenshotButton.setIcon(createIcon("screenshot"));
        viewScreenshotButton.setEnabled(false);
        viewScreenshotButton.setToolTipText("View screenshot (if test failed)");
        
        // Close button
        closeButton = new JButton("Close");
        closeButton.setIcon(createIcon("close"));
        closeButton.setPreferredSize(new Dimension(100, 35));
        
        panel.add(viewReportButton);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(viewScreenshotButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(closeButton);
        
        return panel;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        closeButton.addActionListener(e -> dispose());
        
        viewReportButton.addActionListener(e -> viewReport());
        
        viewScreenshotButton.addActionListener(e -> viewScreenshot());
        
        // Window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!executionCompleted) {
                    int result = JOptionPane.showConfirmDialog(
                        ExecutionConsoleDialog.this,
                        "Test execution is still running. Are you sure you want to close?",
                        "Confirm Close",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (result == JOptionPane.NO_OPTION) {
                        return;
                    }
                    
                    // Stop execution if running
                    stopExecution();
                }
                dispose();
            }
        });
    }
    
    /**
     * Start real test execution with Selenium
     */
    public void startExecution() {
        logger.info("Starting test execution for scenario: {}", scenario.getScenarioName());
        
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Initializing test execution...");
            statusLabel.setForeground(Color.BLUE);
            progressBar.setIndeterminate(true);
            progressBar.setString("Preparing to run...");
            
            // Add initial log
            appendLog("=== BDD Test Execution Started ===");
            appendLog("Feature: " + scenario.getFeatureName());
            appendLog("Scenario: " + scenario.getScenarioName());
            appendLog("");
        });
        
        // Start real execution in background thread
        startRealExecution();
    }
    
    /**
     * Start real test execution with Selenium
     */
    private void startRealExecution() {
        // Create execution engine with configuration
        CucumberTestEngine.ExecutionConfig config = new CucumberTestEngine.ExecutionConfig();
        config.setHeadless(false); // Show browser for demo purposes
        config.setTimeoutSeconds(15);
        config.setStepDelayMs(1500); // Slower for better visibility
        
        executionEngine = new CucumberTestEngine(config);
        
        // Set up callbacks for UI updates
        executionEngine.setStatusCallback(this::updateExecutionStatus);
        executionEngine.setLogCallback(this::appendLog);
        executionEngine.setProgressCallback(this::updateExecutionProgress);
        
        // Execute in background thread
        executionThread = new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(100);
                });
                
                // Execute scenario with real Selenium
                ExecutionResult result = executionEngine.executeScenario(scenario);
                
                // Complete execution on UI thread
                SwingUtilities.invokeLater(() -> {
                    this.executionResult = result;
                    completeRealExecution(result);
                });
                
            } catch (Exception e) {
                logger.error("Test execution failed", e);
                SwingUtilities.invokeLater(() -> {
                    completeExecutionWithError("Execution failed: " + e.getMessage());
                });
            }
        });
        
        executionThread.setDaemon(true);
        executionThread.start();
    }
    
    /**
     * Complete real test execution
     */
    private void completeRealExecution(ExecutionResult result) {
        executionCompleted = true;
        progressBar.setValue(100);
        
        boolean success = (result.getStatus() == ExecutionResult.ExecutionStatus.PASS);
        
        if (success) {
            statusLabel.setText("✓ Test PASSED");
            statusLabel.setForeground(new Color(0, 150, 0)); // Dark green
            progressBar.setString("Execution completed successfully");
            appendLog("");
            appendLog("=== TEST PASSED ===");
            appendLog("All steps executed successfully");
        } else {
            statusLabel.setText("✗ Test FAILED");
            statusLabel.setForeground(Color.RED);
            progressBar.setString("Execution failed");
            appendLog("");
            appendLog("=== TEST FAILED ===");
            if (result.getErrorMessage() != null) {
                appendLog("Error: " + result.getErrorMessage());
            }
        }
        
        appendLog("Execution time: " + result.getFormattedExecutionTime());
        appendLog("Log file: " + (result.getLogPath() != null ? result.getLogPath() : "N/A"));
        if (result.getScreenshotPath() != null) {
            appendLog("Screenshot: " + result.getScreenshotPath());
        }
        appendLog("=== Execution Complete ===");
        
        // Enable result viewing buttons
        updateResultButtons();
    }
    
    /**
     * Complete execution with error
     */
    private void completeExecutionWithError(String errorMessage) {
        executionCompleted = true;
        progressBar.setValue(0);
        
        statusLabel.setText("✗ Execution ERROR");
        statusLabel.setForeground(Color.RED);
        progressBar.setString("Execution error");
        
        appendLog("");
        appendLog("=== EXECUTION ERROR ===");
        appendLog(errorMessage);
        appendLog("=== Execution Aborted ===");
    }
    
    /**
     * Update result viewing buttons
     */
    private void updateResultButtons() {
        if (executionResult != null) {
            viewReportButton.setEnabled(executionResult.hasLogFile());
            viewScreenshotButton.setEnabled(executionResult.hasScreenshot());
        }
    }
    
    /**
     * View execution report
     */
    private void viewReport() {
        if (executionResult != null && executionResult.hasLogFile()) {
            try {
                // TODO: Open log file in text editor or show in dialog
                showInfoDialog("View Report", 
                    "Report file: " + executionResult.getLogPath() + "\n\n" +
                    "This functionality will open the log file in your default text editor.");
                
            } catch (Exception e) {
                logger.error("Failed to open report file", e);
                showErrorDialog("Error", "Failed to open report file: " + e.getMessage());
            }
        }
    }
    
    /**
     * View screenshot
     */
    private void viewScreenshot() {
        if (executionResult != null && executionResult.hasScreenshot()) {
            try {
                // TODO: Display screenshot in image viewer dialog
                showInfoDialog("View Screenshot", 
                    "Screenshot file: " + executionResult.getScreenshotPath() + "\n\n" +
                    "This functionality will display the failure screenshot.");
                
            } catch (Exception e) {
                logger.error("Failed to open screenshot file", e);
                showErrorDialog("Error", "Failed to open screenshot: " + e.getMessage());
            }
        }
    }
    
    /**
     * Update execution status (callback for TestExecutionEngine)
     */
    private void updateExecutionStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            progressBar.setString(status);
        });
    }
    
    /**
     * Update execution progress (callback for TestExecutionEngine)
     */
    private void updateExecutionProgress(int progress) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            if (progress > 0 && progress < 100) {
                progressBar.setString("Progress: " + progress + "%");
            }
        });
    }
    
    /**
     * Append log message
     */
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logTextArea.append("[" + timestamp + "] " + message + "\n");
            
            // Auto-scroll to bottom
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }
    
    /**
     * Show info dialog
     */
    private void showInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Stop test execution
     */
    private void stopExecution() {
        if (executionEngine != null && !executionCompleted) {
            appendLog("Stopping test execution...");
            executionEngine.stopExecution();
        }
        
        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
        }
    }
    
    /**
     * Create simple icon
     */
    private Icon createIcon(String type) {
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = icon.createGraphics();
        
        switch (type) {
            case "report" -> {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(4, 4, 8, 8);
                g2d.setColor(Color.BLUE);
                g2d.drawLine(5, 6, 10, 6);
                g2d.drawLine(5, 8, 9, 8);
                g2d.drawLine(5, 10, 11, 10);
            }
            case "screenshot" -> {
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(4, 4, 8, 6);
                g2d.setColor(Color.GRAY);
                g2d.fillOval(6, 11, 4, 2);
            }
            case "close" -> {
                g2d.setColor(Color.RED);
                g2d.fillOval(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.drawLine(5, 5, 11, 11);
                g2d.drawLine(5, 11, 11, 5);
            }
            default -> {
                g2d.setColor(Color.GRAY);
                g2d.fillRect(2, 2, 12, 12);
            }
        }
        
        g2d.dispose();
        return new ImageIcon(icon);
    }
    
    /**
     * Get execution result
     */
    public ExecutionResult getExecutionResult() {
        return executionResult;
    }
    
    /**
     * Check if execution is completed
     */
    public boolean isExecutionCompleted() {
        return executionCompleted;
    }
}