/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.swing.component.table.BaseTableCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Displays file icon and other useful data using a {@link FileSystemView}
 * instance for it (default {@link FileSystemView#getFileSystemView()})</P>
 * 
 * @author Lyor G.
 * @since Aug 5, 2009 8:09:37 AM
 */
public abstract class AbstractFileDisplayNameCellRenderer extends BaseTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6728342996637319305L;
	private FileSystemView	_view;
	public FileSystemView getFileSystemView ()
	{
		return _view;
	}

	public void setFileSystemView (FileSystemView v)
	{
		_view = v;
	}

	protected AbstractFileDisplayNameCellRenderer (FileSystemView v)
	{
		_view = v;
	}

	protected AbstractFileDisplayNameCellRenderer ()
	{
		this(FileSystemView.getFileSystemView());
	}

	protected abstract String getFileDisplayText (FileSystemView v, File f);

	protected String setText (final Component c, final File f)
	{
		final FileSystemView	v=
			((null == f) || (!AttrUtils.isTextableComponent(c))) ? null : getFileSystemView();
		final String			t=getFileDisplayText(v, f);
		AttrUtils.setComponentText(c, t);
		return t;
	}

	protected Icon setIcon (final Component c, final File f)
	{
		final FileSystemView	v=
			((null == f) || (!AttrUtils.isIconableComponent(c))) ? null : getFileSystemView();
		final Icon				i=
			(null == v) ? null : v.getSystemIcon(f);
		AttrUtils.setComponentIcon(c, i);
		return i;
	}

	protected String setToolTipText (final Component c, final File f)
	{
		final FileSystemView	v=
			((null == f) || (!AttrUtils.isTooltipedComponent(c))) ? null : getFileSystemView();
		String					t=(v == null) ? null : v.getSystemTypeDescription(f);
		if ((t == null) || (t.length() <= 0))
			t = ((null == f) || (!AttrUtils.isTooltipedComponent(c))) ? null : f.getAbsolutePath();
		AttrUtils.setComponentToolTipText(c, t);

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
		setIcon(c, f);
		setToolTipText(c, f);
		return c;
	}
}
