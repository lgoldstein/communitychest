/*
 * 
 */
package net.community.chest.db.sql;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 28, 2010 10:46:09 AM
 */
public enum TransactionIsolationLevel {
	NONE(Connection.TRANSACTION_NONE),
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	private int	_level;
	public final int getLevel ()
	{
		return _level;
	}
	
	TransactionIsolationLevel (int level)
	{
		_level = level;
	}

	public static final List<TransactionIsolationLevel>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final TransactionIsolationLevel fromString (String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final TransactionIsolationLevel fromLevel (final int lvl)
	{
		for (final TransactionIsolationLevel v : VALUES)
		{
			final int	l=(null == v) ? (-1) : v.getLevel();
			if ((v != null) && (l == lvl))
				return v;
		}

		return null;
	}
}
