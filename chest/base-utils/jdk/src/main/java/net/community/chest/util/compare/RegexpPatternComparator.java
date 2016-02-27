/*
 * 
 */
package net.community.chest.util.compare;

import java.util.regex.Pattern;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 8, 2010 7:52:19 AM
 */
public class RegexpPatternComparator extends AbstractComparator<Pattern> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2475010274348850448L;

	public RegexpPatternComparator (boolean ascending)
	{
		super(Pattern.class, !ascending);
	}

	public static final int comparePatternStrings (final Pattern p1, final Pattern p2, final boolean caseSensitive)
	{
		final String	s1=(p1 == null) ? null : p1.pattern(),
						s2=(p2 == null) ? null : p2.pattern();
		return StringUtil.compareDataStrings(s1, s2, caseSensitive);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (Pattern v1, Pattern v2)
	{
		return comparePatternStrings(v1, v2, false);
	}

	public static final RegexpPatternComparator	ASCENDING=new RegexpPatternComparator(true),
												DESCENDING=new RegexpPatternComparator(false);
}
