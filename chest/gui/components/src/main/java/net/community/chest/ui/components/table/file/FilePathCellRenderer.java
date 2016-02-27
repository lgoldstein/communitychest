/*
 * 
 */
package net.community.chest.ui.components.table.file;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Displays the file <U>path</U> + other properties inherited from 
 * {@link AbstractFileDisplayNameCellRenderer}</P>
 * 
 * @author Lyor G.
 * @since Apr 30, 2009 12:42:12 PM
 */
public class FilePathCellRenderer extends AbstractFileDisplayNameCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5919554655211922701L;
	public FilePathCellRenderer (FileSystemView v)
	{
		super(v);
	}

	public FilePathCellRenderer ()
	{
		this(FileSystemView.getFileSystemView());
	}
	/*
	 * @see net.community.chest.ui.components.table.file.AbstractFileDisplayNameCellRenderer#getFileDisplayText(javax.swing.filechooser.FileSystemView, java.io.File)
	 */
	@Override
	protected String getFileDisplayText (FileSystemView v, File f)
	{
		if (null == f)
			return null;
		return (null == v) ? f.getAbsolutePath() : v.getSystemDisplayName(f);
	}
}
