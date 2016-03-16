/*
 *
 */
package net.community.chest.jfree.jfreechart.axis;

import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;

import org.jfree.chart.axis.AxisSpace;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link AxisSpace} type
 * @author Lyor G.
 * @since Feb 8, 2009 10:00:04 AM
 */
public class AxisSpaceReflectiveProxy<A extends AxisSpace> extends ChartReflectiveAttributesProxy<A> {
    protected AxisSpaceReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public AxisSpaceReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final AxisSpaceReflectiveProxy<AxisSpace>    AXISSPACE=
            new AxisSpaceReflectiveProxy<AxisSpace>(AxisSpace.class, true);
}
