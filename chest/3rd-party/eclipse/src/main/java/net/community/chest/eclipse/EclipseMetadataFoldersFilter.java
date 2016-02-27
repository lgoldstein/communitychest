/*
 * 
 */
package net.community.chest.eclipse;

import java.io.File;

import net.community.chest.io.filter.AbstractRootFolderFilesFilter;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Filters out the Eclipse workspace folder and any of its sub-folders</P>
 * 
 * @author Lyor G.
 * @since Aug 19, 2010 9:56:39 AM
 */
public class EclipseMetadataFoldersFilter extends AbstractRootFolderFilesFilter {
	/**
	 * Default sub-folder name used by Eclipse workspace on local file system
	 */
	public static final String	METADATA_SUBFOLDER_NAME=".metadata";
	public static final boolean isMetadataParentFolder (final File f)
	{
		final String	n=(null == f) ? null : f.getName();
		if ((f != null) && f.isDirectory() && METADATA_SUBFOLDER_NAME.equalsIgnoreCase(n))
			return true;	// debug breakpoint
		return false;
	}

	public EclipseMetadataFoldersFilter ()
	{
		super(false, METADATA_SUBFOLDER_NAME);
		setDescription(METADATA_SUBFOLDER_NAME);
	}

	public static final EclipseMetadataFoldersFilter	DEFAULT=new EclipseMetadataFoldersFilter();
}
