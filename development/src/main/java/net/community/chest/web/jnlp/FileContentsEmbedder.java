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

import javax.jnlp.FileContents;
import javax.jnlp.JNLPRandomAccessFile;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 12:27:32 PM
 */
public class FileContentsEmbedder implements FileContents, Cloneable {
	/**
	 * The real {@link File} used for the implementation
	 */
	private File	_file;
	public File getFile ()
	{
		return _file;
	}

	public void setFile (File file)
	{
		_file = file;
	}

	public FileContentsEmbedder (File f)
	{
		_file = f;
	}

	public FileContentsEmbedder ()
	{
		this(null);
	}
	/*
	 * @see javax.jnlp.FileContents#canRead()
	 */
	@Override
	public boolean canRead () throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("canRead() no file set");
		return f.canRead();
	}
	/*
	 * @see javax.jnlp.FileContents#canWrite()
	 */
	@Override
	public boolean canWrite () throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("canWrite() no file set");
		return f.canWrite();
	}
	/*
	 * @see javax.jnlp.FileContents#getInputStream()
	 */
	@Override
	public InputStream getInputStream () throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("getInputStream() no file set");
		return new FileInputStream(f);
	}
	/*
	 * @see javax.jnlp.FileContents#getLength()
	 */
	@Override
	public long getLength () throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("getLength() no file set");
		return f.length();
	}
	/*
	 * @see javax.jnlp.FileContents#getMaxLength()
	 */
	@Override
	public long getMaxLength () throws IOException
	{
		return getLength();
	}
	/*
	 * @see javax.jnlp.FileContents#getName()
	 */
	@Override
	public String getName () throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("getName() no file set");
		return f.getName();
	}
	/*
	 * @see javax.jnlp.FileContents#getOutputStream(boolean)
	 */
	@Override
	public OutputStream getOutputStream (boolean overwrite) throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("getOutputStream() no file set");
		return new FileOutputStream(f, !overwrite);
	}
	/*
	 * @see javax.jnlp.FileContents#getRandomAccessFile(java.lang.String)
	 */
	@Override
	public JNLPRandomAccessFile getRandomAccessFile (String mode) throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("getRandomAccessFile(" + mode + ") no file set");

		return new JNLPRandomAccessFileImpl(f, mode);
	}
	/*
	 * @see javax.jnlp.FileContents#setMaxLength(long)
	 */
	@Override
	public long setMaxLength (long len) throws IOException
	{
		final File	f=getFile();
		if (null == f)
			throw new IOException("setMaxLength(" + len + ") no file set");

		throw new StreamCorruptedException("setMaxLength(" + len + ")[" + f + "] N/A");
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public FileContentsEmbedder clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof FileContentsEmbedder))
			return false;
		if (this == obj)
			return true;

		final File	tf=getFile(), of=((FileContentsEmbedder) obj).getFile();
		return (0 == AbstractComparator.compareComparables(tf, of));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getFile());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final File	f=getFile();
		return (null == f) ? "" : f.toString();
	}
}
