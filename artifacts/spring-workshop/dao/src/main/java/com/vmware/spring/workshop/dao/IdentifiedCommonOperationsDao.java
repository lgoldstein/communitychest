package com.vmware.spring.workshop.dao;

import com.vmware.spring.workshop.model.Identified;

/**
 * @author lgoldstein
 */
public interface IdentifiedCommonOperationsDao<T extends Identified> extends CommonOperationsDao<T,Long> {
    // nothing further
}
