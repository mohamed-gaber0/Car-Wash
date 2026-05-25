
package oop.carwash.dao;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic abstract base class for all DAOs.
 *
 * GENERICS REQUIREMENT: This class uses a type parameter <T> to define
 * reusable CRUD operations. Concrete DAOs extend GenericDAO<ModelClass>
 * and implement the abstract methods to map database rows to objects.
 *
 * Subclasses must implement:
 *   - mapRow(ResultSet) : convert a DB row to a model object
 *   - findAll() : SELECT * query
 *   - findById(id) : SELECT WHERE id
 *   - save(entity) : INSERT
 *   - update(entity) : UPDATE
 *   - delete(id) : DELETE
 */
public abstract class GenericDAO<T> {

    /**
     * Convert a ResultSet row to a model object of type T.
     * ResultSet pointer is on the current row when called.
     */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /**
     * Returns all rows from the table as a list.
     */
    public abstract List<T> findAll();

    /**
     * Returns a single row by primary key, or empty Optional if not found.
     */
    public abstract Optional<T> findById(int id);

    /**
     * Inserts a new row and populates the generated ID back into the object.
     */
    public abstract void save(T entity);

    /**
     * Updates an existing row by primary key.
     */
    public abstract void update(T entity);

    /**
     * Deletes a row by primary key.
     */
    public abstract void delete(int id);
}