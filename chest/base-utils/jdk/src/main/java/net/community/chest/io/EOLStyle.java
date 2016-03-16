package net.community.chest.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.SysPropsEnum;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>An {@link Enum} value used to hold <U><B>E</B></U>nd-<U><B>O</B></U>f-<U><B>L</B></U>ine
 * styles - either CRLF or LF. It implements {@link CharSequence} as a matter of convenience</P>
 * @author Lyor G.
 * @since Jun 16, 2008 9:23:28 AM
 */
public enum EOLStyle implements CharSequence {
    CRLF('\r', '\n'),
    LF('\n'),
    // NOTE: MUST BE LAST TO AVOID "fromChars" confusion" !!!
    LOCAL(SysPropsEnum.LINESEP.getPropertyValue());

    private final char[]    _chars;
    public final char[] getStyleChars ()
    {
        return _chars;
    }

    private final byte[]    _bytes;
    public final byte[] getStyleBytes ()
    {
        return _bytes;
    }

    private final String    _str;
    public final String getStyleString ()
    {
        return _str;
    }

    public final boolean isEquivalent (final EOLStyle s)
    {
        if (null == s)
            return false;

        if (equals(s))
            return true;

        return (0 == StringUtil.compareDataStrings(getStyleString(), s.getStyleString(), true));
    }

    public <A extends Appendable> A appendEOL (final A sb) throws IOException
    {
        if (null == sb)
            throw new IOException("appendEOL(" + name() + ") no " + Appendable.class.getSimpleName() + " instance");

        sb.append(getStyleString());
        return sb;
    }
    /*
     * @see java.lang.CharSequence#charAt(int)
     */
    @Override
    public char charAt (int index)
    {
        final char[]    sc=getStyleChars();
        return sc[index];
    }
    /*
     * @see java.lang.CharSequence#length()
     */
    @Override
    public int length ()
    {
        final char[]    sc=getStyleChars();
        return sc.length;
    }
    /*
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    @Override
    public CharSequence subSequence (int start, int end)
    {
        final int    seqLen=end - start;
        if ((start < 0) || (end < 0) || (start > end))
            throw new IndexOutOfBoundsException("subSequence(" + name() + ")[" + start + "/" + end + "]");

        if (0 == seqLen)
            return "";

        final char[]    sc=getStyleChars();
        return new String(sc, start, seqLen);
    }
    /*
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString ()
    {
        return getStyleString();
    }

    EOLStyle (final String s, final char ... chars)
    {
        _chars = chars;
        _str = s;
        _bytes = new byte[chars.length];

        for (int cIndex=0; cIndex < chars.length; cIndex++)
            _bytes[cIndex] = (byte) (chars[cIndex] & 0x00FF);
    }

    EOLStyle (final char ... chars)
    {
        this(new String(chars), chars);
    }

    EOLStyle (final String s)
    {
        this(s, s.toCharArray());
    }

    public static final List<EOLStyle>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final EOLStyle fromName (final String s)
    {
        return EnumUtil.fromName(VALUES, s, false);
    }
    /**
     * Maximum number of characters used by <U>any</U> {@link EOLStyle} value
     */
    public static final int MAX_STYLE_CHARS_LEN=2;
    public static final EOLStyle fromStyleChars (final CharSequence cs, final int offset, final int len)
    {
        final int    maxLen=(null == cs) ? 0 : cs.length(),
                    maxPos=offset + len;
        if ((offset < 0) || (maxPos > maxLen))
            return null;

        if ((len <= 0) || (len > MAX_STYLE_CHARS_LEN))
            return null;

        for (final EOLStyle v : VALUES)
        {
            final char[]    vChars=(null == v) ? null : v.getStyleChars();
            final int        vcLen=(null == vChars) ? 0 : vChars.length;
            if (vcLen != len)
                continue;

            boolean    match=true;
            for (int    sPos=offset, cIndex=0; cIndex < vcLen; cIndex++, sPos++)
            {
                final char    c1=cs.charAt(sPos), c2=vChars[cIndex];
                if (!(match=(c1 == c2)))
                    break;    // just so we have a debug breakpoint
            }

            if (match)
                return v;
        }

        return null;    // no match found
    }

    public static final EOLStyle fromStyleChars (final CharSequence cs)
    {
        return fromStyleChars(cs, 0, (null == cs) ? 0 : cs.length());
    }

    public static final EOLStyle fromStyleChars (final char[] chars, final int offset, final int len)
    {
        final int    maxLen=(null == chars) ? 0 : chars.length,
                    maxPos=offset + len;
        if ((offset < 0) || (maxPos > maxLen))
            return null;

        if ((len <= 0) || (len > MAX_STYLE_CHARS_LEN))
            return null;

        return fromStyleChars(new String(chars, offset, len), 0, len);
    }

    public static final EOLStyle fromStyleChars (final char ... chars)
    {
        return fromStyleChars(chars, 0, (null == chars) ? 0 : chars.length);
    }
    /**
     * Maximum number of bytes used by <U>any</U> {@link EOLStyle} value
     */
    public static final int MAX_STYLE_BYTES_LEN=MAX_STYLE_CHARS_LEN;
    public static final EOLStyle fromStyleBytes (final byte[] bytes, final int offset, final int len)
    {
        final int    maxLen=(null == bytes) ? 0 : bytes.length,
                    maxPos=offset + len;
        if ((offset < 0) || (maxPos > maxLen))
            return null;

        if ((len <= 0) || (len > MAX_STYLE_BYTES_LEN))
            return null;

        for (final EOLStyle v : VALUES)
        {
            final byte[]    vBytes=(null == v) ? null : v.getStyleBytes();
            final int        vcLen=(null == vBytes) ? 0 : vBytes.length;
            if (vcLen != len)
                continue;

            boolean    match=true;
            for (int    sPos=offset, cIndex=0; cIndex < vcLen; cIndex++, sPos++)
            {
                final byte    b1=bytes[sPos], b2=vBytes[cIndex];
                if (!(match=(b1 == b2)))
                    break;    // just so we have a debug breakpoint
            }

            if (match)
                return v;
        }

        return null;    // no match found
    }

    public static final EOLStyle fromStyleBytes (final byte ... bytes)
    {
        return fromStyleBytes(bytes, 0, (null == bytes) ? 0 : bytes.length);
    }
    /**
     * Truncates the input up to first CR/LF/both (if any)
     * @param s Input {@link String}
     * @return The {@link String} truncated at the 1st CR/LF (whichever
     * comes first) - may be same as input if no CR/LF found or null/empty
     * if null/empty to begin with or null/empty after truncation
     */
    public static final String truncateToEOL (final String s)
    {
        final int    sLen=(null == s) ? 0 : s.length(),
                    lfPos=(sLen <= 0) ? (-1) : s.indexOf('\n'),
                    crPos=(sLen <= 0) ? (-1) : s.indexOf('\r');
        if (lfPos >= 0)
        {
            if (crPos >= 0)
            {
                final int    minPos=Math.min(lfPos, crPos);
                return (minPos > 0) ? s.substring(0, minPos) : "";
            }

            return (lfPos > 0) ? s.substring(0, lfPos) : "";
        }
        else if (crPos >= 0)
        {
            return (crPos > 0) ? s.substring(0, crPos) : "";
        }

        return s;    // neither CR neither LF available
    }
}
