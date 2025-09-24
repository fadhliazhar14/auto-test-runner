package com.fadhli.automation.service.impl;

import com.fadhli.automation.dao.DaoException;
import com.fadhli.automation.dao.ScenarioDao;
import com.fadhli.automation.dao.StepDao;
import com.fadhli.automation.dao.impl.ScenarioDaoImpl;
import com.fadhli.automation.dao.impl.StepDaoImpl;
import com.fadhli.automation.model.Scenario;
import com.fadhli.automation.model.Step;
import com.fadhli.automation.service.ScenarioService;
import com.fadhli.automation.service.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ScenarioService providing business logic for scenario management
 * Handles validation, transaction management, and business rules
 */
public class ScenarioServiceImpl implements ScenarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScenarioServiceImpl.class);
    
    private final ScenarioDao scenarioDao;
    private final StepDao stepDao;
    
    /**
     * Constructor with DAO dependencies
     * @param scenarioDao the scenario DAO
     * @param stepDao the step DAO
     */
    public ScenarioServiceImpl(ScenarioDao scenarioDao, StepDao stepDao) {
        this.scenarioDao = scenarioDao;
        this.stepDao = stepDao;
    }
    
    /**
     * Default constructor - creates DAO instances
     */
    public ScenarioServiceImpl() {
        this.scenarioDao = new ScenarioDaoImpl();
        this.stepDao = new StepDaoImpl();
    }
    
    @Override
    public Scenario createScenario(Scenario scenario) throws ServiceException {
        logger.info("Creating new scenario: {}", scenario.getScenarioName());
        
        try {
            // Validate scenario data
            validateScenario(scenario);
            
            // Check if scenario already exists
            if (scenarioExists(scenario.getFeatureName(), scenario.getScenarioName())) {
                throw new ServiceException(String.format(
                    "Scenario '%s' already exists in feature '%s'", 
                    scenario.getScenarioName(), scenario.getFeatureName()));
            }
            
            // Save scenario
            Scenario savedScenario = scenarioDao.save(scenario);
            
            // Save steps if present
            if (scenario.getSteps() != null && !scenario.getSteps().isEmpty()) {
                List<Step> savedSteps = saveStepsForScenario(savedScenario.getId(), scenario.getSteps());
                savedScenario.setSteps(savedSteps);
            }
            
            logger.info("Scenario created successfully with ID: {}", savedScenario.getId());
            return savedScenario;
            
        } catch (DaoException e) {
            logger.error("Failed to create scenario: {}", scenario.getScenarioName(), e);
            throw new ServiceException("Failed to create scenario", e);
        }
    }
    
    @Override
    public Scenario updateScenario(Scenario scenario) throws ServiceException {
        logger.info("Updating scenario ID: {}", scenario.getId());
        
        try {
            // Validate scenario data
            validateScenario(scenario);
            
            // Check if scenario exists
            if (!scenarioDao.existsById(scenario.getId())) {
                throw new ServiceException("Scenario not found with ID: " + scenario.getId());
            }
            
            // Check for duplicate name if changed
            Optional<Scenario> existingScenario = scenarioDao.findByFeatureAndScenarioName(
                scenario.getFeatureName(), scenario.getScenarioName());
            
            if (existingScenario.isPresent() && !existingScenario.get().getId().equals(scenario.getId())) {
                throw new ServiceException(String.format(
                    "Another scenario with name '%s' already exists in feature '%s'", 
                    scenario.getScenarioName(), scenario.getFeatureName()));
            }
            
            // Update scenario
            Scenario updatedScenario = scenarioDao.update(scenario);
            
            // Update steps if present
            if (scenario.getSteps() != null) {
                logger.debug("Updating {} steps for scenario ID: {}", scenario.getSteps().size(), scenario.getId());
                
                // Delete existing steps for the scenario
                stepDao.deleteByScenarioId(scenario.getId());
                
                // Save new steps if any
                if (!scenario.getSteps().isEmpty()) {
                    List<Step> updatedSteps = saveStepsForScenario(scenario.getId(), scenario.getSteps());
                    updatedScenario.setSteps(updatedSteps);
                } else {
                    updatedScenario.setSteps(new ArrayList<>());
                }
            }
            
            logger.info("Scenario updated successfully");
            return updatedScenario;
            
        } catch (DaoException e) {
            logger.error("Failed to update scenario ID: {}", scenario.getId(), e);
            throw new ServiceException("Failed to update scenario", e);
        }
    }
    
    @Override
    public void deleteScenario(Long scenarioId) throws ServiceException {
        logger.info("Deleting scenario ID: {}", scenarioId);
        
        try {
            if (!scenarioDao.existsById(scenarioId)) {
                throw new ServiceException("Scenario not found with ID: " + scenarioId);
            }
            
            // First, delete all steps associated with the scenario
            stepDao.deleteByScenarioId(scenarioId);
            
            // Then delete the scenario itself
            boolean deleted = scenarioDao.deleteById(scenarioId);
            
            if (!deleted) {
                throw new ServiceException("Failed to delete scenario with ID: " + scenarioId);
            }
            
            logger.info("Scenario deleted successfully");
            
        } catch (DaoException e) {
            logger.error("Failed to delete scenario ID: {}", scenarioId, e);
            throw new ServiceException("Failed to delete scenario", e);
        }
    }
    
    @Override
    public Optional<Scenario> findScenarioById(Long id) throws ServiceException {
        logger.debug("Finding scenario by ID: {}", id);
        
        try {
            return scenarioDao.findById(id);
        } catch (DaoException e) {
            logger.error("Failed to find scenario by ID: {}", id, e);
            throw new ServiceException("Failed to find scenario", e);
        }
    }
    
    @Override
    public Optional<Scenario> findScenarioByIdComplete(Long id) throws ServiceException {
        logger.debug("Finding scenario by ID with complete data: {}", id);
        
        try {
            return scenarioDao.findByIdComplete(id);
        } catch (DaoException e) {
            logger.error("Failed to find complete scenario by ID: {}", id, e);
            throw new ServiceException("Failed to find scenario", e);
        }
    }
    
    @Override
    public List<Scenario> findAllScenarios() throws ServiceException {
        logger.debug("Finding all scenarios");
        
        try {
            List<Scenario> scenarios = scenarioDao.findAll();
            logger.debug("Found {} scenarios", scenarios.size());
            return scenarios;
        } catch (DaoException e) {
            logger.error("Failed to find all scenarios", e);
            throw new ServiceException("Failed to find scenarios", e);
        }
    }
    
    @Override
    public List<Scenario> findAllScenariosWithStepCount() throws ServiceException {
        logger.debug("Finding all scenarios with step count");
        
        try {
            List<Scenario> scenarios = scenarioDao.findAllWithStepCount();
            logger.debug("Found {} scenarios with step count", scenarios.size());
            return scenarios;
        } catch (DaoException e) {
            logger.error("Failed to find all scenarios with step count", e);
            throw new ServiceException("Failed to find scenarios with step count", e);
        }
    }
    
    @Override
    public List<Scenario> findScenariosByFeatureName(String featureName) throws ServiceException {
        logger.debug("Finding scenarios by feature name: {}", featureName);
        
        try {
            return scenarioDao.findByFeatureName(featureName);
        } catch (DaoException e) {
            logger.error("Failed to find scenarios by feature name: {}", featureName, e);
            throw new ServiceException("Failed to find scenarios by feature", e);
        }
    }
    
    @Override
    public List<Scenario> searchScenarios(String searchTerm) throws ServiceException {
        logger.debug("Searching scenarios with term: {}", searchTerm);
        
        try {
            return scenarioDao.findByScenarioNameLike(searchTerm);
        } catch (DaoException e) {
            logger.error("Failed to search scenarios with term: {}", searchTerm, e);
            throw new ServiceException("Failed to search scenarios", e);
        }
    }
    
    @Override
    public List<String> getAllFeatureNames() throws ServiceException {
        logger.debug("Getting all feature names");
        
        try {
            return scenarioDao.findAllFeatureNames();
        } catch (DaoException e) {
            logger.error("Failed to get all feature names", e);
            throw new ServiceException("Failed to get feature names", e);
        }
    }
    
    @Override
    public boolean scenarioExists(String featureName, String scenarioName) throws ServiceException {
        try {
            return scenarioDao.existsByFeatureAndScenarioName(featureName, scenarioName);
        } catch (DaoException e) {
            logger.error("Failed to check if scenario exists", e);
            throw new ServiceException("Failed to check scenario existence", e);
        }
    }
    
    @Override
    public void validateScenario(Scenario scenario) throws ServiceException {
        if (scenario == null) {
            throw new ServiceException("Scenario cannot be null");
        }
        
        // Validate feature name
        if (StringUtils.isBlank(scenario.getFeatureName())) {
            throw new ServiceException("Feature name is required");
        }
        
        if (scenario.getFeatureName().length() > 255) {
            throw new ServiceException("Feature name cannot exceed 255 characters");
        }
        
        // Validate scenario name
        if (StringUtils.isBlank(scenario.getScenarioName())) {
            throw new ServiceException("Scenario name is required");
        }
        
        if (scenario.getScenarioName().length() > 255) {
            throw new ServiceException("Scenario name cannot exceed 255 characters");
        }
        
        // Validate steps if present
        if (scenario.getSteps() != null) {
            for (int i = 0; i < scenario.getSteps().size(); i++) {
                Step step = scenario.getSteps().get(i);
                validateStep(step, i + 1);
            }
        }
        
        logger.debug("Scenario validation passed for: {}", scenario.getScenarioName());
    }
    
    @Override
    public String generateGherkinContent(Long scenarioId) throws ServiceException {
        logger.debug("Generating Gherkin content for scenario ID: {}", scenarioId);
        
        try {
            Optional<Scenario> scenarioOpt = scenarioDao.findByIdWithSteps(scenarioId);
            
            if (scenarioOpt.isEmpty()) {
                throw new ServiceException("Scenario not found with ID: " + scenarioId);
            }
            
            Scenario scenario = scenarioOpt.get();
            String gherkinContent = scenario.toGherkinFeature();
            
            logger.debug("Generated Gherkin content for scenario: {}", scenario.getScenarioName());
            return gherkinContent;
            
        } catch (DaoException e) {
            logger.error("Failed to generate Gherkin content for scenario ID: {}", scenarioId, e);
            throw new ServiceException("Failed to generate Gherkin content", e);
        }
    }
    
    @Override
    public Step addStepToScenario(Long scenarioId, Step step) throws ServiceException {
        logger.debug("Adding step to scenario ID: {}", scenarioId);
        
        try {
            // Check if scenario exists
            if (!scenarioDao.existsById(scenarioId)) {
                throw new ServiceException("Scenario not found with ID: " + scenarioId);
            }
            
            // Set scenario ID and validate step
            step.setScenarioId(scenarioId);
            validateStep(step, -1); // -1 indicates order will be set automatically
            
            // Set step order if not provided
            if (step.getStepOrder() == null || step.getStepOrder() <= 0) {
                step.setStepOrder(stepDao.getMaxStepOrder(scenarioId) + 1);
            }
            
            // Save step
            Step savedStep = stepDao.save(step);
            
            logger.debug("Step added to scenario successfully");
            return savedStep;
            
        } catch (DaoException e) {
            logger.error("Failed to add step to scenario ID: {}", scenarioId, e);
            throw new ServiceException("Failed to add step to scenario", e);
        }
    }
    
    @Override
    public Step updateStep(Step step) throws ServiceException {
        logger.debug("Updating step ID: {}", step.getId());
        
        try {
            validateStep(step, -1);
            
            // Update step
            Step updatedStep = stepDao.update(step);
            
            logger.debug("Step updated successfully");
            return updatedStep;
            
        } catch (DaoException e) {
            logger.error("Failed to update step ID: {}", step.getId(), e);
            throw new ServiceException("Failed to update step", e);
        }
    }
    
    @Override
    public void removeStepFromScenario(Long stepId) throws ServiceException {
        logger.debug("Removing step ID: {}", stepId);
        
        try {
            // Delete step
            boolean deleted = stepDao.deleteById(stepId);
            
            if (!deleted) {
                throw new ServiceException("Step not found with ID: " + stepId);
            }
            
            logger.debug("Step removed successfully");
            
        } catch (DaoException e) {
            logger.error("Failed to remove step ID: {}", stepId, e);
            throw new ServiceException("Failed to remove step", e);
        }
    }
    
    @Override
    public void reorderSteps(Long scenarioId, List<Step> steps) throws ServiceException {
        logger.debug("Reordering steps for scenario ID: {}", scenarioId);
        
        try {
            // Validate and update step orders
            for (int i = 0; i < steps.size(); i++) {
                Step step = steps.get(i);
                step.setStepOrder(i + 1);
                validateStep(step, i + 1);
            }
            
            // Batch update steps
            stepDao.batchUpdate(steps);
            
            logger.debug("Steps reordered successfully");
            
        } catch (DaoException e) {
            logger.error("Failed to reorder steps for scenario ID: {}", scenarioId, e);
            throw new ServiceException("Failed to reorder steps", e);
        }
    }
    
    @Override
    public long getScenarioCount() throws ServiceException {
        try {
            return scenarioDao.count();
        } catch (DaoException e) {
            logger.error("Failed to get scenario count", e);
            throw new ServiceException("Failed to get scenario count", e);
        }
    }
    
    /**
     * Validate individual step
     */
    private void validateStep(Step step, int position) throws ServiceException {
        if (step == null) {
            throw new ServiceException("Step cannot be null");
        }
        
        if (step.getStepType() == null) {
            String positionText = position > 0 ? " at position " + position : "";
            throw new ServiceException("Step type is required" + positionText);
        }
        
        if (StringUtils.isBlank(step.getStepText())) {
            String positionText = position > 0 ? " at position " + position : "";
            throw new ServiceException("Step text is required" + positionText);
        }
        
        if (step.getStepText().length() > 1000) {
            String positionText = position > 0 ? " at position " + position : "";
            throw new ServiceException("Step text cannot exceed 1000 characters" + positionText);
        }
    }
    
    /**
     * Save steps for a scenario
     */
    private List<Step> saveStepsForScenario(Long scenarioId, List<Step> steps) throws DaoException {
        logger.debug("Saving {} steps for scenario ID: {}", steps.size(), scenarioId);
        
        List<Step> savedSteps = new ArrayList<>();
        
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i).copy(); // Create copy to avoid modifying original
            step.setScenarioId(scenarioId);
            step.setStepOrder(i + 1);
            
            Step savedStep = stepDao.save(step);
            savedSteps.add(savedStep);
        }
        
        return savedSteps;
    }
}