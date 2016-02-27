/*
 * 
 */
package net.community.chest.jfree.jfreechart.block;

import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;

import org.jfree.chart.block.AbstractBlock;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link AbstractBlock} instance
 * @author Lyor G.
 * @since Jan 27, 2009 3:23:38 PM
 */
public class AbstractBlockReflectiveProxy<B extends AbstractBlock> extends ChartReflectiveAttributesProxy<B> {
	protected AbstractBlockReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public AbstractBlockReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final AbstractBlockReflectiveProxy<AbstractBlock>	ABSBLK=
		new AbstractBlockReflectiveProxy<AbstractBlock>(AbstractBlock.class, true);
}
