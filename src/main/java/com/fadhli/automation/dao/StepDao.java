package com.fadhli.automation.dao;

import com.fadhli.automation.model.Step;

import java.util.List;

/**
 * DAO interface for Step entity operations
 * Extends BaseDao and adds step-specific query methods
 */
public interface StepDao extends BaseDao<Step, Long> {
    
    /**
     * Find all steps by scenario ID ordered by step_order
     * @param scenarioId the scenario ID
     * @return List of steps for the scenario
     * @throws DaoException if operation fails
     */
    List<Step> findByScenarioId(Long scenarioId) throws DaoException;
    
    /**
     * Delete all steps by scenario ID
     * @param scenarioId the scenario ID
     * @return number of deleted steps
     * @throws DaoException if operation fails
     */
    int deleteByScenarioId(Long scenarioId) throws DaoException;
    
    /**
     * Update step order for a scenario
     * @param scenarioId the scenario ID
     * @param stepId the step ID
     * @param newOrder the new order
     * @throws DaoException if operation fails
     */
    void updateStepOrder(Long scenarioId, Long stepId, int newOrder) throws DaoException;
    
    /**
     * Get maximum step order for a scenario
     * @param scenarioId the scenario ID
     * @return maximum order value
     * @throws DaoException if operation fails
     */
    int getMaxStepOrder(Long scenarioId) throws DaoException;
    
    /**
     * Count steps by scenario ID
     * @param scenarioId the scenario ID
     * @return number of steps
     * @throws DaoException if operation fails
     */
    long countByScenarioId(Long scenarioId) throws DaoException;
    
    /**
     * Batch insert steps for a scenario
     * @param steps the steps to insert
     * @throws DaoException if operation fails
     */
    void batchInsert(List<Step> steps) throws DaoException;
    
    /**
     * Batch update steps
     * @param steps the steps to update
     * @throws DaoException if operation fails
     */
    void batchUpdate(List<Step> steps) throws DaoException;
}