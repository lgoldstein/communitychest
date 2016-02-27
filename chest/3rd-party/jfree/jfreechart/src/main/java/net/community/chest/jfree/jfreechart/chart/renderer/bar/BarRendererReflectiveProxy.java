/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart.renderer.bar;

import net.community.chest.jfree.jfreechart.chart.renderer.AbstractCategoryItemRendererReflectiveProxy;

import org.jfree.chart.renderer.category.BarRenderer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <R> The reflected {@link BarRenderer} type
 * @author Lyor G.
 * @since Jun 8, 2009 1:32:18 PM
 */
public class BarRendererReflectiveProxy<R extends BarRenderer> extends AbstractCategoryItemRendererReflectiveProxy<R> {
	protected BarRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	
	public BarRendererReflectiveProxy (Class<R> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final BarRendererReflectiveProxy<BarRenderer>	BAR=
		new BarRendererReflectiveProxy<BarRenderer>(BarRenderer.class, true);
}
