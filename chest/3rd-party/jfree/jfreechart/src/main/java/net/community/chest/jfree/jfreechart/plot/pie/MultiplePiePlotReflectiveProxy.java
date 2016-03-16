/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;

import org.jfree.chart.plot.MultiplePiePlot;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <P> Type of {@link MultiplePiePlot} being reflected
 * @author Lyor G.
 * @since May 26, 2009 12:35:37 PM
 */
public class MultiplePiePlotReflectiveProxy<P extends MultiplePiePlot> extends PlotReflectiveProxy<P> {
    protected MultiplePiePlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public MultiplePiePlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final MultiplePiePlotReflectiveProxy<MultiplePiePlot>    MULTIPIE=
        new MultiplePiePlotReflectiveProxy<MultiplePiePlot>(MultiplePiePlot.class, true);
}
