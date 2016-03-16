/*
 *
 */
package net.community.chest.math;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 13, 2011 11:56:17 AM
 */
public enum RomanNumeral {
    // NOTE: order is important - DON'T CHANGE IT
    M(1000), CM(900), D(500), CD(400), C(100), XC(90), L(50), XL(40), X(10), IX(9), V(5), IV(4), I(1);

    private final int _value;
    public final int getValue ()
    {
        return _value;
    }

    private final char    _firstChar, _lastChar;
    private RomanNumeral (final int value)
    {
        final String    name=name();
        _value = value;
        _firstChar = name.charAt(0);
        _lastChar = (name.length() > 1) ? name.charAt(1) : '\0';
    }

    public static final List<RomanNumeral>    VALUES=
            Collections.unmodifiableList(Arrays.asList(values()));
    public static final RomanNumeral fromString (final CharSequence cs)
    {
        return fromString(cs, 0, (cs == null) ? 0 : cs.length());
    }

    public static final RomanNumeral fromString (final CharSequence cs, final int startPos, final int len)
    {
        if ((len <= 0) || (len > 2))
            return null;

        final char    ch1=cs.charAt(startPos),
                    ch2=(len > 1) ? cs.charAt(startPos + 1) : '\0';
        for (final RomanNumeral num : VALUES)
        {
            final String    name=num.name();
            if ((name.length() == len)
              && (ch1 == num._firstChar)
              && (ch2 == num._lastChar))
                return num;
        }

        return null;
    }

    public static final RomanNumeral fromChar (final char ch)
    {
        for (final RomanNumeral num : VALUES)
        {
            final String    name=num.name();
            if ((name.length() == 1) && (name.charAt(0) == ch))
                return num;
        }

        return null;
    }

    public static String toRoman (final int value) throws NumberFormatException
    {
        try
        {
            return appendRoman(new StringBuilder(), value).toString();
        }
        catch(IOException e)
        {
            throw new NumberFormatException(e.getMessage());
        }
    }

    public static final <A extends Appendable> A appendRoman (final A sb, final int value) throws IOException
    {
        if (value <= 0L)
            throw new StreamCorruptedException("appendRoman(" + value + ") N/A");

        int    remainder=value;
        for (final RomanNumeral numeral : VALUES)
        {
            final int        numValue=numeral.getValue();
            final String    name=numeral.name();
            while (remainder >= numValue)
            {
                sb.append(name);

                if ((remainder -= numValue) == 0)
                    return sb;
            }
        }

        /*
         * This point should never be reached since we expected the unit
         * numeral (I) to take care of the entire remainder
         */
        throw new StreamCorruptedException("appendRoman(" + value + ") unexpected exhaustion of all numerals");
    }

    public static final int fromRoman (final CharSequence cs) throws NumberFormatException
    {
        return fromRoman(cs, 0, (cs == null) ? 0 : cs.length());
    }

    public static final int fromRoman (final CharSequence cs, final int startPos, final int len) throws NumberFormatException
    {
        final int    maxPos=startPos + len;
        if (len <= 0)
            return 0;

        RomanNumeral    lastNumeral=RomanNumeral.M;    // start with topmost
        int                result=0;
        for (int    curPos=startPos; curPos < maxPos; curPos++)
        {
            final char        ch=cs.charAt(curPos);
            RomanNumeral    curNumeral=null;
            // these 3 can be used on left-hand side thus forming a 2-char value
            if ((('C' == ch) || ('X' == ch) || ('I' == ch))
             && (curPos < (maxPos - 1))
             &&    ((curNumeral=fromString(cs, curPos, 2)) != null))
                 curPos++;    // skip the 2nd character as well

            if (curNumeral == null)
                curNumeral = fromChar(ch);
            if (curNumeral == null)
                throw new NumberFormatException("fromRoman(" + cs + ") unknown digit: " + String.valueOf(ch));

            result += curNumeral.getValue();

            // make sure numerals appear in descending order
            if ((!curNumeral.equals(lastNumeral))
             && (lastNumeral.getValue() < curNumeral.getValue()))
                throw new NumberFormatException("fromRoman(" + cs + ") bad values order: prev=" + lastNumeral + "/cur=" + curNumeral);

            lastNumeral = curNumeral;
        }

        return result;
    }

    public static final Map<RomanNumeral,Integer> toNumeralsMap (final CharSequence cs)
    {
        return toNumeralsMap(cs, 0, (cs == null) ? 0 : cs.length());
    }

    public static final Map<RomanNumeral,Integer> toNumeralsMap (final CharSequence cs, final int startPos, final int len)
        throws NumberFormatException
    {
        if (len <= 0)
            return Collections.emptyMap();

        // go backward (from least-significant) and check if can compress
        RomanNumeral    lastNumeral=null;
        int                lastCount=0;
        final Map<RomanNumeral,Integer>    numsMap=new EnumMap<RomanNumeral,Integer>(RomanNumeral.class);
        for (int    csIndex=startPos + len -1; csIndex >= startPos; csIndex--)
        {
            final char        ch=cs.charAt(csIndex);
            RomanNumeral    curNumeral=null;
            if ((csIndex > 0)
             && ((curNumeral=RomanNumeral.fromString(cs, csIndex - 1, 2)) != null))
                csIndex--;
            if (curNumeral == null)
                curNumeral = RomanNumeral.fromChar(ch);
            if (curNumeral == null)
                throw new NumberFormatException("toNumeralsMap(" + cs + ") unknown numeral: " + String.valueOf(ch));

            if (lastNumeral == null)
                lastNumeral = curNumeral;
            if (lastNumeral.equals(curNumeral))
            {
                lastCount++;
                continue;
            }

            if (lastNumeral.getValue() > curNumeral.getValue())
                throw new NumberFormatException("toNumeralsMap(" + cs + ") bad values order: prev=" + lastNumeral + "/cur=" + curNumeral);

            if (lastCount > 0)
            {
                final Integer    prev=numsMap.put(lastNumeral, Integer.valueOf(lastCount));
                if (prev != null)
                    throw new NumberFormatException("toNumeralsMap(" + cs + ") multiple counts for numeral=" + lastNumeral);
            }

            lastNumeral = curNumeral;
            lastCount = 1;
        }

        if (lastCount > 0)
        {
            final Integer    prev=numsMap.put(lastNumeral, Integer.valueOf(lastCount));
            if (prev != null)
                throw new NumberFormatException("toNumeralsMap(" + cs + ") multiple final count for numeral=" + lastNumeral);
        }

        return numsMap;
    }

    public static final <A extends Appendable> A appendNumeralsMap (final A sb, final Map<RomanNumeral,? extends Number> numsMap)
        throws IOException
    {
        if ((numsMap == null) || numsMap.isEmpty())
            return sb;

        for (final RomanNumeral num : VALUES)
        {
            final Number    count=numsMap.get(num);
            if ((count == null) || (count.intValue() <= 0))
                continue;

            final String    name=num.name();
            for (int    cIndex=0; cIndex < count.intValue(); cIndex++)
                sb.append(name);
        }

        return sb;
    }

    public static final String fromNumeralsMap (final Map<RomanNumeral,? extends Number> numsMap)
    {
        if ((numsMap == null) || numsMap.isEmpty())
            return null;

        try
        {
            return appendNumeralsMap(new StringBuilder(), numsMap).toString();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final CharSequence minimizeRepresentation (final CharSequence cs) throws NumberFormatException
    {
        return minimizeRepresentation(cs, 0, (cs == null) ? 0 : cs.length());
    }

    public static final CharSequence minimizeRepresentation (final CharSequence cs, final int startPos, final int len)
        throws NumberFormatException
    {
        if (len <= 1)
            return cs;

        return toRoman(fromRoman(cs, startPos, len));
    }
}
