/*
 * 
 */
package net.community.chest.spring.orm.hibernate3;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.community.chest.lang.ExceptionUtil;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Provides some default implementations for some {@link GenericHibernateDao} methods</P>
 * 
 * @param <T> Type of object being persisted 
 * @param <ID> Type of {@link Serializable} unique ID being used as primary key 
 * @author Lyor G.
 * @since Jan 11, 2010 2:17:42 PM
 */
public abstract class AbstractGenericHibernateDaoImpl<T,ID extends Serializable>
		extends HibernateDaoSupport
		implements GenericHibernateDao<T,ID> {
	private final Class<T> _persistentClass;
	/*
	 * @see com.emc.fiji.server.persistence.dao.GenericDao#getPersistentClass()
	 */
	@Override
	public final /* no cheating */ Class<T> getPersistentClass ()
	{
		return _persistentClass;
	}
	/**
	 * @return Name of alias to be used in {@link DetachedCriteria}. Default=the
	 * simple name of the {@link #getPersistentClass()}
	 */
	public String getClassAlias ()
	{
		final Class<T> pClass=getPersistentClass();
		return (null == pClass) /* should not happen */ ? null : pClass.getSimpleName();
	}

	private final Class<ID>	_idClass;
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getIdClass()
	 */
	@Override
	public final /* no cheating */ Class<ID> getIdClass ()
	{
		return _idClass;
	}

	protected AbstractGenericHibernateDaoImpl (final Class<T> pClass, final Class<ID> idClass)
	{
		if (null == (_persistentClass=pClass))
			throw new IllegalArgumentException("Missing persistent class instance");
		if (null == (_idClass=idClass))
			throw new IllegalArgumentException("Missing persistent ID type");
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#create(java.lang.Object, boolean)
	 */
	@Override
	public void create (T entity, boolean flushIt)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		final Serializable		saveId=tmpl.save(entity);
		if (null == saveId)
			throw new IllegalStateException("No ID generated for entity=" + entity);
		if (flushIt)
			flush();
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#create(java.lang.Object)
	 */
	@Override
	public void create (final T entity)
	{
		create(entity, true);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#merge(java.lang.Object)
	 */
	@Override
	public T merge (final T entity)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		final Object			res=tmpl.merge(entity);
		if (res != entity)	// debug breakpoint
			return entity;
		else
			return entity;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#refresh(java.lang.Object)
	 */
	@Override
	public void refresh (final T entity)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.refresh(entity);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#createOrUpdate(java.lang.Object)
	 */
	@Override
	public void createOrUpdate (T entity)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.saveOrUpdate(entity);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#delete(java.lang.Object)
	 */
	@Override
	public void delete (final T entity)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.delete(entity);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericHibernateDao#delete(java.io.Serializable)
	 */
	@Override
	public T delete (ID id)
	{
		final T	entity=findById(id);
		if (entity != null)
			delete(entity);
		return entity;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#deleteAll(java.util.Collection)
	 */
	@Override
	public Number deleteAll (final Collection<? extends T> items)
	{
		final int numItems=(null == items) ? 0 : items.size();
		if (numItems <= 0)
			return Integer.valueOf(0);

		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.deleteAll(items);
		return Integer.valueOf(numItems);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#deleteAll()
	 */
	@Override
	public Number deleteAll ()
	{
/* TODO 		final Collection<String>	xtraTbls=getForeignTablesNames();
		if ((null == xtraTbls) || (xtraTbls.size() <= 0))
*/
			return deleteAll(findAll());
/* TODO 
		else
			return countDeletedEntities(xtraTbls);
*/
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#flush()
	 */
	@Override
	public void flush ()
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.flush();
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#update(java.lang.Object, boolean)
	 */
	@Override
	public void update (final T entity, final boolean flushIt)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.update(entity);
		if (flushIt)
			flush();
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#update(java.lang.Object)
	 */
	@Override
	public void update (final T entity)
	{
		update(entity, true);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByQuery(java.lang.String, java.lang.Object[])
	 */
	@Override
	public List<?> findByQuery (String queryString, Object... values)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.find(queryString, values);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByQuery(java.lang.String)
	 */
	@Override
	public List<?> findByQuery (String queryString)
	{
		return findByQuery(queryString, (Object[]) null);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByNamedQuery(java.lang.String, java.lang.Object[])
	 */
	@Override
	public List<?> findByNamedQuery (String queryName, Object... values)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.findByNamedQuery(queryName, values);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findById(java.io.Serializable, boolean)
	 */
	@Override
	public T findById (ID id, boolean lock)
	{
		final Class<T>			pClass=getPersistentClass();
		final HibernateTemplate	tmpl=getHibernateTemplate();
		final Object			res;
		if (lock)
			res = tmpl.get(pClass, id, LockMode.PESSIMISTIC_WRITE);	// just so we have a debug breakpoint
		else
			res = tmpl.get(pClass, id);
		return pClass.cast(res);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findById(java.io.Serializable)
	 */
	@Override
	public T findById (ID id)
	{
		return findById(id, false);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByExample(java.lang.Object)
	 */
	@Override
	public List<T> findByExample (T example)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.findByExample(example);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByNamedParam(java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public List<?> findByNamedParam (String queryString, String paramName, Object value)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.findByNamedParam(queryString, paramName, value);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByNamedParam(java.lang.String, java.lang.String[], java.lang.Object[])
	 */
	@Override
	public List<?> findByNamedParam (String queryString, String[] paramName, Object[] value)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.findByNamedParam(queryString, paramName, value);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByNamedParam(java.lang.String, java.util.Map)
	 */
	@Override
	public List<?> findByNamedParam (final String queryString, final Map<String,?> paramsMap)
	{
		final Collection<? extends Map.Entry<String,?>>	params=
			((null == paramsMap) || (paramsMap.size() <= 0)) ? null : paramsMap.entrySet();
		final int					numParams=(null == params) ? 0 : params.size();
		final Collection<String>	names=
			(numParams <= 0) ? null : new ArrayList<String>(numParams);
		final Collection<Object>	values=
			(numParams <= 0) ? null : new ArrayList<Object>(numParams);
		
		if (numParams > 0)
		{
			for (final Map.Entry<String,?> pe : params)
			{
				final String	name=(null == pe) ? null : pe.getKey();
				if ((null == name) || (name.length() <= 0))
					continue;	// should not happen
				names.add(name);
				values.add(pe.getValue());
			}
		}

		final int		numNames=(null == names) ? 0 : names.size();
		final String[]	na=(numNames <= 0) ? null : names.toArray(new String[numNames]);
		final int		numValues=(null == values) ? 0 : values.size();
		final Object[]	va=(numValues <= 0) ? null : values.toArray(new Object[numValues]); 

		return findByNamedParam(queryString, na, va);
	}
	/**
	 * Convert a collection of entities into a single one
	 * 
	 * @param results The result {@link Collection}
	 * @return The unique result, or null if there are no results.
	 * @throws IllegalStateException if there is more than one result
	 */
	protected static final Object getUniqueResult (final List<?> results) throws IllegalStateException
	{
		final int size=results.size();
		if (size <= 0)
			return null;

		final Object first=results.get(0);
		for (int	rIndex=1; rIndex < size; rIndex++)
		{
			final Object	other=results.get(rIndex);
			if (other != first)
				throw new IllegalStateException("getUniqueResult(" + results + ") multiple results: " + size);
		}

		return first;
	}
	/**
	 * Can be used to replace the default/simple {@link Order} (one that works
	 * only on existing attributes) with some other one - e.g., using a
	 * sub-selection.
	 * 
	 * @param colName Column name on which sorting is required
	 * @param sortDir sort direction <code>null</code>/TRUE=ascending
	 * @return The effective {@link Order} to use - ignored if null (discouraged)
	 */
	protected Order createCriteriaSortOrder (final String colName, final Boolean sortDir)
	{
		if ((null == colName) || (colName.length() <= 0))
			throw new IllegalArgumentException("Missing sort column name in sort-by specification");

		if ((null == sortDir) || sortDir.booleanValue())
			return Order.asc(colName);
		else
			return Order.desc(colName);
	}

	protected Order createCriteriaSortOrder (final Map.Entry<String, Boolean> sortCol)
	{
		if (null == sortCol)
			return null;

		return createCriteriaSortOrder(sortCol.getKey(), sortCol.getValue());
	}

	protected int addSortOrderToCriteria (final DetachedCriteria criteria, final Collection<? extends Order> ol)
	{
		final int	numSorts=(null == ol) ? 0 : ol.size();
		if (numSorts <= 0)
			return numSorts;

		for (final Order o : ol)
		{
			if (null == o)	// should not happen (or rather discouraged)
				continue;

			criteria.addOrder(o);
		}

		return numSorts;
	}
	/**
	 * Can be used to replace the original {@link Criterion} with some other one -
	 * e.g., for "virtual" columns
	 * 
	 * @param c Original {@link Criterion}
	 * @return Effective instance to be used - ignored if null (discouraged)
	 */
	protected Criterion resolveQueryCriterion (final Criterion c)
	{
		return c;
	}

	protected int addCriterionToCriteria (final DetachedCriteria				criteria,
										  final boolean 						conjunctive,
										  final Collection<? extends Criterion> criterionList)
	{
		final int numCriteria=(null == criterionList) ? 0 : criterionList.size();
		if (numCriteria <= 0)
			return numCriteria;

		// special shortcut for one criterion - doesn't matter if conjunctive or disjunctive
		int numAdded = 0;
		if (1 == numCriteria)
		{
			final Iterator<? extends Criterion> i=criterionList.iterator();
			final Criterion						orgCrit=
				((null == i) || (!i.hasNext())) /* should not happen */? null : i.next(),
												c=resolveQueryCriterion(orgCrit);

			if (c != null) // should not be otherwise (or rather discouraged)
			{
				criteria.add(c);
				numAdded++;
			}
		}
		else if (conjunctive) // "shortcut" for conjunctive mode
		{
			for (final Criterion orgCrit : criterionList)
			{
				final Criterion c=resolveQueryCriterion(orgCrit);
				if (null == c) // should not happen (or rather discouraged)
					continue;

				criteria.add(c);
				numAdded++;
			}
		}
		else // >=2 disjunctive criteria
		{
			Disjunction jct=null;
			for (final Criterion orgCrit : criterionList)
			{
				final Criterion c=resolveQueryCriterion(orgCrit);
				if (null == c) // should not happen (or rather discouraged)
					continue;

				if (null == jct)
					jct = Restrictions.disjunction();

				jct.add(c);
				numAdded++;
			}

			if (jct != null) // should not be otherwise (or rather discouraged)
				criteria.add(jct);
		}

		return numAdded;
	}
	/**
	 * Called by default implementation of {@link #createDefaultOrder()} - if
	 * non-<code>null</code>/empty value returned then used to create the
	 * default {@link Order} if no other(s) provided 
	 * @return Name of entity <U>attribute</U> (<B>not</B> the column) on which
	 * to create the order. If null/empty then no special order is assumed
	 */
	public abstract String getDefaultOrderAttributeName ();
	/**
	 * Called by default implementation of {@link #createDefaultOrder()} if
	 * a non-<code>null</code>/empty value returned by {@link #getDefaultOrderAttributeName()}
	 * @return <code>true</code> if default order is ascending
	 */
	public boolean isAscendingDefaultOrderDirection ()
	{
		return true;
	}
	/**
	 * Called by default implementation of {@link #findByDetachedCriteria(int, int, boolean, DetachedCriteria)}
	 * when paged result is requested and no specific ordering imposed.
	 * @return The {@link Order} to be imposed as default - if null, then
	 * ignored. Default=return <code>null</code> (i.e., no order)
	 */
	protected Order createDefaultOrder ()
	{
		final String	aName=getDefaultOrderAttributeName();
		if ((null == aName) || (aName.length() <= 0))
			return null;

		return createCriteriaSortOrder(aName, Boolean.valueOf(isAscendingDefaultOrderDirection()));
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByDetachedCriteria(int, int, boolean, org.hibernate.criterion.DetachedCriteria)
	 */
	@Override
	public List<?> findByDetachedCriteria (final int				maxResult,
										   final int				firstResult,
										   final boolean			addDefaultSortOrder,
										   final DetachedCriteria	criteria)
	{
		final HibernateTemplate tmpl=getHibernateTemplate();
		if (firstResult >= 0)
		{
			/*
			 *  NOTE: see http://www.ianywhere.com/developer/product_manuals/sqlanywhere/0902/en/html/dbugen9/00000276.htm
			 *  
			 *  	FIRST and TOP should be used only in conjunction with an ORDER BY
			 *  	clause to ensure consistent results. Use of FIRST or TOP without
			 *  	an ORDER BY triggers a syntax warning, and will likely yield
			 *  	unpredictable results.
			 */

			if (addDefaultSortOrder)
			{
				final Order	o=createDefaultOrder();
				if (o != null)
					criteria.addOrder(o);
			}

			return tmpl.findByCriteria(criteria, firstResult, maxResult /* <=0 == no limit */);
		}

		return tmpl.findByCriteria(criteria);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByDetachedCriteria(org.hibernate.criterion.DetachedCriteria)
	 */
	@Override
	public List<?> findByDetachedCriteria (final DetachedCriteria criteria)
	{
		return findByDetachedCriteria(-1, -1, false, criteria);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(int, int, boolean, java.util.Collection, java.util.Collection)
	 */
	@Override
	public List<T> findByCriteria (int maxResult, int firstResult,
			boolean conjunctive, Collection<? extends Criterion> criterionList,
			Collection<? extends Order>	orderList)
	{
		final Class<T>			pClass=getPersistentClass();
		final DetachedCriteria	criteria=
			DetachedCriteria.forClass(pClass, getClassAlias());
		@SuppressWarnings("unused")
		final int 				numCriteria=
			addCriterionToCriteria(criteria, conjunctive, criterionList),
		 						numSorts=
		 	addSortOrderToCriteria(criteria, orderList);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		@SuppressWarnings("unchecked")
		final List<T>	res=
			(List<T>) findByDetachedCriteria(maxResult, firstResult, (numSorts <= 0), criteria);
		if ((null == res) || (res.size() <= 0))
			return Collections.emptyList();

		return res;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(int, int, boolean, java.util.Collection, org.hibernate.criterion.Order[])
	 */
	@Override
	public 	List<T> findByCriteria (int maxResult, int firstResult,
		    boolean conjunctive, Collection<? extends Criterion> criterionList,
		    Order ...orderList)
	{
		return findByCriteria(maxResult, firstResult,
							  conjunctive, criterionList,
							  ((null == orderList) || (orderList.length <= 0)) ? null : Arrays.asList(orderList));
	}

	protected List<Order> getSortOrder (final Collection<? extends Map.Entry<String, Boolean>>	sortBy)
	{
		final int 			numSorts=(null == sortBy) ? 0 : sortBy.size();
		if (numSorts <= 0)
			return null;

		final List<Order>	ol=new ArrayList<Order>(numSorts);
		for (final Map.Entry<String, Boolean> sortCol : sortBy)
		{
			final Order o=createCriteriaSortOrder(sortCol);
			if (null == o) // should not happen (or rather discouraged)
				continue;

			ol.add(o);
		}

		return ol;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(int, int, java.util.Collection, boolean, java.util.Collection)
	 */
	@Override
	public List<T> findByCriteria (final int maxResult, final int firstResult,
			   final Collection<? extends Map.Entry<String, Boolean>> sortBy,
			   final boolean conjunctive,
			   final Collection<? extends Criterion> criterionList)
	{
		return findByCriteria(maxResult, firstResult, conjunctive, criterionList, getSortOrder(sortBy));
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(int, int, java.util.Collection, boolean, org.hibernate.criterion.Criterion[])
	 */
	@Override
	public List<T> findByCriteria (final int maxResult, final int firstResult,
			   final Collection<? extends Map.Entry<String, Boolean>> sortBy,
			   final boolean conjunctive, final Criterion... criteria)
	{
		return findByCriteria(maxResult, firstResult, sortBy, conjunctive, ((null == criteria) || (criteria.length <= 0)) ? null : Arrays.asList(criteria));
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(boolean, java.util.Collection, java.util.Collection)
	 */
	@Override
	public List<T> findByCriteria (boolean conjunctive,
								   Collection<? extends Criterion> criteria,
								   Collection<? extends Order> orderList)
	{
		return findByCriteria((-1), (-1), conjunctive, criteria, orderList);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(boolean, java.util.Collection, org.hibernate.criterion.Order[])
	 */
	@Override
	public List<T> findByCriteria (boolean conjunctive,
								   Collection<? extends Criterion> criteria,
								   Order ... orderList)
	{
		return findByCriteria(conjunctive, criteria,
				((null == orderList) || (orderList.length <= 0)) ? null : Arrays.asList(orderList));
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(boolean, java.util.Collection)
	 */
	@Override
	public List<T> findByCriteria (final boolean conjunctive, final Collection<? extends Criterion> criterionList)
	{
		return findByCriteria(conjunctive, criterionList, (Collection<? extends Order>) null);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findByCriteria(boolean, org.hibernate.criterion.Criterion[])
	 */
	@Override
	public List<T> findByCriteria (boolean conjunctive, Criterion... criteria)
	{
		return findByCriteria(conjunctive, ((null == criteria) || (criteria.length <= 0)) ? null : Arrays.asList(criteria));
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findPaged(int, int)
	 */
	@Override
	public List<T> findPaged (int maxResults, int firstResult)
	{
		return findByCriteria(maxResults, firstResult, null, true, (Collection<? extends Criterion>) null);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findAll()
	 */
	@Override
	public List<T> findAll ()
	{
		return findByCriteria(true, (Collection<? extends Criterion>) null);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#findMax(int)
	 */
	@Override
	public List<T> findMax (int maxResults)
	{
		if (maxResults <= 0)
			return findAll();
		else
			return findPaged(0, maxResults);
	}

	protected Number extractCountResult (final List<?> l)
	{
		final Object resCount=((null == l) || (l.size() != 1)) ? null : l.get(0);
		if (resCount instanceof Number)
			return (Number) resCount;
		else
			return null;
	}
	/**
	 * Extracts the result of a count query
	 * @param l result {@link List} - must be non-null, and contain <U>exactly</U>
	 * <B>one</B> element of type {@link Number} (usually it will be an {@link Integer})
	 * @return count result - 0 if null/empty result list or not a number
	 */
	protected Number processCountResult (final List<?> l)
	{
		if ((null == l) || (l.size() != 1))
			return Long.valueOf(0L);

		final Object resCount=l.get(0);
		if (!(resCount instanceof Number))
			return Long.valueOf(0L);

		return (Number) resCount;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#countByCriteria(boolean, java.util.Collection)
	 */
	@Override
	public long countByCriteria (final boolean conjunctive, final Collection<? extends Criterion> criteria)
	{
		final Class<T> 			pClass=getPersistentClass();
		final DetachedCriteria	queryCrit=
			DetachedCriteria.forClass(pClass, getClassAlias())
							.setProjection(Projections.rowCount());
		@SuppressWarnings("unused")
		final int				numCriteria=
			addCriterionToCriteria(queryCrit, conjunctive, criteria);
		queryCrit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		final List<?> 	l=findByDetachedCriteria(queryCrit);
		final Number	itemsCount=processCountResult(l);
		return itemsCount.longValue();
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#countByCriteria(boolean, org.hibernate.criterion.Criterion[])
	 */
	@Override
	public long countByCriteria (final boolean conjunctive, final Criterion... criteria)
	{
		return countByCriteria(conjunctive, ((null == criteria) || (criteria.length <= 0)) ? null : Arrays.asList(criteria));
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#countAllItems()
	 */
	@Override
	public long countAllItems ()
	{
		return countByCriteria(true, (Collection<? extends Criterion>) null);
	}
	/**
	 * Returns count value for a class with constraints on an associated object
	 *
	 * @param associationPath - name of associated object which has a constraint
	 * @param associationPathCriterion - criteria constraining the associated object
	 * @param criterion - criteria constraining the main criteria class
	 * @return Count value
	 */
	protected long countByCriteria (String associationPath, Criterion associationPathCriterion, Criterion... criterion)
	{
		final Class<T> 			pClass=getPersistentClass();
		final DetachedCriteria	criteria=DetachedCriteria.forClass(pClass, getClassAlias())
									.setProjection(Projections.rowCount());
		if ((criterion != null) && (criterion.length > 0))
		{
			for (final Criterion c : criterion)
			{
				if (c != null) // should not be otherwise
					criteria.add(c);
			}
		}

		if ((associationPath != null) && (associationPath.length() > 0) && (associationPathCriterion != null))
			criteria.createCriteria(associationPath).add(associationPathCriterion);

		final List<?> 	l=findByDetachedCriteria(criteria);
		final Number	itemsCount=processCountResult(l);
		return itemsCount.longValue();
	}
	/**
	 * Returns count values for a class with constraints on an associated
	 * object, with a certain grouping
	 *
	 * @param associationPath - name of associated object which has a constraint
	 * @param associationPathCriterion - criteria constraining the associated object
	 * @param groupByProjection - group by projection (may be null)
	 * @param criterion - criteria constraining the main criteria class
	 * @return list of size by group
	 */
	protected List<Integer> getCountByCriteria (final String associationPath,
												final Criterion associationPathCriterion,
												final Projection groupByProjection,
												final Criterion... criterion)
	{
		final ProjectionList projList=Projections.projectionList();
		projList.add(Projections.rowCount());
		if (groupByProjection != null)
			projList.add(groupByProjection);

		final Class<T>			pClass=getPersistentClass();
		final DetachedCriteria	criteria=DetachedCriteria.forClass(pClass,getClassAlias())
									.setProjection(projList);
		if ((criterion != null) && (criterion.length > 0))
		{
			for (final Criterion c : criterion)
			{
				if (c != null) // should not be otherwise
					criteria.add(c);
			}
		}

		if ((associationPath != null) && (associationPath.length() > 0) && (associationPathCriterion != null))
			criteria.createCriteria(associationPath).add(associationPathCriterion);

		@SuppressWarnings("unchecked")
		final List<Integer>	res=(List<Integer>) findByDetachedCriteria(criteria);
		return res;
	}

	protected Projection assignCriteriaProjections (
			final Collection<? extends Projection>	projections,
			final DetachedCriteria 					criteria)
	{
		final int	numProjections=(null == projections) ? 0 : projections.size();
		if (numProjections <= 0)
			return null;

		if (1 == numProjections)
		{
			final Iterator<? extends Projection>	pIter=projections.iterator();
			final Projection						p=
				((null == pIter) || (!pIter.hasNext())) ? null : pIter.next();
			if (p != null)
				criteria.setProjection(p);
			return p;
		}

		ProjectionList	projList=null;
		for (final Projection p : projections)
		{
			if (null == p) // should not happen
				continue;
	
			if (null == projList)
				projList = Projections.projectionList();
			projList.add(p);
		}

		if ((projList != null) && (projList.getLength() > 0))
			criteria.setProjection(projList);

		return projList;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getProjectionByCriteria(java.util.Collection, java.util.Collection, java.lang.String, java.util.Collection, int, java.util.Collection)
	 */
	@Override
	public List<?> getProjectionByCriteria (
			final Collection<? extends Projection> 					projections,
			final Collection<? extends Criterion> 					criterionList,
			final String 											associationPath,
			final Collection<? extends Projection> 					associatedProjections,
			final int 												maxResult,
			final Collection<? extends Map.Entry<String,Boolean>>	sortBy)
	{
		final Class<T> 						pClass=getPersistentClass();
		final DetachedCriteria				criteria=DetachedCriteria.forClass(pClass, getClassAlias());
		final Collection<? extends Order>	ol=getSortOrder(sortBy);
		addSortOrderToCriteria(criteria, ol);
		assignCriteriaProjections(projections, criteria);

		if ((criterionList != null) && (criterionList.size() > 0))
		{
			for (final Criterion c : criterionList)
			{
				if (c != null) // should not be otherwise
					criteria.add(c);
			}
		}

		if ((associationPath != null) && (associationPath.length() > 0))
		{
			final DetachedCriteria associationCriteria=criteria.createCriteria(associationPath);
			assignCriteriaProjections(associatedProjections, associationCriteria);
		}

		return findByDetachedCriteria(maxResult, 0, false, criteria);
	}

	protected <V> List<V> toTypedResultList (final Class<V> resultClass, final List<?> ret)
	{
		final int	resSize=(null == ret) ? 0 : ret.size();
		if (resSize <= 0)
			return Collections.emptyList();

		final List<V> typedRet = new ArrayList<V>(resSize);
		for (final Object o: ret)
		{
			try
			{
				if (o instanceof Object[])
				{
					final Object[]		oArr=(Object[]) o;
					int j = 0;
					for (int i=0; i < oArr.length; i++){
						if (oArr[i] != null)
							j++;
					}
					
					final Class<?>[]	clArr=new Class<?>[j];
					final Object[]	objArr=new Object[j];
					j=0;
					for (int i=0; i < oArr.length; i++)
					{
						final Object	io=oArr[i];
						if (io != null)
						{
							clArr[j] = io.getClass();
							objArr[j] = io;
							j++;
						}
					}
					final Constructor<V>	c=resultClass.getConstructor(clArr);
					final V					v=c.newInstance(objArr);
					typedRet.add(v);
				}
				else
				{
					typedRet.add(resultClass.cast(o));
				}				
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}

		return typedRet;
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getProjectionByCriteria(java.util.Collection, java.util.Collection, java.lang.Class)
	 */
	@Override
	public <V> List<V> getProjectionByCriteria (
			final Collection<? extends Projection> projections,
			final Collection<? extends Criterion> criterionList,
			final Class<V> resultClass)
	{
		final List <?> ret=getProjectionByCriteria(projections, criterionList);
		return toTypedResultList(resultClass, ret);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getProjectionByCriteria(java.util.Collection, java.util.Collection, int, java.util.Collection, java.lang.Class)
	 */
	@Override
	public <V> List <V> getProjectionByCriteria (
			final Collection<? extends Projection> projections,
			final Collection<? extends Criterion> criterionList,
			final int maxResult, 
			final Collection<? extends Map.Entry<String, Boolean>> sortBy,
			final Class<V> resultClass)
	{
		final List <?> ret=getProjectionByCriteria(projections, criterionList, null, null, maxResult, sortBy);
		return toTypedResultList(resultClass, ret);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getProjectionByCriteria(java.util.Collection, java.util.Collection, java.lang.String, java.util.Collection, int, java.util.Collection, java.lang.Class)
	 */
	@Override
	public <V> List<V> getProjectionByCriteria (
			final Collection<? extends Projection> projections,
			final Collection<? extends Criterion> criterionList,
			final String associationPath,
			final Collection<? extends Projection> associatedProjections,
			final int maxResult, 
			final Collection<? extends Map.Entry<String, Boolean>> sortBy,
			final Class<V> resultClass)
	{
		final List <?> ret =  getProjectionByCriteria(projections, criterionList, associationPath, associatedProjections, maxResult, sortBy);
		return toTypedResultList(resultClass, ret);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getProjectionByCriteria(java.util.Collection, java.util.Collection)
	 */
	@Override
	public List<?> getProjectionByCriteria (
			final Collection<? extends Projection> projections,
			final Collection<? extends Criterion> criterionList)
	{
		return getProjectionByCriteria(projections, criterionList, null, null, -1, null);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#getProjectionByCriteria(java.util.Collection, java.util.Collection, int, java.util.Collection)
	 */
	@Override
	public List<?> getProjectionByCriteria (
			final Collection<? extends Projection> 					projections,
			final Collection<? extends Criterion>					criterionList,
			final int 												maxResult, 
			final Collection<? extends Map.Entry<String, Boolean>>	sortBy)
	{
		return getProjectionByCriteria(projections, criterionList, null, null, maxResult, sortBy);
	}

	public List<?> getCountByCriteria (final Projection groupBy, final Collection<? extends Criterion> criterionList)
	{
		final List<Projection> projections=new ArrayList<Projection>(2);
		projections.add(Projections.rowCount());
		if (groupBy != null)
			projections.add(groupBy);
		return getProjectionByCriteria(projections, criterionList);
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#executeListQuery(java.lang.String, boolean)
	 */
	@Override
	public List<?> executeListQuery (final String query, final boolean useSQLQuery)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.execute(new HibernateCallback<List<?>>() {
				/*
				 * @see org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(org.hibernate.Session)
				 */
				@Override
				public List<?> doInHibernate (Session session)
					throws HibernateException, SQLException
				{
					final Query	q=useSQLQuery ? session.createSQLQuery(query) : session.createQuery(query);
					return q.list();
				}
			});
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#executeUpdateQuery(java.lang.String, boolean)
	 */
	@Override
	public Number executeUpdateQuery (final String queryString, final boolean useSQLQuery)
	{
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.execute(new HibernateCallback<Number>() {
				/*
				 * @see org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(org.hibernate.Session)
				 */
				@Override
				public Number doInHibernate (Session session) throws HibernateException, SQLException
				{
					final Query query=useSQLQuery ? session.createSQLQuery(queryString) : session.createQuery(queryString);
					final int	numRows=query.executeUpdate();
					return Integer.valueOf(numRows);
				}
			});
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#executeDeleteQuery(boolean, boolean, java.util.Collection)
	 */
	@Override
	public Number executeDeleteQuery (final boolean useSQLQuery, final boolean conjunctive, final Collection<? extends Criterion> cl)
	{
		if ((null == cl) || (cl.size() <= 0))
			return deleteAll();
/* TODO
		final String		aliasValue=getClassAlias(),
							tblValue=getDefaultTableName(),
							sqlFrag=AbstractModelFilter.getCriteriaSQLValue(aliasValue, conjunctive, cl);
		final int			aLen=(null == aliasValue) ? 0 : aliasValue.length(),
							nLen=(null == tblValue) ? 0 : tblValue.length(),
							fragLen=(null == sqlFrag) ? 0 : sqlFrag.length(),
							tLen=Math.max(0, fragLen) + Math.max(0, aLen) + Math.max(0, nLen) + 32;
		final StringBuilder	sb=new StringBuilder(tLen).append("DELETE FROM ").append(tblValue);
		if (fragLen > 0)
			sb.append(" AS ").append(aliasValue)
			  .append(" WHERE ").append(sqlFrag);
		return executeUpdateQuery(sb.toString(), useSQLQuery);
*/
		throw new UnsupportedOperationException("executeDeleteQuery()");
	}
	/*
	 * @see net.community.chest.spring.dao.support.GenericDao#executeDeleteQuery(boolean, boolean, org.hibernate.criterion.Criterion[])
	 */
	@Override
	public Number executeDeleteQuery (final boolean useSQLQuery, final boolean conjunctive, final Criterion ... cl)
	{
		return executeDeleteQuery(useSQLQuery, conjunctive, ((null == cl) || (cl.length <= 0)) ? null : Arrays.asList(cl));
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final Class<?>	pClass=getPersistentClass(),
						iClass=getIdClass();
		final String	pName=(null == pClass) ? null : pClass.getName(),
						iName=(null == iClass) ? null : iClass.getSimpleName();
		return pName + "[" + iName + "]";
	}
}