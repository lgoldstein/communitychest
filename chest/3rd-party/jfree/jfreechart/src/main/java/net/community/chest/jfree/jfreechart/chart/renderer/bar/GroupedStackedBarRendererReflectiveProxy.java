/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart.renderer.bar;

import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <R> Type of {@link GroupedStackedBarRenderer} being reflected
 * @author Lyor G.
 * @since Jun 8, 2009 1:45:20 PM
 */
public class GroupedStackedBarRendererReflectiveProxy<R extends GroupedStackedBarRenderer>
		extends StackedBarRendererReflectiveProxy<R> {
	protected GroupedStackedBarRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public GroupedStackedBarRendererReflectiveProxy (Class<R> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final GroupedStackedBarRendererReflectiveProxy<GroupedStackedBarRenderer>	GRPSTACKED=
		new GroupedStackedBarRendererReflectiveProxy<GroupedStackedBarRenderer>(GroupedStackedBarRenderer.class, true);
}
