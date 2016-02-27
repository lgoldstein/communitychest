/*
 * 
 */
package net.community.chest.swing.component.filechooser;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Workaround the fact that {@link FileNameExtensionFilter} is
 * <code>final</code> so we cannot derive an {@link Element} based instance</P>
 * 
 * @author Lyor G.
 * @since Jan 28, 2009 2:13:17 PM
 */
public class FileNameExtensionFilterEmbedder extends FileFilterEmbedder<FileNameExtensionFilter> {
	public FileNameExtensionFilterEmbedder (FileNameExtensionFilter f)
	{
		super(FileNameExtensionFilter.class, f);
	}

	public FileNameExtensionFilterEmbedder ()
	{
		this((FileNameExtensionFilter) null);
	}
	
	public FileNameExtensionFilterEmbedder (Element elem) throws Exception
	{
		this((null == elem) ? null : FileNameExtensionFilterXmlValueInstantiator.DEFAULT.fromXml(elem));
	}
}
