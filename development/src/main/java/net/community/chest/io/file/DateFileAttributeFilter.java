/*
 * 
 */
package net.community.chest.io.file;

import java.util.Date;

import net.community.chest.math.compare.ComparisonExecutor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 1:08:37 PM
 */
public class DateFileAttributeFilter extends ComparableFileAttributeFilter<Date> {
	public DateFileAttributeFilter (FileAttributeType a, ComparisonExecutor ce) throws IllegalArgumentException
	{
		super(Date.class, a, ce);
	}

	public void setComparedValue (Number n)
	{
		setComparedValue((null == n) ? null : new Date(n.longValue()));
	}

	public void setComparedValue (long n)
	{
		setComparedValue(new Date(n));
	}
}
