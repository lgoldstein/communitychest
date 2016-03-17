package com.vmware.spring.workshop.dao;

import java.io.Flushable;
import java.io.Serializable;

import org.springframework.data.repository.CrudRepository;

/**
 * @param <T> Type of object being persisted
 * @param <ID> Type of {@link Serializable} unique ID being used as primary key
 * @author Lyor G.
 * @since Oct 4, 2011 7:15:04 AM
 */
public interface CommonOperationsDao<T,ID extends Serializable> extends Flushable, CrudRepository<T,ID> {
    // nothing extra
}
