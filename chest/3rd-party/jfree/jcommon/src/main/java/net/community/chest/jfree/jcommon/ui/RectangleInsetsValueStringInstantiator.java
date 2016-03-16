/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.List;
import java.util.NoSuchElementException;

import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.jfree.jcommon.util.UnitTypeEnum;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> The instantiated {@link RectangleInsets} class
 * @author Lyor G.
 * @since Jan 27, 2009 4:18:02 PM
 */
public abstract class RectangleInsetsValueStringInstantiator<I extends RectangleInsets>
        extends AbstractXmlValueStringInstantiator<I> {
    protected RectangleInsetsValueStringInstantiator (Class<I> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    public static final String NO_INSETS_VALUE="none";
    public static final String toString (final RectangleInsets i) throws NoSuchElementException
    {
        if (null == i)
            return null;

        if (RectangleInsets.ZERO_INSETS.equals(i))
            return NO_INSETS_VALUE;

        final UnitType        ut=i.getUnitType();
        final UnitTypeEnum    et=UnitTypeEnum.fromUnitType(ut);
        if (null == et)
            throw new NoSuchElementException("toString(" + i + ") unknown unit type: " + ut);

        return et.toString()
            + "," + i.getTop()
            + "," + i.getLeft()
            + "," + i.getBottom()
            + "," + i.getRight();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (I inst) throws Exception
    {
        return toString(inst);
    }

    public static final RectangleInsets fromString (final String v)
        throws IllegalArgumentException, NoSuchElementException, NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        if (NO_INSETS_VALUE.equalsIgnoreCase(s))
            return RectangleInsets.ZERO_INSETS;

        final List<String>    cl=StringUtil.splitString(s, ',');
        if ((null == cl) || (cl.size() <= 4))
            throw new IllegalArgumentException("fromString(" + s + ") malformed value (missing components)");

        final String        tt=cl.get(0);
        final UnitTypeEnum    et=UnitTypeEnum.fromString(tt);
        if (null == et)
            throw new NoSuchElementException("fromString(" + s + ") unknown " + UnitType.class.getSimpleName() + ": " + tt);

        final double[]    da=new double[4];
        for (int    dIndex=0; dIndex < da.length; dIndex++)
        {
            final String    dv=cl.get(dIndex+1);
            da[dIndex] = DoubleValueStringConstructor.DEFAULT.fromString(dv);
        }

        return new RectangleInsets(et.getUnitType(), da[0], da[1], da[2], da[3]);
    }

    public static final RectangleInsetsValueStringInstantiator<RectangleInsets>    DEFAULT=
        new RectangleInsetsValueStringInstantiator<RectangleInsets>(RectangleInsets.class) {
            /*
             * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
             */
            @Override
            public RectangleInsets newInstance (String s) throws Exception
            {
                return fromString(s);
            }
        };
}
