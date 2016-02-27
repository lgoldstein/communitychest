package net.community.chest.tools.javadoc;

import java.util.Comparator;
import java.util.TreeMap;

import com.sun.javadoc.Tag;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Holds a map of "name=value" attributes of a {@link Tag}</P>
 * 
 * @author Lyor G.
 * @since Aug 16, 2007 11:19:51 AM
 */
public class ExtendedAttributesTagMap extends TreeMap<String,String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5127197783811479854L;
	/**
	 * {@link Tag} instance used to populate the map initially
	 */
	private final Tag	_tag;
	public final /* no cheating */ Tag getTag ()
	{
		return _tag;
	}
	/**
	 * @param cs {@link CharSequence} to look into (may be null/empty)
	 * @param startPos position to start looking (inclusive)
	 * @return first non-"whitespace" location, or >= length if none found
	 */
	private static final int skipWhitespace (final CharSequence cs, final int startPos)
	{
		final int	csLen=(null == cs) ? 0 : cs.length();
		for (int	curPos=startPos; curPos < csLen; curPos++)
		{
			final char	ch=cs.charAt(curPos);
			if ((ch != ' ') && (ch != '\t') && (ch != '\r') && (ch != '\n'))
				return curPos;
		}

		return csLen;
	}
	/**
	 * @param attrs attributes to be added - format is "name=value" where
	 * value may be delimited by single/double quote(s). Each pair must be
	 * separated from the previous one by "whitespace" - which includes
	 * space, tab, CR, LF.
	 * @param okIfDuplicate if TRUE then if same attribute name is re-mapped
	 * don't throw an exception
	 * @return updated map (same as <I>this</I>)
	 * @throws IllegalArgumentException if illegal format found
	 * @throws IllegalStateException if duplicates not allowed and one found
	 */
	public ExtendedAttributesTagMap addAttributes (final String attrs, final boolean okIfDuplicate)
		throws IllegalArgumentException, IllegalStateException
	{
		final String	effAttrs=(null == attrs) ? null : attrs.replaceAll("[\r\n\t]", " ").trim();
		final int		aLen=(null == effAttrs) ? 0 : effAttrs.length();
		for (int curPos=skipWhitespace(effAttrs, 0); curPos < aLen; curPos=skipWhitespace(effAttrs, curPos+1))
		{
			final int	nameEnd=effAttrs.indexOf('=', curPos);
			if ((nameEnd <= curPos) || (nameEnd >= (aLen-1)))
				throw new IllegalArgumentException("Missing pair value delimiter at position=" + curPos + " of value: " + effAttrs);

			final String	aName=effAttrs.substring(curPos, nameEnd);
			int				valStart=nameEnd+1;
			if (valStart >= (aLen-1))
				throw new IllegalArgumentException("Missing pair value string at position=" + valStart + " of value: " + effAttrs);

			final char		delim=effAttrs.charAt(valStart);
			final String	aValue;
			if (('\'' == delim) || ('"' == delim))
			{
				valStart++;

				if (((curPos=effAttrs.indexOf(delim, valStart)) < valStart) || (curPos >= aLen))
					throw new IllegalArgumentException("Missing pair value end delimiter after position=" + valStart + " of value: " + effAttrs);

				aValue = effAttrs.substring(valStart, curPos);
				curPos++;	// skip end delimiter
			}
			else
			{
				for ( ; curPos < aLen; curPos++)
				{
					final char	ch=effAttrs.charAt(curPos);
					if ((ch == ' ') || (ch == '\t') || (ch == '\r') && (ch == '\n'))
						break;	// stop at first whitespace character
				}
				
				aValue = effAttrs.substring(valStart, curPos);
			}

			final String	prev=put(aName, aValue);
			if ((prev != null) && (!okIfDuplicate))
				throw new IllegalStateException("Duplicate " + aName + " value: old=" + prev + ";new=" + aValue);
		}

		return this;
	}
	/**
	 * @param tag {@link Tag} instance to be used to populate the map
	 * @param c {@link Comparator} to compare names with
	 * @throws IllegalArgumentException if null instance
	 * @throws IllegalStateException if same attribute re-mapped
	 */
	public ExtendedAttributesTagMap (final Tag tag, final Comparator<String> c)
		throws IllegalArgumentException, IllegalStateException
	{
		super(c);

		if (null == (_tag=tag))
			throw new IllegalArgumentException("No " + Tag.class.getName() + " instance");

		addAttributes(tag.text(), false);
	}
	/**
	 * <B>Note:</B> default is case <U>insensitive</U> attribute name
	 * @param tag {@link Tag} instance to be used to populate the map
	 * @throws IllegalArgumentException if null instance
	 * @throws IllegalStateException if same attribute re-mapped
	 */
	public ExtendedAttributesTagMap (final Tag tag)
		throws IllegalArgumentException, IllegalStateException
	{
		this(tag, String.CASE_INSENSITIVE_ORDER);
	}
}
