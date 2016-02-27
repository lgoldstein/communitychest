/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart.renderer.bar;

import org.jfree.chart.renderer.category.StackedBarRenderer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <R> Type of {@link StackedBarRenderer} being reflected
 * @author Lyor G.
 * @since Jun 8, 2009 1:43:35 PM
 */
public class StackedBarRendererReflectiveProxy<R extends StackedBarRenderer>
		extends BarRendererReflectiveProxy<R> {
	protected StackedBarRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public StackedBarRendererReflectiveProxy (Class<R> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final StackedBarRendererReflectiveProxy<StackedBarRenderer>	STACKED=
		new StackedBarRendererReflectiveProxy<StackedBarRenderer>(StackedBarRenderer.class, true);
}
