/**
 * 
 */
package net.community.chest.apache.maven.helpers;

import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares 2 group ID(s) by examining each member of the group ID
 * <U>individually</U>. If same &quot;prefix&quot; then shorter group ID
 * comes <U>first</U></P>
 * 
 * @author Lyor G.
 * @since Aug 17, 2008 8:47:01 AM
 */
public class GroupIdComparator extends AbstractComparator<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5654204901034110556L;

	public GroupIdComparator (boolean ascending)
	{
		super(String.class, !ascending);
	}

	public static final int compareGroupIds (final String g1, final String g2)
	{
		final List<String>	cl1=StringUtil.splitString(g1, '.'),
							cl2=StringUtil.splitString(g2, '.');
		final int			n1=(null == cl1) ? 0 : cl1.size(),
							n2=(null == cl2) ? 0 : cl2.size(),
							ml=Math.min(n1,n2),
							cl=Math.max(ml,0);
		// check components up to common length
		for (int	cIndex=0; cIndex < cl; cIndex++)
		{
			final String	c1=cl1.get(cIndex), c2=cl2.get(cIndex);
			// TODO check if need to use case-sensitive comparison
			final int		nRes=StringUtil.compareDataStrings(c1, c2, false);
			if (nRes != 0)
				return nRes;
		}

		// if same prefix, then shorter ID comes first
		return (n1 - n2);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (final String g1, final String g2)
	{
		return compareGroupIds(g1, g2);
	}

	public static final GroupIdComparator	ASCENDING=new GroupIdComparator(true),
											DESCENDING=new GroupIdComparator(false);
}
