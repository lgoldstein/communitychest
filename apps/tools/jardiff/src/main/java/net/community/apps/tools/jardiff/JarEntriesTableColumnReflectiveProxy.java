/*
 * 
 */
package net.community.apps.tools.jardiff;

import net.community.chest.ui.helpers.table.EnumTableColumnReflectiveProxy;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 1:06:00 PM
 *
 */
public class JarEntriesTableColumnReflectiveProxy extends EnumTableColumnReflectiveProxy<JarEntriesTableColumns,JarEntriesTableColInfo> {
	public JarEntriesTableColumnReflectiveProxy ()
	{
		super(JarEntriesTableColumns.class, JarEntriesTableColInfo.class, true);
	}

	public static final JarEntriesTableColumnReflectiveProxy	DEFAULT=new JarEntriesTableColumnReflectiveProxy();
}
