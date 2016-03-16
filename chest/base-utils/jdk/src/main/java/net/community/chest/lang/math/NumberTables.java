package net.community.chest.lang.math;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.community.chest.util.datetime.Duration;
import net.community.chest.util.locale.LocalesMap;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful pre-calculated tables of numbers
 *
 * @author Lyor G.
 * @since Jun 28, 2007 2:28:32 PM
 */
public final class NumberTables {
    /**
     * Private constructor to disable instantiation
     */
    private NumberTables ()
    {
        // no instance
    }
    /**
     * Contains values that can be used to set/test bits in a byte - e.g.,
     * <code>setByteMaskBits[3] => 8</code> - i.e., 3rd bit set
     */
    public static final byte[]    setByteMaskBits=new byte[Byte.SIZE];
    static
    {
        setByteMaskBits[0] = 1;
        for (int    i=1; i < setByteMaskBits.length; i++)
            setByteMaskBits[i] = (byte) ((setByteMaskBits[i - 1] << 1) & 0x00FF);
    }
    /**
     * @param value The original value
     * @return A {@link List} of all the zero-based bit positions that are set
     * (<code>null</code>/empty) if not bits set
     */
    public static final List<Integer> getSetBitPositions (final byte value)
    {
        if (value == 0)
            return Collections.emptyList();

        final List<Integer>    bits=new ArrayList<Integer>(Byte.SIZE);
        for (int    bitPos=0; bitPos < setByteMaskBits.length; bitPos++)
        {
            if ((value & setByteMaskBits[bitPos]) != 0)
                bits.add(Integer.valueOf(bitPos));
        }

        return bits;
    }
    /**
     * Contains values that can be used to clear bits in a byte - e.g.,
     * <code>x & clearByteMaskBits[3]</code> clear the 3rd bit of <I>x</I>
     * leaving all other bits unchanged.
     */
    public static final byte[]    clearByteMaskBits=new byte[setByteMaskBits.length];
    static
    {
        for (int    i=0; i < clearByteMaskBits.length; i++)
            clearByteMaskBits[i] = (byte) ~setByteMaskBits[i];
    }
    /**
     * Contains values that can be used to set/test bits in an integer - e.g.,
     * 5 & {@link #setIntMaskBits}[2] => 110 & 100 => 100 => 4
     */
    public static final int[]    setIntMaskBits=new int[Integer.SIZE];
    static
    {
        setIntMaskBits[0] = 1;
        for (int    i=1; i < setIntMaskBits.length; i++)
            setIntMaskBits[i] = (setIntMaskBits[i - 1] << 1);
    }
    /**
     * @param value The original value
     * @return A {@link List} of all the zero-based bit positions that are set
     * (<code>null</code>/empty) if not bits set
     */
    public static final List<Integer> getSetBitPositions (final int value)
    {
        if (value == 0)
            return Collections.emptyList();

        final List<Integer>    bits=new ArrayList<Integer>(Integer.SIZE);
        for (int    bitPos=0; bitPos < setIntMaskBits.length; bitPos++)
        {
            if ((value & setIntMaskBits[bitPos]) != 0)
                bits.add(Integer.valueOf(bitPos));
        }

        return bits;
    }
    /**
     * Contains values that can be used to clear bits in an integer - e.g.,
     * 5 & clearIntMaskBits[2] => 110 & 011 => 010 => 2
     */
    public static final int[]    clearIntMaskBits=new int[setIntMaskBits.length];
    static
    {
        for (int    i=0; i < clearIntMaskBits.length; i++)
            clearIntMaskBits[i] = ~setIntMaskBits[i];
    }
    /**
     * Contains values that can be used to set/test bits in a long value - e.g.,
     * 5 & {@link #setLongMaskBits}[2] => 110 & 100 => 100 => 4
     */
    public static final long[]    setLongMaskBits=new long[Long.SIZE];
    static
    {
        setLongMaskBits[0] = 1L;
        for (int    i=1; i < setLongMaskBits.length; i++)
            setLongMaskBits[i] = (setLongMaskBits[i - 1] << 1);
    }
    /**
     * @param value The original value
     * @return A {@link List} of all the zero-based bit positions that are set
     * (<code>null</code>/empty) if not bits set
     */
    public static final List<Integer> getSetBitPositions (final long value)
    {
        if (value == 0L)
            return Collections.emptyList();

        final List<Integer>    bits=new ArrayList<Integer>(Long.SIZE);
        for (int    bitPos=0; bitPos < setByteMaskBits.length; bitPos++)
        {
            if ((value & setByteMaskBits[bitPos]) != 0L)
                bits.add(Integer.valueOf(bitPos));
        }

        return bits;
    }
    /**
     * Contains values that can be used to clear bits in a long - e.g.,
     * 5 & clearLongMaskBits[2] => 110 & 011 => 010 => 2
     */
    public static final long[]    clearLongMaskBits=new long[setLongMaskBits.length];
    static
    {
        for (int    i=0; i < clearLongMaskBits.length; i++)
            clearLongMaskBits[i] = ~setLongMaskBits[i];
    }
    /**
     * String of maximum unsigned int that can appear
     */
    public static final String    MAX_UNSIGNED_INT_DIGITSString=String.valueOf(Integer.MAX_VALUE);
    /**
     * Digits of maximum unsigned int that can appear in the string
     */
    public static final char[] MAX_UNSIGNED_INT_DIGITS=MAX_UNSIGNED_INT_DIGITSString.toCharArray();
    /**
     * Maximum number of digits an unsigned int may have
     */
    public static final int MAX_UNSIGNED_INT_DIGITS_NUM=MAX_UNSIGNED_INT_DIGITS.length;
    /**
     * Array of unsigned integer powers of 10 - the value at index N is the
     * Nth power of 10 - e.g.  <code>unsignedIntPowers10[2] => 100</code>
     */
    public static final int[] unsignedIntPowers10=new int[MAX_UNSIGNED_INT_DIGITS_NUM];
    static
    {
        unsignedIntPowers10[0] = 1;
        for (int    i=1; i < unsignedIntPowers10.length; i++)
            unsignedIntPowers10[i] = unsignedIntPowers10[i - 1] * 10;
    }
    /**
     * String of maximum an unsigned long
     */
    public static final String    MAX_UNSIGNED_LONG_DIGITSString=String.valueOf(Long.MAX_VALUE);
    /**
     * Digits of maximum an unsigned long
     */
    public static final char[] MAX_UNSIGNED_LONG_DIGITS=MAX_UNSIGNED_LONG_DIGITSString.toCharArray();
    /**
     * Maximum digits that can appear in an unsigned long
     */
    public static final int MAX_UNSIGNED_LONG_DIGITS_NUM=MAX_UNSIGNED_LONG_DIGITS.length;
    /**
     * Array of unsigned long powers of 10 - the value at index N is the
     * Nth power of 10 - e.g.  <code>unsignedLongPowers10[2] => 100</code>
     */
    public static final long[] unsignedLongPowers10=new long[MAX_UNSIGNED_LONG_DIGITS_NUM];
    static
    {
        unsignedLongPowers10[0] = 1L;
        for (int    i=1; i < unsignedLongPowers10.length; i++)
            unsignedLongPowers10[i] = unsignedLongPowers10[i - 1] * 10L;
    }
    /**
     * @param value value to be checked if integer or floating point
     * @return One of the following:</BR>
     * <UL>
     *         <LI>
     *         {@link Boolean#TRUE} if no floating point detected
     *         </LI>
     *
     *         <LI>
     *         {@link Boolean#FALSE} if floating point detected
     *         </LI>
     *
     *         <LI>
     *         <code>null<code> if format error (not a valid number or
     *         null/empty sequence)
     *         </LI>
     * </UL>
     */
    public static final Boolean checkNumericalValue (final CharSequence value)
    {
        final int    numChars=(null == value) ? 0 : value.length();
        if (numChars <= 0)
            return null;

        int    dotPos=-1;
        for (int    cIndex=0; cIndex < numChars; cIndex++)
        {
            final char    ch=value.charAt(cIndex);
            if ((ch >= '0') && (ch <= '9'))
                continue;

            switch(ch)
            {
                case '+'    :
                case '-'    :
                    if ((cIndex != 0)        // must be first character
                     || (1 == numChars))    // cannot be the only character
                        return null;
                    break;

                case '.'    :    // check floating point
                    if ((dotPos >= 0)    // not allowed more than one dot
                     || (cIndex < 1)    // dot must be preceded by at least one digit
                     || (cIndex >= (numChars - 1))) // dot must be followed by at least one digit
                        return null;

                    {
                        final char    p=value.charAt(cIndex - 1), n=value.charAt(cIndex + 1);
                        if ((p < '0') || (p > '9')    // preceding char must be a digit
                         || (n < '0') || (n > '9'))    // following char must be a digit
                            return null;
                    }

                    dotPos = cIndex;
                    break;

                default        :
                    return null;
            }
        }

        return Boolean.valueOf(dotPos < 0);
    }
    /**
     * @param nc A {@link Class} instance
     * @return <P>One of the following:</P></BR>
     * <UL>
     *         <LI>{@link Boolean#TRUE} if one of the integer types</LI>
     *         <LI>{@link Boolean#FALSE} if one of the floating point types</LI>
     *         <LI><code>null</code> otherwise (including <code>null</code> itself)</LI>
     * </UL>
     */
    public static final Boolean classifyNumberType (final Class<?> nc)
    {
        if (null == nc)
            return null;

        if (Long.class.isAssignableFrom(nc) || Long.TYPE.isAssignableFrom(nc)
         || Integer.class.isAssignableFrom(nc) || Integer.TYPE.isAssignableFrom(nc)
         || Short.class.isAssignableFrom(nc) || Short.TYPE.isAssignableFrom(nc)
         || Byte.class.isAssignableFrom(nc) || Byte.TYPE.isAssignableFrom(nc)
         || Duration.class.isAssignableFrom(nc))
            return Boolean.TRUE;

        if (Float.class.isAssignableFrom(nc) || Float.TYPE.isAssignableFrom(nc)
         || Double.class.isAssignableFrom(nc) || Double.TYPE.isAssignableFrom(nc))
            return Boolean.FALSE;

        return null;
    }
    /**
     * @param n A {@link Number} instance
     * @return <P>One of the following:</P></BR>
     * <UL>
     *         <LI>{@link Boolean#TRUE} if one of the integer types</LI>
     *         <LI>{@link Boolean#FALSE} if one of the floating point types</LI>
     *         <LI><code>null</code> otherwise (including <code>null</code> itself)</LI>
     * </UL>
     */
    public static final Boolean classifyNumberValue (final Number n)
    {
        if (null == n)
            return null;

        if ((n instanceof Long)
         || (n instanceof Integer)
         || (n instanceof Short)
         || (n instanceof Byte)
         || (n instanceof Duration))
            return Boolean.TRUE;

        if ((n instanceof Float)
         || (n instanceof Double))
            return Boolean.FALSE;

        return null;
    }
    /**
     * Caches the {@link DecimalFormatSymbols} for each requested {@link Locale}
     * used in call to {@link #getDecimalFormatSymbols(Locale)}
     */
    private static LocalesMap<DecimalFormatSymbols>    _dfsMap    /* =null */;
    public static final DecimalFormatSymbols getDecimalFormatSymbols (final Locale l)
    {
        final Locale ll=(null == l) ? null : Locale.getDefault();
        synchronized(NumberTables.class)
        {
            if (null == _dfsMap)
                _dfsMap = new LocalesMap<DecimalFormatSymbols>();
        }

        DecimalFormatSymbols    dfs=null;
        synchronized(_dfsMap)
        {
            if (null == (dfs=_dfsMap.get(ll)))
            {
                dfs = new DecimalFormatSymbols(ll);
                _dfsMap.put(ll, dfs);
            }
        }

        return dfs;
    }

    public static final int getLongValueHashCode (final long l)
    {
        if ((l > Integer.MIN_VALUE) && (l < Integer.MAX_VALUE))
            return (int) l;
        else
            return (int) (((l >> 32) & 0x00FFFFFFFFL) ^ (l & 0x00FFFFFFFFL));
    }

    public static final int getLongValueHashCode (final Long l)
    {
        return (null == l) ? 0 : getLongValueHashCode(l.longValue());
    }
}
