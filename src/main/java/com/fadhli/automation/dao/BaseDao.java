package com.fadhli.automation.dao;

import java.util.List;
import java.util.Optional;

/**
 * Base DAO interface providing common CRUD operations
 * Follows repository pattern for data access abstraction
 * 
 * @param <T> the entity type
 * @param <ID> the primary key type
 */
public interface BaseDao<T, ID> {
    
    /**
     * Save entity to database (insert or update)
     * @param entity the entity to save
     * @return the saved entity with updated ID and timestamps
     * @throws DaoException if operation fails
     */
    T save(T entity) throws DaoException;
    
    /**
     * Find entity by ID
     * @param id the entity ID
     * @return Optional containing the entity if found, empty otherwise
     * @throws DaoException if operation fails
     */
    Optional<T> findById(ID id) throws DaoException;
    
    /**
     * Find all entities
     * @return List of all entities
     * @throws DaoException if operation fails
     */
    List<T> findAll() throws DaoException;
    
    /**
     * Update existing entity
     * @param entity the entity to update
     * @return the updated entity
     * @throws DaoException if operation fails or entity not found
     */
    T update(T entity) throws DaoException;
    
    /**
     * Delete entity by ID
     * @param id the entity ID to delete
     * @return true if entity was deleted, false if not found
     * @throws DaoException if operation fails
     */
    boolean deleteById(ID id) throws DaoException;
    
    /**
     * Delete entity
     * @param entity the entity to delete
     * @return true if entity was deleted, false if not found
     * @throws DaoException if operation fails
     */
    boolean delete(T entity) throws DaoException;
    
    /**
     * Check if entity exists by ID
     * @param id the entity ID
     * @return true if entity exists, false otherwise
     * @throws DaoException if operation fails
     */
    boolean existsById(ID id) throws DaoException;
    
    /**
     * Count total number of entities
     * @return the total count
     * @throws DaoException if operation fails
     */
    long count() throws DaoException;
}