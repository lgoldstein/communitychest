/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import net.community.chest.jfree.jfreechart.chart.renderer.BaseGeneratorConverter;

import org.jfree.chart.labels.AbstractPieItemLabelGenerator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <V> The contained {@link AbstractPieItemLabelGenerator} type
 * @author Lyor G.
 * @since May 26, 2009 2:59:29 PM
 */
public abstract class AbstractPieItemLabelGeneratorConverter<V extends AbstractPieItemLabelGenerator>
		extends BaseGeneratorConverter<V> {
	protected AbstractPieItemLabelGeneratorConverter (Class<V> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}
}
