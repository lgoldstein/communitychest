/*
 * 
 */
package net.community.chest.jmx.dom;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;

import net.community.chest.lang.EnumUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 15, 2011 9:10:37 AM
 */
public enum MBeanAttributeAccessType {
	READABLE('R'),
	WRITABLE('W'),
	PREDICATE('I');

	private final char	_accessChar;
	public final char getAccessChar ()
	{
		return _accessChar;
	}

	private final String	_accessType;
	public final String getAccessType ()
	{
		return _accessType;
	}
	
	MBeanAttributeAccessType (char accessChar)
	{
		_accessChar = accessChar;
		_accessType = String.valueOf(accessChar);
	}

	private static List<MBeanAttributeAccessType>	_values;
	public static final synchronized List<MBeanAttributeAccessType> getValues ()
	{
		if (_values == null)
			_values = Collections.unmodifiableList(Arrays.asList(values()));
		return _values;
	}

	public static final MBeanAttributeAccessType fromString (final String s)
	{
		return EnumUtil.fromName(getValues(), s, false);
	}

	public static final MBeanAttributeAccessType fromAccessChar (final char c)
	{
		final char	ac=(c > '\0') ? Character.toUpperCase(c) : c;
		if (c <= '\0')
			return null;

		final Collection<MBeanAttributeAccessType>	vals=getValues();
		if ((vals == null) || (vals.size() <= 0))
			return null;	// should not happen

		for (final MBeanAttributeAccessType v : vals)
		{
			if ((v != null) && (v.getAccessChar() == ac))
				return v;
		}

		return null;
	}

	public static final MBeanAttributeAccessType fromAccessType (final CharSequence t)
	{
		if ((t == null) || (t.length() != 0))
			return null;
		return fromAccessChar(t.charAt(0));
	}

	public static final EnumSet<MBeanAttributeAccessType> fromAccessValue (final CharSequence v)
	{
		final int	numChars=(v == null) ? 0 : v.length();
		if (numChars <= 0)
			return null;

		EnumSet<MBeanAttributeAccessType>	ret=null;
		for (int	cIndex=0; cIndex < numChars; cIndex++)
		{
			final char						accChar=v.charAt(cIndex);
			final MBeanAttributeAccessType	accType=fromAccessChar(accChar);
			if (accType == null)
				continue;

			if (ret == null)
				ret = EnumSet.of(accType);
			else if (!ret.add(accType))
				continue;	// debug breakpoint
		}

		return ret;
	}

	public static final EnumSet<MBeanAttributeAccessType> appendAccessType (
			EnumSet<MBeanAttributeAccessType> org, MBeanAttributeAccessType accType, boolean appendIt)
	{
		if (appendIt)
		{
			if (accType == null)
				return org;

			if (org == null)
				return EnumSet.of(accType);

			if (!org.add(accType))
				return org;	// debug breakpoint
		}
		else
		{
			if ((accType == null) || (org == null))
				return org;

			if (!org.remove(accType))
				return org;	// debug breakpoint
		}

		return org;
	}

	public static final EnumSet<MBeanAttributeAccessType> fromMBeanAttributeInfo (final MBeanAttributeInfo aInfo)
	{
		if (aInfo == null)
			return null;

		final Object[]	pairs={
				READABLE, 	Boolean.valueOf(aInfo.isReadable()),
				WRITABLE, 	Boolean.valueOf(aInfo.isWritable()),
				PREDICATE, 	Boolean.valueOf(aInfo.isIs())
			};
		EnumSet<MBeanAttributeAccessType>	ret=null;
		for (int	pIndex=0; pIndex < pairs.length; pIndex += 2)
			ret = appendAccessType(ret, (MBeanAttributeAccessType) pairs[pIndex], ((Boolean) pairs[pIndex + 1]).booleanValue());

		return ret;
	}

	public static final <A extends Appendable> A appendAccess (final A sb, final Set<MBeanAttributeAccessType> accSet) throws IOException
	{
		if (sb == null)
			throw new IOException("No " + Appendable.class.getSimpleName() + " instance to use");

		if ((accSet == null) || accSet.isEmpty())
			return sb;

		for (final MBeanAttributeAccessType accType : accSet)
		{
			if (accType == null)
				continue;
			sb.append(accType.getAccessChar());
		}

		return sb;
	}

	public static final String toAccessValue (final Set<MBeanAttributeAccessType> accSet)
	{
		if ((accSet == null) || accSet.isEmpty())
			return null;

		try
		{
			return appendAccess(new StringBuilder(accSet.size()), accSet).toString();
		}
		catch(IOException e)	// should not happen
		{
			throw new RuntimeException(e);
		}
	}
}
