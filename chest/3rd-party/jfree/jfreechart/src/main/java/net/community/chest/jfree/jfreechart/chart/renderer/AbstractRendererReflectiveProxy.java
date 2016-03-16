/*
 *
 */
package net.community.chest.jfree.jfreechart.chart.renderer;

import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;

import org.jfree.chart.renderer.AbstractRenderer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <R> Type of {@link AbstractRenderer} being reflected
 * @author Lyor G.
 * @since Jun 8, 2009 12:02:13 PM
 */
public class AbstractRendererReflectiveProxy<R extends AbstractRenderer> extends ChartReflectiveAttributesProxy<R> {
    protected AbstractRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

}
