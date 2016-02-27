/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import net.community.chest.jfree.jfreechart.axis.AxisReflectiveProxy;

import org.jfree.chart.axis.ValueAxis;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link ValueAxis} instance
 * @author Lyor G.
 * @since Feb 5, 2009 3:32:47 PM
 */
public abstract class ValueAxisReflectiveProxy<A extends ValueAxis> extends AxisReflectiveProxy<A> {
	protected ValueAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
}
