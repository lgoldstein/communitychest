/*
 *
 */
package net.community.chest.spring.orm.hibernate3;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.spring.orm.GenericDao;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 * <P>Adds some Hibernate-specific API(s)
 * @param <T> Type of object being persisted
 * @param <ID> Type of {@link Serializable} unique ID being used as primary key
 * @author Lyor G.
 * @since Jan 11, 2010 2:11:56 PM
 */
public interface GenericHibernateDao<T, ID extends Serializable> extends GenericDao<T, ID> {
    /**
     * Find an object by its identifier
     * @param id The object ID to look for
     * @param lock Hibernate lock mode
     * @return The object, or null if could not find it.
     */
    T findById (ID id, boolean lock);
    /**
     * Creates or updates existing object
     * @param entity parameter to save or update
     */
    void createOrUpdate (T entity);
    /**
     * Find all objects matching the fields of an example object
     * @param example The example object
     * @return The objects found.
     */
    List<T> findByExample (T example);
    /**
     * Find a list of objects filtered by criteria, ordered by some field(s) and
     * with limited number of results (i.e., can be used for paging)
     * @param maxResult max. results to return - non-positive means "all"
     * @param firstResult first result to return - ignored if negative
     * @param conjunctive TRUE=use AND for the supplied {@link Criterion}-s,
     * FALSE=use OR
     * @param criterionList The {@link Criterion}-s to restrict the query - ignored
     * if null/empty
     * @param orderList The <U>order</U> of the entries in the {@link Collection}
     * determines the sort {@link Order} - primary, secondary, tertiary, etc..
     * If null/empty then same as no sorting
     * @return {@link List} of objects matching the generated query - sorted according
     * to specified order (if any)
     */
    List<T> findByCriteria (int maxResult, int firstResult,
                            boolean conjunctive, Collection<? extends Criterion> criterionList,
                            Collection<? extends Order>    orderList);
    List<T> findByCriteria (int maxResult, int firstResult,
                            boolean conjunctive, Collection<? extends Criterion> criterionList,
                            Order ...orderList);

    /**
     * Find a list of objects filtered by criteria, ordered by some field(s) and
     * with limited number of results (i.e., can be used for paging)
     * @param maxResult max. results to return - non-positive means "all"
     * @param firstResult first result to return - ignored if negative
     * @param sortBy Sorting {@link Collection} of {@link java.util.Map.Entry} pairs
     * where the key=column name and the value={@link Boolean} specifying the
     * sort direction (TRUE/null=ascending). If null/empty then same as no
     * sorting. <B>Note:</B> the <U>order</U> of the entries in the {@link Collection}
     * determines the sort order - primary, secondary, tertiary, etc.
     * @param conjunctive TRUE=use AND for the supplied {@link Criterion}-s,
     * FALSE=use OR
     * @param criteria criteria to restrict the query - ignored if null/empty
     * @return {@link List} of objects matching the generated query
     */
    List<T> findByCriteria (int maxResult, int firstResult, Collection<? extends Map.Entry<String,Boolean>> sortBy, boolean conjunctive, Criterion... criteria);
    List<T> findByCriteria (int maxResult, int firstResult, Collection<? extends Map.Entry<String,Boolean>> sortBy, boolean conjunctive, Collection<? extends Criterion> criterionList);

    List<T> findByCriteria (boolean conjunctive, Collection<? extends Criterion> criteria,
                            Collection<? extends Order> orderList);
    List<T> findByCriteria (boolean conjunctive, Collection<? extends Criterion> criteria,
                            Order ... orderList);

    /**
     * Find a list of objects filtered by criteria.
     * @param conjunctive TRUE=use AND for the supplied {@link Criterion}-s,
     * FALSE=use OR
     * @param criteria - {@link Collection} of {@link Criterion} - null/empty
     * is equivalent to {@link #findAll()}
     * @return {@link List} of objects filtered by criteria
     */
    List<T> findByCriteria (boolean conjunctive, Collection<? extends Criterion> criteria);
    List<T> findByCriteria (boolean conjunctive, Criterion ... criteria);
    /**
     * @param conjunctive TRUE=use AND for the supplied {@link Criterion}-s,
     * FALSE=use OR
     * @param criteria {@link Collection} of {@link Criterion} - null/empty is
     * equivalent to counting <U>all</U> objects
     * @return number of objects matching the specified criteria
     */
    long countByCriteria (boolean conjunctive, Collection<? extends Criterion> criteria);
    long countByCriteria (boolean conjunctive, Criterion ... criteria);
    /**
     * @return Currently available number of items - equivalent to calling
     * {@link #countByCriteria(boolean, Collection)} with no criteria
     */
    long countAllItems ();
    /**
     * Get a list of projection results given certain criteria
     * projection(s) may be functions (sum, min, max, etc) or group by.
     * @param projections - list of projections
     * @param criterionList - list of criteria
     * @return List of query results
     */
    List<?> getProjectionByCriteria (Collection<? extends Projection> projections, Collection<? extends Criterion> criterionList);
    /**
     * Get a list of projection results given certain criteria
     * projection(s) may be functions (sum, min, max, etc) or group by.
     * @param projections - list of projections
     * @param criterionList - list of criteria
     * @param maxResult - number of results to return
     * @param sortBy - sort by
     * @return List of query results
     */
    List<?> getProjectionByCriteria (
            final Collection<? extends Projection> projections,
            final Collection<? extends Criterion> criterionList,
            final int maxResult,
            final Collection<? extends Map.Entry<String, Boolean>> sortBy);
    /**
     * Get a list of projection results given certain criteria
     * projection(s) may be functions (sum, min, max, etc) or group by.
     * @param projections - list of projections
     * @param criterionList - list of criteria
     * @param associationPath - path to an associated entity
     * @param associatedProjections - projections on the associated entity
     * @param maxResult - number of results to return
     * @param sortBy - sort by
     * @return List of query results
     */
    List<?> getProjectionByCriteria (
            final Collection<? extends Projection> projections,
            final Collection<? extends Criterion> criterionList,
            final String associationPath,
            final Collection<? extends Projection> associatedProjections,
            final int maxResult,
            final Collection<? extends Map.Entry<String, Boolean>> sortBy);

    /**
     * Get a list of projection results given certain criteria
     * projection(s) may be functions (sum, min, max, etc) or group by.
     * @param <V> Type of expected result
     * @param projections - list of projections
     * @param criterionList - list of criteria
     * @param resultClass - class to which to assign each item in the result list
     * @return List of query results
     */
    <V> List<V> getProjectionByCriteria (final Collection<? extends Projection>    projections,
                                         final Collection<? extends Criterion>    criterionList,
                                         final Class<V>                            resultClass);
    /**
     * Get a list of projection results given certain criteria
     * projection(s) may be functions (sum, min, max, etc) or group by.
     * @param <V> Type of expected result
     * @param projections - list of projections
     * @param criterionList - list of criteria
     * @param maxResult - number of results to return
     * @param sortBy - sort by
     * @param resultClass - class to which to assign each item in the result list
     * @return List of query results
     */
    <V> List <V> getProjectionByCriteria (
            final Collection<? extends Projection> projections,
            final Collection<? extends Criterion> criterionList,
            final int maxResult,
            final Collection<? extends Map.Entry<String, Boolean>> sortBy,
            final Class<V> resultClass);
    /**
     * Get a list of projection results given certain criteria
     * projection(s) may be functions (sum, min, max, etc) or group by.
     * @param <V> Type of expected result
     * @param projections - list of projections
     * @param criterionList - list of criteria
     * @param associationPath - path to an associated entity
     * @param associatedProjections - projections on the associated entity
     * @param maxResult - number of results to return
     * @param sortBy - sort by
     * @param resultClass - class to which to assign each item in the result list
     * @return List of query results
     */
    <V> List <V>  getProjectionByCriteria (
            final Collection<? extends Projection> projections,
            final Collection<? extends Criterion> criterionList,
            final String associationPath,
            final Collection<? extends Projection> associatedProjections,
            final int maxResult,
            final Collection<? extends Map.Entry<String, Boolean>> sortBy,
            final Class<V> resultClass);
    /**
     * Executes a LIST/SELECT query
     * @param query Query to be executed
     * @param useSQLQuery TRUE=use a Query object, FALSE=use an SQLQuery object
     * @return A {@link List} of the result set (may be null/empty)
     */
    List<?> executeListQuery (String query, boolean useSQLQuery);
    /**
     * Executes an UPDATE/DELETE query
     * @param query Query to be executed
     * @param useSQLQuery TRUE=use a Query object, FALSE=use an SQLQuery object
     * @return Number of affected rows
     */
    Number executeUpdateQuery (String query, boolean useSQLQuery);
    /**
     * Delete items from table that match the specified {@link Criterion}-s
     * @param useSQLQuery TRUE=use a Query object, FALSE=use an SQLQuery object
     * @param conjunctive TRUE=AND, FALSE=OR
     * @param cl The {@link Criterion}-s - if null/empty then calls {@link #deleteAll()}
     * @return The {@link Number} of items that were deleted
     */
    Number executeDeleteQuery (boolean useSQLQuery, boolean conjunctive, Collection<? extends Criterion> cl);
    Number executeDeleteQuery (boolean useSQLQuery, boolean conjunctive, Criterion ... cl);

    List<?> findByDetachedCriteria (final DetachedCriteria criteria);
    List<?> findByDetachedCriteria (final int                maxResult,
                                       final int                firstResult,
                                       final boolean            addDefaultSortOrder,
                                       final DetachedCriteria    criteria);
    /**
     * @param queryString Query to be executed
     * @param values The values to be used as <U>positional</U> arguments
     * (if needed) in the final query - may be null/empty
     * @return A {@link List} of {@link Object}-s returned as query result -
     * may be null/empty
     */
    List<?> findByQuery (String queryString, Object... values);
    /**
     * @param queryString Query to be executed
     * @return A {@link List} of {@link Object}-s returned as query result -
     * may be null/empty
     */
    List<?> findByQuery (String queryString);
    List<?> findByNamedQuery (String queryName, Object... values);
    /**
     * @param queryString Query to be executed
     * @param paramName Name of parameter to be replaced by the value
     * @param value Value to be used as replacement
     * @return A {@link List} of {@link Object}-s returned as query result -
     * may be null/empty
     */
    List<?> findByNamedParam (String queryString, String paramName, Object value);
    /**
     * @param queryString Query to be executed
     * @param paramName Parameter names to be replaced by their matching values
     * @param value The values to be used for replacement - <B>Note:</B> the
     * basic assumption is that the value at index <code><i>i</i></code>
     * represents the value for the parameter whose name appears in the names
     * array at the same index.
     * @return A {@link List} of {@link Object}-s returned as query result -
     * may be null/empty
     */
    List<?> findByNamedParam (String queryString, String[] paramName, Object[] value);
    /**
     * @param queryString Query to be executed
     * @param paramsMap A {@link Map} whose key=the parameter name to be replaced,
     * value=the matching value to be used as replacement - may be null/empty
     * @return A {@link List} of {@link Object}-s returned as query result -
     * may be null/empty
     */
    List<?> findByNamedParam (String queryString, Map<String,?> paramsMap);
}
