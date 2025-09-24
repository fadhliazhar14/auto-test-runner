package com.fadhli.automation.dao;

import com.fadhli.automation.model.Scenario;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Scenario entity operations
 * Extends BaseDao and adds scenario-specific query methods
 */
public interface ScenarioDao extends BaseDao<Scenario, Long> {
    
    /**
     * Find scenario by feature name and scenario name
     * @param featureName the feature name
     * @param scenarioName the scenario name
     * @return Optional containing the scenario if found
     * @throws DaoException if operation fails
     */
    Optional<Scenario> findByFeatureAndScenarioName(String featureName, String scenarioName) throws DaoException;
    
    /**
     * Find all scenarios by feature name
     * @param featureName the feature name
     * @return List of scenarios for the feature
     * @throws DaoException if operation fails
     */
    List<Scenario> findByFeatureName(String featureName) throws DaoException;
    
    /**
     * Find scenarios by scenario name (partial match)
     * @param scenarioName the scenario name (can be partial)
     * @return List of matching scenarios
     * @throws DaoException if operation fails
     */
    List<Scenario> findByScenarioNameLike(String scenarioName) throws DaoException;
    
    /**
     * Find all distinct feature names
     * @return List of unique feature names
     * @throws DaoException if operation fails
     */
    List<String> findAllFeatureNames() throws DaoException;
    
    /**
     * Find scenario with its steps loaded
     * @param id the scenario ID
     * @return Optional containing scenario with steps if found
     * @throws DaoException if operation fails
     */
    Optional<Scenario> findByIdWithSteps(Long id) throws DaoException;
    
    /**
     * Find scenario with its execution results loaded
     * @param id the scenario ID
     * @return Optional containing scenario with execution results if found
     * @throws DaoException if operation fails
     */
    Optional<Scenario> findByIdWithExecutionResults(Long id) throws DaoException;
    
    /**
     * Find scenario with both steps and execution results loaded
     * @param id the scenario ID
     * @return Optional containing fully loaded scenario if found
     * @throws DaoException if operation fails
     */
    Optional<Scenario> findByIdComplete(Long id) throws DaoException;
    
    /**
     * Check if scenario exists by feature and scenario name
     * @param featureName the feature name
     * @param scenarioName the scenario name
     * @return true if scenario exists
     * @throws DaoException if operation fails
     */
    boolean existsByFeatureAndScenarioName(String featureName, String scenarioName) throws DaoException;
    
    /**
     * Find all scenarios with step count for performance optimization
     * @return List of scenarios with stepCount field populated
     * @throws DaoException if operation fails
     */
    List<Scenario> findAllWithStepCount() throws DaoException;
}