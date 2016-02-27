package com.vmware.spring.workshop.dao.finder;

import java.io.Serializable;

import com.vmware.spring.workshop.dao.CommonOperationsDao;

/**
 * @author lgoldstein
 */
public interface InstanceFinder<ID extends Serializable,T,DAO extends CommonOperationsDao<T,ID>> {
	T findInstance (DAO dao, T sourceInstance);
}
