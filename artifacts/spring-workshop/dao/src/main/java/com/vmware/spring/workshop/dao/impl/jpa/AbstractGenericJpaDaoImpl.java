package com.vmware.spring.workshop.dao.impl.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.vmware.spring.workshop.dao.DaoUtils;
import com.vmware.spring.workshop.dao.GenericDao;
import com.vmware.spring.workshop.dao.impl.QueryNameResolver;

/**
 * @author lgoldstein
 */
public abstract class AbstractGenericJpaDaoImpl<T,ID extends Serializable> implements GenericDao<T, ID> {
    private final Class<T> _persistentClass;
    private final Class<ID> _idClass;
    @PersistenceContext
    private EntityManager    _entityManager;
    private final String    _findAllQuery, _countAllQuery;
    protected final Logger    logger=LoggerFactory.getLogger(getClass());
    protected final QueryNameResolver    _queryNameResolver;

    protected AbstractGenericJpaDaoImpl (final Class<T> pClass, final Class<ID> idClass) {
        Assert.state((_persistentClass = pClass) != null, "Missing persistent class instance");
        Assert.state((_idClass = idClass) != null, "Missing persistent ID type");
        _queryNameResolver = new QueryNameResolver(pClass);
        _findAllQuery = createFindAllQuery(pClass, idClass);
        _countAllQuery = createCountAllQuery(pClass, idClass);
        Assert.hasText(_findAllQuery, "No find-all query created");
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        final EntityManager    em=getEntityManager();
        final TypedQuery<T>    q=em.createQuery(_findAllQuery, getPersistentClass());
        return getQueryResults(q);
    }

    @Override
    public void delete (T entity) {
        final EntityManager    em=getEntityManager();
        em.remove(entity);
    }

    public T deleteById (ID id) {
        final T    entity=findOne(id);
        if (entity == null)
            return null;    // debug breakpoint

        delete(entity);
        return entity;
    }

    @Override
    public void deleteAll () {
        deleteAll(findAll());
    }

    public Number deleteAll(final Collection<? extends T> items) {
        final int    numItems=(null == items) ? 0 : items.size();
        if (numItems <= 0)
            return Integer.valueOf(0);

        int    numDeleted=0;
        for (final T entity : items)
        {
            if (null == entity)
                continue;

            delete(entity);
            numDeleted++;
        }

        return Integer.valueOf(numDeleted);
    }

    @Override
    public void flush() {
        final EntityManager    em=getEntityManager();
        em.flush();
    }

    @Override
    public final/* no cheating */Class<ID> getIdClass() {
        return _idClass;
    }

    @Override
    public final/* no cheating */Class<T> getPersistentClass() {
        return _persistentClass;
    }

    @Override
    public List<T> save(Iterable<? extends T> entities) {
        final List<T>    result=new ArrayList<T>();
        for (final T entity : entities) {
            save(entity);
            result.add(entity);
        }
        return result;
    }

    @Override
    @Transactional(readOnly=true)
    public T findOne(ID id) {
        final EntityManager    em=getEntityManager();
        final Class<T>        entityClass=getPersistentClass();
        return em.find(entityClass, id);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean exists(ID id) {
        return findOne(id) != null;
    }

    @Override
    @Transactional(readOnly=true)
    public long count() {
        final EntityManager            em=getEntityManager();
        final TypedQuery<Number>    q=em.createQuery(_countAllQuery, Number.class);
        return getUniqueResult(q, Number.class).longValue();
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        for (final T entity : entities) {
            delete(entity);
        }
    }

    @PostConstruct
    public void afterPropertiesSet () throws Exception {
        Assert.state(_entityManager != null, "No entity manager available");
    }

    protected List<T> getQueryResults (final TypedQuery<T> q) {
        Assert.notNull(q, "No query instance");
        return q.getResultList();
    }

    protected T getUniqueResult (final TypedQuery<T> q) {
        return getUniqueResult(q, getPersistentClass());
    }

    protected <V> V getUniqueResult (final TypedQuery<V> q, final Class<V> resClass) {
        final V    result=q.getSingleResult();
        if (result == null)
            return null;

        Assert.isInstanceOf(resClass, result, "Unexpected result type");
        return result;
    }

    protected TypedQuery<T> getNamedQuery (final String name) {
        return getNamedQuery(name, getPersistentClass());
    }

    protected <V> TypedQuery<V> getNamedQuery (final String name, final Class<V> resultType) {
        final String    effectiveName=_queryNameResolver.getEffectiveQueryName(name);
        Assert.state(StringUtils.hasText(effectiveName), "No effective query name");
        final EntityManager    em=getEntityManager();
        return em.createNamedQuery(effectiveName, resultType);
    }

    protected EntityManager getEntityManager () {
        return _entityManager;
    }

    protected String createFindAllQuery (final Class<?> pClass, final Class<?> idClass) {
        final Map.Entry<String,String>    nameAndAlias=resolveNameAndAlias(pClass, idClass);
        return "SELECT " + nameAndAlias.getValue() + " FROM " + nameAndAlias.getKey() + " " + nameAndAlias.getValue();
    }

    protected String createCountAllQuery (final Class<?> pClass, final Class<?> idClass) {
        final Map.Entry<String,String>    nameAndAlias=resolveNameAndAlias(pClass, idClass);
        return "SELECT COUNT(*) FROM " + nameAndAlias.getKey() + " " + nameAndAlias.getValue();
    }

    protected Map.Entry<String,String> resolveNameAndAlias (final Class<?> pClass, final Class<?> idClass) {
        Assert.notNull(pClass, "No persistent class provided");
        final String    name=DaoUtils.resolveEntityName(pClass), alias=getClassAlias(pClass);
        Assert.hasText(alias, "No alias provided");
        if (StringUtils.hasText(name))
            return ImmutablePair.of(name, alias);
        else
            return ImmutablePair.of(pClass.getSimpleName(), alias);
    }

    protected String getClassAlias (final Class<?> pClass) {
        Assert.notNull(pClass, "No persistent class provided");
        return pClass.getSimpleName();
    }
}
