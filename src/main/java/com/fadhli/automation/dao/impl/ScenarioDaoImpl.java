package com.fadhli.automation.dao.impl;

import com.fadhli.automation.dao.DaoException;
import com.fadhli.automation.dao.ScenarioDao;
import com.fadhli.automation.dao.StepDao;
import com.fadhli.automation.dao.impl.StepDaoImpl;
import com.fadhli.automation.model.ExecutionResult;
import com.fadhli.automation.model.Scenario;
import com.fadhli.automation.model.Step;
import com.fadhli.automation.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ScenarioDao for MySQL database operations
 * Handles CRUD operations for Scenario entity with related Steps and ExecutionResults
 */
public class ScenarioDaoImpl implements ScenarioDao {
    
    private static final Logger logger = LoggerFactory.getLogger(ScenarioDaoImpl.class);
    
    private final DatabaseUtil databaseUtil;
    private final StepDao stepDao;
    
    // SQL Queries
    private static final String INSERT_SCENARIO = 
        "INSERT INTO scenario (feature_name, scenario_name, created_at, updated_at) VALUES (?, ?, ?, ?)";
    
    private static final String UPDATE_SCENARIO = 
        "UPDATE scenario SET feature_name = ?, scenario_name = ?, updated_at = ? WHERE id = ?";
    
    private static final String SELECT_SCENARIO_BY_ID = 
        "SELECT id, feature_name, scenario_name, created_at, updated_at FROM scenario WHERE id = ?";
    
    private static final String SELECT_ALL_SCENARIOS = 
        "SELECT id, feature_name, scenario_name, created_at, updated_at FROM scenario ORDER BY updated_at DESC";
    
    private static final String SELECT_ALL_SCENARIOS_WITH_STEP_COUNT = 
        "SELECT s.id, s.feature_name, s.scenario_name, s.created_at, s.updated_at, " +
        "COUNT(st.id) as step_count " +
        "FROM scenario s " +
        "LEFT JOIN step st ON s.id = st.scenario_id " +
        "GROUP BY s.id, s.feature_name, s.scenario_name, s.created_at, s.updated_at " +
        "ORDER BY s.updated_at DESC";
    
    private static final String DELETE_SCENARIO = 
        "DELETE FROM scenario WHERE id = ?";
    
    private static final String SELECT_SCENARIO_BY_FEATURE_AND_NAME = 
        "SELECT id, feature_name, scenario_name, created_at, updated_at FROM scenario WHERE feature_name = ? AND scenario_name = ?";
    
    private static final String SELECT_BY_FEATURE_NAME = 
        "SELECT id, feature_name, scenario_name, created_at, updated_at FROM scenario WHERE feature_name = ?";
    
    private static final String SELECT_BY_SCENARIO_NAME_LIKE = 
        "SELECT id, feature_name, scenario_name, created_at, updated_at FROM scenario WHERE scenario_name LIKE ?";
    
    private static final String SELECT_DISTINCT_FEATURE_NAMES = 
        "SELECT DISTINCT feature_name FROM scenario ORDER BY feature_name";
    
    private static final String COUNT_SCENARIOS = 
        "SELECT COUNT(*) FROM scenario";
    
    private static final String EXISTS_BY_ID = 
        "SELECT 1 FROM scenario WHERE id = ?";
    
    private static final String EXISTS_BY_FEATURE_AND_NAME = 
        "SELECT 1 FROM scenario WHERE feature_name = ? AND scenario_name = ?";
    
    // Step related queries
    private static final String SELECT_STEPS_BY_SCENARIO_ID = 
        "SELECT id, scenario_id, step_type, step_text, step_order FROM step WHERE scenario_id = ? ORDER BY step_order";
    
    // Execution result related queries
    private static final String SELECT_EXECUTION_RESULTS_BY_SCENARIO_ID = 
        "SELECT id, scenario_id, status, log_path, screenshot_path, error_message, execution_time_ms, executed_at " +
        "FROM execution_result WHERE scenario_id = ? ORDER BY executed_at DESC";
    
    /**
     * Constructor
     */
    public ScenarioDaoImpl() {
        this.databaseUtil = DatabaseUtil.getInstance();
        this.stepDao = new StepDaoImpl();
    }
    
    @Override
    public Scenario save(Scenario scenario) throws DaoException {
        if (scenario == null) {
            throw new DaoException("Scenario cannot be null");
        }
        
        if (scenario.getId() == null) {
            return insert(scenario);
        } else {
            return update(scenario);
        }
    }
    
    /**
     * Insert new scenario
     */
    private Scenario insert(Scenario scenario) throws DaoException {
        logger.debug("Inserting new scenario: {}", scenario.getScenarioName());
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_SCENARIO, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            scenario.setCreatedAt(now);
            scenario.setUpdatedAt(now);
            
            stmt.setString(1, scenario.getFeatureName());
            stmt.setString(2, scenario.getScenarioName());
            stmt.setTimestamp(3, Timestamp.valueOf(scenario.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(scenario.getUpdatedAt()));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoException("Failed to insert scenario, no rows affected");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    scenario.setId(generatedKeys.getLong(1));
                    logger.debug("Scenario inserted with ID: {}", scenario.getId());
                } else {
                    throw new DaoException("Failed to get generated key for inserted scenario");
                }
            }
            
            return scenario;
            
        } catch (SQLException e) {
            logger.error("Failed to insert scenario: {}", scenario.getScenarioName(), e);
            throw new DaoException("Failed to insert scenario", e);
        }
    }
    
    @Override
    public Optional<Scenario> findById(Long id) throws DaoException {
        if (id == null) {
            return Optional.empty();
        }
        
        logger.debug("Finding scenario by ID: {}", id);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_SCENARIO_BY_ID)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Scenario scenario = mapResultSetToScenario(rs);
                    logger.debug("Found scenario: {}", scenario.getScenarioName());
                    return Optional.of(scenario);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Failed to find scenario by ID: {}", id, e);
            throw new DaoException("Failed to find scenario by ID", e);
        }
    }
    
    @Override
    public List<Scenario> findAll() throws DaoException {
        logger.debug("Finding all scenarios");
        
        List<Scenario> scenarios = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SCENARIOS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                scenarios.add(mapResultSetToScenario(rs));
            }
            
            logger.debug("Found {} scenarios", scenarios.size());
            return scenarios;
            
        } catch (SQLException e) {
            logger.error("Failed to find all scenarios", e);
            throw new DaoException("Failed to find all scenarios", e);
        }
    }
    
    @Override
    public Scenario update(Scenario scenario) throws DaoException {
        if (scenario == null || scenario.getId() == null) {
            throw new DaoException("Scenario and scenario ID cannot be null");
        }
        
        logger.debug("Updating scenario ID: {}", scenario.getId());
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_SCENARIO)) {
            
            scenario.touch(); // Update timestamp
            
            stmt.setString(1, scenario.getFeatureName());
            stmt.setString(2, scenario.getScenarioName());
            stmt.setTimestamp(3, Timestamp.valueOf(scenario.getUpdatedAt()));
            stmt.setLong(4, scenario.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoException("Scenario not found with ID: " + scenario.getId());
            }
            
            logger.debug("Scenario updated successfully");
            return scenario;
            
        } catch (SQLException e) {
            logger.error("Failed to update scenario ID: {}", scenario.getId(), e);
            throw new DaoException("Failed to update scenario", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) throws DaoException {
        if (id == null) {
            return false;
        }
        
        logger.debug("Deleting scenario by ID: {}", id);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_SCENARIO)) {
            
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            boolean deleted = rowsAffected > 0;
            
            if (deleted) {
                logger.debug("Scenario deleted successfully");
            } else {
                logger.debug("No scenario found with ID: {}", id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            logger.error("Failed to delete scenario by ID: {}", id, e);
            throw new DaoException("Failed to delete scenario", e);
        }
    }
    
    @Override
    public boolean delete(Scenario scenario) throws DaoException {
        if (scenario == null || scenario.getId() == null) {
            return false;
        }
        
        return deleteById(scenario.getId());
    }
    
    @Override
    public boolean existsById(Long id) throws DaoException {
        if (id == null) {
            return false;
        }
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_BY_ID)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            logger.error("Failed to check if scenario exists by ID: {}", id, e);
            throw new DaoException("Failed to check scenario existence", e);
        }
    }
    
    @Override
    public long count() throws DaoException {
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_SCENARIOS);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to count scenarios", e);
            throw new DaoException("Failed to count scenarios", e);
        }
    }
    
    @Override
    public Optional<Scenario> findByFeatureAndScenarioName(String featureName, String scenarioName) throws DaoException {
        if (featureName == null || scenarioName == null) {
            return Optional.empty();
        }
        
        logger.debug("Finding scenario by feature '{}' and name '{}'", featureName, scenarioName);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_SCENARIO_BY_FEATURE_AND_NAME)) {
            
            stmt.setString(1, featureName);
            stmt.setString(2, scenarioName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Scenario scenario = mapResultSetToScenario(rs);
                    return Optional.of(scenario);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Failed to find scenario by feature and name", e);
            throw new DaoException("Failed to find scenario by feature and name", e);
        }
    }
    
    @Override
    public List<Scenario> findByFeatureName(String featureName) throws DaoException {
        if (featureName == null) {
            return new ArrayList<>();
        }
        
        logger.debug("Finding scenarios by feature name: {}", featureName);
        
        List<Scenario> scenarios = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_FEATURE_NAME)) {
            
            stmt.setString(1, featureName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scenarios.add(mapResultSetToScenario(rs));
                }
            }
            
            logger.debug("Found {} scenarios for feature: {}", scenarios.size(), featureName);
            return scenarios;
            
        } catch (SQLException e) {
            logger.error("Failed to find scenarios by feature name: {}", featureName, e);
            throw new DaoException("Failed to find scenarios by feature name", e);
        }
    }
    
    @Override
    public List<Scenario> findByScenarioNameLike(String scenarioName) throws DaoException {
        if (scenarioName == null) {
            return new ArrayList<>();
        }
        
        logger.debug("Finding scenarios by name like: {}", scenarioName);
        
        List<Scenario> scenarios = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_SCENARIO_NAME_LIKE)) {
            
            stmt.setString(1, "%" + scenarioName + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scenarios.add(mapResultSetToScenario(rs));
                }
            }
            
            logger.debug("Found {} scenarios matching: {}", scenarios.size(), scenarioName);
            return scenarios;
            
        } catch (SQLException e) {
            logger.error("Failed to find scenarios by name like: {}", scenarioName, e);
            throw new DaoException("Failed to find scenarios by name like", e);
        }
    }
    
    @Override
    public List<String> findAllFeatureNames() throws DaoException {
        logger.debug("Finding all distinct feature names");
        
        List<String> featureNames = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_DISTINCT_FEATURE_NAMES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                featureNames.add(rs.getString("feature_name"));
            }
            
            logger.debug("Found {} distinct feature names", featureNames.size());
            return featureNames;
            
        } catch (SQLException e) {
            logger.error("Failed to find all feature names", e);
            throw new DaoException("Failed to find all feature names", e);
        }
    }
    
    @Override
    public Optional<Scenario> findByIdWithSteps(Long id) throws DaoException {
        if (id == null) {
            return Optional.empty();
        }
        
        logger.debug("Finding scenario with steps by ID: {}", id);
        
        Optional<Scenario> scenarioOpt = findById(id);
        if (scenarioOpt.isPresent()) {
            Scenario scenario = scenarioOpt.get();
            scenario.setSteps(stepDao.findByScenarioId(id));
            return Optional.of(scenario);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Scenario> findByIdWithExecutionResults(Long id) throws DaoException {
        if (id == null) {
            return Optional.empty();
        }
        
        logger.debug("Finding scenario with execution results by ID: {}", id);
        
        Optional<Scenario> scenarioOpt = findById(id);
        if (scenarioOpt.isPresent()) {
            Scenario scenario = scenarioOpt.get();
            scenario.setExecutionResults(findExecutionResultsByScenarioId(id));
            return Optional.of(scenario);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Scenario> findByIdComplete(Long id) throws DaoException {
        if (id == null) {
            return Optional.empty();
        }
        
        logger.debug("Finding complete scenario by ID: {}", id);
        
        Optional<Scenario> scenarioOpt = findById(id);
        if (scenarioOpt.isPresent()) {
            Scenario scenario = scenarioOpt.get();
            scenario.setSteps(stepDao.findByScenarioId(id));
            scenario.setExecutionResults(findExecutionResultsByScenarioId(id));
            return Optional.of(scenario);
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean existsByFeatureAndScenarioName(String featureName, String scenarioName) throws DaoException {
        if (featureName == null || scenarioName == null) {
            return false;
        }
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_BY_FEATURE_AND_NAME)) {
            
            stmt.setString(1, featureName);
            stmt.setString(2, scenarioName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            logger.error("Failed to check if scenario exists by feature and name", e);
            throw new DaoException("Failed to check scenario existence", e);
        }
    }
    
    /**
     * Find steps by scenario ID
     */
    private List<Step> findStepsByScenarioId(Long scenarioId) throws DaoException {
        List<Step> steps = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_STEPS_BY_SCENARIO_ID)) {
            
            stmt.setLong(1, scenarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Step step = new Step();
                    step.setId(rs.getLong("id"));
                    step.setScenarioId(rs.getLong("scenario_id"));
                    step.setStepType(Step.StepType.fromString(rs.getString("step_type")));
                    step.setStepText(rs.getString("step_text"));
                    step.setStepOrder(rs.getInt("step_order"));
                    
                    steps.add(step);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to find steps by scenario ID: {}", scenarioId, e);
            throw new DaoException("Failed to find steps", e);
        }
        
        return steps;
    }
    
    /**
     * Find execution results by scenario ID
     */
    private List<ExecutionResult> findExecutionResultsByScenarioId(Long scenarioId) throws DaoException {
        List<ExecutionResult> results = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_EXECUTION_RESULTS_BY_SCENARIO_ID)) {
            
            stmt.setLong(1, scenarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExecutionResult result = new ExecutionResult();
                    result.setId(rs.getLong("id"));
                    result.setScenarioId(rs.getLong("scenario_id"));
                    result.setStatus(ExecutionResult.ExecutionStatus.fromString(rs.getString("status")));
                    result.setLogPath(rs.getString("log_path"));
                    result.setScreenshotPath(rs.getString("screenshot_path"));
                    result.setErrorMessage(rs.getString("error_message"));
                    result.setExecutionTimeMs(rs.getLong("execution_time_ms"));
                    
                    Timestamp executedAt = rs.getTimestamp("executed_at");
                    if (executedAt != null) {
                        result.setExecutedAt(executedAt.toLocalDateTime());
                    }
                    
                    results.add(result);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to find execution results by scenario ID: {}", scenarioId, e);
            throw new DaoException("Failed to find execution results", e);
        }
        
        return results;
    }
    
    @Override
    public List<Scenario> findAllWithStepCount() throws DaoException {
        logger.debug("Finding all scenarios with step count");
        
        List<Scenario> scenarios = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SCENARIOS_WITH_STEP_COUNT);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                scenarios.add(mapResultSetToScenarioWithStepCount(rs));
            }
            
            logger.debug("Found {} scenarios with step count", scenarios.size());
            return scenarios;
            
        } catch (SQLException e) {
            logger.error("Failed to find all scenarios with step count", e);
            throw new DaoException("Failed to find all scenarios with step count", e);
        }
    }
    
    /**
     * Map ResultSet to Scenario object
     */
    private Scenario mapResultSetToScenario(ResultSet rs) throws SQLException {
        Scenario scenario = new Scenario();
        scenario.setId(rs.getLong("id"));
        scenario.setFeatureName(rs.getString("feature_name"));
        scenario.setScenarioName(rs.getString("scenario_name"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            scenario.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            scenario.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return scenario;
    }
    
    /**
     * Map ResultSet to Scenario object with step count
     */
    private Scenario mapResultSetToScenarioWithStepCount(ResultSet rs) throws SQLException {
        Scenario scenario = new Scenario();
        scenario.setId(rs.getLong("id"));
        scenario.setFeatureName(rs.getString("feature_name"));
        scenario.setScenarioName(rs.getString("scenario_name"));
        scenario.setStepCount(rs.getInt("step_count"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            scenario.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            scenario.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return scenario;
    }
}