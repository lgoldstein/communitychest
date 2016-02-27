/*
 * 
 */
package net.community.chest.util.set;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>By default it is case <U>insensitive</U></P>
 * @author Lyor G.
 * @since Jan 6, 2009 11:56:43 AM
 */
public class StringSet extends TreeSet<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1462865111322479108L;

	public StringSet (Comparator<? super String> comparator, final Collection<? extends String> vals)
	{
		super(comparator);

		if ((vals != null) && (vals.size() > 0))
			addAll(vals);
	}

	public StringSet (Comparator<? super String> comparator, final String ... vals)
	{
		this(comparator, ((null == vals) || (vals.length <= 0)) ? null : Arrays.asList(vals));
	}

	public StringSet (Comparator<? super String> comparator)
	{
		this(comparator, (Collection<? extends String>) null);
	}

	public StringSet (Collection<? extends String> c)
	{
		this(String.CASE_INSENSITIVE_ORDER, c);
	}

	public StringSet (final String ... vals)
	{
		this(((null == vals) || (vals.length <= 0)) ? null : Arrays.asList(vals));
	}

	public StringSet ()
	{
		this(String.CASE_INSENSITIVE_ORDER);
	}

}
