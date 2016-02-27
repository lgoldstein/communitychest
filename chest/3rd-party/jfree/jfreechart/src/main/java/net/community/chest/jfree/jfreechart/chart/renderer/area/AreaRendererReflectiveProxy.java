/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart.renderer.area;

import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AreaRenderer;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.jfree.jfreechart.chart.renderer.AbstractCategoryItemRendererReflectiveProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <R> Type of {@link AreaRenderer} being reflected
 * @author Lyor G.
 * @since Jun 8, 2009 1:48:00 PM
 */
public class AreaRendererReflectiveProxy<R extends AreaRenderer> extends AbstractCategoryItemRendererReflectiveProxy<R> {
	protected AreaRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public AreaRendererReflectiveProxy (Class<R> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if ((type != null) && AreaRendererEndType.class.isAssignableFrom(type))
			return (ValueStringInstantiator<C>) AreaRendererEndTypeValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public static final AreaRendererReflectiveProxy<AreaRenderer>	AREA=
		new AreaRendererReflectiveProxy<AreaRenderer>(AreaRenderer.class, true);
}
