package net.community.chest.rrd4j.common.core.util;

import net.community.chest.util.compare.AbstractComparator;

import org.rrd4j.core.RrdDef;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The {@link RrdDef} generic type
 * @author Lyor G.
 * @since Jan 10, 2008 11:13:05 AM
 */
public abstract class AbstractRrdDefComparator<D extends RrdDef> extends AbstractComparator<D> {
	protected AbstractRrdDefComparator (Class<D> defClass, boolean reverseMatch) throws IllegalArgumentException
	{
		super(defClass, reverseMatch);
	}
}
