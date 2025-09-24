package com.fadhli.automation.dao.impl;

import com.fadhli.automation.dao.DaoException;
import com.fadhli.automation.dao.StepDao;
import com.fadhli.automation.model.Step;
import com.fadhli.automation.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of StepDao for MySQL database operations
 * Handles CRUD operations for Step entity
 */
public class StepDaoImpl implements StepDao {
    
    private static final Logger logger = LoggerFactory.getLogger(StepDaoImpl.class);
    
    private final DatabaseUtil databaseUtil;
    
    // SQL Queries
    private static final String INSERT_STEP = 
        "INSERT INTO step (scenario_id, step_type, step_text, step_order) VALUES (?, ?, ?, ?)";
    
    private static final String UPDATE_STEP = 
        "UPDATE step SET scenario_id = ?, step_type = ?, step_text = ?, step_order = ? WHERE id = ?";
    
    private static final String SELECT_STEP_BY_ID = 
        "SELECT id, scenario_id, step_type, step_text, step_order FROM step WHERE id = ?";
    
    private static final String SELECT_ALL_STEPS = 
        "SELECT id, scenario_id, step_type, step_text, step_order FROM step ORDER BY scenario_id, step_order";
    
    private static final String DELETE_STEP = 
        "DELETE FROM step WHERE id = ?";
    
    private static final String SELECT_BY_SCENARIO_ID = 
        "SELECT id, scenario_id, step_type, step_text, step_order FROM step WHERE scenario_id = ? ORDER BY step_order";
    
    private static final String DELETE_BY_SCENARIO_ID = 
        "DELETE FROM step WHERE scenario_id = ?";
    
    private static final String UPDATE_STEP_ORDER = 
        "UPDATE step SET step_order = ? WHERE id = ? AND scenario_id = ?";
    
    private static final String SELECT_MAX_ORDER = 
        "SELECT COALESCE(MAX(step_order), 0) FROM step WHERE scenario_id = ?";
    
    private static final String COUNT_BY_SCENARIO_ID = 
        "SELECT COUNT(*) FROM step WHERE scenario_id = ?";
    
    private static final String COUNT_ALL_STEPS = 
        "SELECT COUNT(*) FROM step";
    
    private static final String EXISTS_BY_ID = 
        "SELECT 1 FROM step WHERE id = ?";
    
    /**
     * Constructor
     */
    public StepDaoImpl() {
        this.databaseUtil = DatabaseUtil.getInstance();
    }
    
    @Override
    public Step save(Step step) throws DaoException {
        if (step == null) {
            throw new DaoException("Step cannot be null");
        }
        
        if (step.getId() == null) {
            return insert(step);
        } else {
            return update(step);
        }
    }
    
    /**
     * Insert new step
     */
    private Step insert(Step step) throws DaoException {
        logger.debug("Inserting new step for scenario ID: {}", step.getScenarioId());
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_STEP, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, step.getScenarioId());
            stmt.setString(2, step.getStepType().getKeyword());
            stmt.setString(3, step.getStepText());
            stmt.setInt(4, step.getStepOrder());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoException("Failed to insert step, no rows affected");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    step.setId(generatedKeys.getLong(1));
                    logger.debug("Step inserted with ID: {}", step.getId());
                } else {
                    throw new DaoException("Failed to get generated key for inserted step");
                }
            }
            
            return step;
            
        } catch (SQLException e) {
            logger.error("Failed to insert step for scenario ID: {}", step.getScenarioId(), e);
            throw new DaoException("Failed to insert step", e);
        }
    }
    
    @Override
    public Optional<Step> findById(Long id) throws DaoException {
        if (id == null) {
            return Optional.empty();
        }
        
        logger.debug("Finding step by ID: {}", id);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_STEP_BY_ID)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Step step = mapResultSetToStep(rs);
                    logger.debug("Found step: {}", step.getStepText());
                    return Optional.of(step);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Failed to find step by ID: {}", id, e);
            throw new DaoException("Failed to find step by ID", e);
        }
    }
    
    @Override
    public List<Step> findAll() throws DaoException {
        logger.debug("Finding all steps");
        
        List<Step> steps = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_STEPS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                steps.add(mapResultSetToStep(rs));
            }
            
            logger.debug("Found {} steps", steps.size());
            return steps;
            
        } catch (SQLException e) {
            logger.error("Failed to find all steps", e);
            throw new DaoException("Failed to find all steps", e);
        }
    }
    
    @Override
    public Step update(Step step) throws DaoException {
        if (step == null || step.getId() == null) {
            throw new DaoException("Step and step ID cannot be null");
        }
        
        logger.debug("Updating step ID: {}", step.getId());
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_STEP)) {
            
            stmt.setLong(1, step.getScenarioId());
            stmt.setString(2, step.getStepType().getKeyword());
            stmt.setString(3, step.getStepText());
            stmt.setInt(4, step.getStepOrder());
            stmt.setLong(5, step.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoException("Step not found with ID: " + step.getId());
            }
            
            logger.debug("Step updated successfully");
            return step;
            
        } catch (SQLException e) {
            logger.error("Failed to update step ID: {}", step.getId(), e);
            throw new DaoException("Failed to update step", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) throws DaoException {
        if (id == null) {
            return false;
        }
        
        logger.debug("Deleting step by ID: {}", id);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_STEP)) {
            
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            boolean deleted = rowsAffected > 0;
            
            if (deleted) {
                logger.debug("Step deleted successfully");
            } else {
                logger.debug("No step found with ID: {}", id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            logger.error("Failed to delete step by ID: {}", id, e);
            throw new DaoException("Failed to delete step", e);
        }
    }
    
    @Override
    public boolean delete(Step step) throws DaoException {
        if (step == null || step.getId() == null) {
            return false;
        }
        
        return deleteById(step.getId());
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
            logger.error("Failed to check if step exists by ID: {}", id, e);
            throw new DaoException("Failed to check step existence", e);
        }
    }
    
    @Override
    public long count() throws DaoException {
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_ALL_STEPS);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to count steps", e);
            throw new DaoException("Failed to count steps", e);
        }
    }
    
    @Override
    public List<Step> findByScenarioId(Long scenarioId) throws DaoException {
        if (scenarioId == null) {
            return new ArrayList<>();
        }
        
        logger.debug("Finding steps by scenario ID: {}", scenarioId);
        
        List<Step> steps = new ArrayList<>();
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_SCENARIO_ID)) {
            
            stmt.setLong(1, scenarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    steps.add(mapResultSetToStep(rs));
                }
            }
            
            logger.debug("Found {} steps for scenario ID: {}", steps.size(), scenarioId);
            return steps;
            
        } catch (SQLException e) {
            logger.error("Failed to find steps by scenario ID: {}", scenarioId, e);
            throw new DaoException("Failed to find steps by scenario ID", e);
        }
    }
    
    @Override
    public int deleteByScenarioId(Long scenarioId) throws DaoException {
        if (scenarioId == null) {
            return 0;
        }
        
        logger.debug("Deleting steps by scenario ID: {}", scenarioId);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_BY_SCENARIO_ID)) {
            
            stmt.setLong(1, scenarioId);
            
            int deletedCount = stmt.executeUpdate();
            logger.debug("Deleted {} steps for scenario ID: {}", deletedCount, scenarioId);
            
            return deletedCount;
            
        } catch (SQLException e) {
            logger.error("Failed to delete steps by scenario ID: {}", scenarioId, e);
            throw new DaoException("Failed to delete steps by scenario ID", e);
        }
    }
    
    @Override
    public void updateStepOrder(Long scenarioId, Long stepId, int newOrder) throws DaoException {
        logger.debug("Updating step order for step ID: {} to order: {}", stepId, newOrder);
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_STEP_ORDER)) {
            
            stmt.setInt(1, newOrder);
            stmt.setLong(2, stepId);
            stmt.setLong(3, scenarioId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoException("Step not found with ID: " + stepId + " in scenario: " + scenarioId);
            }
            
            logger.debug("Step order updated successfully");
            
        } catch (SQLException e) {
            logger.error("Failed to update step order for step ID: {}", stepId, e);
            throw new DaoException("Failed to update step order", e);
        }
    }
    
    @Override
    public int getMaxStepOrder(Long scenarioId) throws DaoException {
        if (scenarioId == null) {
            return 0;
        }
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_MAX_ORDER)) {
            
            stmt.setLong(1, scenarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get max step order for scenario ID: {}", scenarioId, e);
            throw new DaoException("Failed to get max step order", e);
        }
    }
    
    @Override
    public long countByScenarioId(Long scenarioId) throws DaoException {
        if (scenarioId == null) {
            return 0;
        }
        
        try (Connection connection = databaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_BY_SCENARIO_ID)) {
            
            stmt.setLong(1, scenarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to count steps by scenario ID: {}", scenarioId, e);
            throw new DaoException("Failed to count steps by scenario ID", e);
        }
    }
    
    @Override
    public void batchInsert(List<Step> steps) throws DaoException {
        if (steps == null || steps.isEmpty()) {
            return;
        }
        
        logger.debug("Batch inserting {} steps", steps.size());
        
        try (Connection connection = databaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(INSERT_STEP, Statement.RETURN_GENERATED_KEYS)) {
                
                for (Step step : steps) {
                    stmt.setLong(1, step.getScenarioId());
                    stmt.setString(2, step.getStepType().getKeyword());
                    stmt.setString(3, step.getStepText());
                    stmt.setInt(4, step.getStepOrder());
                    stmt.addBatch();
                }
                
                int[] results = stmt.executeBatch();
                
                // Get generated keys
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    int index = 0;
                    while (generatedKeys.next() && index < steps.size()) {
                        steps.get(index).setId(generatedKeys.getLong(1));
                        index++;
                    }
                }
                
                connection.commit();
                logger.debug("Batch inserted {} steps successfully", results.length);
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("Failed to batch insert steps", e);
            throw new DaoException("Failed to batch insert steps", e);
        }
    }
    
    @Override
    public void batchUpdate(List<Step> steps) throws DaoException {
        if (steps == null || steps.isEmpty()) {
            return;
        }
        
        logger.debug("Batch updating {} steps", steps.size());
        
        try (Connection connection = databaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE_STEP)) {
                
                for (Step step : steps) {
                    stmt.setLong(1, step.getScenarioId());
                    stmt.setString(2, step.getStepType().getKeyword());
                    stmt.setString(3, step.getStepText());
                    stmt.setInt(4, step.getStepOrder());
                    stmt.setLong(5, step.getId());
                    stmt.addBatch();
                }
                
                int[] results = stmt.executeBatch();
                connection.commit();
                logger.debug("Batch updated {} steps successfully", results.length);
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("Failed to batch update steps", e);
            throw new DaoException("Failed to batch update steps", e);
        }
    }
    
    /**
     * Map ResultSet to Step object
     */
    private Step mapResultSetToStep(ResultSet rs) throws SQLException {
        Step step = new Step();
        step.setId(rs.getLong("id"));
        step.setScenarioId(rs.getLong("scenario_id"));
        step.setStepType(Step.StepType.fromString(rs.getString("step_type")));
        step.setStepText(rs.getString("step_text"));
        step.setStepOrder(rs.getInt("step_order"));
        
        return step;
    }
}