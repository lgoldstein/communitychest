/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.category;

import org.jfree.chart.plot.CombinedRangeCategoryPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link CombinedRangeCategoryPlot} instance
 * @author Lyor G.
 * @since Feb 8, 2009 2:29:31 PM
 */
public class CombinedRangeCategoryPlotReflectiveProxy<P extends CombinedRangeCategoryPlot>
        extends CategoryPlotReflectiveProxy<P> {

    protected CombinedRangeCategoryPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public CombinedRangeCategoryPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final CombinedRangeCategoryPlotReflectiveProxy<CombinedRangeCategoryPlot>    COMBRNGCATPLOT=
        new CombinedRangeCategoryPlotReflectiveProxy<CombinedRangeCategoryPlot>(CombinedRangeCategoryPlot.class, true);
}
