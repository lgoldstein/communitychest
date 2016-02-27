/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * Encapsulates the available file separator styles into an {@link Enum}
 * @author Lyor G.
 * @since Mar 16, 2011 7:47:06 AM
 */
public enum FileSeparatorStyle {
	UNIX('/'),
	WINDOWS('\\'),
	LOCAL(File.separatorChar, File.separator);

	private final char	_sepChar;
	public final char getSeparatorChar ()
	{
		return _sepChar;
	}

	private final String	_sepString;
	public final String getSeparatorString ()
	{
		return _sepString;
	}

	FileSeparatorStyle (char sepChar, String sepString)
	{
		_sepChar = sepChar;
		_sepString = sepString;
	}

	FileSeparatorStyle (char sepChar)
	{
		this(sepChar, String.valueOf(sepChar));
	}
	/**
	 * Appends the separator used by this style
	 * @param <A> The {@link Appendable} type
	 * @param sb The {@link Appendable} instance
	 * @return Same as input
	 * @throws IOException If failed to append separator
	 */
	public <A extends Appendable> A append (A sb) throws IOException
	{
		if (sb == null)
			throw new IOException("No " + Appendable.class.getSimpleName() + " instance to append to");

		sb.append(getSeparatorChar());
		return sb;
	}
	/**
	 * Replaces the separator used in the original path with the one used by
	 * this style
	 * @param path Original path {@link String}
	 * @param sepChar The separator used in the input path
	 * @return The adjusted path - may be the same as input if no adjustment
	 * was required
	 */
	public String normalizePath (final String path, final char sepChar)
	{
		final char	newChar=getSeparatorChar();
		if ((path == null) || (path.length() <= 0) || (sepChar == newChar))
			return path;

		final String	repVal=path.replace(sepChar, newChar);
		if (repVal != path)
			return repVal;	// debug breakpoint

		return path;
	}
	/**
	 * Replaces the separator used by the specific style with the one
	 * specified as parameter
	 * @param path Original path {@link String} - <U>assumed</U> to use the
	 * separator of this style
	 * @param sepChar The separator to be used instead of the style(d) one
	 * @return The adjusted path - may be the same as input if no adjustment
	 * was required
	 */
	public String denormalizePath (final String path, final char sepChar)
	{
		final char	newChar=getSeparatorChar();
		if ((path == null) || (path.length() <= 0) || (sepChar == newChar))
			return path;

		final String	repVal=path.replace(newChar, sepChar);
		if (repVal != path)
			return repVal;	// debug breakpoint

		return path;
	}

	public static final List<FileSeparatorStyle>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FileSeparatorStyle fromString (String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FileSeparatorStyle fromSeparatorChar (final char ch)
	{
		for (final FileSeparatorStyle val : VALUES)
		{
			if ((val != null) && (!LOCAL.equals(val)) && (val.getSeparatorChar() == ch))
				return val;
		}

		// check LOCAL last
		if (LOCAL.getSeparatorChar() == ch)
			return LOCAL;

		return null;
	}
	
	public static final FileSeparatorStyle fromSeparatorString (final String sep)
	{
		if ((sep == null) || (sep.length() != 1))
			return null;

		return fromSeparatorChar(sep.charAt(0));
	}
}
