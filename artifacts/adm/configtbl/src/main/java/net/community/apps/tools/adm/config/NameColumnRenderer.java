/*
 * 
 */
package net.community.apps.tools.adm.config;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 20, 2009 1:00:37 PM
 */
public class NameColumnRenderer extends DefaultTableCellRenderer {
	public NameColumnRenderer ()
	{
		super();
	}
	/*
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component	c=
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		return c;
	}
}
