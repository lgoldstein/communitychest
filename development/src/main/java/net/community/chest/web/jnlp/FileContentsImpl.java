/*
 * 
 */
package net.community.chest.web.jnlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URI;

import javax.jnlp.FileContents;
import javax.jnlp.JNLPRandomAccessFile;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 12:35:48 PM
 */
public class FileContentsImpl extends File implements FileContents {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7002840935747627543L;
	public FileContentsImpl (String pathname)
	{
		super(pathname);
	}

	public FileContentsImpl (URI uri)
	{
		super(uri);
	}

	public FileContentsImpl (String parent, String child)
	{
		super(parent, child);
	}

	public FileContentsImpl (File parent, String child)
	{
		super(parent, child);
	}
	/*
	 * @see javax.jnlp.FileContents#getInputStream()
	 */
	@Override
	public InputStream getInputStream () throws IOException
	{
		return new FileInputStream(this);
	}
	/*
	 * @see javax.jnlp.FileContents#getLength()
	 */
	@Override
	public long getLength () throws IOException
	{
		return length();
	}
	/*
	 * @see javax.jnlp.FileContents#getMaxLength()
	 */
	@Override
	public long getMaxLength () throws IOException
	{
		return length();
	}
	/*
	 * @see javax.jnlp.FileContents#getOutputStream(boolean)
	 */
	@Override
	public OutputStream getOutputStream (boolean overwrite) throws IOException
	{
		return new FileOutputStream(this, !overwrite);
	}
	/*
	 * @see javax.jnlp.FileContents#getRandomAccessFile(java.lang.String)
	 */
	@Override
	public JNLPRandomAccessFile getRandomAccessFile (String mode) throws IOException
	{
		return new JNLPRandomAccessFileImpl(this, mode);
	}
	/*
	 * @see javax.jnlp.FileContents#setMaxLength(long)
	 */
	@Override
	public long setMaxLength (long len) throws IOException
	{
		throw new StreamCorruptedException("setMaxLength(" + len + ")[" + getAbsolutePath() + "] N/A");
	}
}
