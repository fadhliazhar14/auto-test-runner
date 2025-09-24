package com.fadhli.automation.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity model for ExecutionResult table
 * Represents the result of a test scenario execution
 */
public class ExecutionResult {
    
    private Long id;
    private Long scenarioId;
    private ExecutionStatus status;
    private String logPath;
    private String screenshotPath;
    private LocalDateTime executedAt;
    private String errorMessage;
    private Long executionTimeMs;
    
    /**
     * Enum for execution status
     */
    public enum ExecutionStatus {
        PASS("PASS"),
        FAIL("FAIL"),
        SKIP("SKIP"),
        RUNNING("RUNNING");
        
        private final String status;
        
        ExecutionStatus(String status) {
            this.status = status;
        }
        
        public String getStatus() {
            return status;
        }
        
        /**
         * Get ExecutionStatus from string value
         * @param value the string value
         * @return corresponding ExecutionStatus
         */
        public static ExecutionStatus fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            
            for (ExecutionStatus status : ExecutionStatus.values()) {
                if (status.status.equalsIgnoreCase(value.trim())) {
                    return status;
                }
            }
            
            throw new IllegalArgumentException("Invalid execution status: " + value);
        }
        
        @Override
        public String toString() {
            return status;
        }
    }
    
    /**
     * Default constructor
     */
    public ExecutionResult() {
        this.executedAt = LocalDateTime.now();
        this.executionTimeMs = 0L;
    }
    
    /**
     * Constructor with scenario ID and status
     * @param scenarioId the scenario ID
     * @param status the execution status
     */
    public ExecutionResult(Long scenarioId, ExecutionStatus status) {
        this();
        this.scenarioId = scenarioId;
        this.status = status;
    }
    
    /**
     * Constructor with all main fields
     * @param scenarioId the scenario ID
     * @param status the execution status
     * @param logPath path to log file
     * @param screenshotPath path to screenshot file
     */
    public ExecutionResult(Long scenarioId, ExecutionStatus status, String logPath, String screenshotPath) {
        this(scenarioId, status);
        this.logPath = logPath;
        this.screenshotPath = screenshotPath;
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
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
    
    public String getLogPath() {
        return logPath;
    }
    
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
    
    public String getScreenshotPath() {
        return screenshotPath;
    }
    
    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    // Helper methods
    
    /**
     * Check if execution was successful
     * @return true if status is PASS
     */
    public boolean isSuccess() {
        return status == ExecutionStatus.PASS;
    }
    
    /**
     * Check if execution failed
     * @return true if status is FAIL
     */
    public boolean isFailed() {
        return status == ExecutionStatus.FAIL;
    }
    
    /**
     * Check if execution is currently running
     * @return true if status is RUNNING
     */
    public boolean isRunning() {
        return status == ExecutionStatus.RUNNING;
    }
    
    /**
     * Check if screenshot is available
     * @return true if screenshotPath is not null and not empty
     */
    public boolean hasScreenshot() {
        return screenshotPath != null && !screenshotPath.trim().isEmpty();
    }
    
    /**
     * Check if log file is available
     * @return true if logPath is not null and not empty
     */
    public boolean hasLogFile() {
        return logPath != null && !logPath.trim().isEmpty();
    }
    
    /**
     * Get execution time in seconds
     * @return execution time in seconds with decimal places
     */
    public double getExecutionTimeSeconds() {
        return executionTimeMs != null ? executionTimeMs / 1000.0 : 0.0;
    }
    
    /**
     * Set execution time from seconds
     * @param seconds execution time in seconds
     */
    public void setExecutionTimeSeconds(double seconds) {
        this.executionTimeMs = (long) (seconds * 1000);
    }
    
    /**
     * Get formatted execution time
     * @return formatted time string (e.g., "2.45s" or "1m 30.5s")
     */
    public String getFormattedExecutionTime() {
        if (executionTimeMs == null || executionTimeMs == 0) {
            return "0s";
        }
        
        double seconds = executionTimeMs / 1000.0;
        if (seconds < 60) {
            return String.format("%.2fs", seconds);
        } else {
            int minutes = (int) (seconds / 60);
            double remainingSeconds = seconds % 60;
            return String.format("%dm %.1fs", minutes, remainingSeconds);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionResult that = (ExecutionResult) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(scenarioId, that.scenarioId) &&
                status == that.status &&
                Objects.equals(executedAt, that.executedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, scenarioId, status, executedAt);
    }
    
    @Override
    public String toString() {
        return "ExecutionResult{" +
                "id=" + id +
                ", scenarioId=" + scenarioId +
                ", status=" + status +
                ", logPath='" + logPath + '\'' +
                ", screenshotPath='" + screenshotPath + '\'' +
                ", executedAt=" + executedAt +
                ", errorMessage='" + errorMessage + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}