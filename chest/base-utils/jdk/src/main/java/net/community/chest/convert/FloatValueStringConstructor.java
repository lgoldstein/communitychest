package net.community.chest.convert;

import java.util.NoSuchElementException;

import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 1:20:32 PM
 */
public class FloatValueStringConstructor extends NonIntegerNumberValueStringConstructor<Float> {
    public FloatValueStringConstructor ()
    {
        super(Float.TYPE, Float.class);
    }

    public static final String convertSpecialInstance (final float n)
    {
        if (Float.isNaN(n))
            return NAN_VALUE;

        if (Float.isInfinite(n))
        {
            if (n == Float.POSITIVE_INFINITY)
                return POSITIVE_INFINITY_VALUE;
            else if (n == Float.NEGATIVE_INFINITY)
                return NEGATIVE_INFINITY_VALUE;
            else
                throw new NoSuchElementException("convertSpecialInstance(" + n + ") unknown infinity value");
        }

        return null;
    }

    public String convertInstance (final float n)
    {
        final String    v=convertSpecialInstance(n);
        if ((v != null) && (v.length() > 0))
            return v;

        return String.valueOf(n);
    }
    /*
     * @see net.community.chest.convert.NonIntegerNumberValueStringConstructor#convertSpecialInstance(java.lang.Number)
     */
    @Override
    public String convertSpecialInstance (final Float inst)
    {
        if (null == inst)
            return null;

        return convertSpecialInstance(inst.floatValue());
    }

    public static final Float    NAN_NUMBER=Float.valueOf(Float.NaN),
                                POSINF_NUMBER=Float.valueOf(Float.POSITIVE_INFINITY),
                                NEGINF_NUMBER=Float.valueOf(Float.NEGATIVE_INFINITY);
    public static final Float convertSpecialInstance (final String s)
    {
        if ((null == s) || (s.length() <= 0))
            return null;
        else if (NAN_VALUE.equalsIgnoreCase(s))
            return NAN_NUMBER;
        else if (POSITIVE_INFINITY_VALUE.equalsIgnoreCase(s))
            return POSINF_NUMBER;
        else if (NEGATIVE_INFINITY_VALUE.equalsIgnoreCase(s))
            return NEGINF_NUMBER;

        return null;
    }
    /*
     * @see net.community.chest.convert.NonIntegerNumberValueStringConstructor#newSpecialInstance(java.lang.String)
     */
    @Override
    public Float newSpecialInstance (final String v)
    {
        return convertSpecialInstance(StringUtil.getCleanStringValue(v));
    }

    public float fromString (final String v) throws RuntimeException
    {
        try
        {
            final String    s=StringUtil.getCleanStringValue(v);
            final Float        f=newInstance(s);
            if (null == f)
                throw new IllegalArgumentException("fromString(" + s + ") no value extracted");
            return f.floatValue();
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    public static final FloatValueStringConstructor    DEFAULT=new FloatValueStringConstructor();
}
