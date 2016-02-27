/*
 * 
 */
package net.community.chest.db.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;

/**
 * An {@link Enum} of useful SQL keywords
 * 
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 1, 2010 11:35:06 AM
 */
public enum SQLKeyword {
	SELECT,
	FROM,
	AS,
	WHERE,
	NULL,
	IN,
	IS,
	BETWEEN,
	LIKE,
	AND,
	OR,
	NOT,
	ASC,
	DESC,
	MIN,
	MAX,
	COUNT,
	DISTINCT,
	ORDERBY("ORDER BY"),
	GROUPBY("GROUP BY");
	
	private final String	_keyword;
	public final String getKeyword ()
	{
		return _keyword;
	}
	/*
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString ()
	{
		return getKeyword();
	}

	SQLKeyword (String keyword)
	{
		if ((null == keyword) || (keyword.length() <= 0))
			_keyword = name().toUpperCase();
		else
			_keyword = keyword;
	}

	SQLKeyword ()
	{
		this(null);
	}
	
	public static final List<SQLKeyword>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SQLKeyword fromName (String s)
	{
		return EnumUtil.fromName(VALUES, s, false);
	}
	
	public static final SQLKeyword fromKeyword (String s)
	{
		if ((null == s) || (s.length() <= 0))
			return null;
		
		for (final SQLKeyword v : VALUES)
		{
			final String	kw=(null == v) ? null : v.getKeyword();
			if (s.equalsIgnoreCase(kw))
				return v;
		}
		
		return null;	// no match found
	}
}
