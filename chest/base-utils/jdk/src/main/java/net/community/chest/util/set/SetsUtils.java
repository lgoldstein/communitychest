/*
 * 
 */
package net.community.chest.util.set;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 9:56:14 AM
 */
public final class SetsUtils {
	private SetsUtils ()
	{
		// no instance
	}

    @SafeVarargs
	public static final <E extends Enum<E>> Set<E> setOf (E ... values)
	{
		if ((null == values) || (values.length <= 0))
			return null;

		final Set<E>	s=EnumSet.of(values[0]);
		if (values.length > 1)
			s.addAll(Arrays.asList(values));
		return s;
	}

	public static final <E extends Enum<E>> Set<E> union (
			final Collection<E> s1, final Collection<E> s2)
	{
		if ((null == s1) || (s1.size() <= 0))
		{
			if ((null == s2) || (s1.size() <= 0))
				return null;
	
			if (s2 instanceof Set<?>)
				return (Set<E>) s2;
			else
				return EnumSet.copyOf(s2);
		}
		else if ((null == s2) || (s1.size() <= 0))
		{
			if (s1 instanceof Set<?>)
				return (Set<E>) s1;
			else
				return EnumSet.copyOf(s1);
		}
		else
		{
			final Set<E>	ret=EnumSet.copyOf(s1);
			ret.addAll(s2);
			return ret;
		}
	}

	public static final <E extends Enum<E>> Set<E> intersect (
			final Collection<E> s1, final Collection<E> s2)
	{
		if ((null == s1) || (s1.size() <= 0)
		 || (null == s2) || (s1.size() <= 0))
			return null;

		Set<E>	ret=null;
		for (final E v : s1)
		{
			if (!s2.contains(v))
				continue;

			if (null == ret)
				ret = EnumSet.of(v);
			else
				ret.add(v);
		}

		return ret;
	}

    @SafeVarargs
	public static final <V extends Comparable<V>> SortedSet<V> comparableSetOf (final V ... values)
	{
		if ((null == values) || (values.length <= 0))
			return null;

		return new TreeSet<V>(Arrays.asList(values));
	}

	public static final <V> SortedSet<V> setOf (Comparator<? super V> c, Collection<? extends V> values) throws IllegalArgumentException
	{
		if ((null == values) || (values.size() <= 0))
			return null;

		if (null == c)
			throw new IllegalArgumentException("No comparator provided");

		final SortedSet<V>	s=new TreeSet<V>(c);
		s.addAll(values);
		return s;
	}

    @SafeVarargs
	public static final <V> SortedSet<V> setOf (Comparator<? super V> c, V ... values) throws IllegalArgumentException
	{
		return setOf(c, ((null == values) || (values.length <= 0)) ? null : Arrays.asList(values));
	}

	public static final <V> Set<V> uniqueSetOf (List<? extends V> values) throws IllegalArgumentException
	{
		if ((null == values) || (values.size() <= 0))
			return null;

		final V			v=values.get(0);
		@SuppressWarnings("unchecked")
		final Class<V>	vc=(null == v) ? null : (Class<V>) v.getClass();
		return setOf((null == vc) ? null : new InstancesComparator<V>(vc), values);
	}

    @SafeVarargs
	public static final <V> Set<V> uniqueSetOf (V ... values) throws IllegalArgumentException
	{
		return uniqueSetOf(((null == values) || (values.length <= 0)) ? null : Arrays.asList(values));
	}
}
