/*
 * 
 */
package net.community.chest.util.locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 10:08:34 AM
 */
public final class LocaleUtils {
	private LocaleUtils ()
	{
		// no instance
	}
	/**
	 * @param vals order is <I>language[,country[,variant]]</I> - i.e.,
	 * each optional value can be omitted. <B>Note:</B> if more than
	 * 3 elements then error is assumed
	 * @return created {@link Locale} - null if error
	 * @see Locale
	 */
	public static final Locale getFormattingLocale (final List<String> vals)
	{
		final int	numVals=(null == vals) ? 0 : vals.size();
		if (numVals <= 0)
			return null;

		switch(numVals)
 		{
			case 1 	:
				{
					final String	lang=StringUtil.getCleanStringValue(vals.get(0));
					if ((null == lang) || (lang.length() <= 0))
						return null;

					if ("default".equalsIgnoreCase(lang))
						return Locale.getDefault();

					return new Locale(lang);
				}

			case 2 	:
				{
					final String	lang=StringUtil.getCleanStringValue(vals.get(0)),
									country=StringUtil.getCleanStringValue(vals.get(1));
					if ((null == lang) || (lang.length() <= 0)
					 || (null == country) || (country.length() <= 0))
						return null;
					
					return new Locale(lang, country);
				}

			case 3 	:
				{
					final String	lang=StringUtil.getCleanStringValue(vals.get(0)),
									country=StringUtil.getCleanStringValue(vals.get(1)),
									var=StringUtil.getCleanStringValue(vals.get(2));
					if ((null == lang) || (lang.length() <= 0)
					 || (null == country) || (country.length() <= 0)
					 || (null == var) || (var.length() <= 0))
						return null;
	
					return new Locale(lang, country, var);
				}

			default	:
				return null;
		}

	}
	/**
	 * @param vals order is <I>language[,country[,variant]]</I> - i.e.,
	 * each optional value can be omitted. <B>Note:</B> if more than
	 * 3 elements then error is assumed
	 * @return created {@link Locale} - null if error
	 * @see Locale
	 */
	public static final Locale getFormattingLocale (final String ... vals)
	{
		if ((null == vals) || (vals.length <= 0))
			return null;

		return getFormattingLocale(Arrays.asList(vals));
	}
	/**
	 * @param pattern must be <I>language[,country[,variant]]</I>
	 * @param delim delimiter to be used to separate the {@link Locale}
	 * components - may not be '\0'. <B>Note:</B> if parsing yields more than
	 * 3 elements then error is assumed
	 * @return created {@link Locale} - null if error
	 * @see #getFormattingLocale(String[])
	 * @see #getFormattingLocale(List)
	 * @see Locale
	 */
	public static final Locale getFormattingLocale (final String pattern, final char delim)
	{
		if ((null == pattern) || (pattern.length() <= 0) || ('\0' == delim))
			return null;

		return getFormattingLocale(StringUtil.splitString(pattern, delim));
	}
	/**
	 * @param pattern must be <I>language[,country[,variant]]</I>
	 * @return created {@link Locale} - null if error
	 * @see #getFormattingLocale(String, char)
	 */
	public static final Locale getFormattingLocale (final String pattern)
	{
		return getFormattingLocale(pattern, ',');
	}
	/**
	 * @param l The {@link Locale} instance - may be <code>null</code>
	 * @return A {@link List} of {@link String}-s that can be used to encode
	 * the {@link Locale} - i.e. <I>language[,country[,variant]]</I> order.
	 * May be null/empty if original instance is <code>null</code> 
	 * @see #getFormattingLocale(List)
	 * @throws IllegalStateException if bad/illegal {@link Locale} properties
	 * (e.g., no country but have variant)
	 */
	public static final List<String> getLocaleFormatting (final Locale l) throws IllegalStateException
	{
		if (null == l)
			return null;

		final String[]	vals={	// same order as LocaleUtils#getFormattingLocale
				l.getLanguage(), l.getCountry(), l.getVariant()
			};
		final List<String>	rl=new ArrayList<String>(vals.length);
		int					vIndex=0;
		for (; vIndex < vals.length; vIndex++)
		{
			final String	vs=vals[vIndex];
			if ((null == vs) || (vs.length() <= 0))
				break;	// assume that 1st empty value signals end of data

			rl.add(vs);
		}

		// make sure rest of values are empty
		for (vIndex++; vIndex < vals.length; vIndex++)
		{
			final String	vs=vals[vIndex];
			if ((vs != null) && (vs.length() > 0))
				throw new IllegalStateException("getLocaleFormatting(" + l + ") invalid data at index=" + vIndex);
		}

		if (rl.size() <= 0)
			throw new IllegalStateException("getLocaleFormatting(" + l + ") no data extracted");

		return rl;
	}
	/**
	 * @param l A {@link Locale} instance - may be <code>null</code>
	 * @return A pattern representing the locale
	 * @throws IllegalStateException if bad/illegal {@link Locale} properties
	 * (e.g., no country but have variant)
	 * @see #getFormattingLocale(String)
	 * @see #getLocaleFormatting(Locale)
	 */
	public static final String getLocalePattern (final Locale l) throws IllegalStateException
	{
		final List<String>	vals=getLocaleFormatting(l);
		final int			numVals=(null == vals) ? 0 : vals.size();
		if (numVals <= 0)
			return null;

		final StringBuilder	sb=new StringBuilder(numVals * 8);
		for (final String	c : vals)
		{
			if ((null == c) || (c.length() <= 0))
				continue;

			if (sb.length() > 0)
				sb.append(',');
			sb.append(c);
		}

		return (sb.length() > 0) ? sb.toString() : null;
	}
}
