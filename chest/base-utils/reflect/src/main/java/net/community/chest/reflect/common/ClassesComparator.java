/*
 * 
 */
package net.community.chest.reflect.common;

import java.util.Comparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares the "strength" of the 2 class according to following logic:</P></BR>
 * <UL>
 * 		<LI>null objects come first</LI>
 * 		<LI>if classes not compatible, then lexicographical order</LI>
 * 		<LI>the superclass comes before the derived one</LI>
 * </UL>
 * @author Lyor G.
 * @since Sep 14, 2008 9:20:23 AM
 */
public class ClassesComparator implements Comparator<Class<?>> {
	public ClassesComparator ()
	{
		super();
	}
	/**
	 * @param c1 1st class
	 * @param c2 2nd class
	 * @return negative if 1st class "stronger" than the other, positive if
	 * other way around, and zero if "same"
	 */
	public static final int compareClasses (final Class<?> c1, final Class<?> c2)
	{
		if (c1 == c2)	// check the obvious
			return 0;

		if (null == c1)
			return (null == c2) ? 0 : (-1);
		else if (null == c2)
			return (+1);

		// if one is assignable from the other, use name equality to check which is superclass
		final String	n1=c1.getName(), n2=c2.getName();
		if (c1.isAssignableFrom(c2))
			return n1.equals(n2) ? 0 : (-1);
		else if (c2.isAssignableFrom(c1))
			return n2.equals(n1) ? 0 : (+1);
		else	// if classes not compatible, then use lexicographical order
			return n1.compareTo(n2);
	}
	/*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @throws ClassCastException if objects are not classes
	 */
	@Override
	public int compare (Class<?> o1, Class<?> o2) throws ClassCastException
	{
		return compareClasses(o1, o2);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		return (obj != null) && (obj instanceof ClassesComparator);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return 0;
	}
	/**
	 * A globally allocated comparator
	 */
	public static final ClassesComparator	DEFAULT=new ClassesComparator();
}
