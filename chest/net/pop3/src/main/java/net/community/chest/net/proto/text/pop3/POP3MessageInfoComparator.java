package net.community.chest.net.proto.text.pop3;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 2:47:28 PM
 */
public class POP3MessageInfoComparator extends AbstractComparator<POP3MessageInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8557899034053026754L;

	public POP3MessageInfoComparator (boolean ascending)
	{
		super(POP3MessageInfo.class, !ascending);
	}

	public static final int compareInfo (final POP3MessageInfo o1, final POP3MessageInfo o2)
	{
		if (null == o1)
			return (null == o2) ? 0 : (+1);	// push nulls to end
		else if (null == o2)
			return (-1);	// push nulls to end

		{
			final int	n1=o1.getMsgNum(), n2=o2.getMsgNum(), diff=n1 - n2;
			if (diff != 0)
				return diff;
		}

		{
			final long	n1=o1.getMsgSize(), n2=o2.getMsgSize(), diff=n1 - n2;
			if (diff != 0L)
				return (diff > 0L) ? (+1) : (-1);
		}

		return StringUtil.compareDataStrings(o1.getMsgUIDL(), o2.getMsgUIDL(), true);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (POP3MessageInfo o1, POP3MessageInfo o2)
	{
		return compareInfo(o1, o2);
	}

	public static final POP3MessageInfoComparator	ASCENDING=new POP3MessageInfoComparator(true),
													DESCENDING=new POP3MessageInfoComparator(false);
}
