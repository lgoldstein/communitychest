package com.vmware.spring.workshop.dao;

import com.vmware.spring.workshop.model.Identified;

/**
 * @param <T> Type of {@link Identified} entity being persisted
 * @author Lyor G.
 * @since Oct 3, 2011 4:11:28 PM
 */
public interface IdentifiedGenericDao<T extends Identified> extends GenericDao<T,Long> {
    // nothing extra for now
}
