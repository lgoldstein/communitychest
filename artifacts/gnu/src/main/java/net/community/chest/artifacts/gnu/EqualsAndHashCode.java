/*
 * 
 */
package net.community.chest.artifacts.gnu;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 9, 2011 9:47:39 AM
 *
 */
public final class EqualsAndHashCode {
	private EqualsAndHashCode ()
	{
		// no instance
	}

	public static final int hashCode (Object o)
	{
		return (o == null) ? 0 : o.hashCode();
	}

	public static final boolean equals (Object o1, Object o2)
	{
		if (o1 == o2)
			return true;

		// means at least one is not null
		if ((o1 == null) || (o2 == null))
			return false;
		
		return o1.equals(o2);
	}

	public static final <C extends Comparable<C>> int compare (C v1, C v2)
	{
		if (v1 == v2)
			return 0;

		// push null(s) to end
		if (v1 == null)
			return (+1);
		else if (v2 == null)
			return (-1);

		return v1.compareTo(v2);
	}
}
