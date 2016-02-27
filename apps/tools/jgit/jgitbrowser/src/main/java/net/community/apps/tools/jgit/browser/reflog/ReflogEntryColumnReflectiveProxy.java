/*
 * 
 */
package net.community.apps.tools.jgit.browser.reflog;

import net.community.chest.ui.helpers.table.EnumTableColumnReflectiveProxy;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 11:58:06 AM
 */
public class ReflogEntryColumnReflectiveProxy extends EnumTableColumnReflectiveProxy<ReflogEntryColumns,ReflogEntryColInfo> {
	public ReflogEntryColumnReflectiveProxy ()
	{
		super(ReflogEntryColumns.class, ReflogEntryColInfo.class, true);
	}

	public static final ReflogEntryColumnReflectiveProxy	DEFAULT=new ReflogEntryColumnReflectiveProxy();
}
