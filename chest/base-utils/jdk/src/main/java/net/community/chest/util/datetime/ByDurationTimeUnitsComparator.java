/*
 * 
 */
package net.community.chest.util.datetime;

import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares 2 {@link TimeUnits} according to their &quot;duration&quot;
 * (i.e., {@link TimeUnits#getMilisecondValue()})</P>
 * 
 * @author Lyor G.
 * @since Sep 17, 2008 11:34:36 AM
 */
public class ByDurationTimeUnitsComparator extends AbstractComparator<TimeUnits> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3913013555885066858L;

	public ByDurationTimeUnitsComparator (final boolean ascending)
	{
		super(TimeUnits.class, !ascending);
	}

	public static final int compareSize (final TimeUnits o1, final TimeUnits o2)
	{
		final long	t1=(null == o1) ? 0L : o1.getMilisecondValue(),
					t2=(null == o2) ? 0L : o2.getMilisecondValue();
		return LongsComparator.compare(t1, t2);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (final TimeUnits o1, final TimeUnits o2)
	{
		return compareSize(o1, o2);
	}

	public static final ByDurationTimeUnitsComparator	ASCENDING=
									new ByDurationTimeUnitsComparator(true),
														DESCENDING=
									new ByDurationTimeUnitsComparator(false);
}
