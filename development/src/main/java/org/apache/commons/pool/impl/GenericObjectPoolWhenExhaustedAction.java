/*
 * 
 */
package org.apache.commons.pool.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link Enum} wrapper for {@link GenericObjectPool#setWhenExhaustedAction(byte)} value(s)
 * @author Lyor G.
 * @since Sep 6, 2011 3:32:44 PM
 */
public enum GenericObjectPoolWhenExhaustedAction {
	BLOCK(GenericObjectPool.WHEN_EXHAUSTED_BLOCK),
	FAIL(GenericObjectPool.WHEN_EXHAUSTED_FAIL),
    GROW(GenericObjectPool.WHEN_EXHAUSTED_GROW),
    // MUST BE LAST !!!
    DEFAULT(GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION);

	private final byte	_action;
	public final byte getAction ()
	{
		return _action;
	}
	
	GenericObjectPoolWhenExhaustedAction (byte action)
	{
		_action = action;
	}

	public static final Set<GenericObjectPoolWhenExhaustedAction>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(GenericObjectPoolWhenExhaustedAction.class));

	public static final GenericObjectPoolWhenExhaustedAction fromString (final String s)
	{
		if (StringUtils.isBlank(s))
			return null;

		for (final GenericObjectPoolWhenExhaustedAction v : VALUES)
		{
			if (s.equalsIgnoreCase(v.toString()))
				return v;
		}

		return null;
	}

	public static final GenericObjectPoolWhenExhaustedAction fromAction (final byte aVal)
	{
		for (final GenericObjectPoolWhenExhaustedAction v : VALUES)
		{
			if (v.getAction() == aVal)
				return v;
		}

		return null;
	}
}
