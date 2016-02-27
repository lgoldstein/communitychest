package net.community.chest.rrd4j.common.graph;

import java.awt.Paint;

import net.community.chest.rrd4j.common.graph.helpers.AbstractLegendColorSource;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 16, 2008 2:33:55 PM
 */
public class StackExt extends AbstractLegendColorSource {
	public StackExt (String legend, Paint color, String srcName)
	{
		super(legend, color, srcName);
	}

	public StackExt ()
	{
		super();
	}

	public StackExt (String legend, Paint color)
	{
		this(legend, color, null);
	}

	public StackExt (String legend)
	{
		this(legend, null);
	}

	public StackExt (Element elem) throws Exception
	{
		super(elem);
	}
	/*
	 * @see net.community.chest.rrd4j.common.graph.helpers.AbstractLegend#getRootElementName()
	 */
	@Override
	public String getRootElementName ()
	{
		return RrdGraphDefExt.STACK_ATTR;
	}
}
