/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import org.jfree.chart.plot.PiePlot3D;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link PiePlot3D} type
 * @author Lyor G.
 * @since Feb 1, 2009 3:26:48 PM
 */
public class PiePlot3DReflectiveProxy<P extends PiePlot3D> extends PiePlotReflectiveProxy<P> {
	public PiePlot3DReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public PiePlot3DReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final PiePlot3DReflectiveProxy<PiePlot3D>	PIEPLOT3D=
			new PiePlot3DReflectiveProxy<PiePlot3D>(PiePlot3D.class, true);
}
