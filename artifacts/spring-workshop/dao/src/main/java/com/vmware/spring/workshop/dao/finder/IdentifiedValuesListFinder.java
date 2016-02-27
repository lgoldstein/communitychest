package com.vmware.spring.workshop.dao.finder;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.model.Identified;

/**
 * @author lgoldstein
 */
public interface IdentifiedValuesListFinder<T extends Identified,DAO extends IdentifiedCommonOperationsDao<T>,ARG>
		extends ValuesListFinder<Long,T,DAO,ARG> {
	// nothing extra
}
