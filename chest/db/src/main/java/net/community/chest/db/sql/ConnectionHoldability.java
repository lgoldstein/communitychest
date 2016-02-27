/*
 * 
 */
package net.community.chest.db.sql;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 28, 2010 12:46:35 PM
 */
public enum ConnectionHoldability {
	HOLD(ResultSet.HOLD_CURSORS_OVER_COMMIT),
	CLOSE(ResultSet.CLOSE_CURSORS_AT_COMMIT);
	
	private final int	_h;
	public final int getHoldability ()
	{
		return _h;
	}
	
	ConnectionHoldability (int h)
	{
		_h = h;
	}

	public static final List<ConnectionHoldability>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ConnectionHoldability fromString (String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final ConnectionHoldability fromValue (final int h)
	{
		for (final ConnectionHoldability v : VALUES)
		{
			final int	l=(null == v) ? (-1) : v.getHoldability();
			if ((v != null) && (l == h))
				return v;
		}

		return null;
	}
}
