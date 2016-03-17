package com.vmware.spring.workshop.dao.impl.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.GenericDao;
import com.vmware.spring.workshop.dao.impl.QueryNameResolver;

/**
 * @author lgoldstein
 */
public abstract class AbstractGenericHibernateDaoImpl<T,ID extends Serializable>
                extends HibernateDaoSupport
                implements GenericDao<T, ID> {
    protected final QueryNameResolver    _queryNameResolver;
    protected AbstractGenericHibernateDaoImpl(final Class<T> pClass, final Class<ID> idClass) {
        if (null == (_persistentClass = pClass))
            throw new IllegalStateException("Missing persistent class instance");
        if (null == (_idClass = idClass))
            throw new IllegalStateException("Missing persistent ID type");
        _queryNameResolver = new QueryNameResolver(pClass);
    }

    private ResultTransformer _defaultResultTransformer = DistinctRootEntityResultTransformer.INSTANCE;
    public ResultTransformer getDefaultResultTransformer() {
        return _defaultResultTransformer;
    }

    public void setDefaultResultTransformer(
            ResultTransformer defaultResultTransformer) {
        _defaultResultTransformer = defaultResultTransformer;
    }

    private final Class<T> _persistentClass;

    @Override
    public final/* no cheating */Class<T> getPersistentClass() {
        return _persistentClass;
    }

    /**
     * @return Name of alias to be used in {@link DetachedCriteria}s.
     *         Default=the simple name of the {@link #getPersistentClass()}
     */
    public String getClassAlias() {
        final Class<T> pClass = getPersistentClass();
        return (null == pClass) /* should not happen */? null : pClass
                .getSimpleName();
    }

    private final Class<ID> _idClass;
    @Override
    public final/* no cheating */Class<ID> getIdClass() {
        return _idClass;
    }

    // Ugly hack since inherited 'setSessionFactory' is 'final'
    @Inject
    public void setDaoSessionFactory(SessionFactory sessionFactory) {
        setSessionFactory(sessionFactory);
    }

    @Override
    public Iterable<T> save(Iterable<? extends T> entities) {
        final Collection<T>    result=new LinkedList<T>();
        for (final T entity : entities) {
            save(entity);
            result.add(entity);
        }
        return result;
    }

    @Override
    @Transactional(readOnly=true)
    public T findOne(ID id) {
        Assert.notNull(id, "No entity identifier provided");

        final Class<T> pClass = getPersistentClass();
        final HibernateTemplate tmpl = getHibernateTemplate();
        final Object res=tmpl.get(pClass, id);
        return pClass.cast(res);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean exists(ID id) {
        return findOne(id) != null;
    }

    @Override
    @Transactional(readOnly=true)
    public long count() {
        return countByCriteria(true, (Collection<? extends Criterion>) null);
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        for (final T entity : entities) {
            delete(entity);
        }
    }

    /*
     * @see
     * com.vmware.webmon.drwho.collector.dao.CommonOperationsDao#delete(java
     * .lang.Object)
     */
    @Override
    public void delete(final T entity) {
        final HibernateTemplate tmpl = getHibernateTemplate();
        tmpl.delete(entity);
        if (logger.isTraceEnabled())
            logger.trace("delete(" + entity + ") deleted");
    }

    public T deleteById(ID id) {
        final T entity = findOne(id);
        if (entity != null) {
            if (logger.isTraceEnabled())
                logger.trace("delete(ID=" + id + "): " + entity);
            delete(entity);
        } else {
            if (logger.isTraceEnabled())
                logger.trace("delete(ID=" + id + ") no entity found");
        }
        return entity;
    }

    public Number deleteAll(final Collection<? extends T> items) {
        final int numItems = (null == items) ? 0 : items.size();
        if (numItems <= 0)
            return Integer.valueOf(0);

        final HibernateTemplate tmpl = getHibernateTemplate();
        tmpl.deleteAll(items);
        return Integer.valueOf(numItems);
    }

    @Override
    public void deleteAll() {
        deleteAll(findAll());
    }

    @Override
    public void flush() {
        final HibernateTemplate tmpl = getHibernateTemplate();
        tmpl.flush();
    }

    protected Number extractCountResult(final List<?> l) {
        final Object resCount = ((null == l) || (l.size() != 1)) ? null : l
                .get(0);
        if (resCount instanceof Number)
            return (Number) resCount;
        else
            return null;
    }

    /**
     * Extracts the result of a count query
     *
     * @param l
     *            result {@link List} - must be non-null, and contain
     *            <U>exactly</U> <B>one</B> element of type {@link Number}
     *            (usually it will be an {@link Integer})
     * @return count result - 0 if null/empty result list or not a number
     */
    protected Number processCountResult(final List<?> l) {
        if ((null == l) || (l.size() != 1))
            return Long.valueOf(0L);

        final Object resCount = l.get(0);
        if (!(resCount instanceof Number))
            return Long.valueOf(0L);

        return (Number) resCount;
    }

    /**
     * Convert a collection of entities into a single one
     *
     * @param results
     *            The result {@link Collection}
     * @return The unique result, or null if there are no results.
     * @throws IllegalStateException
     *             if there is more than one result
     */
    protected static final Object getUniqueResult(final List<?> results)
            throws IllegalStateException {
        final int size = results.size();
        if (size <= 0)
            return null;

        final Object first = results.get(0);
        for (int rIndex = 1; rIndex < size; rIndex++) {
            final Object other = results.get(rIndex);
            if (other != first)
                throw new IllegalStateException("getUniqueResult(" + results
                        + ") multiple results: " + size);
        }

        return first;
    }

    /**
     * Can be used to replace the default/simple {@link Order} (one that works
     * only on existing attributes) with some other one - e.g., using a
     * sub-selection.
     *
     * @param colName
     *            Column name on which sorting is required
     * @param sortDir
     *            sort direction <code>null</code>/TRUE=ascending
     * @return The effective {@link Order} to use - ignored if null
     *         (discouraged)
     */
    protected Order createCriteriaSortOrder(final String colName,
            final Boolean sortDir) {
        if ((null == colName) || (colName.length() <= 0))
            throw new IllegalArgumentException(
                    "Missing sort column name in sort-by specification");

        if ((null == sortDir) || sortDir.booleanValue())
            return Order.asc(colName);
        else
            return Order.desc(colName);
    }

    protected Order createCriteriaSortOrder(
            final Map.Entry<String, Boolean> sortCol) {
        if (null == sortCol)
            return null;

        return createCriteriaSortOrder(sortCol.getKey(), sortCol.getValue());
    }

    protected int addSortOrderToCriteria(final DetachedCriteria criteria,
            final Collection<? extends Order> ol) {
        final int numSorts = (null == ol) ? 0 : ol.size();
        if (numSorts <= 0)
            return numSorts;

        for (final Order o : ol) {
            if (null == o) // should not happen (or rather discouraged)
                continue;

            criteria.addOrder(o);
        }

        return numSorts;
    }

    /**
     * Can be used to replace the original {@link Criterion} with some other one
     * - e.g., for "virtual" columns
     *
     * @param c
     *            Original {@link Criterion}
     * @return Effective instance to be used - ignored if null (discouraged)
     */
    protected Criterion resolveQueryCriterion(final Criterion c) {
        return c;
    }

    protected int addCriterionToCriteria(final DetachedCriteria criteria,
            final boolean conjunctive,
            final List<? extends Criterion> criterionList) {
        final int numCriteria = (null == criterionList) ? 0 : criterionList
                .size();
        if (numCriteria <= 0)
            return numCriteria;

        // special shortcut for one criterion - doesn't matter if conjunctive or
        // disjunctive
        int numAdded = 0;
        if (1 == numCriteria) {
            final Criterion orgCrit = criterionList.get(0), c = resolveQueryCriterion(orgCrit);
            if (c != null) // should not be otherwise (or rather discouraged)
            {
                criteria.add(c);
                numAdded++;
            }
        } else if (conjunctive) // "shortcut" for conjunctive mode
        {
            for (final Criterion orgCrit : criterionList) {
                final Criterion c = resolveQueryCriterion(orgCrit);
                if (null == c) // should not happen (or rather discouraged)
                    continue;

                criteria.add(c);
                numAdded++;
            }
        } else // >=2 disjunctive criteria
        {
            Disjunction jct = null;
            for (final Criterion orgCrit : criterionList) {
                final Criterion c = resolveQueryCriterion(orgCrit);
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
     *
     * @return Name of entity <U>attribute</U> (<B>not</B> the column) on which
     *         to create the order. If null/empty then no special order is
     *         assumed
     */
    public abstract String getDefaultOrderAttributeName();

    /**
     * Called by default implementation of {@link #createDefaultOrder()} if a
     * non-<code>null</code>/empty value returned by
     * {@link #getDefaultOrderAttributeName()}
     *
     * @return <code>true</code> if default order is ascending
     */
    public boolean isAscendingDefaultOrderDirection() {
        return true;
    }

    /**
     * Called by default implementation of
     * {@link #findByDetachedCriteria(int, int, int, DetachedCriteria)} when
     * paged result is requested and no specific ordering imposed.
     *
     * @return The {@link Order} to be imposed as default - if null, then
     *         ignored. Default=return <code>null</code> (i.e., no order)
     */
    protected Order createDefaultOrder() {
        final String aName = getDefaultOrderAttributeName();
        if (StringUtils.isBlank(aName))
            return null;

        return createCriteriaSortOrder(aName,
                Boolean.valueOf(isAscendingDefaultOrderDirection()));
    }

    @Transactional(readOnly = true)
    public List<?> findByDetachedCriteria(final int maxResult,
            final int firstResult, final boolean addDefaultSortOrder,
            final DetachedCriteria criteria) {
        final HibernateTemplate tmpl = getHibernateTemplate();
        final List<?> res;
        if (firstResult >= 0) {
            /*
             * NOTE: see
             * http://www.ianywhere.com/developer/product_manuals/sqlanywhere
             * /0902/en/html/dbugen9/00000276.htm
             *
             * FIRST and TOP should be used only in conjunction with an ORDER BY
             * clause to ensure consistent results. Use of FIRST or TOP without
             * an ORDER BY triggers a syntax warning, and will likely yield
             * unpredictable results.
             */

            if (addDefaultSortOrder) {
                final Order o = createDefaultOrder();
                if (o != null)
                    criteria.addOrder(o);
            }

            res = tmpl.findByCriteria(criteria, firstResult, maxResult /*
                                                                         * <=0
                                                                         * == no
                                                                         * limit
                                                                         */);
        } else {
            res = tmpl.findByCriteria(criteria);
        }

        if (CollectionUtils.isEmpty(res))
            return Collections.emptyList();
        else
            return res;
    }

    @Transactional(readOnly = true)
    public List<?> findByDetachedCriteria(final DetachedCriteria criteria) {
        return findByDetachedCriteria(-1, -1, false, criteria);
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(int maxResult, int firstResult,
            boolean conjunctive, Collection<? extends Criterion> criterionList,
            Collection<? extends Order> orderList) {
        final Class<T> pClass = getPersistentClass();
        final DetachedCriteria criteria = DetachedCriteria.forClass(pClass,
                getClassAlias());
        @SuppressWarnings("unused")
        final int numCriteria = addCriterionToCriteria(criteria, conjunctive,
                resolveCriteriaList(criterionList)), numSorts = addSortOrderToCriteria(
                criteria, orderList);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        @SuppressWarnings("unchecked")
        final List<T> res = (List<T>) findByDetachedCriteria(maxResult,
                firstResult, (numSorts <= 0), criteria);
        return res;
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(int maxResult, int firstResult,
            boolean conjunctive, Collection<? extends Criterion> criterionList,
            Order... orderList) {
        return findByCriteria(maxResult, firstResult, conjunctive,
                criterionList,
                ArrayUtils.isEmpty(orderList) ? null : Arrays.asList(orderList));
    }

    protected List<Order> getSortOrder(
            final Collection<? extends Map.Entry<String, Boolean>> sortBy) {
        final int numSorts = (null == sortBy) ? 0 : sortBy.size();
        if (numSorts <= 0)
            return null;

        final List<Order> ol = new ArrayList<Order>(numSorts);
        for (final Map.Entry<String, Boolean> sortCol : sortBy) {
            final Order o = createCriteriaSortOrder(sortCol);
            if (null == o) // should not happen (or rather discouraged)
                continue;

            ol.add(o);
        }

        return ol;
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(final int maxResult, final int firstResult,
            final Collection<? extends Map.Entry<String, Boolean>> sortBy,
            final boolean conjunctive,
            final Collection<? extends Criterion> criterionList) {
        return findByCriteria(maxResult, firstResult, conjunctive,
                criterionList, getSortOrder(sortBy));
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(final int maxResult, final int firstResult,
            final Collection<? extends Map.Entry<String, Boolean>> sortBy,
            final boolean conjunctive, final Criterion... criteria) {
        return findByCriteria(maxResult, firstResult, sortBy, conjunctive,
                ArrayUtils.isEmpty(criteria) ? null : Arrays.asList(criteria));
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(boolean conjunctive,
            Collection<? extends Criterion> criteria,
            Collection<? extends Order> orderList) {
        return findByCriteria((-1), (-1), conjunctive, criteria, orderList);
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(boolean conjunctive,
            Collection<? extends Criterion> criteria, Order... orderList) {
        return findByCriteria(conjunctive, criteria,
                ArrayUtils.isEmpty(orderList) ? null : Arrays.asList(orderList));
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(final boolean conjunctive,
            final Collection<? extends Criterion> criterionList) {
        return findByCriteria(conjunctive, criterionList,
                (Collection<? extends Order>) null);
    }

    @Transactional(readOnly = true)
    public List<T> findByCriteria(boolean conjunctive, Criterion... criteria) {
        return findByCriteria(conjunctive, ArrayUtils.isEmpty(criteria) ? null
                : Arrays.asList(criteria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return findByCriteria(true, (Collection<? extends Criterion>) null);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(final boolean conjunctive,
            final Collection<? extends Criterion> criteria) {
        final Class<T> pClass = getPersistentClass();
        final DetachedCriteria queryCrit = DetachedCriteria.forClass(pClass,
                getClassAlias()).setProjection(Projections.rowCount());
        @SuppressWarnings("unused")
        final int numCriteria = addCriterionToCriteria(queryCrit, conjunctive,
                resolveCriteriaList(criteria));
        queryCrit
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        final List<?> l = findByDetachedCriteria(queryCrit);
        final Number itemsCount = processCountResult(l);
        return itemsCount.longValue();
    }

    @Transactional(readOnly = true)
    public long countByCriteria(final boolean conjunctive,
            final Criterion... criteria) {
        return countByCriteria(conjunctive, ArrayUtils.isEmpty(criteria) ? null
                : Arrays.asList(criteria));
    }

    protected List<? extends Criterion> resolveCriteriaList(
            final Collection<? extends Criterion> orgList) {
        if (CollectionUtils.isEmpty(orgList))
            return Collections.emptyList();
        else if (orgList instanceof List<?>)
            return (List<? extends Criterion>) orgList;
        else
            return new ArrayList<Criterion>(orgList);
    }

    protected Session getCurrentSession() {
        final SessionFactory factory = getSessionFactory();
        return factory.getCurrentSession();
    }

    protected Query getNamedQuery(final String queryName) {
        final Session     session = getCurrentSession();
        final String    effectiveName = _queryNameResolver.getEffectiveQueryName(queryName);
        return session.getNamedQuery(effectiveName);
    }

    protected Query getNamedQuery(final String namedQueryString,
            final Object... values) {
        final Query query = getNamedQuery(namedQueryString);
        final int numValues = (values == null) ? 0 : values.length;
        for (int i = 0; i < numValues; i++) {
            query.setParameter(i, values[i]);
        }

        return query;
    }

    protected T getDefaultUniqueResult(final Query query) {
        return getUniqueResult(query, getDefaultResultTransformer());
    }

    protected T getUniqueResult(final Query query,
            final ResultTransformer transformer) {
        return getUniqueResult(query, transformer, getPersistentClass());
    }

    protected <V> V getUniqueResult(final Query query,
            final ResultTransformer transformer, final Class<V> valueType) {
        if (transformer != null)
            query.setResultTransformer(transformer);

        final List<?> res = query.list();
        final int numResults = (res == null) ? 0 : res.size();
        if (numResults <= 0)
            return null;
        if (numResults != 1)
            throw new IllegalStateException("Multiple results (" + numResults
                    + ")" + " for type=" + valueType.getSimpleName()
                    + " on query " + query.getQueryString());

        final Object value = res.get(0);
        if (value == null)
            throw new IllegalStateException("Null unique result  for type="
                    + valueType.getSimpleName() + " on query "
                    + query.getQueryString());

        final Class<?> vc = value.getClass();
        if (!valueType.isAssignableFrom(vc))
            throw new IllegalStateException("Mismatched unqiue result type ("
                    + vc.getName() + ")" + " instead of " + valueType.getName()
                    + " on querry " + query.getQueryString());

        return valueType.cast(value);
    }

    protected List<T> getDefaultQueryResults(final Query query) {
        return getDefaultQueryResults(query, -1, -1);
    }

    protected List<T> getQueryResults(final Query query,
            final ResultTransformer transformer) {
        return getQueryResults(query, transformer, -1, -1);
    }

    protected List<T> getDefaultQueryResults(final Query query,
            final int startIndex, final int maxResults) {
        return getQueryResults(query, getDefaultResultTransformer(),
                startIndex, maxResults);
    }

    protected List<T> getQueryResults(final Query query,
            final ResultTransformer transformer, final int startIndex,
            final int maxResults) {
        if (startIndex >= 0)
            query.setFirstResult(startIndex);
        if (maxResults > 0)
            query.setMaxResults(maxResults);

        return getQueryResults(query, transformer, getPersistentClass());
    }

    protected <V> List<V> getQueryResults(final Query query,
            final ResultTransformer transformer, final Class<V> valueType) {
        if (transformer != null)
            query.setResultTransformer(transformer);

        final List<?> res = query.list();
        final int numResults = (res == null) ? 0 : res.size();
        if (numResults <= 0)
            return Collections.emptyList();

        // test the 1st object in list
        final Object value = res.get(0);
        if (value == null)
            throw new IllegalStateException("Null query result value for type="
                    + valueType.getSimpleName() + " on query "
                    + query.getQueryString());
        final Class<?> vc = value.getClass();
        if (!valueType.isAssignableFrom(vc))
            throw new IllegalStateException("Mismatched result member type ("
                    + vc.getName() + ")" + " instead of " + valueType.getName()
                    + " on query " + query.getQueryString());

        @SuppressWarnings("unchecked")
        final List<V> ret = (List<V>) res;
        return ret;
    }

    protected Number countDefaultQuery(final Query query) {
        return countQuery(query, getDefaultResultTransformer());
    }

    protected Number countQuery(final Query query,
            final ResultTransformer transformer) {
        return getUniqueQueryResult(query, transformer, Number.class);
    }

    protected <V> V getUniqueDefaultQueryResult(final Query query,
            final Class<V> resultType) {
        return getUniqueQueryResult(query, getDefaultResultTransformer(),
                resultType);
    }

    protected <V> V getUniqueQueryResult(final Query query,
            final ResultTransformer transformer, final Class<V> resultType) {
        if (transformer != null)
            query.setResultTransformer(transformer);

        final Object result = query.uniqueResult();
        final Class<?> resClass = (result == null) ? null : result.getClass();
        if (resClass == null)
            return null;

        if (!resultType.isAssignableFrom(resClass))
            throw new UnsupportedOperationException("getUniqueQueryResult("
                    + query + ")" + " unknown result type ("
                    + resClass.getName() + ")" + ": " + result);
        return resultType.cast(result);
    }

}
