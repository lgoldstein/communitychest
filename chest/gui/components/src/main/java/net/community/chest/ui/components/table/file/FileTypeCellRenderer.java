/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.awt.Component;
import java.io.File;

import javax.swing.JTable;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.swing.component.table.BaseTableCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 2:35:53 PM
 */
public class FileTypeCellRenderer extends BaseTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4912373330820847945L;
	private FileSystemView	_view;
	public FileSystemView getFileSystemView ()
	{
		return _view;
	}

	public void setFileSystemView (FileSystemView v)
	{
		_view = v;
	}

	public FileTypeCellRenderer (FileSystemView v)
	{
		_view = v;
	}

	public FileTypeCellRenderer ()
	{
		this(FileSystemView.getFileSystemView());
	}

	protected String setText (final Component c, final File f)
	{
		final FileSystemView	v=
			((null == f) || (!AttrUtils.isTextableComponent(c))) ? null : getFileSystemView();
		final String			t=
			(null == v) ? null : v.getSystemTypeDescription(f);
		if ((t != null) && (t.length() > 0))
			AttrUtils.setComponentText(c, t);
		return t;
	}
	/*
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component	c=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final File		f;
		if (null == value)
			f = null;
		else if (value instanceof File)
			f = (File) value;
		else if (value instanceof String)
			f = new File(value.toString());
		else
			throw new IllegalArgumentException("getTableCellRendererComponent(" + row + "," + column + ")[" + value + "] unknown value type: " + ((null == value) ? null : value.getClass().getName()));

		setText(c, f);
		return c;
	}
}
