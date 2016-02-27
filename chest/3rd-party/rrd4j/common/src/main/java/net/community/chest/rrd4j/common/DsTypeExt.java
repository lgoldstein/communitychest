package net.community.chest.rrd4j.common;

import net.community.chest.dom.transform.DOMEnumExt;

import org.rrd4j.DsType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 12:31:36 PM
 */
public final class DsTypeExt extends DOMEnumExt<DsType> {
	private DsTypeExt ()
	{
		super(DsType.class);
	}

	public static final DsTypeExt	DEFAULT=new DsTypeExt();
}
