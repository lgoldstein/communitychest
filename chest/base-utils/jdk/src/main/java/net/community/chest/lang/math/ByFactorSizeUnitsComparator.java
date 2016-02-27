/*
 * 
 */
package net.community.chest.lang.math;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 22, 2008 9:19:19 AM
 */
public class ByFactorSizeUnitsComparator extends AbstractComparator<SizeUnits> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3567408250302832405L;

	public ByFactorSizeUnitsComparator (boolean ascending)
	{
		super(SizeUnits.class, !ascending);
	}

	public static final int compareFactor (SizeUnits v1, SizeUnits v2)
	{
		final long	t1=(null == v1) ? 0L : v1.getMultiplicationFactor(),
					t2=(null == v2) ? 0L : v2.getMultiplicationFactor();
		return LongsComparator.compare(t1, t2);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (SizeUnits v1, SizeUnits v2)
	{
		return compareFactor(v1, v2);
	}

	public static final ByFactorSizeUnitsComparator	ASCENDING=new ByFactorSizeUnitsComparator(true),
													DESCENDING=new ByFactorSizeUnitsComparator(false);
}
