/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.math.compare.ComparisonExecutor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <V> Type of {@link Comparable} attribute being checked
 * @author Lyor G.
 * @since Apr 13, 2009 12:36:32 PM
 */
public class ComparableFileAttributeFilter<V extends Comparable<V>> extends FileAttributeFilter<V> {
	private ComparisonExecutor	_ce;
	public ComparisonExecutor getComparisonExecutor ()
	{
		return _ce;
	}
	/*
	 *  	The executor checks the file value vs. the compared on (in this order).
	 *  E.g., if use ComparisonOperator.GT with a FileAttributeType.LASTMODTIME
	 *  then the filter will check if the file's last-modified-time is greater
	 *  than the Date value that was set as value-to-compare
	 */
	public void setComparisonExecutor (ComparisonExecutor ce)
	{
		_ce = ce;
	}

	public ComparableFileAttributeFilter (Class<V> vc, FileAttributeType a, ComparisonExecutor ce) throws IllegalArgumentException
	{
		super(vc, a);
		_ce = ce;
	}

	public ComparableFileAttributeFilter (Class<V> vc, FileAttributeType a) throws IllegalArgumentException
	{
		this(vc, a, null);
	}

	public ComparableFileAttributeFilter (Class<V> vc)
	{
		this(vc, null);
	}
	/*
	 * @see net.community.chest.io.file.FileAttributeFilter#checkFileAttributeValue(java.io.File, net.community.chest.io.file.FileAttributeType, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected Boolean checkFileAttributeValue (File f, FileAttributeType a, V val, V cmpVal)
	{
		final ComparisonExecutor	ce=getComparisonExecutor();
		if (null == ce)
			return null;

		return ce.invoke(val, cmpVal);
	}
}
