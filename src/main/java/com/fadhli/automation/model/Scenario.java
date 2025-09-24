package com.fadhli.automation.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity model for Scenario table
 * Represents a test scenario with its associated steps
 */
public class Scenario {
    
    private Long id;
    private String featureName;
    private String scenarioName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // One-to-many relationship with Step
    private List<Step> steps;
    
    // Step count for performance optimization (avoiding loading all steps)
    private Integer stepCount;
    
    // One-to-many relationship with ExecutionResult
    private List<ExecutionResult> executionResults;
    
    /**
     * Default constructor
     */
    public Scenario() {
        this.steps = new ArrayList<>();
        this.executionResults = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with feature name and scenario name
     * @param featureName the feature name
     * @param scenarioName the scenario name
     */
    public Scenario(String featureName, String scenarioName) {
        this();
        this.featureName = featureName;
        this.scenarioName = scenarioName;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFeatureName() {
        return featureName;
    }
    
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
    
    public String getScenarioName() {
        return scenarioName;
    }
    
    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Step> getSteps() {
        return steps;
    }
    
    public void setSteps(List<Step> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
    
    public List<ExecutionResult> getExecutionResults() {
        return executionResults;
    }
    
    public void setExecutionResults(List<ExecutionResult> executionResults) {
        this.executionResults = executionResults != null ? executionResults : new ArrayList<>();
    }
    
    public Integer getStepCount() {
        return stepCount;
    }
    
    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }
    
    // Helper methods
    
    /**
     * Add a step to this scenario
     * @param step the step to add
     */
    public void addStep(Step step) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        step.setScenarioId(this.id);
        this.steps.add(step);
    }
    
    /**
     * Remove a step from this scenario
     * @param step the step to remove
     */
    public void removeStep(Step step) {
        if (this.steps != null) {
            this.steps.remove(step);
        }
    }
    
    /**
     * Update the updatedAt timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Generate Gherkin feature content from this scenario
     * @return Gherkin feature file content
     */
    public String toGherkinFeature() {
        StringBuilder gherkin = new StringBuilder();
        gherkin.append("Feature: ").append(featureName).append("\n\n");
        gherkin.append("  Scenario: ").append(scenarioName).append("\n");
        
        if (steps != null && !steps.isEmpty()) {
            steps.stream()
                    .sorted((s1, s2) -> Integer.compare(s1.getStepOrder(), s2.getStepOrder()))
                    .forEach(step -> gherkin.append("    ")
                            .append(step.getStepType())
                            .append(" ")
                            .append(step.getStepText())
                            .append("\n"));
        }
        
        return gherkin.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return Objects.equals(id, scenario.id) &&
                Objects.equals(featureName, scenario.featureName) &&
                Objects.equals(scenarioName, scenario.scenarioName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, featureName, scenarioName);
    }
    
    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", featureName='" + featureName + '\'' +
                ", scenarioName='" + scenarioName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", stepsCount=" + (stepCount != null ? stepCount : (steps != null ? steps.size() : 0)) +
                '}';
    }
}