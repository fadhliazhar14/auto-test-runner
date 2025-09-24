package com.fadhli.automation.service;

import com.fadhli.automation.model.Scenario;
import com.fadhli.automation.model.Step;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Scenario business logic operations
 * Provides higher-level business operations built on top of DAO layer
 */
public interface ScenarioService {
    
    /**
     * Create a new scenario with steps
     * @param scenario the scenario to create
     * @return the created scenario with assigned ID
     * @throws ServiceException if operation fails
     */
    Scenario createScenario(Scenario scenario) throws ServiceException;
    
    /**
     * Update existing scenario and its steps
     * @param scenario the scenario to update
     * @return the updated scenario
     * @throws ServiceException if operation fails
     */
    Scenario updateScenario(Scenario scenario) throws ServiceException;
    
    /**
     * Delete scenario by ID
     * @param scenarioId the scenario ID to delete
     * @throws ServiceException if operation fails
     */
    void deleteScenario(Long scenarioId) throws ServiceException;
    
    /**
     * Find scenario by ID
     * @param id the scenario ID
     * @return Optional containing the scenario if found
     * @throws ServiceException if operation fails
     */
    Optional<Scenario> findScenarioById(Long id) throws ServiceException;
    
    /**
     * Find scenario by ID with all related data (steps and execution results)
     * @param id the scenario ID
     * @return Optional containing the fully loaded scenario if found
     * @throws ServiceException if operation fails
     */
    Optional<Scenario> findScenarioByIdComplete(Long id) throws ServiceException;
    
    /**
     * Find all scenarios
     * @return List of all scenarios
     * @throws ServiceException if operation fails
     */
    List<Scenario> findAllScenarios() throws ServiceException;
    
    /**
     * Find all scenarios with step count for performance optimization
     * @return List of all scenarios with stepCount field populated
     * @throws ServiceException if operation fails
     */
    List<Scenario> findAllScenariosWithStepCount() throws ServiceException;
    
    /**
     * Find scenarios by feature name
     * @param featureName the feature name
     * @return List of scenarios for the feature
     * @throws ServiceException if operation fails
     */
    List<Scenario> findScenariosByFeatureName(String featureName) throws ServiceException;
    
    /**
     * Search scenarios by scenario name (partial match)
     * @param searchTerm the search term
     * @return List of matching scenarios
     * @throws ServiceException if operation fails
     */
    List<Scenario> searchScenarios(String searchTerm) throws ServiceException;
    
    /**
     * Get all distinct feature names
     * @return List of feature names
     * @throws ServiceException if operation fails
     */
    List<String> getAllFeatureNames() throws ServiceException;
    
    /**
     * Check if scenario exists by feature and scenario name
     * @param featureName the feature name
     * @param scenarioName the scenario name
     * @return true if scenario exists
     * @throws ServiceException if operation fails
     */
    boolean scenarioExists(String featureName, String scenarioName) throws ServiceException;
    
    /**
     * Validate scenario data
     * @param scenario the scenario to validate
     * @throws ServiceException if validation fails
     */
    void validateScenario(Scenario scenario) throws ServiceException;
    
    /**
     * Generate Gherkin feature file content for scenario
     * @param scenarioId the scenario ID
     * @return Gherkin feature file content
     * @throws ServiceException if operation fails
     */
    String generateGherkinContent(Long scenarioId) throws ServiceException;
    
    /**
     * Add step to scenario
     * @param scenarioId the scenario ID
     * @param step the step to add
     * @return the added step with assigned ID
     * @throws ServiceException if operation fails
     */
    Step addStepToScenario(Long scenarioId, Step step) throws ServiceException;
    
    /**
     * Update step in scenario
     * @param step the step to update
     * @return the updated step
     * @throws ServiceException if operation fails
     */
    Step updateStep(Step step) throws ServiceException;
    
    /**
     * Remove step from scenario
     * @param stepId the step ID to remove
     * @throws ServiceException if operation fails
     */
    void removeStepFromScenario(Long stepId) throws ServiceException;
    
    /**
     * Reorder steps in scenario
     * @param scenarioId the scenario ID
     * @param steps the steps in new order
     * @throws ServiceException if operation fails
     */
    void reorderSteps(Long scenarioId, List<Step> steps) throws ServiceException;
    
    /**
     * Get total number of scenarios
     * @return total count
     * @throws ServiceException if operation fails
     */
    long getScenarioCount() throws ServiceException;
}