package com.vmware.spring.workshop.dao;

import javax.persistence.Entity;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author lgoldstein
 */
public final class DaoUtils {
	private DaoUtils() {
		throw new UnsupportedOperationException("Instantiantion N/A");
	}

	public static final String getEffectiveQueryName (final Class<?> entityClass, final String queryName) {
		return getEffectiveQueryName(resolveEntityName(entityClass), queryName);
	}

	public static final String getEffectiveQueryName (final String baseName, final String queryName) {
		Assert.hasText(baseName, "No base name provided");
		Assert.hasText(queryName, "No query name provided");
		return baseName + "." + queryName;
	}

	public static final String resolveEntityName (final Class<?> entityClass) {
		Assert.notNull(entityClass, "No entityClass provided");
		final Entity	entity=entityClass.getAnnotation(Entity.class);
		Assert.state(entity != null, "Entity class not annotated as such");
		final String	name=entity.name();
		if (StringUtils.hasText(name))
			return entityClass.getSimpleName();
		else
			return name;
	}
}
