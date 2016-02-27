package com.vmware.spring.workshop.dao.impl.hibernate;

import java.util.Collections;
import java.util.List;

import org.hibernate.transform.ResultTransformer;

/**
 * @author Lyor G.
 * @since Oct 4, 2011 9:17:07 AM
 */
public class CountEntitiesResultTransformer implements ResultTransformer {
	private static final long serialVersionUID = -3751360857970617420L;

	public static final CountEntitiesResultTransformer INSTANCE = new CountEntitiesResultTransformer();

	protected CountEntitiesResultTransformer() {
		super();
	}

	@Override
	public Object transformTuple(Object[] tuple, String[] aliases) {
		return tuple[tuple.length - 1];
	}

	@Override
	public List<Integer> transformList(
			@SuppressWarnings("rawtypes") List collection) {
		return Collections.singletonList(Integer.valueOf(collection.size()));
	}
}
