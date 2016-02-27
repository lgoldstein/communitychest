package com.vmware.spring.workshop.dao.impl;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.vmware.spring.workshop.dao.DaoUtils;

/**
 * @author lgoldstein
 */
public class QueryNameResolver {
	private final String	_baseName;
	private final Map<String,String>	_queriesMap=
			Collections.synchronizedMap(new TreeMap<String,String>());
	public QueryNameResolver(final Class<?> entityClass) {
		_baseName = DaoUtils.resolveEntityName(entityClass);
		Assert.state(StringUtils.hasText(_baseName), "No base name available");
	}

	public String getEffectiveQueryName (final String queryName) {
		Assert.hasText(queryName, "No query name provided");
		
		String	effectiveName=_queriesMap.get(queryName);
		if (effectiveName == null) {
			effectiveName = DaoUtils.getEffectiveQueryName(_baseName, queryName);
			Assert.state(StringUtils.hasText(effectiveName), "No effective name resolved");
			_queriesMap.put(queryName, effectiveName);
		}

		return effectiveName;
	}
}
