/*
 * 
 */
package net.community.chest.util.compare;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <T> Type of {@link CharSequence} being compared 
 * @author Lyor G.
 * @since Aug 3, 2011 3:04:03 PM
 *
 */
public class CharSequenceComparator<T extends CharSequence> extends AbstractComparator<T> {
	private static final long serialVersionUID = -5165088584538961878L;

	public CharSequenceComparator (Class<T> valsClass, boolean reverseMatch) throws IllegalArgumentException
	{
		super(valsClass, reverseMatch);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (T v1, T v2)
	{
		return compareSequences(v1, v2);
	}

	public static final int compareSequences (CharSequence s1, CharSequence s2)
	{
		final int	l1=(s1 == null) ? 0 : s1.length(),
					l2=(s2 == null) ? 0 : s2.length(),
					lCommon=Math.min(l1, l2);
		for (int	cIndex=0; cIndex < lCommon; cIndex++)
		{
			final char	c1=s1.charAt(cIndex), c2=s2.charAt(cIndex);
			if (c1 != c2)
				return c1 - c2;
		}

		return l1 - l2;
	}

	public static final CharSequenceComparator<CharSequence>	ASCENDING=
				new CharSequenceComparator<CharSequence>(CharSequence.class, false),
																DESCENDING=
				new CharSequenceComparator<CharSequence>(CharSequence.class, true);
}
