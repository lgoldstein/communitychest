/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.category;

import org.jfree.chart.plot.CombinedDomainCategoryPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link CombinedDomainCategoryPlot} type
 * @author Lyor G.
 * @since Feb 8, 2009 2:22:34 PM
 */
public class CombinedDomainCategoryPlotReflectiveProxy<P extends CombinedDomainCategoryPlot>
        extends CategoryPlotReflectiveProxy<P> {

    protected CombinedDomainCategoryPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public CombinedDomainCategoryPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final CombinedDomainCategoryPlotReflectiveProxy<CombinedDomainCategoryPlot>    COMBDOMAINCATPLOT=
        new CombinedDomainCategoryPlotReflectiveProxy<CombinedDomainCategoryPlot>(CombinedDomainCategoryPlot.class, true);
}
