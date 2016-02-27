/*
 * 
 */
package net.community.chest.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.compare.AbstractComparator;

/**
 * Encapsulates the available visibilities
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 8:45:54 AM
 */
public enum Visibility {
	PUBLIC(Modifier.PUBLIC) {
			/*
			 * @see net.community.chest.reflect.Visibility#isMatchingVisibility(int)
			 */
			@Override
			public boolean isMatchingVisibility (final int mod)
			{
				return Modifier.isPublic(mod);
			}
		},
	PROTETCTED(Modifier.PROTECTED) {
			/*
			 * @see net.community.chest.reflect.Visibility#isMatchingVisibility(int)
			 */
			@Override
			public boolean isMatchingVisibility (final int mod)
			{
				return Modifier.isProtected(mod);
			}
		},
	PRIVATE(Modifier.PRIVATE) {
			/*
			 * @see net.community.chest.reflect.Visibility#isMatchingVisibility(int)
			 */
			@Override
			public boolean isMatchingVisibility (final int mod)
			{
				return Modifier.isPrivate(mod);
			}
		},
	// NOTE !!! there really isn't a modifier for it - it is considered such if all others fail...
	DEFAULT(0) {
			/*
			 * @see net.community.chest.reflect.Visibility#isMatchingVisibility(int)
			 */
			@Override
			public boolean isMatchingVisibility (final int mod)
			{
				return (!Modifier.isPublic(mod))
					&& (!Modifier.isProtected(mod))
					&& (!Modifier.isPrivate(mod))
					;
			}
		};
	
	private final int	_modifier;
	public final int getModifier ()
	{
		return _modifier;
	}

	public abstract boolean isMatchingVisibility (final int mod);

	Visibility (int modifier)
	{
		_modifier = modifier;
	}
	
	public static final Set<Visibility>	VALUES=Collections.unmodifiableSet(EnumSet.allOf(Visibility.class));
	public static final Visibility fromString (String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final Visibility fromModifier (final int mod)
	{
		for (final Visibility v : VALUES)
		{
			if (v.isMatchingVisibility(mod))
				return v;
		}

		// the DEFAULT should match something that is none of the others
		throw new IllegalStateException("fromModifier(" + mod + ") no match found");
	}

	public static final int compareVisibility (final Method m1, final Method m2)
	{
		return compareVisibility((null == m1) ? 0 : m1.getModifiers(), (null == m2) ? 0 : m2.getModifiers()); 
	}
	/**
	 * Compares modifiers according to "more visibility comes first"
	 * @param m1 first modifier value
	 * @param m2 second modifier value
	 * @return value according to logic
	 * @see Visibility
	 */
	public static final int compareVisibility (final int m1, final int m2)
	{
		final Visibility	v1=fromModifier(m1), v2=fromModifier(m2);
		return AbstractComparator.compareComparables(v1, v2);
	}
}
