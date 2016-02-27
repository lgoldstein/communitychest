/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.io.File;

import net.community.chest.CoVariantReturn;
import net.community.chest.svn.ui.filesmgr.SVNLocalCopyFileManagerTable;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 2:11:31 PM
 */
public class WCLocalFilesManager extends SVNLocalCopyFileManagerTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4118546142268163958L;

	public WCLocalFilesManager (WCLocalFilesModel model)
	{
		super(model);
	}
	/*
	 * @see net.community.chest.ui.helpers.table.TypedTable#getTypedModel()
	 */
	@Override
	@CoVariantReturn
	public WCLocalFilesModel getTypedModel ()
	{
		return (WCLocalFilesModel) super.getTypedModel();
	}

	public File getParentFolder ()
	{
		final WCLocalFilesModel	m=getTypedModel();
		return (null == m) ? null : m.getParentFolder();
	}

	public void setParentFolder (File d)
	{
		final WCLocalFilesModel	m=getTypedModel();
		if (m != null)
			m.setParentFolder(d);
	}
}
