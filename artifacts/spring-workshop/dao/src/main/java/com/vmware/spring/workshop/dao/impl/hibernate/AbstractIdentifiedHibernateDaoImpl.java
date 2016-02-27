package com.vmware.spring.workshop.dao.impl.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.transform.ResultTransformer;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.IdentifiedGenericDao;
import com.vmware.spring.workshop.model.Identified;

/**
 * @author lgoldstein
 */
public abstract class AbstractIdentifiedHibernateDaoImpl<T extends Identified>
				extends AbstractGenericHibernateDaoImpl<T,Long>
				implements IdentifiedGenericDao<T> {
	protected AbstractIdentifiedHibernateDaoImpl (Class<T> pClass)
	{
		super(pClass, Long.class);
	}

	@Override
	public T save(T entity) {
		final HibernateTemplate tmpl=getHibernateTemplate();
		Serializable			saveId=entity.getId();
		if (saveId == null) {	// i.e., create
			if ((saveId=tmpl.save(entity)) == null)
				throw new IllegalStateException("No ID generated for entity=" + entity);

			if (logger.isTraceEnabled())
				logger.trace("save(" + entity + ") created: ID=" + saveId);
		} else {
			tmpl.update(entity);

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
	public String getDefaultOrderAttributeName ()
	{
		return Identified.ID_COL_NAME;
	}

	protected List<T> getNamedIdDefaultQueryResults (String name, Long id)
	{
		return getNamedIdQueryResults(name, id, getDefaultResultTransformer());
	}

	protected List<T> getNamedIdQueryResults (String name, Long id, ResultTransformer transformer)
	{
		return getIdQueryResults(getNamedQuery(name), transformer, id);
	}

	protected List<T> getIdQueryResults (Query query, ResultTransformer transformer, Long id)
	{
		Assert.state(query != null, "No query provided");
		Assert.notNull(id, "No identifier provided");
		query.setParameter(Identified.ID_COL_NAME, id);
		return getQueryResults(query, transformer);
	}

	protected Number countByNamedIdDefaultQuery (String name, Long id)
	{
		return countByNamedIdQuery(name, id, getDefaultResultTransformer());
	}

	protected Number countByNamedIdQuery (String name, Long id, ResultTransformer transformer)
	{
		return countByIdQuery(getNamedQuery(name), transformer, id);
	}

	protected Number countByIdQuery (Query query, ResultTransformer transformer, Long id)
	{
		Assert.state(query != null, "No query provided");
		Assert.notNull(id, "No identifier provided");
		query.setParameter(Identified.ID_COL_NAME, id);
		return countQuery(query, transformer);
	}
}
