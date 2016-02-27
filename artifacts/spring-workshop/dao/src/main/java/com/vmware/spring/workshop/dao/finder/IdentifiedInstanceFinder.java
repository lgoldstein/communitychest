package com.vmware.spring.workshop.dao.finder;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.model.Identified;

/**
 * @author lgoldstein
 */
public interface IdentifiedInstanceFinder<T extends Identified,DAO extends IdentifiedCommonOperationsDao<T>>
		extends InstanceFinder<Long,T,DAO> {
	// nothing extra
}
