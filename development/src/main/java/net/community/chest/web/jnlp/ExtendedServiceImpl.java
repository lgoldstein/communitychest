/*
 * 
 */
package net.community.chest.web.jnlp;

import java.io.File;
import java.io.IOException;

import javax.jnlp.ExtendedService;
import javax.jnlp.FileContents;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 12:40:58 PM
 */
public class ExtendedServiceImpl implements ExtendedService {
	public ExtendedServiceImpl ()
	{
		super();
	}
	/*
	 * @see javax.jnlp.ExtendedService#openFile(java.io.File)
	 */
	@Override
	public FileContents openFile (File f) throws IOException
	{
		if (null == f)
			throw new IOException("openFile() no file specified");

		return new FileContentsEmbedder(f);
	}
	/*
	 * @see javax.jnlp.ExtendedService#openFiles(java.io.File[])
	 */
	@Override
	public FileContents[] openFiles (File[] fa) throws IOException
	{
		if ((null == fa) || (fa.length <= 0))
			return null;

		final FileContents[]	fc=new FileContents[fa.length];
		for (int	fIndex=0; fIndex < fa.length; fIndex++)
			fc[fIndex] = openFile(fa[fIndex]);

		return fc;
	}

}
