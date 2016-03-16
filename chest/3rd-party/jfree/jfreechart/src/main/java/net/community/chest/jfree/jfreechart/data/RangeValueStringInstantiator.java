/*
 *
 */
package net.community.chest.jfree.jfreechart.data;

import java.util.List;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;

import org.jfree.data.Range;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <R> The instantiated {@link Range} type
 * @author Lyor G.
 * @since Feb 9, 2009 11:48:56 AM
 */
public abstract class RangeValueStringInstantiator<R extends Range> extends AbstractXmlValueStringInstantiator<R> {
    protected RangeValueStringInstantiator (Class<R> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    public static final String toString (Range r)
    {
        if (null == r)
            return null;

        final double[]        da={ r.getLowerBound(), r.getUpperBound() };
        final StringBuilder    sb=new StringBuilder(da.length * NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM);
        for (final double d : da)
        {
            final String    ds=DoubleValueStringConstructor.DEFAULT.convertInstance(d);
            if (sb.length() > 0)
                sb.append(',');
            sb.append(ds);
        }

        return sb.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (R inst) throws Exception
    {
        return toString(inst);
    }

    public static final Range fromString (final String v)
    {
        final String        s=StringUtil.getCleanStringValue(v);
        final List<String>    dl=StringUtil.splitString(s, ',');
        final int            numVals=(null == dl) ? 0 : dl.size();
        if (numVals < 2)
        {
            if (numVals <= 0)
                return null;

            throw new IllegalArgumentException("fromString(" + s + ") incomplete specification");
        }

        final double[]    da=new double[numVals];
        for (int    dIndex=0; dIndex < numVals; dIndex++)
        {
            final String    ds=dl.get(dIndex);
            final double    dv=DoubleValueStringConstructor.DEFAULT.fromString(ds);
            if (Double.isInfinite(dv) || Double.isNaN(dv))
                throw new IllegalArgumentException("fromString(" + s + ") bad value: " + ds);

            da[dIndex] = dv;
        }

        return new Range(da[0], da[1]);
    }

    public static final RangeValueStringInstantiator<Range>    DEFAULT=
        new RangeValueStringInstantiator<Range>(Range.class) {
            /*
             * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
             */
            @Override
            public Range newInstance (String s) throws Exception
            {
                return fromString(s);
            }
    };
}
