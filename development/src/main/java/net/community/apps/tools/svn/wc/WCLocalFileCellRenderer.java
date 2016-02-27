/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.awt.Component;
import java.io.File;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileNameRenderer;
import net.community.chest.svnkit.core.wc.SVNStatusTypeEnum;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 12:01:37 PM
 */
public class WCLocalFileCellRenderer extends SVNLocalCopyFileNameRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1232331671481189517L;
	private WCLocalFilesModel	_m;
	public WCLocalFilesModel getModel ()
	{
		return _m;
	}

	public void setModel (WCLocalFilesModel m)
	{
		_m = m;
	}

	public boolean isParentFolder (final File f)
	{
		final WCLocalFilesModel	m=(null == f) ? null : getModel();
		return (m != null) && m.isParentFolder(f);
	}

	public WCLocalFileCellRenderer (WCLocalFilesModel m, FileSystemView v, Map<SVNStatusTypeEnum,? extends Icon> i)
	{
		super(v, i);
		_m = m;
	}

	public WCLocalFileCellRenderer (WCLocalFilesModel m, Map<SVNStatusTypeEnum,? extends Icon> i)
	{
		this(m, FileSystemView.getFileSystemView(), i);
	}
	// TODO read from configuration
	public static final String	PARENT_FOLDER_TEXT="..";
	/*
	 * @see net.community.chest.ui.components.table.file.FileNameCellRenderer#getFileDisplayText(javax.swing.filechooser.FileSystemView, java.io.File)
	 */
	@Override
	protected String getFileDisplayText (FileSystemView v, File f)
	{
		if (isParentFolder(f))
			return PARENT_FOLDER_TEXT;

		return super.getFileDisplayText(v, f);
	}
	/*
	 * @see net.community.chest.ui.components.table.file.AbstractFileDisplayNameCellRenderer#setIcon(java.awt.Component, java.io.File)
	 */
	@Override
	protected Icon setIcon (Component c, File f)
	{
		if (AttrUtils.isIconableComponent(c) && isParentFolder(f))
		{
			// TODO set a special icon
			return super.setIcon(c, f);
		}

		return super.setIcon(c, f);
	}

}
