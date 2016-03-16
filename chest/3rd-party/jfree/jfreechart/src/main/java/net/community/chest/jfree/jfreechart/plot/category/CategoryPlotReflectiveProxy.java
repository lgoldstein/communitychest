/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.category;

import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;

import org.jfree.chart.plot.CategoryPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Reflected {@link CategoryPlot} type
 * @author Lyor G.
 * @since Feb 5, 2009 3:05:31 PM
 */
public class CategoryPlotReflectiveProxy<P extends CategoryPlot> extends PlotReflectiveProxy<P> {
    protected CategoryPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public CategoryPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final CategoryPlotReflectiveProxy<CategoryPlot>    CATPLOT=
        new CategoryPlotReflectiveProxy<CategoryPlot>(CategoryPlot.class, true);
}
