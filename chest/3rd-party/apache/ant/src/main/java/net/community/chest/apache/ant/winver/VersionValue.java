/*
 * 
 */
package net.community.chest.apache.ant.winver;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.resources.PropertiesResolver;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 7, 2009 10:34:51 AM
 */
public class VersionValue extends EnumMap<VersionComponent,Number> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4597203026958510155L;
	public VersionValue ()
	{
		super(VersionComponent.class);
	}
	// returns zero if component value not set
	public Number getResolvedValue (final VersionComponent vc)
	{
		final Number	n=get(vc);
		if (null == n)
			return Integer.valueOf(0);
		return n;
	}
	// returns '\0' if no match found
	public static char resolveSeparatorChar (final String s, final CharSequence allowedDelims)
	{
		final int	sLen=(null == s) ? 0 : s.length(),
					dLen=(null == allowedDelims) ? 0 : allowedDelims.length();
		if (sLen <= 0)
			return '\0';

		for (int	dIndex=0; dIndex < dLen; dIndex++)
		{
			final char	c=allowedDelims.charAt(dIndex);
			final int	p=s.indexOf(c);
			// delimiter cannot be 1st or last
			if ((p > 0) && (p < (sLen-1)))
				return c;
		}

		return '\0';
	}
	/**
	 * Default separator used in version resources
	 */
	public static final char	DEFAULT_VERSION_COMP_SEP=',';
	/**
	 * Property name that can be used to override the default version
	 * components separators {@link String}
	 */
	public static final String	DEFAULT_VERSION_COMPS_PROPNAME=
			PropertiesResolver.getClassPropertyName(VersionValue.class, "version.delims"),
	/**
	 * Default version components separators {@link String} - unless
	 * overridden via the {@link #DEFAULT_VERSION_COMPS_PROPNAME} property
	 */
								DEFAULT_VERSION_COMPS_DELIMS=
		String.valueOf(DEFAULT_VERSION_COMP_SEP) + ".";
	private static CharSequence	_defaultDelims;
	public static final synchronized CharSequence getDefaultVersionComponentsSeparators ()
	{
		if (null == _defaultDelims)
			_defaultDelims = System.getProperty(DEFAULT_VERSION_COMPS_PROPNAME, DEFAULT_VERSION_COMPS_DELIMS);
		return _defaultDelims;
	}
	// returns previous
	public static final synchronized CharSequence setDefaultVersionComponentsSeparators (final CharSequence s)
	{
		final CharSequence	prev=_defaultDelims;
		_defaultDelims = s;
		return prev;
	}

	public static final char resolveSeparatorChar (final String s)
	{
		return resolveSeparatorChar(s, getDefaultVersionComponentsSeparators());
	}

	public static final <V extends EnumMap<VersionComponent,Number>> V updateVersionValues (
			final V ver, final String s, final CharSequence allowedDelims) throws NumberFormatException
	{
		if ((null == s) || (s.length() <= 0) || (null == ver))
			return ver;
		
		final char					delim=resolveSeparatorChar(s, allowedDelims);
		final List<String>			cl=(delim != '\0') ? StringUtil.splitString(s, delim) : Arrays.asList(s);
		final int					numComps=(null == cl) ? 0 : cl.size();
		if ((numComps <= 0) || (numComps > VersionComponent.VALUES.size()))
			throw new NumberFormatException("updateVersionValues(" + s + ")[" + allowedDelims + "] illegal number of components: " + numComps);

		for (int	cIndex=0; cIndex < numComps; cIndex++)
		{
			final String			cv=cl.get(cIndex);
			final VersionComponent	vk=VersionComponent.VALUES.get(cIndex);
			final Integer			vv=
				((null == cv) || (cv.length() <= 0)) ? null : Integer.valueOf(cv);
			if ((null == vk) || (!VersionComponent.isValidComponentNumber(vv)))
				throw new NumberFormatException("updateVersionValues(" + s + ")[" + allowedDelims + "] bad/missing component(" + vk + ") value: " + cv);

			ver.put(vk, vv);
		}

		return ver;
	}

	public static final <V extends EnumMap<VersionComponent,Number>> V updateVersionValues (
			final V ver, final String s) throws NumberFormatException
	{
		return updateVersionValues(ver, s, getDefaultVersionComponentsSeparators());
	}

	public static final VersionValue fromString (final String s) throws NumberFormatException
	{
		if ((null == s) || (s.length() <= 0))
			return null;

		return updateVersionValues(new VersionValue(), s);
	}

	public void fromVersionString (final String s, final boolean clearBeforeUpdate) throws NumberFormatException
	{
		if (clearBeforeUpdate)
			clear();

		if (updateVersionValues(this, s) != this)	// should not happen
			throw new IllegalStateException("fromString(" + s + ") mismatched updated instances");
	}
	
	public void fromVersionString (final String s) throws NumberFormatException
	{
		fromVersionString(s, true);
	}

	public VersionValue (final String s) throws NumberFormatException
	{
		super(VersionComponent.class);

		if (updateVersionValues(this, s) != this)	// should not happen
			throw new IllegalStateException("<cinit>(" + s + ") mismatched updated instances");
	}
	/* 
	 * NOTE(s):
	 * 		- takes only the "intValue"
	 * 		- replaces missing components with zero
	 * 		- throws StreamCorruptedException if bad value found for a component
	 */
	public static final <A extends Appendable> A appendVersionString (
			final A sb, final EnumMap<VersionComponent,? extends Number> ver, final char delim)
		throws IOException
	{
		if ((null == ver) || (ver.size() <= 0))
			return sb;
		if (null == sb)
			throw new IOException("appendVersionString(" + ver + ") no " + Appendable.class.getSimpleName() + " instance");

		for (int	cIndex=0; cIndex < VersionComponent.VALUES.size(); cIndex++)
		{
			final VersionComponent	vk=VersionComponent.VALUES.get(cIndex);
			final Number			n=ver.get(vk);
			if (cIndex > 0)
				sb.append(delim);
			if (n != null)
			{
				if (!VersionComponent.isValidComponentNumber(n))
					throw new StreamCorruptedException("appendVersionString(" + ver + ") bad " + vk + " component value: " + n);
				sb.append(String.valueOf(n.intValue()));
			}
			else
				sb.append('0');
		}

		return sb;
	}

	public static final <A extends Appendable> A appendVersionString (
			final A sb, final EnumMap<VersionComponent,? extends Number> ver)
		throws IOException
	{
		return appendVersionString(sb, ver, DEFAULT_VERSION_COMP_SEP);
	}

	public static final String toVersionString (
			final EnumMap<VersionComponent,? extends Number> ver, final char delim)
	{
		try
		{
			return appendVersionString(new StringBuilder(VersionComponent.VALUES.size() * 6), ver, delim)
						.toString()
						;
		}
		catch(IOException e)	// can happen if bad values found
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public static final String toVersionString (final EnumMap<VersionComponent,? extends Number> ver)
	{
		return toVersionString(ver, DEFAULT_VERSION_COMP_SEP);
	}

	public String toVersionString ()
	{
		return toVersionString(this);
	}
}
