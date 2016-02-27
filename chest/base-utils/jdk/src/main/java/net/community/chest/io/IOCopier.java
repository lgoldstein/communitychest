package net.community.chest.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;

import net.community.chest.util.datetime.TimeUnits;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Contains mostly static functions that copy {@link InputStream}-s to
 * {@link OutputStream}-s and/or {@link Reader} to {@link Writer}-s
 * @author Lyor G.
 * @since Jul 12, 2007 5:06:29 PM
 */
public final class IOCopier {
	private IOCopier ()
	{
		// no instance
	}
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @param workBuf work buffer to be used to copy - attempt is made to maximize its use
	 * @param nOffset offset in work buffer where data can be stored while copying
	 * @param nLen size of data that can be used in the work buffer (starting at specified offset)
	 * @param copySize if >= 0 then maximum number of bytes to copy, otherwise, till EOF on input
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o, byte[] workBuf, int nOffset, int nLen, long copySize) throws IOException
	{
		if ((null == i) || (null == o) || (nLen <= 0) || (nOffset < 0) ||
		    (null == workBuf) || ((nOffset + nLen) > workBuf.length))
			throw new StreamCorruptedException("copyStreams() bad arguments");

		long	curSize=0L;
		for (int    rLen=0, nCurPos=nOffset; rLen != (-1); nCurPos = nOffset)
        {
			int	nRemLen=(copySize < 0L) ? nLen : (int) Math.min(nLen, Math.min(Integer.MAX_VALUE, (copySize - curSize)));
			if (nRemLen <= 0)
				break;

			// fill work buffer as much as possible from the input stream
            for (rLen=i.read(workBuf, nCurPos, nRemLen); (rLen != (-1)) && (nRemLen > 0); rLen=i.read(workBuf, nCurPos, nRemLen))
            {
                nRemLen -= rLen;
	            nCurPos += rLen;
	            curSize += rLen; 
            }

            o.write(workBuf, 0, nCurPos);
        }

		return curSize;
	}
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @param workBuf work buffer to be used to copy - attempt is made to maximize its use
	 * @param nOffset offset in work buffer where data can be stored while copying
	 * @param nLen size of data that can be used in the work buffer (starting at specified offset)
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o, byte[] workBuf, int nOffset, int nLen) throws IOException
	{
		return copyStreams(i, o, workBuf, nOffset, nLen, (-1L));
	}
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @param workBuf work buffer to be used to copy - attempt is made to maximize its use
	 * @param copySize if >= 0 then maximum number of bytes to copy, otherwise, till EOF on input
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o, byte[] workBuf, long copySize) throws IOException
	{
		return copyStreams(i, o, workBuf, 0, (null == workBuf) ? 0 : workBuf.length, copySize);
	}
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @param workBuf work buffer to be used to copy - attempt is made to maximize its use
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o, byte[] workBuf) throws IOException
	{
		return copyStreams(i, o, workBuf, (-1L));
	}
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @param nSize size of work buffer to be used to copy - attempt is made to maximize its use
	 * @param copySize if >= 0 then maximum number of bytes to copy, otherwise, till EOF on input
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o, int nSize, long copySize) throws IOException
	{
		if ((null == i) || (null == o) || (nSize <= 0))
			throw new StreamCorruptedException("copyStreams(size=" + + nSize + "/copy=" + copySize + ") bad parameters");

		return copyStreams(i, o, new byte[nSize], copySize);
	}
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @param nSize size of work buffer to be used to copy - attempt is made to maximize its use
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o, int nSize) throws IOException
	{
		return copyStreams(i, o, nSize, (-1L));
	}
    /**
     * Default work buffer size used for copying streams
     */
	public static final int DEFAULT_COPY_SIZE=4 * 1024;
	/**
	 * @param i {@link InputStream}
	 * @param o {@link OutputStream}
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyStreams (InputStream i, OutputStream o) throws IOException
	{
		return copyStreams(i, o, DEFAULT_COPY_SIZE);
	}
	/**
	 * Copies 2 {@link File}-s
	 * @param srcFile source file object
	 * @param dstFile destination file object - if destination folder does not
	 * exist it is created using {@link File#mkdirs()}
	 * @param cpySize Max. number of bytes to copy - if negative then till EOF
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	@SuppressWarnings("resource")
	public static final long copyFile (final File srcFile, final File dstFile, final long cpySize) throws IOException
	{
		if ((null == srcFile) || (null == dstFile))
			return (-1L);

		final File	dstFolder=dstFile.getParentFile();
		if ((!dstFolder.exists()) && (!dstFolder.mkdirs()))
			throw new IOException("Failed to created destination folder(s)");

		FileChannel	srcChannel=null, dstChannel=null;
		try
		{
			srcChannel = new FileInputStream(srcFile).getChannel();
			dstChannel = new FileOutputStream(dstFile).getChannel();

			// Copy file contents from source to destination
			final long	srcLen=srcFile.length(),
						copyLen=dstChannel.transferFrom(srcChannel, 0, (cpySize < 0L) ? srcLen : cpySize);
			if ((cpySize < 0L) && (copyLen != srcLen))	// make sure full copy
				return (-2L);

			return copyLen;
		}
		finally
		{
			FileUtil.closeAll(srcChannel, dstChannel);
		}

	}
	/**
	 * Copies 2 {@link File}-s
	 * @param srcFile source file object
	 * @param dstFile destination file object - if destination folder does not
	 * exist it is created using {@link File#mkdirs()}
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyFile (final File srcFile, final File dstFile) throws IOException
	{
		return copyFile(srcFile, dstFile, (-1L));
	}
	/**
	 * @param inFile Input {@link File} whose contents are to be copied
	 * @param out The {@link OutputStream} to which to copy
	 * @param copySize Max. number of bytes to copy - if negative then till EOF
	 * @return Number of copied bytes - negative if error
	 * @throws IOException If failed to access/read/write the data
	 */
	public static final long copyFromFile (File inFile, OutputStream out, long copySize) throws IOException
	{
		InputStream	in=null;
		try
		{
			in = new FileInputStream(inFile);

			return copyStreams(in, out, DEFAULT_COPY_SIZE, copySize);
		}
		finally
		{
			FileUtil.closeAll(in);
		}
	}
	/**
	 * @param inFile Input {@link File} whose contents are to be copied
	 * @param out The {@link OutputStream} to which to copy
	 * @return Number of copied bytes - negative if error
	 * @throws IOException If failed to access/read/write the data
	 */
	public static final long copyFromFile (File inFile, OutputStream out) throws IOException
	{
		return copyFromFile(inFile, out, (-1L));
	}
	/**
	 * @param in The {@link InputStream} to read from
	 * @param outFile The {@link File} to which to write the data - if parent
	 * folder does not exist it is created using {@link File#mkdirs()} call.
	 * @param copySize Max. number of bytes to copy - if negative, then till EOF
	 * @return Number of copied bytes - negative if error
	 * @throws IOException If failed to access/read/write the data
	 */
	public static final long copyToFile (InputStream in, File outFile, long copySize) throws IOException
	{
		if (null == outFile)
		   return (-1L);

		final File	dstFolder=outFile.getParentFile();
		if ((!dstFolder.exists()) && (!dstFolder.mkdirs()))
			throw new IOException("Failed to created destination folder(s)");

		OutputStream	out=null;
		try
		{
			out = new FileOutputStream(outFile);
			return copyStreams(in, out, DEFAULT_COPY_SIZE, copySize);
		}
		finally
		{
			FileUtil.closeAll(out);
		}
	}
	/**
	 * @param in The {@link InputStream} to read from
	 * @param outFile The {@link File} to which to write the data
	 * @return Number of copied bytes - negative if error
	 * @throws IOException If failed to access/read/write the data
	 */
	public static final long copyToFile (InputStream in, File outFile) throws IOException
	{
		return copyToFile(in, outFile, (-1L));
	}
	/**
	 * Copies files
	 * @param srcFile source file path
	 * @param dstFile destination file path
	 * @return number of copied bytes - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyFile (final String srcFile, final String dstFile) throws IOException
	{
		if ((null == srcFile) || (srcFile.length() <= 0) ||
			(null == dstFile) || (dstFile.length() <= 0))
			throw new FileNotFoundException("copyFile(" + srcFile + " => " + dstFile + ") bad arguments");

		return copyFile(new File(srcFile), new File(dstFile));
	}
	/**
	 * Copies everything found in the reader into the writer
	 * @param in {@link Reader}
	 * @param out {@link Writer}
	 * @param copyBuf temporary copy buffer (error if null)
	 * @param offset offset in copy buffer that can be used
	 * @param len number of characters that can be used for copying (error if <=0)
	 * @param copySize max. number of characters to copy - if <=0 then till EOF
	 * @return number of copied characters - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyReaderToWriter (Reader in, Writer out, char[] copyBuf, int offset, int len, long copySize) throws IOException
	{
		if ((null == in) || (null == out) ||
			(null == copyBuf) || (offset < 0) || (len <= 0) || ((offset + len) > copyBuf.length))
			return (-1L);

		long curCopy=0L;
		for ( ; ; )
		{
			int	reqRead=len;
			// check if reached copy size limit
			if (copySize > 0L)
			{
				if (curCopy >= copySize)
					break;

				// check if need to copy less than entire copy buffer
				final long	remCopy=copySize - curCopy;
				if (remCopy < len)
					reqRead = (int) remCopy;
			}

			final int	readLen=in.read(copyBuf, offset, reqRead);
			if (readLen < 0)
				break;	// stop if EOF

			if (readLen > 0)
			{
				out.write(copyBuf, offset, readLen);
				curCopy += readLen;
			}
		}

		// this point is successful on end of successful copy
		return curCopy;
	}
	/**
	 * Copies everything found in the reader into the writer
	 * @param in {@link Reader}
	 * @param out {@link Writer}
	 * @param copyBuf temporary copy buffer (error if null)
	 * @param offset offset in copy buffer that can be used
	 * @param len number of characters that can be used for copying (error if <=0)
	 * @return number of copied characters - negative if error
	 * @throws IOException 
	 */
	public static final long copyReaderToWriter (Reader in, Writer out, char[] copyBuf, int offset, int len) throws IOException
	{
		return copyReaderToWriter(in, out, copyBuf, offset, len, (-1L));
	}
	/**
	 * Copies everything found in the reader into the writer
	 * @param in {@link Reader}
	 * @param out {@link Writer}
	 * @param copyBuf temporary copy buffer (error if null or length <= 0)
	 * @return number of copied characters - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyReaderToWriter (Reader in, Writer out, char[] copyBuf) throws IOException
	{
		return copyReaderToWriter(in, out, copyBuf, 0, (null == copyBuf) ? 0 : copyBuf.length);
	}
	/**
	 * Copies everything found in the reader into the writer
	 * @param in {@link Reader}
	 * @param out {@link Writer}
	 * @param copySize temporary copy buffer size (error if <= 0)
	 * @param maxCopy maximum number of characters to copy - if <=0 then till EOF
	 * @return number of copied characters - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyReaderToWriter (Reader in, Writer out, int copySize, long maxCopy) throws IOException
	{
		if (copySize <= 0)
			throw new StreamCorruptedException("copyReaderToWriter(" + copySize + ") bad size");

		return copyReaderToWriter(in, out, new char[copySize], 0, copySize, maxCopy);
	}
	/**
	 * Copies everything found in the reader into the writer
	 * @param in {@link Reader}
	 * @param out {@link Writer}
	 * @param copySize temporary copy buffer size (error if <= 0)
	 * @return number of copied characters - negative if error (Note: if
	 * unsuccessful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyReaderToWriter (Reader in, Writer out, int copySize) throws IOException
	{
		if (copySize <= 0)
			throw new StreamCorruptedException("copyReaderToWriter(" + copySize + ") bad size");

		return copyReaderToWriter(in, out, new char[copySize]);
	}
	/**
	 * Copies everything found in the reader into the writer
	 * @param in {@link Reader}
	 * @param out {@link Writer}
	 * @return number of copied characters - negative if error (Note: if
	 * un-successful, some partial content may have been copied).
	 * @throws IOException if read/write error
	 */
	public static final long copyReaderToWriter (Reader in, Writer out) throws IOException
	{
		return copyReaderToWriter(in, out, DEFAULT_COPY_SIZE);
	}

	public static final int	DEFAULT_URL_TIMEOUT=(int) TimeUnits.SECOND.getMilisecondValue(30L);
	/**
	 * {@link InputStream} or {@link OutputStream} class
	 * @param url The {@link URL} to open - ignored if <code>null</code>
	 * @param maxTimeout Max. timeout (sec.) - ignored if non-positive
	 * @param forWrite TRUE=open an {@link OutputStream}, otherwise open
	 * an {@link InputStream}
	 * @return The opened stream - <code>null</code> if no URL provided
	 * @throws IOException If failed to open the required stream
	 */
	public static final Closeable openURL (
			final URL url, final int maxTimeout, final boolean forWrite)
		throws IOException
	{
		if (null == url)
			return null;

		final URLConnection	conn=url.openConnection();
		if (maxTimeout > 0)
		{
			conn.setConnectTimeout(maxTimeout);
			conn.setReadTimeout(maxTimeout);
		}

		if (forWrite)
			return conn.getOutputStream();
		else
			return conn.getInputStream();
	}

	public static final Closeable openURL (final URI uri, final int maxTimeout, final boolean forWrite)
		throws IOException
	{
		if (null == uri)
			return null;
		
		return openURL(uri.toURL(), maxTimeout, forWrite);
	}

	public static final Closeable openURL (final String url, final int maxTimeout, final boolean forWrite)
		throws IOException, URISyntaxException
	{
		if ((null == url) || (url.length() <= 0))
			return null;

		return openURL(new URI(url), maxTimeout, forWrite);
	}

	public static final OutputStream openURLForWrite (final URL url, final int maxTimeout)
		throws IOException
	{
		return (OutputStream) openURL(url, maxTimeout, true);
	}

	public static final OutputStream openURLForWrite (final URI uri, final int maxTimeout)
		throws IOException
	{
		return openURLForWrite(uri.toURL(), maxTimeout);
	}

	public static final OutputStream openURLForWrite (final String url, final int maxTimeout)
		throws IOException, URISyntaxException
	{
		if ((null == url) || (url.length() <= 0))
			return null;
		
		return openURLForWrite(new URI(url), maxTimeout);
	}

	public static final InputStream openURLForRead (final URL url, final int maxTimeout)
		throws IOException
	{
		return (InputStream) openURL(url, maxTimeout, false);
	}

	public static final InputStream openURLForRead (final URI uri, final int maxTimeout)
		throws IOException
	{
		return openURLForRead(uri.toURL(), maxTimeout);
	}

	public static final InputStream openURLForRead (final String url, final int maxTimeout)
		throws IOException, URISyntaxException
	{
		if ((null == url) || (url.length() <= 0))
			return null;
		
		return openURLForRead(new URI(url), maxTimeout);
	}
	
	public static final long copyURLtoURL (
			final URL in, final URL out, final int maxTimeout,
			final byte[] workBuf, final int nOffset, final int nLen, final long copySize)
		throws IOException
	{
		InputStream		inStream=null;
		OutputStream	outStream=null;
		try
		{
			inStream = openURLForRead(in, maxTimeout);
			outStream = openURLForWrite(out, maxTimeout);
			return copyStreams(inStream, outStream, workBuf, nOffset, nLen, copySize);
		}
		finally
		{
			FileUtil.closeAll(inStream, outStream);
		}
	}

	public static final long copyCharacters (final StringBuilder sb, final int startIndex, final long cpySize,
											 final Appendable out, final char[] workBuf, final int startPos, final int bufLen)
		throws IOException
	{
		long	numCopied=0L, remSize=cpySize;
		for (int	cpyIndex=startIndex; numCopied < cpySize; )
		{
			final int	chunkSize=(int) Math.min(remSize, bufLen);
			sb.getChars(cpyIndex, cpyIndex + chunkSize, workBuf, startPos);
			out.append(CharBuffer.wrap(workBuf, startPos, chunkSize));
			cpyIndex += chunkSize;
			numCopied += chunkSize;
		}

		return numCopied;
	}

	public static final long copyCharacters (final StringBuilder sb, final Appendable out, final char[] workBuf)
			throws IOException
	{
		return copyCharacters(sb, 0, sb.length(), out, workBuf, 0, workBuf.length);
	}
	
	public static final long copyCharacters (final StringBuilder sb, final Appendable out) throws IOException
	{
		return copyCharacters(sb, out, new char[DEFAULT_COPY_SIZE]);
	}
}
