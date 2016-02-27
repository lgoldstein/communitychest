/*
 * 
 */
package net.community.chest.svn.ui.filesmgr;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.awt.image.ImageUtils;
import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.svnkit.core.wc.SVNStatusTypeEnum;
import net.community.chest.ui.components.table.file.FileNameCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 11:28:05 AM
 */
public class SVNLocalCopyFileNameRenderer extends FileNameCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3997347297375953795L;
	private Map<SVNStatusTypeEnum,? extends Icon>	_statusIconMap;
	public Map<SVNStatusTypeEnum,? extends Icon> getStatusIconsMap ()
	{
		return _statusIconMap;
	}

	public void setStatusIconsMap (Map<SVNStatusTypeEnum,? extends Icon> m)
	{
		_statusIconMap = m;
	}

	public SVNLocalCopyFileNameRenderer (FileSystemView v, Map<SVNStatusTypeEnum,? extends Icon> m)
	{
		super(v);
		_statusIconMap = m;
	}

	public SVNLocalCopyFileNameRenderer (Map<SVNStatusTypeEnum,? extends Icon> m)
	{
		this(FileSystemView.getFileSystemView(), m);
	}

	public SVNLocalCopyFileNameRenderer (FileSystemView v)
	{
		this(v, null);
	}

	public SVNLocalCopyFileNameRenderer ()
	{
		this(FileSystemView.getFileSystemView());
	}
	/*
	 * @see net.community.chest.ui.components.table.file.AbstractFileDisplayNameCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		final Component	c;
		if (value instanceof SVNLocalCopyData)
		{
			final SVNLocalCopyData	ld=(SVNLocalCopyData) value;
			c = super.getTableCellRendererComponent(table, ld.getFile(), isSelected, hasFocus, row, column);

			final SVNStatusTypeEnum						st=
				AttrUtils.isIconableComponent(c) ? ld.getStatus() : null;
			final Map<SVNStatusTypeEnum,? extends Icon>	im=
				(null == st) ? null : getStatusIconsMap();
			final Icon									addIcon=
				((null == im) || (im.size() <= 0)) ? null : im.get(st);
			if (addIcon != null)
			{
				final Icon	orgIcon=
					AttrUtils.getComponentIcon(c),
							newIcon=
					ImageUtils.getOverlayIcon(orgIcon, addIcon, BorderLayoutPosition.AFTER_LAST_LINE, 75, 75, c);
				if (newIcon != orgIcon)
					AttrUtils.setComponentIcon(c, newIcon);
			}
		}
		else
			c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		return c;
	}
}
