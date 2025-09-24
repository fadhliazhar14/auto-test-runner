package com.fadhli.automation.ui;

import com.fadhli.automation.BddTestRunnerApplication;
import com.fadhli.automation.model.Scenario;
import com.fadhli.automation.service.ScenarioService;
import com.fadhli.automation.service.ServiceException;
import com.fadhli.automation.service.impl.ScenarioServiceImpl;
import com.fadhli.automation.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main Window for BDD Test Runner application
 * Provides the primary user interface with scenario table and action buttons
 */
public class MainWindow extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    // Service Layer
    private final ScenarioService scenarioService;
    
    // UI Components
    private JTable scenarioTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton runButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    
    // Table columns
    private static final String[] COLUMN_NAMES = {
        "ID", "Feature Name", "Scenario Name", "Steps", "Last Updated", "Status"
    };
    
    /**
     * Constructor - initializes the main window
     */
    public MainWindow() {
        // Initialize service layer
        this.scenarioService = new ScenarioServiceImpl();
        
        initializeUI();
        setupEventHandlers();
        loadScenarios();
        
        logger.info("Main window initialized successfully");
    }
    
    /**
     * Initialize UI components and layout
     */
    private void initializeUI() {
        // Window settings
        setTitle("BDD Test Runner - Scenario Management");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setIconImage(createApplicationIcon());
        
        // Create menu bar
        setJMenuBar(createMenuBar());
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center panel with scenario table
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Create menu bar with File, Tools, and Help menus
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem refreshItem = new JMenuItem("Refresh", 'R');
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> loadScenarios());
        fileMenu.add(refreshItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit", 'X');
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        
        JMenuItem dbStatusItem = new JMenuItem("Database Status", 'D');
        dbStatusItem.addActionListener(e -> showDatabaseStatus());
        toolsMenu.add(dbStatusItem);
        
        JMenuItem settingsItem = new JMenuItem("Settings", 'S');
        settingsItem.addActionListener(e -> showSettings());
        toolsMenu.add(settingsItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About", 'A');
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Create title panel with application header
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel("BDD Test Runner - Scenario Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 25, 112));
        
        JLabel subtitleLabel = new JLabel("Create, edit, and execute BDD test scenarios");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel);
        
        return panel;
    }
    
    /**
     * Create center panel with scenario table
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Table model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Table setup
        scenarioTable = new JTable(tableModel);
        scenarioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scenarioTable.setRowHeight(25);
        scenarioTable.getTableHeader().setReorderingAllowed(false);
        
        // Column widths
        scenarioTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        scenarioTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Feature Name
        scenarioTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Scenario Name
        scenarioTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Steps
        scenarioTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Last Updated
        scenarioTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Status
        
        // Selection listener
        scenarioTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click to edit
        scenarioTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedScenario();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(scenarioTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Test Scenarios"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create button panel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        // Create Button
        createButton = new JButton("Create New");
        createButton.setIcon(createIcon("add"));
        createButton.setPreferredSize(new Dimension(120, 35));
        createButton.addActionListener(e -> createNewScenario());
        gbc.gridy = 0;
        panel.add(createButton, gbc);
        
        // Edit Button
        editButton = new JButton("Edit");
        editButton.setIcon(createIcon("edit"));
        editButton.setPreferredSize(new Dimension(120, 35));
        editButton.addActionListener(e -> editSelectedScenario());
        editButton.setEnabled(false);
        gbc.gridy = 1;
        panel.add(editButton, gbc);
        
        // Delete Button
        deleteButton = new JButton("Delete");
        deleteButton.setIcon(createIcon("delete"));
        deleteButton.setPreferredSize(new Dimension(120, 35));
        deleteButton.addActionListener(e -> deleteSelectedScenario());
        deleteButton.setEnabled(false);
        gbc.gridy = 2;
        panel.add(deleteButton, gbc);
        
        // Separator
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 0, 5, 0);
        panel.add(new JSeparator(), gbc);
        
        // Run Button
        runButton = new JButton("Run Test");
        runButton.setIcon(createIcon("run"));
        runButton.setPreferredSize(new Dimension(120, 35));
        runButton.setBackground(new Color(34, 139, 34));
//        runButton.setForeground(Color.WHITE);
        runButton.addActionListener(e -> runSelectedScenario());
        runButton.setEnabled(false);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(runButton, gbc);
        
        // Refresh Button
        refreshButton = new JButton("Refresh");
        refreshButton.setIcon(createIcon("refresh"));
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> loadScenarios());
        gbc.gridy = 5;
        panel.add(refreshButton, gbc);
        
        return panel;
    }
    
    /**
     * Create status panel
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel);
        
        return panel;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    /**
     * Load scenarios from database and populate table
     */
    private void loadScenarios() {
        SwingUtilities.invokeLater(() -> {
            try {
                setStatus("Loading scenarios...");
                
                // Clear existing data
                tableModel.setRowCount(0);
                
                // Load scenarios from database via service layer with step count
                List<Scenario> scenarios = scenarioService.findAllScenariosWithStepCount();
                
                // Populate table with scenario data
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (Scenario scenario : scenarios) {
                    Object[] rowData = {
                        scenario.getId(),
                        scenario.getFeatureName(),
                        scenario.getScenarioName(),
                        scenario.getStepCount() != null ? scenario.getStepCount() : 0,
                        scenario.getUpdatedAt() != null ? scenario.getUpdatedAt().format(formatter) : "",
                        "Ready"
                    };
                    tableModel.addRow(rowData);
                }
                
                setStatus(String.format("Loaded %d scenarios", scenarios.size()));
                updateButtonStates();
                
            } catch (ServiceException e) {
                logger.error("Failed to load scenarios", e);
                setStatus("Failed to load scenarios");
                // Fallback to sample data if database fails
                loadSampleData();
                setStatus("Loaded sample data (database unavailable)");
            } catch (Exception e) {
                logger.error("Unexpected error loading scenarios", e);
                setStatus("Failed to load scenarios");
                showErrorDialog("Load Error", "Failed to load scenarios: " + e.getMessage());
            }
        });
    }
    
    /**
     * Load sample data for testing
     */
    private void loadSampleData() {
        Object[][] sampleData = {
            {1L, "User Login", "Successful login with valid credentials", 3, "2025-09-22 14:30:00", "Ready"},
            {2L, "User Login", "Failed login with invalid password", 3, "2025-09-22 14:25:00", "Ready"},
            {3L, "Shopping Cart", "Add product to cart", 4, "2025-09-22 14:20:00", "Ready"},
            {4L, "Checkout", "Complete order with credit card", 6, "2025-09-22 14:15:00", "Ready"}
        };
        
        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
    }
    
    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = scenarioTable.getSelectedRow() >= 0;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        runButton.setEnabled(hasSelection);
    }
    
    /**
     * Create new scenario
     */
    private void createNewScenario() {
        logger.info("Creating new scenario");
        setStatus("Creating new scenario...");
        
        ScenarioFormDialog dialog = new ScenarioFormDialog(this, scenarioService);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadScenarios(); // Reload list to show new scenario
            setStatus("Scenario created successfully");
        } else {
            setStatus("Ready");
        }
    }
    
    /**
     * Edit selected scenario
     */
    private void editSelectedScenario() {
        int selectedRow = scenarioTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long scenarioId = (Long) tableModel.getValueAt(selectedRow, 0);
            logger.info("Editing scenario with ID: {}", scenarioId);
            setStatus("Loading scenario for editing...");
            
            try {
                // Load scenario with steps for editing
                Optional<Scenario> scenarioOpt = scenarioService.findScenarioByIdComplete(scenarioId);
                
                if (scenarioOpt.isPresent()) {
                    ScenarioFormDialog dialog = new ScenarioFormDialog(this, scenarioService, scenarioOpt.get());
                    dialog.setVisible(true);
                    
                    if (dialog.isSaved()) {
                        loadScenarios(); // Reload list to show updated data
                        setStatus("Scenario updated successfully");
                    } else {
                        setStatus("Ready");
                    }
                } else {
                    showErrorDialog("Edit Error", "Scenario not found with ID: " + scenarioId);
                    setStatus("Ready");
                }
                
            } catch (ServiceException e) {
                logger.error("Failed to load scenario for editing: {}", scenarioId, e);
                showErrorDialog("Edit Error", "Failed to load scenario: " + e.getMessage());
                setStatus("Ready");
            }
        }
    }
    
    /**
     * Delete selected scenario
     */
    private void deleteSelectedScenario() {
        int selectedRow = scenarioTable.getSelectedRow();
        if (selectedRow >= 0) {
            String scenarioName = (String) tableModel.getValueAt(selectedRow, 2);
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete scenario: " + scenarioName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                Long scenarioId = (Long) tableModel.getValueAt(selectedRow, 0);
                logger.info("Deleting scenario with ID: {}", scenarioId);
                setStatus("Deleting scenario...");
                
                try {
                    // Delete scenario via service layer
                    scenarioService.deleteScenario(scenarioId);
                    tableModel.removeRow(selectedRow);
                    setStatus("Scenario deleted successfully");
                    
                } catch (ServiceException e) {
                    logger.error("Failed to delete scenario ID: {}", scenarioId, e);
                    setStatus("Failed to delete scenario");
                    showErrorDialog("Delete Error", "Failed to delete scenario: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Run selected scenario
     */
    private void runSelectedScenario() {
        int selectedRow = scenarioTable.getSelectedRow();
        if (selectedRow >= 0) {
            Long scenarioId = (Long) tableModel.getValueAt(selectedRow, 0);
            String scenarioName = (String) tableModel.getValueAt(selectedRow, 2);
            logger.info("Running scenario: {} (ID: {})", scenarioName, scenarioId);
            setStatus("Loading scenario for execution...");
            
            try {
                // Load complete scenario with steps
                Optional<Scenario> scenarioOpt = scenarioService.findScenarioByIdComplete(scenarioId);
                
                if (scenarioOpt.isPresent()) {
                    Scenario scenario = scenarioOpt.get();
                    
                    // Show execution console
                    ExecutionConsoleDialog consoleDialog = new ExecutionConsoleDialog(this, scenario);
                    
                    // Start execution immediately after showing dialog
                    SwingUtilities.invokeLater(() -> {
                        consoleDialog.startExecution();
                        setStatus("Test execution started");
                    });
                    
                    // Show dialog (non-modal now)
                    consoleDialog.setVisible(true);
                    
                } else {
                    showErrorDialog("Execution Error", "Scenario not found with ID: " + scenarioId);
                    setStatus("Ready");
                }
                
            } catch (ServiceException e) {
                logger.error("Failed to load scenario for execution: {}", scenarioId, e);
                showErrorDialog("Execution Error", "Failed to load scenario: " + e.getMessage());
                setStatus("Ready");
            }
        }
    }
    
    /**
     * Show database status
     */
    private void showDatabaseStatus() {
        try {
            DatabaseUtil dbUtil = DatabaseUtil.getInstance();
            String status = dbUtil.isHealthy() ? "Connected" : "Disconnected";
            String poolStats = dbUtil.getPoolStats();
            
            String message = String.format(
                "Database Status: %s%n%n%s",
                status, poolStats
            );
            
            showInfoDialog("Database Status", message);
            
        } catch (Exception e) {
            logger.error("Failed to get database status", e);
            showErrorDialog("Database Error", "Failed to get database status: " + e.getMessage());
        }
    }
    
    /**
     * Show settings dialog
     */
    private void showSettings() {
        showInfoDialog("Settings", "Settings functionality will be implemented in future version.");
    }
    
    /**
     * Show about dialog
     */
    private void showAbout() {
        showInfoDialog("About BDD Test Runner", BddTestRunnerApplication.getApplicationInfo());
    }
    
    /**
     * Exit application
     */
    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            logger.info("Application exiting...");
            dispose();
            System.exit(0);
        }
    }
    
    /**
     * Set status bar text
     */
    private void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show info dialog
     */
    private void showInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Create simple application icon
     */
    private Image createApplicationIcon() {
        // Create a simple colored square as icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setColor(new Color(25, 25, 112));
        g2d.fillRect(0, 0, 32, 32);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("T", 10, 23);
        g2d.dispose();
        return icon;
    }
    
    /**
     * Create simple icon for buttons
     */
    private Icon createIcon(String type) {
        // Create simple colored icons
        BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = icon.createGraphics();
        
        switch (type) {
            case "add" -> {
                g2d.setColor(Color.GREEN);
                g2d.fillOval(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.drawLine(8, 5, 8, 11);
                g2d.drawLine(5, 8, 11, 8);
            }
            case "edit" -> {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(2, 2, 12, 12);
            }
            case "delete" -> {
                g2d.setColor(Color.RED);
                g2d.fillOval(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.drawLine(5, 5, 11, 11);
                g2d.drawLine(5, 11, 11, 5);
            }
            case "run" -> {
                g2d.setColor(new Color(34, 139, 34));
                int[] xPoints = {4, 12, 4};
                int[] yPoints = {2, 8, 14};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
            case "refresh" -> {
                g2d.setColor(Color.ORANGE);
                g2d.fillOval(2, 2, 12, 12);
            }
            default -> {
                g2d.setColor(Color.GRAY);
                g2d.fillRect(2, 2, 12, 12);
            }
        }
        
        g2d.dispose();
        return new ImageIcon(icon);
    }
}