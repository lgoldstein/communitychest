/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import org.jfree.chart.plot.RingPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Type of {@link RingPlot} being reflected
 * @author Lyor G.
 * @since Feb 1, 2009 3:41:20 PM
 */
public class RingPlotReflectiveProxy<P extends RingPlot> extends PiePlotReflectiveProxy<P> {
    protected RingPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public RingPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final RingPlotReflectiveProxy<RingPlot>    RINGPLOT=
            new RingPlotReflectiveProxy<RingPlot>(RingPlot.class, true);
}
