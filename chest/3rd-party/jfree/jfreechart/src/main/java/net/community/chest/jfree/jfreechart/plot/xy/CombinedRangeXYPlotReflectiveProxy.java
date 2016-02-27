/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.xy;

import org.jfree.chart.plot.CombinedRangeXYPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link CombinedRangeXYPlot} instance
 * @author Lyor G.
 * @since Feb 8, 2009 2:50:48 PM
 */
public class CombinedRangeXYPlotReflectiveProxy<P extends CombinedRangeXYPlot>
		extends XYPlotReflectiveProxy<P> {

	protected CombinedRangeXYPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public CombinedRangeXYPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final CombinedRangeXYPlotReflectiveProxy<CombinedRangeXYPlot>	COMBRNGXYPLOT=
		new CombinedRangeXYPlotReflectiveProxy<CombinedRangeXYPlot>(CombinedRangeXYPlot.class, true);
}
