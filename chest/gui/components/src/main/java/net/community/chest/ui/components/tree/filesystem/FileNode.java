/*
 * 
 */
package net.community.chest.ui.components.tree.filesystem;

import java.io.File;

import net.community.chest.ui.helpers.tree.TypedTreeNode;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 3, 2009 3:50:48 PM
 */
public class FileNode extends TypedTreeNode<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2464060911461994223L;

	public FileNode (File nodeObject, String nodeText, boolean withChildren)
	{
		super(File.class, nodeObject, nodeText, withChildren);
	}

	public FileNode (File nodeObject, boolean withChildren)
	{
		this(nodeObject, null, withChildren);
	}

	public FileNode (File nodeObject, String nodeText)
	{
		this(nodeObject, nodeText, true);
	}

	public FileNode (File nodeObject)
	{
		this(nodeObject, null);
	}

	public FileNode ()
	{
		this(null);
	}

	public File getFile ()
	{
		return getUserObject();
	}
}
