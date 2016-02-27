/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.io.File;

import net.community.chest.svnkit.core.wc.SVNLocalFileNameComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 12:14:35 PM
 */
public class WCLocalFileNameComparator extends SVNLocalFileNameComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2629463284825878912L;
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

	public WCLocalFileNameComparator (WCLocalFilesModel m, boolean ascending)
	{
		super(ascending);
		_m = m;
	}
	/*
	 * @see net.community.chest.svn.core.wc.AbstractSVNLocalFileComparator#compareFiles(java.io.File, java.io.File)
	 */
	@Override
	public int compareFiles (File f1, File f2)
	{
		// parent comes before everything
		if (isParentFolder(f1))
		{
			if (isParentFolder(f2))
				return 0;

			return (-1);
		}
		else if (isParentFolder(f2))
			return (+1);

		return super.compareFiles(f1, f2);
	}

}
