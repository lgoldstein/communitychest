package com.vmware.spring.workshop.dao;
import java.io.Serializable;

/**
 * @param <T> Type of object being persisted 
 * @param <ID> Type of {@link Serializable} unique ID being used as primary key 
 * @author Lyor G.
 * @since Oct 3, 2011 4:08:27 PM
 */
public interface GenericDao<T,ID extends Serializable> extends CommonOperationsDao<T,ID> {
	/**
	 * @return The {@link Class} representing the entity managed by this DAO
	 */
	Class<T> getPersistentClass ();
	/**
	 * @return The {@link Class} representing the primary entity key type
	 */
	Class<ID> getIdClass ();
}