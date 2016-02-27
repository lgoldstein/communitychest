package net.community.chest.lang.math;


/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 23, 2007 12:16:00 PM
 */
public class LongsComparator extends DefaultNumbersComparator<Long> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6657534686846561615L;

	public LongsComparator (boolean ascending)
	{
		super(Long.class, ascending);
	}

	public static final int compare (long v1, long v2)
	{
		if (v1 < v2)
			return (-1);
		else if (v1 > v2)
			return (+1);
		else
			return 0;
	}

	public static final LongsComparator	ASCENDING=new LongsComparator(true),
										DESCENDING=new LongsComparator(false);
}
