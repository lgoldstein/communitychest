/*
 * 
 */
package net.community.chest.spring.orm;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Generic super-interface for all data access objects, defining CRUD
 * methods and many useful find()-ers. <B>Note:</B> all the methods may
 * throw {@link RuntimeException}-s if failed to executed the requested
 * operation - unless otherwise specified</P>
 * @param <T> Type of object being persisted 
 * @param <ID> Type of {@link Serializable} unique ID being used as primary key 
 * @author Lyor G.
 * @since Aug 30, 2010 10:49:59 AM
 */
public interface GenericDao<T, ID extends Serializable> {
	/**
	 * @return The {@link Class} representing the entity managed by this DAO
	 */
	Class<T> getPersistentClass ();
	/**
	 * @return The {@link Class} representing the primary entity key type
	 */
	Class<ID> getIdClass ();
	/**
	 * <B>Note:</B> does not lock the table
	 * @param id The unique/primary ID of the object
	 * @return The relevant object - may be <code>null</code> if none found
	 */
	T findById (ID id);
	/**
	 * Flushes the last executed modification
	 */
	void flush ();
	/**
	 * @param entity The object to store
	 * @param flushIt If <code>true</code> then flush the entity
	 * change after a successful create.
	 */
    void create (T entity, boolean flushIt);
	/**
	 * Store a new object - <B>Note:</B> automatically flushes the created object
	 * @param entity The object to store
	 */
    void create (T entity);
	/**
	 * Update an existing object
	 * @param entity The object to persist. The object version will
	 * @param flushIt If <code>true</code> then flush the update
	 * change after a successful update.
	 */
    void update (T entity, boolean flushIt);
	/**
	 * Update an existing object - <B>Note:</B> automatically flushes the update
	 * @param entity The object to persist. The object version will
	 * change after a successful update.
	 * @see #update(Object, boolean)
	 */
    void update (T entity);
    /**
     * Delete an existing object from persistent storage
     * @param entity The object to delete.
     */
    void delete (T entity);
    /**
     * @param id Id of entity to be deleted
     * @return The deleted entity - <code>null</code> if referenced entity
     * does not exist
     */
    T delete (ID id);
    /**
     * Delete all objects of type <code>T</code>. <B>Note:</B> this deletion
     * might not trigger the pre/post-delete events listeners registered in
     * the Hibernate framework since it might be done via some code that does
     * not invoke them
     * @return number of deleted items
     */
    Number deleteAll ();
    /**
     * @param items items to be deleted
     * @return number of deleted items - same as input
     */
    Number deleteAll (Collection<? extends T> items);
    /**
     * Find all objects of type <code>T</code>
     * @return A {@link List} of all entities in the persisted table
     */
    List<T> findAll();
	/**
	 * Update an existing object
	 * @param entity The object to persist. The object version will
	 * change after a successful update.
	 * @return The merged entity
	 */
    T merge (T entity);
    /**
     * Refresh an existing object with any changes which took place at the data store.
     * @param entity The object to refresh.
     */
    void refresh (T entity);
    /**
     * @param maxResults max. results to return - non-positive=same as "all"
     * @return {@link List} of up to specified <code>maxResults</code>
     * items
     */
    List<T> findMax (int maxResults);
    /**
     * @param maxResults max. results to return - non-positive=same as "all"
     * @param firstResult zero-based first result to return - ignored if negative
     * @return {@link List} of up to specified <code>maxResults</code>
     * items, starting from specified <code>firstResult</code> item
     */
    List<T> findPaged (int maxResults, int firstResult);
}
