package com.fadhli.automation.ui;

import com.fadhli.automation.model.Scenario;
import com.fadhli.automation.model.Step;
import com.fadhli.automation.service.ScenarioService;
import com.fadhli.automation.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for creating and editing BDD test scenarios
 * Provides dynamic step management with add/remove functionality
 */
public class ScenarioFormDialog extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(ScenarioFormDialog.class);
    
    // Service layer
    private final ScenarioService scenarioService;
    
    // Dialog state
    private final boolean isEditMode;
    private Scenario currentScenario;
    private boolean saved = false;
    
    // UI Components
    private JTextField featureNameField;
    private JTextField scenarioNameField;
    private JPanel stepsPanel;
    private JScrollPane stepsScrollPane;
    private JButton addStepButton;
    private JButton saveButton;
    private JButton cancelButton;
    
    // Step management
    private final List<StepRow> stepRows = new ArrayList<>();
    private int nextStepOrder = 1;
    
    /**
     * Constructor for creating new scenario
     */
    public ScenarioFormDialog(Window parent, ScenarioService scenarioService) {
        this(parent, scenarioService, null);
    }
    
    /**
     * Constructor for editing existing scenario
     */
    public ScenarioFormDialog(Window parent, ScenarioService scenarioService, Scenario scenario) {
        super(parent, scenario == null ? "Create New Scenario" : "Edit Scenario", ModalityType.APPLICATION_MODAL);
        
        this.scenarioService = scenarioService;
        this.isEditMode = (scenario != null);
        this.currentScenario = scenario;
        
        initializeDialog();
        setupComponents();
        setupEventHandlers();
        
        if (isEditMode && scenario != null) {
            populateFields(scenario);
        } else {
            addInitialStep();
        }
        
        pack();
        setLocationRelativeTo(parent);
        
        logger.debug("Scenario form dialog initialized in {} mode", isEditMode ? "edit" : "create");
    }
    
    /**
     * Initialize dialog properties
     */
    private void initializeDialog() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(700, 500));
        setResizable(true);
    }
    
    /**
     * Setup UI components
     */
    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header panel with scenario info
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Steps panel
        JPanel stepsContainer = createStepsPanel();
        mainPanel.add(stepsContainer, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Create header panel with scenario information fields
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Scenario Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Feature Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Feature Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        featureNameField = new JTextField(30);
        featureNameField.setToolTipText("Enter the feature name (e.g., 'User Authentication')");
        panel.add(featureNameField, gbc);
        
        // Scenario Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Scenario Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        scenarioNameField = new JTextField(30);
        scenarioNameField.setToolTipText("Enter the scenario name (e.g., 'Successful login with valid credentials')");
        panel.add(scenarioNameField, gbc);
        
        return panel;
    }
    
    /**
     * Create steps panel with dynamic step management
     */
    private JPanel createStepsPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new TitledBorder("Test Steps"));
        
        // Steps panel (will hold step rows)
        stepsPanel = new JPanel();
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        
        // Scroll pane for steps
        stepsScrollPane = new JScrollPane(stepsPanel);
        stepsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        stepsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        stepsScrollPane.setPreferredSize(new Dimension(650, 200));
        
        container.add(stepsScrollPane, BorderLayout.CENTER);
        
        // Add step button
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addStepButton = new JButton("Add Step");
        addStepButton.setIcon(createIcon("add"));
        addStepButton.setToolTipText("Add a new test step");
        addButtonPanel.add(addStepButton);
        
        container.add(addButtonPanel, BorderLayout.SOUTH);
        
        return container;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton(isEditMode ? "Update" : "Create");
        saveButton.setIcon(createIcon("save"));
        saveButton.setPreferredSize(new Dimension(100, 35));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setIcon(createIcon("cancel"));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        panel.add(saveButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(cancelButton);
        
        return panel;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Add step button
        addStepButton.addActionListener(e -> addStepRow());
        
        // Save button
        saveButton.addActionListener(e -> saveScenario());
        
        // Cancel button
        cancelButton.addActionListener(e -> {
            if (hasUnsavedChanges()) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes. Are you sure you want to cancel?",
                    "Confirm Cancel",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                }
            } else {
                dispose();
            }
        });
        
        // Window closing
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelButton.doClick();
            }
        });
    }
    
    /**
     * Add initial empty step
     */
    private void addInitialStep() {
        addStepRow();
    }
    
    /**
     * Add new step row
     */
    private void addStepRow() {
        StepRow stepRow = new StepRow(nextStepOrder++);
        stepRows.add(stepRow);
        stepsPanel.add(stepRow.getPanel());
        
        refreshStepsPanel();
        
        // Focus on the new step text field
        SwingUtilities.invokeLater(() -> stepRow.getStepTextField().requestFocus());
    }
    
    /**
     * Remove step row
     */
    private void removeStepRow(StepRow stepRow) {
        stepRows.remove(stepRow);
        stepsPanel.remove(stepRow.getPanel());
        
        // Renumber remaining steps
        for (int i = 0; i < stepRows.size(); i++) {
            stepRows.get(i).setStepNumber(i + 1);
        }
        
        nextStepOrder = stepRows.size() + 1;
        refreshStepsPanel();
    }
    
    /**
     * Refresh steps panel
     */
    private void refreshStepsPanel() {
        stepsPanel.revalidate();
        stepsPanel.repaint();
        
        // Scroll to bottom if needed
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = stepsScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
    
    /**
     * Populate fields with existing scenario data
     */
    private void populateFields(Scenario scenario) {
        featureNameField.setText(scenario.getFeatureName());
        scenarioNameField.setText(scenario.getScenarioName());
        
        // Clear existing step rows
        stepRows.clear();
        stepsPanel.removeAll();
        nextStepOrder = 1;
        
        // Add steps from scenario
        if (scenario.getSteps() != null && !scenario.getSteps().isEmpty()) {
            for (Step step : scenario.getSteps()) {
                StepRow stepRow = new StepRow(step.getStepOrder());
                stepRow.setStepType(step.getStepType());
                stepRow.setStepText(step.getStepText());
                
                stepRows.add(stepRow);
                stepsPanel.add(stepRow.getPanel());
            }
            nextStepOrder = scenario.getSteps().size() + 1;
        } else {
            addInitialStep();
        }
        
        refreshStepsPanel();
    }
    
    /**
     * Save scenario
     */
    private void saveScenario() {
        try {
            // Validate input
            if (!validateInput()) {
                return;
            }
            
            // Create or update scenario
            Scenario scenario = buildScenarioFromForm();
            
            if (isEditMode) {
                // Update existing scenario
                scenario.setId(currentScenario.getId());
                scenario.setCreatedAt(currentScenario.getCreatedAt());
                scenarioService.updateScenario(scenario);
                logger.info("Scenario updated successfully: {}", scenario.getScenarioName());
            } else {
                // Create new scenario
                scenarioService.createScenario(scenario);
                logger.info("Scenario created successfully: {}", scenario.getScenarioName());
            }
            
            saved = true;
            dispose();
            
        } catch (ServiceException e) {
            logger.error("Failed to save scenario", e);
            showErrorDialog("Save Error", "Failed to save scenario: " + e.getMessage());
        }
    }
    
    /**
     * Validate form input
     */
    private boolean validateInput() {
        // Validate feature name
        if (featureNameField.getText().trim().isEmpty()) {
            showErrorDialog("Validation Error", "Feature name is required.");
            featureNameField.requestFocus();
            return false;
        }
        
        // Validate scenario name
        if (scenarioNameField.getText().trim().isEmpty()) {
            showErrorDialog("Validation Error", "Scenario name is required.");
            scenarioNameField.requestFocus();
            return false;
        }
        
        // Validate steps
        if (stepRows.isEmpty()) {
            showErrorDialog("Validation Error", "At least one test step is required.");
            return false;
        }
        
        // Validate each step
        for (int i = 0; i < stepRows.size(); i++) {
            StepRow stepRow = stepRows.get(i);
            if (stepRow.getStepType() == null) {
                showErrorDialog("Validation Error", "Step type is required for step " + (i + 1) + ".");
                return false;
            }
            if (stepRow.getStepText().trim().isEmpty()) {
                showErrorDialog("Validation Error", "Step text is required for step " + (i + 1) + ".");
                stepRow.getStepTextField().requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Build scenario object from form data
     */
    private Scenario buildScenarioFromForm() {
        Scenario scenario = new Scenario();
        scenario.setFeatureName(featureNameField.getText().trim());
        scenario.setScenarioName(scenarioNameField.getText().trim());
        
        // Build steps
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < stepRows.size(); i++) {
            StepRow stepRow = stepRows.get(i);
            Step step = new Step();
            step.setStepType(stepRow.getStepType());
            step.setStepText(stepRow.getStepText().trim());
            step.setStepOrder(i + 1);
            steps.add(step);
        }
        
        scenario.setSteps(steps);
        return scenario;
    }
    
    /**
     * Check if there are unsaved changes
     */
    private boolean hasUnsavedChanges() {
        if (saved) {
            return false;
        }
        
        // Check if any field has content
        if (!featureNameField.getText().trim().isEmpty() ||
            !scenarioNameField.getText().trim().isEmpty() ||
            !stepRows.isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Create simple icon
     */
    private Icon createIcon(String type) {
        // Simple colored icons (same as MainWindow)
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
            case "remove" -> {
                g2d.setColor(Color.RED);
                g2d.fillOval(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.drawLine(5, 8, 11, 8);
            }
            case "save" -> {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(2, 2, 12, 12);
            }
            case "cancel" -> {
                g2d.setColor(Color.GRAY);
                g2d.fillOval(2, 2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.drawLine(5, 5, 11, 11);
                g2d.drawLine(5, 11, 11, 5);
            }
            default -> {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(2, 2, 12, 12);
            }
        }
        
        g2d.dispose();
        return new ImageIcon(icon);
    }
    
    /**
     * Get whether the scenario was saved
     */
    public boolean isSaved() {
        return saved;
    }
    
    /**
     * Inner class representing a step row in the form
     */
    private class StepRow {
        private final JPanel panel;
        private final JLabel stepLabel;
        private final JComboBox<Step.StepType> stepTypeCombo;
        private final JTextField stepTextField;
        private final JButton removeButton;
        private int stepNumber;
        
        public StepRow(int stepNumber) {
            this.stepNumber = stepNumber;
            this.panel = new JPanel(new BorderLayout(5, 5));
            this.panel.setBorder(new EmptyBorder(2, 2, 2, 2));
            
            // Step number label
            this.stepLabel = new JLabel(String.valueOf(stepNumber));
            this.stepLabel.setPreferredSize(new Dimension(25, 25));
            this.stepLabel.setHorizontalAlignment(JLabel.CENTER);
            this.stepLabel.setBorder(BorderFactory.createEtchedBorder());
            
            // Step type combo
            this.stepTypeCombo = new JComboBox<>(Step.StepType.values());
            this.stepTypeCombo.setPreferredSize(new Dimension(80, 25));
            this.stepTypeCombo.setSelectedIndex(0); // Default to Given
            
            // Step text field
            this.stepTextField = new JTextField();
            this.stepTextField.setToolTipText("Enter the step description (e.g., 'I open the login page')");
            
            // Remove button
            this.removeButton = new JButton(createIcon("remove"));
            this.removeButton.setPreferredSize(new Dimension(25, 25));
            this.removeButton.setToolTipText("Remove this step");
            this.removeButton.addActionListener(e -> removeStepRow(this));
            
            // Layout
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            leftPanel.add(stepLabel);
            leftPanel.add(stepTypeCombo);
            
            panel.add(leftPanel, BorderLayout.WEST);
            panel.add(stepTextField, BorderLayout.CENTER);
            panel.add(removeButton, BorderLayout.EAST);
        }
        
        public JPanel getPanel() {
            return panel;
        }
        
        public JTextField getStepTextField() {
            return stepTextField;
        }
        
        public Step.StepType getStepType() {
            return (Step.StepType) stepTypeCombo.getSelectedItem();
        }
        
        public void setStepType(Step.StepType stepType) {
            stepTypeCombo.setSelectedItem(stepType);
        }
        
        public String getStepText() {
            return stepTextField.getText();
        }
        
        public void setStepText(String text) {
            stepTextField.setText(text);
        }
        
        public void setStepNumber(int number) {
            this.stepNumber = number;
            stepLabel.setText(String.valueOf(number));
        }
        
        public int getStepNumber() {
            return stepNumber;
        }
    }
}