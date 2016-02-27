package net.community.chest.jmx;

import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Assumes all XML(s) are relative to some root folder</P>
 * 
 * @author Lyor G.
 * @since Aug 19, 2007 12:16:17 PM
 */
public class FolderMBeanDescriptorAccessor extends XmlMBeanDescriptorAccessor {
	private String	_rootFolder	/* =null */;
	/**
	 * @return the root folder path under which all XML(s) are relative
	 */
	public String getRootFolder ()
	{
		return _rootFolder;
	}

	public void setRootFolder (String rootFolder)
	{
		_rootFolder = rootFolder;
	}

	public FolderMBeanDescriptorAccessor (String rootFolder)
	{
		_rootFolder = rootFolder;
	}

	public FolderMBeanDescriptorAccessor ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.jmx.XmlMBeanDescriptorAccessor#getDescriptorFilePath(java.lang.String)
	 */
	@Override
	public String getDescriptorFilePath (final String clsName) throws IOException
	{
		final String	rootFolder=getRootFolder();
		if ((null == rootFolder) || (rootFolder.length() <= 0))
			throw new StreamCorruptedException("getDescriptorFilePath(" + clsName + ") no root folder set");

		if ((null == clsName) || (clsName.length() <= 0))
			throw new IOException("getDescriptorFilePath(" + clsName + ") null/empty class name");

		final String	relPath=clsName.replace('.', File.separatorChar);
		return rootFolder + File.separator + relPath + ".xml";
	}
}
