/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.xy;

import org.jfree.chart.plot.CombinedDomainXYPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link CombinedDomainXYPlot} instance
 * @author Lyor G.
 * @since Feb 8, 2009 2:50:48 PM
 */
public class CombinedDomainXYPlotReflectiveProxy<P extends CombinedDomainXYPlot>
		extends XYPlotReflectiveProxy<P> {

	protected CombinedDomainXYPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public CombinedDomainXYPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final CombinedDomainXYPlotReflectiveProxy<CombinedDomainXYPlot>	COMBDOMXYPLOT=
		new CombinedDomainXYPlotReflectiveProxy<CombinedDomainXYPlot>(CombinedDomainXYPlot.class, true);
}
