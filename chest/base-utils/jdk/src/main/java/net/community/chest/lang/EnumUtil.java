package net.community.chest.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.compare.AbstractComparator;


/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful enum values related utilities
 * 
 * @author Lyor G.
 * @since Jul 9, 2007 2:46:59 PM
 */
public final class EnumUtil {
	private EnumUtil ()
	{
		// no instance
	}
	/**
	 * @param <E> The generic {@link Enum} class
	 * @param values enumerated values array from which to check the name
	 * @param name specified name
	 * @param caseSensitive TRUE if comparison with {@link Enum#name()} should
	 * be made case sensitive
	 * @return found matching value - null if no match found (or null/empty name/array)
	 */
	public static <E extends Enum<E>> E fromName (final Collection<? extends E> values, final String name, final boolean caseSensitive)
	{
		if ((null == name) || (name.length() <= 0)
		 || (null == values) || (values.size() <= 0))
			return null;

		for (final E v : values)
		{
			final String	vName=(null == v) /* should not happen */ ? null : v.name();
			if ((null == vName) || (vName.length() <= 0))
				continue;	// should not happen
		
			final int	vDiff=StringUtil.compareDataStrings(vName, name, caseSensitive);
			if (0 == vDiff)
				return v;
		}

		return null;	// no match found
	}
	/**
	 * @param <E> The generic {@link Enum} class
	 * @param eClass enumeration values {@link Class} instance
	 * @param name specified name
	 * @param caseSensitive TRUE if comparison with {@link Enum#name()} should
	 * be made case sensitive
	 * @return found matching value - null if no match found (or null/empty name/array)
	 * @see Class#getEnumConstants()
	 */
	public static <E extends Enum<E>> E fromName (final Class<E> eClass, final String name, final boolean caseSensitive)
	{
		if ((null == eClass) || (null == name) || (name.length() <= 0))
			return null;
		else
			return fromName(Arrays.asList(eClass.getEnumConstants()), name, caseSensitive);
	}
	/**
	 * @param <E> The generic {@link Enum} class
	 * @param eClass enumeration values {@link Class} instance
	 * @param name specified name
	 * @param caseSensitive TRUE if comparison with {@link Enum#toString()} should
	 * be made case sensitive
	 * @return found matching value - null if no match found (or null/empty name/array)
	 * @see Class#getEnumConstants()
	 */
	public static <E extends Enum<E>> E fromString (final Class<E> eClass, final String name, final boolean caseSensitive)
	{
		if ((null == eClass) || (null == name) || (name.length() <= 0))
			return null;
		else
			return CollectionsUtils.fromString(Arrays.asList(eClass.getEnumConstants()), name, caseSensitive);
	}
	/**
	 * Compares 2 enumeration values
	 * @param <E> The generic {@link Enum} class
	 * @param e1 1st value
	 * @param e2 2nd value
	 * @return 0=equal, <0 if 1st comes before 2nd value, >0 otherwise
	 */
	public static <E extends Enum<E>> int compareValues (final E e1, final E e2)
	{
		return AbstractComparator.compareComparables(e1, e2);
	}
	/**
	 * @param <E> The generic {@link Enum} class
	 * @param value {@link Enum} value whose hash code we want
	 * @return (-1) if null value, {@link Enum#ordinal()} otherwise
	 */
	public static <E extends Enum<E>> int getValueHashCode (final E value)
	{
		return (null == value) ? (-1) : value.ordinal();
	}
	/**
	 * @param <E> The generic {@link Enum} class
	 * @param values enumerated values array from which to check the ordinal
	 * @param ordValue - ordinal value
	 * @return found matching value - null if no match found (or null/empty
	 * array or negative ordinal)
	 */
	public static <E extends Enum<E>> E fromOrdinal (final Collection<? extends E> values, final int ordValue)
	{
		if ((null == values) || (values.size() <= 0) || (ordValue < 0))
			return null;

		for (final E val : values)
		{
			if ((val != null) && (val.ordinal() == ordValue))
				return val;
		}

		return null;
	}
	/**
	 * @param <E> The generic {@link Enum} class
	 * @param textsMap A {@link Map} where key=enumerated value and the value
	 * is an associated text {@link String} (not necessarily its {@link Enum#name()}
	 * or {@link Enum#toString()}).
	 * @param text text to be checked
	 * @param caseSensitive TRUE if comparison is to be made case sensitive
	 * @return Associated {@link Enum} value - null if no match found (or
	 * null/empty {@link Map} or text to begin with)
	 */
	public static <E extends Enum<E>> E fromTextsMap (final Map<E,String> textsMap, final String text, final boolean caseSensitive)
	{
		final Collection<Map.Entry<E,String>>	textsEntries=
			((null == textsMap) || (textsMap.size() <= 0) || (null == text) || (text.length() <= 0)) ? null : textsMap.entrySet();
		if ((null == textsEntries) || (textsEntries.size() <= 0))
			return null;

		for (final Map.Entry<E,String> e : textsEntries)
		{
			final String	t=(null == e) /* should not happen */ ? null : e.getValue();
			final int		vDiff=StringUtil.compareDataStrings(t, text, caseSensitive);
			if (0 == vDiff)
				return e.getKey();
		}

		return null;	// no match found
	}

	public static <E extends Enum<E>> String toAttributeName (final E val)
	{
		return (null == val) ? null : AttributeMethodType.toAttributeName(val.name().toLowerCase());
	}

	public static <E extends Enum<E>> String toAttributeString (final E val)
	{
		return (null == val) ? null : AttributeMethodType.toAttributeName(val.toString());
	}
}
