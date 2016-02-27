/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.xy;

import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;

import org.jfree.chart.plot.XYPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link XYPlot} instance
 * @author Lyor G.
 * @since Feb 8, 2009 2:35:04 PM
 */
public class XYPlotReflectiveProxy<P extends XYPlot> extends PlotReflectiveProxy<P> {
	protected XYPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public XYPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final XYPlotReflectiveProxy<XYPlot>	XYPLOT=
				new XYPlotReflectiveProxy<XYPlot>(XYPlot.class, true);
}
