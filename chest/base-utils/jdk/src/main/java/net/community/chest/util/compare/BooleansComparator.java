package net.community.chest.util.compare;


/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 21, 2007 1:49:28 PM
 */
public class BooleansComparator extends AbstractComparator<Boolean> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7517253373520763428L;

	public BooleansComparator (final boolean ascending)
	{
		super(Boolean.class, !ascending);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public final int compareValues (final Boolean o1, final Boolean o2)
	{
		return compareComparables(o1, o2);
	}

	public static final int compare (final boolean b1, final boolean b2)
	{
		return Boolean.valueOf(b1).compareTo(Boolean.valueOf(b2));
	}

	public static final BooleansComparator	ASCENDING=new BooleansComparator(true),
											DESCENDING=new BooleansComparator(false);
}
