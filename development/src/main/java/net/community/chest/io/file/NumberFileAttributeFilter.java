/*
 * 
 */
package net.community.chest.io.file;

import net.community.chest.math.compare.ComparisonExecutor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 1:13:38 PM
 */
public class NumberFileAttributeFilter extends ComparableFileAttributeFilter<Long> {
	public NumberFileAttributeFilter (FileAttributeType a, ComparisonExecutor ce) throws IllegalArgumentException
	{
		super(Long.class, a, ce);
	}

	public void setComparedValue (Number n)
	{
		if (null == n)
			setComparedValue(null);
		else if (n instanceof Long)
			setComparedValue((Long) n);
		else
			setComparedValue(n.longValue());
	}

	public void setComparedValue (long n)
	{
		setComparedValue(Long.valueOf(n));
	}
}
