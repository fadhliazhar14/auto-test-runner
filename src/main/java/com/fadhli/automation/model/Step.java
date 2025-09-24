package com.fadhli.automation.model;

import java.util.Objects;

/**
 * Entity model for Step table
 * Represents individual test steps within a scenario
 */
public class Step {
    
    private Long id;
    private Long scenarioId;
    private StepType stepType;
    private String stepText;
    private Integer stepOrder;
    
    /**
     * Enum for step types
     */
    public enum StepType {
        GIVEN("Given"),
        WHEN("When"),
        THEN("Then"),
        AND("And");
        
        private final String keyword;
        
        StepType(String keyword) {
            this.keyword = keyword;
        }
        
        public String getKeyword() {
            return keyword;
        }
        
        /**
         * Get StepType from string value
         * @param value the string value
         * @return corresponding StepType
         */
        public static StepType fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            
            for (StepType type : StepType.values()) {
                if (type.keyword.equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
            
            throw new IllegalArgumentException("Invalid step type: " + value);
        }
        
        @Override
        public String toString() {
            return keyword;
        }
    }
    
    /**
     * Default constructor
     */
    public Step() {
        this.stepOrder = 0;
    }
    
    /**
     * Constructor with all required fields
     * @param scenarioId the scenario ID this step belongs to
     * @param stepType the type of step (Given, When, Then, And)
     * @param stepText the step content
     * @param stepOrder the execution order
     */
    public Step(Long scenarioId, StepType stepType, String stepText, Integer stepOrder) {
        this.scenarioId = scenarioId;
        this.stepType = stepType;
        this.stepText = stepText;
        this.stepOrder = stepOrder;
    }
    
    /**
     * Constructor without scenario ID (for new steps)
     * @param stepType the type of step
     * @param stepText the step content
     * @param stepOrder the execution order
     */
    public Step(StepType stepType, String stepText, Integer stepOrder) {
        this(null, stepType, stepText, stepOrder);
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getScenarioId() {
        return scenarioId;
    }
    
    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }
    
    public StepType getStepType() {
        return stepType;
    }
    
    public void setStepType(StepType stepType) {
        this.stepType = stepType;
    }
    
    public String getStepText() {
        return stepText;
    }
    
    public void setStepText(String stepText) {
        this.stepText = stepText;
    }
    
    public Integer getStepOrder() {
        return stepOrder;
    }
    
    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }
    
    // Helper methods
    
    /**
     * Check if this step is valid
     * @return true if step has required fields
     */
    public boolean isValid() {
        return stepType != null && 
               stepText != null && 
               !stepText.trim().isEmpty() && 
               stepOrder != null && 
               stepOrder >= 0;
    }
    
    /**
     * Get the full step text with keyword
     * @return formatted step text (e.g., "Given I open the page")
     */
    public String getFullStepText() {
        if (stepType == null || stepText == null) {
            return "";
        }
        return stepType.getKeyword() + " " + stepText;
    }
    
    /**
     * Create a copy of this step
     * @return new Step instance with same values (excluding ID)
     */
    public Step copy() {
        Step copy = new Step(this.scenarioId, this.stepType, this.stepText, this.stepOrder);
        return copy;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(id, step.id) &&
                Objects.equals(scenarioId, step.scenarioId) &&
                stepType == step.stepType &&
                Objects.equals(stepText, step.stepText) &&
                Objects.equals(stepOrder, step.stepOrder);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, scenarioId, stepType, stepText, stepOrder);
    }
    
    @Override
    public String toString() {
        return "Step{" +
                "id=" + id +
                ", scenarioId=" + scenarioId +
                ", stepType=" + stepType +
                ", stepText='" + stepText + '\'' +
                ", stepOrder=" + stepOrder +
                '}';
    }
}