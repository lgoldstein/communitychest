package com.vmware.spring.workshop.dao.impl.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.IdentifiedGenericDao;
import com.vmware.spring.workshop.model.Identified;

/**
 * @author lgoldstein
 */
public abstract class AbstractIdentifiedJpaDaoImpl<T extends Identified>
		extends AbstractGenericJpaDaoImpl<T, Long>
		implements IdentifiedGenericDao<T> {
	protected AbstractIdentifiedJpaDaoImpl (Class<T> pClass) {
		super(pClass, Long.class);
	}

	@Override
	public T save(T entity) {
		final EntityManager	em=getEntityManager();
		Serializable		saveId=entity.getId();
		if (saveId == null) {	// i.e., create
			em.persist(entity);

			if (logger.isTraceEnabled())
				logger.trace("save(" + entity + ") created: ID=" + saveId);
		} else {
			em.merge(entity);

			if (logger.isTraceEnabled())
			logger.trace("save(" + entity + ") updated: ID=" + saveId);
		}

		flush();
		return entity;
	}

	@Override
	public void delete(Long id) {
		deleteById(id);
	}

	@Override
	protected String createCountAllQuery(Class<?> pClass, Class<?> idClass) {
		final Map.Entry<String,String>	nameAndAlias=resolveNameAndAlias(pClass, idClass);
		return "SELECT COUNT(" + nameAndAlias.getValue() + "." + Identified.ID_COL_NAME + ")"
			 + " FROM " + nameAndAlias.getKey() + " " + nameAndAlias.getValue()
			 ;
	}
	
	protected List<T> getNamedIdQueryResults (String name, Long id) {
		return getIdQueryResults(getNamedQuery(name), id);
	}

	protected List<T> getIdQueryResults (TypedQuery<T> query, Long id) {
		Assert.notNull(id, "No id specified");
		Assert.state(query != null, "No query provided");
		return getQueryResults(query.setParameter(Identified.ID_COL_NAME, id));
	}

	protected <V> List<V> getNamedIdQueryResults (String name, Long id, Class<V> resultClass) {
		return getResultIdQueryResults(getNamedQuery(name, resultClass), id);
	}

	protected <V> List<V> getResultIdQueryResults (TypedQuery<V> query, Long id) {
		Assert.notNull(id, "No id specified");
		Assert.state(query != null, "No query provided");
		return query.setParameter(Identified.ID_COL_NAME, id).getResultList();
	}
}
