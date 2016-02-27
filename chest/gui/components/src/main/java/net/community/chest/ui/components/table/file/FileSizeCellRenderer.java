/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.awt.Component;
import java.io.File;

import javax.swing.JTable;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.lang.math.SizeUnits;
import net.community.chest.swing.HAlignmentValue;
import net.community.chest.ui.components.table.renderer.SizeCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 2:38:45 PM
 */
public class FileSizeCellRenderer extends SizeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7910317224873131604L;
	public FileSizeCellRenderer (SizeUnits u, boolean showSizeName, HAlignmentValue hv)
	{
		super(u, showSizeName, hv);
	}

	public FileSizeCellRenderer (SizeUnits u, boolean showSizeName)
	{
		this(u, showSizeName, HAlignmentValue.RIGHT);
	}

	public FileSizeCellRenderer ()
	{
		this(SizeUnits.B, false);
	}
	/*
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		final File	f;
		if (value instanceof File)
			f = (File) value; 
		else if (value instanceof String)
			f = new File(value.toString());
		else
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// folders have no size...
		final boolean	isDir=f.isDirectory();
		final Component c=
			super.getTableCellRendererComponent(table, isDir ? Long.valueOf(0L) : Long.valueOf(f.length()), isSelected, hasFocus, row, column);
		if (isDir && AttrUtils.isTextableComponent(c))
			AttrUtils.setComponentText(c, "");
		return c;
	}
}
