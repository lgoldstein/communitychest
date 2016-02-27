package net.community.chest.rrd4j.common.proxy;

import net.community.chest.reflect.FieldsAccessor;

import org.rrd4j.graph.RrdGraphDef;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The {@link RrdGraphDef} generic type
 * @author Lyor G.
 * @since Jan 22, 2008 8:52:15 AM
 */
public class RrdGraphDefFieldsAccessor<D extends RrdGraphDef> extends FieldsAccessor<D> {
	public RrdGraphDefFieldsAccessor (Class<D> valsClass)
	{
		super(valsClass, RrdGraphDef.class);
	}

	public static final RrdGraphDefFieldsAccessor<RrdGraphDef>	DEFAULT=new RrdGraphDefFieldsAccessor<RrdGraphDef>(RrdGraphDef.class);
}
