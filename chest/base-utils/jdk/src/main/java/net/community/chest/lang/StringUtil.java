
package net.community.chest.lang;

/* NOTE !!! do not add dependencies on classes other than pure JDK */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.management.ObjectName;

import net.community.chest.reflect.AttributeAccessor;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful manipulations of {@link String}/{@link CharSequence}-s
 *
 * @author Lyor G.
 * @since Jun 13, 2007 3:03:56 PM
 */
public final class StringUtil {
    private StringUtil ()
    {
        throw new UnsupportedOperationException("Instance N/A");
    }
    /**
     * Compares 2 potentially null/empty strings - intended to help {@link Object#equals(java.lang.Object)}
     * and/or {@link Comparable#compareTo(java.lang.Object)} implementations
     * @param s1 first string
     * @param s2 second string
     * @param caseSensitive true if case sensitive comparison required
     * @return according to {@link java.lang.String#compareTo(java.lang.String)} and/or
     * {@link java.lang.String#compareToIgnoreCase(java.lang.String)}. <B>Note:</B>
     * null/empty strings are considered equal
     */
    public static final int compareDataStrings (final String s1, final String s2, final boolean caseSensitive)
    {
        if ((null == s1) || (s1.length() <= 0))
        {
            if ((null == s2) || (s2.length() <= 0))
                return 0;
            else    // null/empty comes last
                return (+1);
        }

        // s1 cannot be null/empty here
        if ((null == s2) || (s2.length() <= 0))
            return (-1);    // null/empty comes last

        if (s1 == s2)    // check the obvious
            return 0;

        if (caseSensitive)
            return s1.compareTo(s2);
        else
            return s1.compareToIgnoreCase(s2);
    }
    /**
     * Calculates a hash code on the given string (which may be null/empty).
     * Intended to help implementors of {@link Object#hashCode()}
     * @param s data component string
     * @param caseSensitive TRUE if hash code should be case-sensitive. Otherwise,
     * the string's lowercase hash code is calculated
     * @return hash code - <B>Note:</B> null/empty values have a zero hash code
     */
    public static final int getDataStringHashCode (final CharSequence s, final boolean caseSensitive)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return 0;

        if (caseSensitive)
            return s.hashCode();

        int    h=sLen;
        for (int i = 0; i < sLen; i++)
        {
            final char    oc=s.charAt(i),
                        lc=Character.toLowerCase(oc);
            h = 31 * h + lc;
        }

        return h;
    }
    /**
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} to append to - may NOT be null
     * @param cs original {@link CharSequence} whose order we want to reverse
     * @param startPos position to start reversing from (inclusive)
     * @param len number of characters to reverse
     * @return appended reversed contents - same as input if nothing to reverse
     * @throws IOException if failed to append
     */
    public static final <A extends Appendable> A reverseAppend (final A sb, final CharSequence cs, final int startPos, final int len) throws IOException
    {
        final int    maxPos=startPos + len;
        if ((null == sb) || (null == cs) || (startPos < 0) || (len <= 0) || (maxPos > cs.length()))
            return sb;

        for (int pos=maxPos-1; pos >= startPos; pos--)
            sb.append(cs.charAt(pos));

        return sb;
    }
    /**
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link StringBuilder} to append to - may NOT be null
     * @param cs original {@link CharSequence} whose order we want to reverse
     * @return reversed contents - null/empty if nothing to reverse
     * @throws IOException if failed to append
     */
    public static final <A extends Appendable> A reverseAppend (final A sb, final CharSequence cs) throws IOException
    {
        final int    csLen=(null == cs) ? 0 : cs.length();
        return ((null == sb) || (csLen <= 0)) ? sb : reverseAppend(sb, cs, 0, csLen);
    }
    /**
     * @param cs original {@link CharSequence} whose order we want to reverse
     * @param startPos position to start reversing from (inclusive)
     * @param len number of characters to reverse
     * @return reversed contents - null/empty if nothing to reverse
     * @throws IOException if failed to append (which should be never...)
     */
    public static final StringBuilder reverse (final CharSequence cs, final int startPos, final int len) throws IOException
    {
        final int    maxPos=startPos + len;
        if ((null == cs) || (startPos < 0) || (len <= 0) || (maxPos > cs.length()))
            return null;

        return reverseAppend(new StringBuilder(len), cs, startPos, len);
    }
    /**
     * @param cs original {@link CharSequence} whose order we want to reverse
     * @return reversed contents - null/empty if nothing to reverse
     * @throws IOException if failed to append (which should be never...)
     */
    public static final StringBuilder reverse (final CharSequence cs) throws IOException
    {
        final int    csLen=(null == cs) ? 0 : cs.length();
        return (csLen <= 0) ? null : reverse(cs, 0, csLen);
    }
    /**
     * @param utf8Str {@link CharSequence} containing UTF-8 encoding characters
     * @param startPos position to start translation from (inclusive)
     * @param len number of characters to translate
     * @return un-encoded Java {@link String}
     * @throws UnsupportedEncodingException if unable to translate the initial
     * string (may be null/empty if initial string is null/empty)
     */
    public static final String toJavaStr (final CharSequence utf8Str, final int startPos, final int len) throws UnsupportedEncodingException
    {
        if (0 == len)
            return null;

        final int        maxPos=startPos + len;
        final byte[]    utf8Bytes=new byte[len];
        for(int sPos=startPos, aPos=0; sPos < maxPos; sPos++, aPos++)
        {
            final char c=utf8Str.charAt(sPos);
            if ((c & 0x00FFFF) > 0x00FF)//non utf8 character
                throw new UnsupportedEncodingException("Non UTF-8 character at position " + sPos + " in string=" + utf8Str);

            utf8Bytes[aPos] = (byte) (c & 0x00FF);
        }

        return new String(utf8Bytes,"UTF-8");
    }
    /**
     * @param utf8Str {@link CharSequence} containing UTF-8 encoding characters
     * @return un-encoded Java {@link String}
     * @throws UnsupportedEncodingException if unable to translate the initial
     * string (may be null/empty if initial string is null/empty)
     */
    public static final String toJavaStr (final CharSequence utf8Str) throws UnsupportedEncodingException
    {
        return (null == utf8Str) ? null : toJavaStr(utf8Str, 0, utf8Str.length());
    }
    /**
     * @param c initial character
     * @return UNICODE "code-page" part (the MSB)
     */
    public static final short getCharCodePage (final char c)
    {
        return (short) ((c >> 8) & 0x00FF);
    }
    /**
     * @param c initial character
     * @return UNICODE "value" part (the LSB)
     */
    public static final short getCharCodeValue (final char c)
    {
        return (short) (c & 0x00FF);
    }
    /**
     * @param s original value - may be null/empty
     * @return <I>trim</I>-ed value - may be null/empty if original was such
     */
    public static final String getCleanStringValue (final String s)
    {
        if ((null == s) || (s.length() <= 0))
            return s;
        return s.trim();
    }
    /**
     * Converts an array of bytes assumed to contain US-ASCII characters into
     * a characters array
     * @param b initial bytes array
     * @param bOffset offset in array to start converting
     * @param len number of bytes to convert
     * @param c target characters array to receive conversion results
     * @param cOffset offset in characters array to place the result
     * @return number of converted bytes (same as <I>len(gth)</I> parameter)
     * @throws IllegalArgumentException if negative length provided
     */
    public static final int toASCIIChars (final byte[] b, final int bOffset, final int len, final char[] c, final int cOffset)
            throws IllegalArgumentException
    {
        if (len < 0)
            throw new IllegalArgumentException("Bad/Illegal buffer length to convert to ASCII chars: " + len);

        for (int    bPos=bOffset, maxPos=bOffset+len, cPos=cOffset; bPos < maxPos; bPos++, cPos++)
            c[cPos] = (char) (b[bPos] & 0x00FF);

        return len;
    }
    /**
     * Converts an array of characters assumed to contain US-ASCII characters
     * into its bytes array counter part
     * @param c original characters array
     * @param cOffset offset in characters array to start conversion
     * @param len number of characters to convert
     * @param b bytes array to receive conversion results
     * @param bOffset offset in bytes array to place the result
     * @return number of converted characters (same as <I>len</I> parameter)
     * @throws IllegalArgumentException if negative length provided
     * @throws UnsupportedEncodingException if non-ASCII character encountered
     * (i.e., one whose code-page value is not zero...)
     */
    public static final int toASCIIBytes (final char[] c, final int cOffset, final int len, final byte[] b, final int bOffset)
        throws IllegalArgumentException, UnsupportedEncodingException
    {
        if (len < 0)
            throw new IllegalArgumentException("Bad/Illegal buffer length to convert to ASCII bytes: " + len);

        for (int    cPos=cOffset, maxPos=cOffset+len, bPos=bOffset; cPos < maxPos; cPos++, bPos++)
        {
            final char    ch=c[cPos];
            if (getCharCodePage(ch) != 0)
                throw new UnsupportedEncodingException("Bad character value (" + ((int) ch) + ") at position=" + cPos + " in string=" + new String(c, cOffset, len));

            b[bPos] = (byte) (ch & 0x00FF);
        }

        return len;
    }

    public static <A extends Appendable> A appendStringList (final Collection<?> vals, final char delim, final A ap) throws IOException
    {
        if ((null == vals) || (vals.size() <= 0))
            return ap;

        int    vIndex=0;
        for (final Object o : vals)
        {
            final String    v=(null == o) ? null : o.toString();
            if ((null == v) || (v.length() <= 0))
                continue;
            if ((vIndex > 0) && (delim != '\0'))
                ap.append(delim);
            ap.append(v);
            vIndex++;
        }

        return ap;
    }

    public static String asStringList (final Collection<?> vals, final char delim)
    {
        final int    numVals=(null == vals) ? 0 : vals.size();
        if (numVals <= 0)
            return null;

        try
        {
            final StringBuilder    sb=appendStringList(vals, delim, new StringBuilder(numVals * 32));
            if ((null == sb) || (sb.length() <= 0))
                return null;

            return sb.toString();
        }
        catch(IOException ioe)    // should not happen since StringBuilder does not throw exceptions
        {
            throw new IllegalStateException("asStringList() - unexpected " + ioe.getClass().getName() + ": " + ioe.getMessage());
        }
    }

    public static String asStringList (final char delim, final Object ... vals)
    {
        return ((null == vals) || (vals.length <= 0)) ? null : asStringList(Arrays.asList(vals), delim);
    }
    /**
     * Appends a name/value pair using the specified delimiter
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param name attribute name - may NOT be NULL/empty
     * @param delim delimiter to use - may NOT be '\0'
     * @param value value - if null/empty then it is not appended (and
     * neither is the delimiter)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendNameValuePair (final A sb, final String name, final char delim, final String value) throws IOException
    {
        if ((null == sb) || (null == name) || (name.length() <= 0) || ('\0' == delim))
            throw new IOException("appendNameValuePair(" + name + "/" + value + ") incomplete parameters");

        sb.append(name);
        if ((value != null) && (value.length() > 0))
            sb.append(delim).append(value);

        return sb;
    }
    /**
     * Appends the specified character the specified number of times
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c character to be appended
     * @param numReps number of times to append it - if <= 0 then nothing is appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A repeat (final A sb, final char c, final int numReps) throws IOException
    {
        if (numReps <= 0)
            return sb;

        if (null == sb)
            throw new IOException("repeat(" + String.valueOf(c) + ")[" + numReps + "] no " + Appendable.class.getName() + " instance");

        for (int    i=0; i < numReps; i++)
            sb.append(c);

        return sb;
    }
    /**
     * Appends specified characters array several times
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param c array - may be null (in which case nothing is appended)
     * @param numReps number of times to re-append - if <= 0 then nothing appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A repeat (final A sb, final char[] c, final int numReps) throws IOException
    {
        if ((null == c) || (c.length <= 0) || (numReps <= 0))
            return sb;

        return repeat(sb, c, 0, c.length, numReps);
    }
    /**
     * Appends specified string several times
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param s string to be appended
     * @param offset offset in string to copy data from
     * @param len number of character to copy each time
     * @param numReps number of times to re-append - if <= 0 then nothing appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A repeat (final A sb, final CharSequence s, final int offset, final int len, final int numReps) throws IOException
    {
        final int    strLen=(null == s) ? 0 : s.length(), maxOffset=offset + len;
        if ((offset < 0) || (len < 0) || (maxOffset > strLen))
            throw new IOException("repeat(" + s + ")[" + numReps + "] invalid string range");
        if (1 == len)    // trivial optimization
            return repeat(sb, s.charAt(offset), numReps);
        if (numReps <= 0)
            return sb;
        if (null == sb)
            throw new IOException("repeat(" + s + ")[" + numReps + "] no " + Appendable.class.getName() + " instance");

        for (int    rIndex=0; rIndex < numReps; rIndex++)
            sb.append(s, offset, maxOffset);

        return sb;
    }
    /**
     * Appends specified string several times
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param s string to be appended - may be null/empty (in which case nothing is appended)
     * @param numReps number of times to re-append - if <= 0 then nothing appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A repeat (final A sb, final CharSequence s, final int numReps) throws IOException
    {
        if ((null == s) || (s.length() <= 0) || (numReps <= 0))
            return sb;

        return repeat(sb, s, 0, s.length(), numReps);
    }
    /**
     * Appends specified characters array section several times
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param off - offset in array to start appending
     * @param len - number of character to append
     * @param c array - may be null (in which case nothing is appended)
     * @param numReps number of times to re-append - if <= 0 then nothing appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A repeat (final A sb, final char[] c, final int off, final int len, final int numReps) throws IOException
    {
        final int    cLen=(null == c) ? 0 : c.length;
        if ((off < 0) || (len < 0) || ((off + len) > cLen))
            throw new IOException("repeat(char[])[" + numReps + "] bad range");
        // if nothing to do, then return immediately
        if ((0 == len) || (numReps <= 0))
            return sb;
        if (null == c)
            throw new IOException("repeat(char[])[" + numReps + "] no char[] to append");
        if (null == sb)
            throw new IOException("repeat(char[])[" + numReps + "] no " + Appendable.class.getName() + " instance");

        if (1 == len)    // trivial optimization
            return repeat(sb, c[off], numReps);
        else
            return repeat(sb, new String(c, off, len), numReps);
    }
    /**
     * Appends the specified value to the string buffer, padding (to the left) if necessary
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param val value characters to be added
     * @param padLen field length to be padded to if necessary - Note: value is appended even if exceeds specified field width
     * @param padChar character to be used for padding
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendPadded (final A sb, final char[] val, final int padLen, final char padChar) throws IOException
    {
        repeat(sb, padChar, padLen - ((null == val) ? 0 : val.length));
        repeat(sb, val, 1);
        return sb;
    }
    /**
     * Appends the specified value to the string buffer, padding (to the left) if necessary
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param val value characters to be added
     * @param padLen field length to be padded to if necessary - Note: value is appended even if exceeds specified field width
     * @param padChar character to be used for padding
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendPadded (final A sb, final CharSequence val, final int padLen, final char padChar) throws IOException
    {
        final int    vLen=Math.max((null == val) ? 0 : val.length(), 0);
        repeat(sb, padChar, (padLen - vLen));
        repeat(sb, val, 1);
        return sb;
    }
    /**
     * Appends specified number using '0' to (left-) pad it up to specified field width
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param num number to be appended
     * @param padLen field width - Note: if number exceeds this value, it is appended anyway (no padding)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendPaddedNum (final A sb, final byte num, final int padLen) throws IOException
    {
        return appendPadded(sb, Byte.toString(num), padLen, '0');
    }
    /**
     * Appends specified number using '0' to (left-) pad it up to specified field width
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param num number to be appended
     * @param padLen field width - Note: if number exceeds this value, it is appended anyway (no padding)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendPaddedNum (final A sb, final short num, final int padLen) throws IOException
    {
        return appendPadded(sb, Short.toString(num), padLen, '0');
    }
    /**
     * Appends specified number using '0' to (left-) pad it up to specified field width
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param num number to be appended
     * @param padLen field width - Note: if number exceeds this value, it is appended anyway (no padding)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendPaddedNum (final A sb, final int num, final int padLen) throws IOException
    {
        return appendPadded(sb, Integer.toString(num), padLen, '0');
    }
    /**
     * Appends specified number using '0' to (left-) pad it up to specified field width
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param num number to be appended
     * @param padLen field width - Note: if number exceeds this value, it is appended anyway (no padding)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendPaddedNum (final A sb, final long num, final int padLen) throws IOException
    {
        return appendPadded(sb, Long.toString(num), padLen, '0');
    }
    /**
     * Appends supplied buffer as if it were a sequence of ASCII characters
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param buf buffer of bytes to be appended
     * @param startPos index to start appending (inclusive)
     * @param len number of bytes/characters to be appended - if <=0 then
     * nothing is appended
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendASCIIBytes (final A sb, final byte[] buf, final int startPos, final int len) throws IOException
    {
        if (len <= 0)  // if nothing to do, then return immediately
            return sb;
        if ((null == buf) || (startPos < 0) || ((startPos + len) > buf.length))
            throw new IOException("appendASCIIBytes(" + startPos + "-" + (startPos + len) + ") invalid range");
        if (null == sb)
            throw new IOException("appendASCIIBytes(" + startPos + "-" + (startPos + len) + ") no " + Appendable.class.getName() + " instance");

        for (int    curOffset=startPos, nIndex=0; nIndex < len; nIndex++, curOffset++)
            sb.append((char) (buf[curOffset] & 0x00FF));

        return sb;
    }
    /**
     * Appends supplied buffer as if it were a sequence of ASCII characters
     * @param <A> The {@link Appendable} generic type
     * @param sb {@link Appendable} instance to append to - may NOT be null
     * @param buf buffer of bytes to be appended - ignored if null/empty
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append or bad parameters
     */
    public static <A extends Appendable> A appendASCIIBytes (final A sb, final byte[] buf) throws IOException
    {
        return appendASCIIBytes(sb, buf, 0, (null == buf) ? 0 : buf.length);
    }
    /**
     * Builds a <I>main</I> invocation command line from given argument
     * @param args arguments list - may be null/empty and contain null/empty
     * elements (which are ignored)
     * @return updated command line - may be null/empty if no arguments found
     */
    public static String getArgsLine (final Object ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        if (numArgs <= 0)
            return null;
        else
            return asStringList(Arrays.asList(args), ' ');
    }
    /**
     * @param v string value
     * @return same string value trimmed and any quote/double-quote(s) removed
     */
    public static final String stripDelims (final String v)
    {
        final String    s=(null == v) ? null : v.trim();
        final int        sLen=(null == s) ? 0 : s.length();
        if (sLen < 2)    // at least 2 delimiters are required
            return s;

        final char    qtCh=s.charAt(0);
        if ((qtCh != '\'') && (qtCh != '"'))
            return s;    // OK if not quoted

        if (s.charAt(sLen-1) != qtCh)
            return s;    // OK if "un-balanced" quote

        return s.substring(1, sLen-1);    // strip the delimiters
    }
    /**
     * Splits a {@link String} using the specified separator character (e.g., comma
     * separated list of strings)
     * @param s The {@link String} to be split
     * @param sepChar Separation character
     * @return A <U>non-modifiable</U> {@link List} containing the
     * split string - may be null/empty if null/empty string input provided.
     * If no separator found in string, then a list of 1 member is returned.
     */
    public static final List<String> splitString (final String s, final char sepChar)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return null;

        final int    sPos=s.indexOf(sepChar);
        if ((sPos < 0) || (sPos >= sLen))
            return Arrays.asList(s);

        final String    sepStr;
        // special REGEX characters require escaping
        if (('.' == sepChar)
         || ('*' == sepChar)
         || ('?' == sepChar))
            sepStr = "\\" + String.valueOf(sepChar);
        else
            sepStr = String.valueOf(sepChar);

        final String[]    comps=s.split(sepStr);
        return Arrays.asList(comps);
    }

    private static Method    _sbAccessor    /* =null */;
    private static final synchronized Method getStringBuilderValueAccessor () throws Exception
    {
        if (null == _sbAccessor)
        {
            // TODO review this code when new JDK version released
            final Thread        t=Thread.currentThread();
            final ClassLoader    cl=t.getContextClassLoader();
            final Class<?>        c=cl.loadClass("java.lang.AbstractStringBuilder");
            if ((_sbAccessor=c.getDeclaredMethod("getValue")) != null)
            {
                if (!_sbAccessor.isAccessible())
                    _sbAccessor.setAccessible(true);
            }
        }

        return _sbAccessor;
    }
    /**
     * Extracts the array used internally by the {@link StringBuilder} via
     * reflection API
     * @param sb The {@link StringBuilder} instance
     * @return Backing char array - may be null if not initialized yet or
     * a null {@link StringBuilder} instance provided or reflection exception
     */
    public static final char[] getBackingArray (final StringBuilder sb)
    {
        if (null == sb)
            return null;

        try
        {
            final Method    m=getStringBuilderValueAccessor();
            if (m != null)
                return (char[]) m.invoke(sb, AttributeAccessor.EMPTY_OBJECTS_ARRAY);

            return null;
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    /**
     * Replaces all instances of a given character with another in a
     * {@link StringBuilder} instance
     * @param sb The {@link StringBuilder} instance - ignored if
     * <code>null</code>/empty
     * @param oldChar Character value to be replaced
     * @param newChar New character value to be used
     * @return Same as input {@link StringBuilder} instance with instances
     * replaced (if any match found)
     */
    public static final StringBuilder replace (
            final StringBuilder sb, final char oldChar, final char newChar)
    {
        final int    sbLen=(null == sb) ? 0 : sb.length();
        if ((sbLen <= 0) || (oldChar == newChar))
            return sb;

        for (int    cIndex=0; cIndex < sbLen; cIndex++)
        {
            final char    c=sb.charAt(cIndex);
            if (c == oldChar)
                sb.setCharAt(cIndex, newChar);
        }

        return sb;
    }
    /**
     * Counts number of times that specified character appears in the provided
     * {@link CharSequence}
     * @param c The character to count its instances
     * @param cs The {@link CharSequence} to use for counting - if null/empty
     * then zero count returned
     * @param startPos Start position in the sequence (inclusive) - if negative
     * or beyond {@link CharSequence#length()} then zero count returned
     * @param len Maximum number of character to scan - if negative then zero
     * count returned
     * @return Number of instances of the character in the {@link CharSequence}
     */
    public static final int getInstancesCount (final char c, final CharSequence cs, final int startPos, final int len)
    {
        final int    csLen=(null == cs) ? 0 : cs.length(), maxPos=startPos + len, csMax=Math.min(csLen,maxPos);
        if ((csLen <= 0) || (startPos < 0) || (len <= 0) || (startPos >= csLen))
            return 0;

        int    count=0;
        for (int    curPos=startPos; curPos < csMax; curPos++)
        {
            if (cs.charAt(curPos) == c)
                count++;
        }

        return count;
    }
    /**
     * Counts number of times that specified character appears in the provided
     * {@link CharSequence}
     * @param c The character to count its instances
     * @param cs The {@link CharSequence} to use for counting - if null/empty
     * then zero count returned
     * @return Number of instances of the character in the {@link CharSequence}
     */
    public static final int getInstancesCount (final char c, final CharSequence cs)
    {
        return (null == cs) ? 0 : getInstancesCount(c, cs, 0, cs.length());
    }
    /**
     * @param s Input {@link String} to be checked
     * @param p Prefix to be checked
     * @param strictPrefix <code>true</code>=the checked string must be
     * <U>longer</U> than the prefix
     * @param caseSensitive <code>true</code> if comparison is to be made
     * using case sensitive comparison
     * @return <code>true</code> if input string starts with prefix (and is
     * a strict prefix if so indicated)
     */
    public static final boolean startsWith (final String s, final String p, final boolean strictPrefix, final boolean caseSensitive)
    {
        final int    sLen=(null == s) ? 0 : s.length(),
                    pLen=(null == p) ? 0 : p.length();
        if (pLen > sLen)
            return false;

        if (pLen < sLen)
        {
            final String    ss=s.substring(0, pLen);
            if (compareDataStrings(ss, p, caseSensitive) != 0)
                return false;
        }
        else    // same length
        {
            if (strictPrefix)    // no need to compare if strict required
                return false;

            if (compareDataStrings(s, p, caseSensitive) != 0)
                return false;
        }

        return true;
    }
    /**
     * @param s Input {@link String} to be checked
     * @param e Suffix to be checked
     * @param strictSuffix <code>true</code>=the checked string must be
     * <U>longer</U> than the suffix
     * @param caseSensitive <code>true</code> if comparison is to be made
     * using case sensitive comparison
     * @return <code>true</code> if input string ends with suffix (and is
     * a strict suffix if so indicated)
     */
    public static final boolean endsWith (final String s, final String e, final boolean strictSuffix, final boolean caseSensitive)
    {
        final int    sLen=(null == s) ? 0 : s.length(),
                    eLen=(null == e) ? 0 : e.length();
        if (eLen > sLen)
            return false;

        if (eLen < sLen)
        {
            final String    ss=s.substring(sLen - eLen);
            if (compareDataStrings(ss, e, caseSensitive) != 0)
                return false;
        }
        else    // same length
        {
            if (strictSuffix)    // no need to compare if strict required
                return false;

            if (compareDataStrings(s, e, caseSensitive) != 0)
                return false;
        }

        return true;
    }
    /**
     * @param s Input {@link String} to be checked
     * @param c Content to be checked
     * @param strictContain <code>true</code>=the checked string must be
     * <U>longer</U> than the content
     * @param caseSensitive <code>true</code> if comparison is to be made
     * using case sensitive comparison
     * @return <code>true</code> if input string contains the indicated
     * content (and is a strict container if so indicated). <B>Note:</B>
     * <code>null</code>/empty string is contained in any other
     * non-<code>null</code>/empty string
     */
    public static final boolean contains (final String s, final String c, final boolean strictContain, final boolean caseSensitive)
    {
        final int    sLen=(null == s) ? 0 : s.length(),
                    cLen=(null == c) ? 0 : c.length();
        if (cLen > sLen)    // if container greater than input obviously cannot be contained
            return false;

        if (cLen < sLen)
        {
            if (cLen <= 0)    // null/empty is contained in any non-null/empty string
                return true;

            if (caseSensitive)
            {
                if (!s.contains(c))
                    return false;
            }
            else
            {
                if (!s.toLowerCase().contains(c.toLowerCase()))
                    return false;
            }
        }
        else    // same length
        {
            if (strictContain)    // no need to compare if strict required
                return false;

            if (compareDataStrings(s, c, caseSensitive) != 0)
                return false;
        }

        return true;
    }
    /**
     * Goes over a {@link String} and replaces all escaped characters with
     * their real value (backslash is escaped by repeating it)
     * @param s The original {@link String}
     * @return The modified {@link String} - may be same as input if no
     * replacement occurred.
     */
    public static final String replaceEscapedCharacters (final String s)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 1)    // must be at least backslash + one more character
            return s;

        StringBuilder    sb=null;
        int                lOffset=0;
        for (int    cOffset=lOffset; cOffset < sLen; cOffset++)
        {
            if (s.charAt(cOffset) != '\\')
                continue;

            if (cOffset >= (sLen-1))
                break;    // no more data

            final char    ec=s.charAt(cOffset + 1), tc;
            switch(ec)
            {
                case 't'    :
                    tc = '\t';
                    break;

                case 'b'    :
                    tc = '\b';
                    break;

                case 'n'    :
                    tc = '\n';
                    break;

                case 'r'    :
                    tc = '\r';
                    break;

                case '\\'    :
                    tc = '\\';
                    break;

                case '0'    :
                    tc = '\0';
                    break;

                default    :
                    tc = (char) (-1);
            }

            if (tc < 0)    // if unknown escape then ignore it
                continue;

            if (null == sb)
                sb = new StringBuilder(sLen - 1);

            final int    cLen=cOffset - lOffset;
            if (cLen > 0)    // copy clear text
            {
                final String    d=s.substring(lOffset, cOffset);
                sb.append(d);
            }
            sb.append(tc);

            cOffset++;    // skip the escaped character
            lOffset = cOffset + 1;    // clear text starts from next character
        }

        if (null == sb)    // no replacement
            return s;

        final int    remLen=sLen - lOffset;
        if (remLen > 0)    // copy leftover(s)
        {
            final String    d=s.substring(lOffset);
            sb.append(d);
        }

        return sb.toString();
    }
    /**
     * Finds the 1st occurrence of <U>any</U> character in a {@link CharSequence}
     * within another
     * @param cs The {@link CharSequence} to be scanned - ignored if null/empty
     * @param startPos Scan start position - ignored if negative or above
     * {@link CharSequence#length()}.
     * @param len Max. number of characters to scan - ignored if non-positive
     * @param ascending TRUE=start scan from start position and go "up".
     * FALSE=start at end position and go "down"
     * @param matchSeq The {@link CharSequence} of characters to look for
     * @return 1st index of a character from the match sequence in the scanned
     * sequence - negative if none found (or input ignored)
     */
    public static final int findIndexOf (final CharSequence    cs,
                                         final int             startPos,
                                         final int            len,
                                         final boolean        ascending,
                                         final CharSequence    matchSeq)
    {
        final int    csLen=(null == cs) ? 0 : cs.length(),
                    maxPos=Math.min(startPos + len, csLen),
                    posStep=ascending ? 1 : (-1),
                    matchLen=(null == matchSeq) ? 0 : matchSeq.length();
        if ((csLen <= startPos)
         || (startPos < 0)
         || (maxPos <= 0)
         || (len <= 0)
         || (matchLen <= 0))
            return (-1);

        for (int curPos=ascending ? startPos : maxPos - 1; (curPos >= startPos) && (curPos < maxPos) ; curPos += posStep)
        {
            final char    c=cs.charAt(curPos);
            for (int    mPos=0; mPos < matchLen; mPos++)
            {
                if (matchSeq.charAt(mPos) == c)
                    return curPos;
            }
        }

        return (-1);
    }

    public static final int anyIndexOf (final CharSequence    cs,
                                        final int             startPos,
                                        final int            len,
                                        final CharSequence    matchSeq)
    {
        return findIndexOf(cs, startPos, len, true, matchSeq);
    }

    public static final int anyIndexOf (final CharSequence    cs,
                                        final int             startPos,
                                        final CharSequence    matchSeq)
    {
        final int    csLen=(null == cs) ? 0 : cs.length();
        return anyIndexOf(cs, startPos, csLen - startPos, matchSeq);
    }

    public static final int anyIndexOf (final CharSequence    cs,
                                        final CharSequence    matchSeq)
    {
        return anyIndexOf(cs, 0, matchSeq);
    }

    public static final int anyLastIndexOf (final CharSequence    cs,
                                            final int             startPos,
                                            final int            len,
                                            final CharSequence    matchSeq)
    {
        return findIndexOf(cs, startPos, len, false, matchSeq);
    }

    public static final int anyLastIndexOf (final CharSequence    cs,
                                            final int             startPos,
                                            final CharSequence    matchSeq)
    {
        final int    csLen=(null == cs) ? 0 : cs.length();
        return anyLastIndexOf(cs, startPos, csLen - startPos, matchSeq);
    }

    public static final int anyLastIndexOf (final CharSequence    cs,
                                            final CharSequence    matchSeq)
    {
        return anyLastIndexOf(cs, 0, matchSeq);
    }

    public static final int anyIndexOf (final CharSequence    cs,
                                        final int             startPos,
                                        final int            len,
                                        final char ...        matchSeq)
    {
        if ((null == matchSeq) || (matchSeq.length <= 0))
            return (-1);
        return anyIndexOf(cs, startPos, len, CharBuffer.wrap(matchSeq));
    }

    public static final int anyIndexOf (final CharSequence    cs,
                                        final int             startPos,
                                        final char ...        matchSeq)
    {
        final int    csLen=(null == cs) ? 0 : cs.length();
        return anyIndexOf(cs, startPos, csLen - startPos, matchSeq);
    }

    public static final int anyIndexOf (final CharSequence    cs,
                                        final char ...    matchSeq)
    {
        return anyIndexOf(cs, 0, matchSeq);
    }

    public static final int anyLastIndexOf (final CharSequence    cs,
                                            final int             startPos,
                                            final int            len,
                                            final char ...        matchSeq)
    {
        if ((null == matchSeq) || (matchSeq.length <= 0))
            return (-1);
        return anyLastIndexOf(cs, startPos, len, CharBuffer.wrap(matchSeq));
    }

    public static final int anyLastIndexOf (final CharSequence    cs,
                                            final int             startPos,
                                            final char ...        matchSeq)
    {
        final int    csLen=(null == cs) ? 0 : cs.length();
        return anyLastIndexOf(cs, startPos, csLen - startPos, matchSeq);
    }

    public static final int anyLastIndexOf (final CharSequence    cs,
                                            final char ...        matchSeq)
    {
        return anyLastIndexOf(cs, 0, matchSeq);
    }
    /**
     * @param s1 First {@link CharSequence} to compare
     * @param s2 Second {@link CharSequence} to compare
     * @param caseSensitive <code>true</code> if comparison is to be made case-sensitive
     * @return The max. number of characters that are a common prefix of <U>both</U> input
     * values - zero if no match (or either sequence is null/empty)
     */
    public static final int getLongestPrefixMatchLength (final CharSequence s1, final CharSequence s2, final boolean caseSensitive)
    {
        final int    l1=(s1 == null) ? 0 : s1.length(),
                    l2=(s2 == null) ? 0 : s2.length();
        if ((l1 <= 0) || (l2 <= 0))
            return 0;

        final int    cmpLen=Math.min(l1, l2);
        for (int    cIndex=0; cIndex < cmpLen; cIndex++)
        {
            final char    c1=s1.charAt(cIndex), c2=s2.charAt(cIndex),
                        v1=caseSensitive ? c1 : Character.toLowerCase(c1),
                        v2=caseSensitive ? c2 : Character.toLowerCase(c2);
            if (v1 != v2)
                return cIndex;
        }

        return cmpLen;
    }
    /**
     * Checks if a value is quoted, and if so, then un-quotes it using {@link ObjectName#unquote(String)}
     * @param value The input {@link String} value - if <code>null</code> then nothing is done
     * @return The un-quoted result - same as input if already un-quoted
     * @throws IllegalArgumentException If the value is an &quot;imbalanced&quot;
     * quoted value - i.e., starts with a quote but does not end in one or
     * vice versa
     */
    public static String smartUnquoteObjectName (String value) throws IllegalArgumentException {
        int vLen=getSafeLength(value);
        if (vLen > 0) {
            char    startChar=value.charAt(0), endChar=value.charAt(vLen - 1);

            if ((startChar == '"') || (endChar == '"')) {
                if (vLen < 2)
                    throw new IllegalArgumentException("Imbalanced quotes[string too small]: " + value);
                if (startChar != '"')
                    throw new IllegalArgumentException("Imbalanced quotes[no start quote]: " + value);
                if (endChar != '"')
                    throw new IllegalArgumentException("Imbalanced quotes[no end quote]: " + value);

                return ObjectName.unquote(value);
            }
        }

        return value;   // no quotes
    }
    /**
     * Checks is a value is already quoted - if so, then does nothing, else
     * invokes {@link ObjectName#quote(String)} on it
     * @param value The input {@link String} value - if <code>null</code> then nothing is done
     * @return The quoted result - same as input if already quoted
     * @throws IllegalArgumentException If the value is an &quot;imbalanced&quot;
     * quoted value - i.e., starts with a quote but does not end in one or
     * vice versa
     */
    public static String smartQuoteObjectName (String value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }

        int vLen=value.length();
        if (vLen > 0) {
            char    startChar=value.charAt(0), endChar=value.charAt(vLen - 1);
            if ((startChar == '"') || (endChar == '"')) {
                if (vLen < 2)
                    throw new IllegalArgumentException("Imbalanced quotes[string too small]: " + value);
                if (startChar != '"')
                    throw new IllegalArgumentException("Imbalanced quotes[no start quote]: " + value);
                if (endChar != '"')
                    throw new IllegalArgumentException("Imbalanced quotes[no end quote]: " + value);

                return value;   // already quoted
            }
        }

        return ObjectName.quote(value);
    }
    /**
     * Execute {@link #chopHead(String, int)} and add ellipses to the <U>head</U>
     * of the string if it was chopped. The resulting string will never be &gt; maxLen
     * in length.
     * @param s The {@link String} to be chopped
     * @param maxLen The maximum allowed length
     * @return Chopped result or same as input if no chopping
     * @throws IllegalArgumentException if negative max length
     */
    public static String chopHeadAndEllipsify(String s, int maxLen) {
        if (maxLen < 0) {
            throw new IllegalArgumentException("chopHeadAndEllipsify(" + s + ")[" + maxLen + "] negative length N/A");
        }

        if (getSafeLength(s) <= maxLen) {
            return s;
        } else if (maxLen == 0) {
            return "";
        } else if (maxLen <= ELLIPSIS.length()) {
            return ELLIPSIS.substring(0, maxLen);
        } else {
            String  str=chopHead(s, maxLen - ELLIPSIS.length());
            return ELLIPSIS + str;
        }
    }

    /**
     * Chops the head from a given string if it exceeds the specified max.
     * length, such that its length does not exceed it.
     * @param str The {@link String} to be chopped
     * @param maxLen The maximum allowed length
     * @return Chopped result or same as input if no chopping
     * @throws IllegalArgumentException if negative max length
     */
    public static String chopHead(String str, int maxLen) throws IllegalArgumentException {
        if (maxLen < 0) {
            throw new IllegalArgumentException("chopHead(" + str + ")[" + maxLen + "] negative length N/A");
        }

        int strLen=getSafeLength(str);
        if (strLen <= maxLen) {
            return str; // OK if below or equal to max length
        } else if (maxLen == 0) {
            return "";
        } else {
            return str.substring(strLen - maxLen, strLen);
        }
    }
    /**
     * Chop the tailoff a string, such that its length does not exceed
     * maxChars.
     *
     * @param str String to chop
     * @param maxLen Maximum # that returnValue.length() should be
     * @return a new string, with or without a tail.
     * @throws IllegalArgumentException if negative max length
     */
    public static String chopTail(String str, int maxLen) {
        if (maxLen < 0) {
            throw new IllegalArgumentException("chopTail(" + str + ")[" + maxLen + "] negative length N/A");
        }

        int strLen=getSafeLength(str);
        if (strLen <= maxLen) {
            return str; // OK if below or equal to max length
        } else if (maxLen == 0) {
            return "";
        } else {
            return str.substring(0, maxLen);
        }
    }

    public static final String ELLIPSIS = "...";
    /**
     * Execute {@link #chopTail(String, int)} and add ellipses to the tail of
     * the string if it was chopped. The resulting string will never be > maxLen
     * in length.
     * @param s The {@link String} to manipulate
     * @param maxLen The maximum allowed length
     * @return The updated string - same as input if below max. length
     */
    public static String chopTailAndEllipsify(String s, int maxLen) {
        if (maxLen < 0) {
            throw new IllegalArgumentException("chopTailAndEllipsify(" + s + ")[" + maxLen + "] negative length N/A");
        }

        if (getSafeLength(s) <= maxLen) {
            return s;
        } else if (maxLen == 0) {
            return "";
        } else if (maxLen <= ELLIPSIS.length()) {
            return ELLIPSIS.substring(0, maxLen);
        } else {
            String  str=chopTail(s, maxLen - ELLIPSIS.length());
            return str + ELLIPSIS;
        }
    }
    /**
     * @param obj The {@link Object} to be checked
     * @return <code>null</code> if the object is <code>null</code>,
     * {@link Object#toString()} otherwise. <B>Note:</B> if the input object
     * is already a {@link String} then it is simply returned.
     */
    public static String safeToString (Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return obj.toString();
        }
    }

    /**
     * @param str The {@link CharSequence} to check
     * @return <code>true</code> if <code>null</code> or {@link CharSequence#length()} non-positive
     */
    public static boolean isEmpty(CharSequence str){
        return (getSafeLength(str) <= 0);
    }
    /**
     * @param seq Input {@link CharSequence}
     * @return The {@link CharSequence#length()} or zero if <code>null</code>
     */
    public static int getSafeLength (CharSequence seq) {
        if (seq == null) {
            return 0;
        } else {
            return seq.length();
        }
    }
}
