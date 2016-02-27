package com.vmware.spring.workshop.dao.finder;

import java.io.Serializable;
import java.util.List;

import com.vmware.spring.workshop.dao.CommonOperationsDao;

/**
 * @author lgoldstein
 */
public interface ValuesListFinder<ID extends Serializable,T,DAO extends CommonOperationsDao<T,ID>,ARG> {
	List<T> findMatches (DAO dao, ARG arg);
}
