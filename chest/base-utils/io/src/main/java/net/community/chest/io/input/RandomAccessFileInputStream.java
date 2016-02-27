/*
 * 
 */
package net.community.chest.io.input;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.IOPositionTracker;
import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Embeds a {@link RandomAccessFile} into an {@link InputStream}</P>
 * @author Lyor G.
 * @since Jan 1, 2009 2:04:02 PM
 */
public class RandomAccessFileInputStream extends InputStream implements
		OptionallyCloseable, IOAccessEmbedder<RandomAccessFile>, IOPositionTracker {
	/*
	 * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
	 */
	@Override
	public boolean isMutableRealClosure ()
	{
		return true;
	}

	private boolean	_realClosure;
	/*
	 * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
	 */
	@Override
	public boolean isRealClosure ()
	{
		return _realClosure;
	}
	/*
	 * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
	 */
	@Override
	public void setRealClosure (boolean enabled) throws UnsupportedOperationException
	{
		_realClosure = enabled;
	}

	private RandomAccessFile	_rf;
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
	 */
	@Override
	public RandomAccessFile getEmbeddedAccess ()
	{
		return _rf;
	}
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.lang.Object)
	 */
	@Override
	public void setEmbeddedAccess (RandomAccessFile c) throws IOException
	{
		_rf = c;
	}
	/**
	 * Start position to read from random access file - default=0
	 */
	private long	_startPos	/* =0L	*/;
	public long getStartOffset ()
	{
		return _startPos;
	}

	public void setStartOffset (long o)
	{
		_startPos = o;
	}
	/**
	 * Maximum number of bytes allowed to read - negative means no limit
	 */
	private long	_maxRead=Long.MIN_VALUE;
	public long getMaxReadValue ()
	{
		return _maxRead;
	}

	public void setMaxReadValue (long v)
	{
		_maxRead = v;
	}

	public RandomAccessFileInputStream (RandomAccessFile rf, long startPos, long maxRead, boolean realClose)
	{
		_rf = rf;
		_startPos = startPos;
		_maxRead = maxRead;
		_realClosure = realClose;
	}

	public RandomAccessFileInputStream (RandomAccessFile rf, long maxRead, boolean realClose)
	{
		this(rf, 0L, maxRead, realClose);
	}

	public RandomAccessFileInputStream (RandomAccessFile rf, boolean realClose)
	{
		this(rf, (-1L), realClose);
	}
	// NOTE: auto-closes the embedded accessor by default if set later
	public RandomAccessFileInputStream (RandomAccessFile rf)
	{
		this(rf, true);
	}
	/*
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark (int readlimit)
	{
		throw new UnsupportedOperationException("mark(" + readlimit + ") N/A");
	}
	/*
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported ()
	{
		return false;
	}

	private boolean	_closed	/* =false */;
	public boolean isClosed ()
	{
		return _closed;
	}
	/*
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (!isClosed())
		{
			try
			{
				if (isRealClosure())
				{
					Closeable	rf=getEmbeddedAccess();
					if (rf != null) {
					    rf.close();
					}
				}
			}
			finally
			{
				_closed = true;
			}
		}
	}

	private long	_curPos	/* =0L */;
	/*
	 * @see net.community.chest.io.IOPositionTracker#getPos()
	 */
	@Override
	public long getPos ()
	{
		return _curPos;
	}
	/*
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available () throws IOException
	{
		@SuppressWarnings("resource")
		final RandomAccessFile	rf=isClosed() ? null : getEmbeddedAccess();
		if (null == rf)
			throw new EOFException("No file available");

		final long	totalLen=Math.max(0L, rf.length()),
					startPos=Math.max(0L, getStartOffset()),
					curPos=Math.max(0L, getPos()),
					curOffset=curPos + startPos,
					remLen=Math.max(0L, totalLen - curOffset),
					maxLen=getMaxReadValue(),
					avLen;
		if (maxLen >= 0L)
		{
			final long	remAvail=Math.max(0L, maxLen - curPos); 
			avLen = Math.min(remAvail, remLen);
		}
		else
			avLen = remLen;

		if (avLen > Integer.MAX_VALUE)
			throw new StreamCorruptedException("Remaining size (" + avLen + ") too big");

		return (int) avLen;
	}
	/*
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read () throws IOException
	{
		@SuppressWarnings("resource")
		final RandomAccessFile	rf=isClosed() ? null : getEmbeddedAccess();
		if (null == rf)
			throw new EOFException("No file available");

		if (available() <= 0)
			return (-1);

		final int	v=rf.read();
		if (v != (-1))
			_curPos++;
		return v;
	}
	/*
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read (byte[] b, int off, int len) throws IOException
	{
		@SuppressWarnings("resource")
		final RandomAccessFile	rf=isClosed() ? null : getEmbeddedAccess();
		if (null == rf)
			throw new EOFException("No file available");

		final int	remLen=available();
		if (remLen <= 0L)
			return (-1);

		final int	rLen=Math.min(len, remLen);
		if (rLen <= 0)
			return rLen;

		final long	startPos=Math.max(0L, getStartOffset()),
					curPos=Math.max(0L, getPos()),
					curOffset=curPos + startPos;
		rf.seek(curOffset);	// makes sure we are at the right offset

		final int	eLen=rf.read(b, off, rLen);
		if (eLen > 0)
			_curPos += eLen;

		return eLen;
	}
	/*
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read (byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}
	/*
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset () throws IOException
	{
		@SuppressWarnings("resource")
		final RandomAccessFile	rf=isClosed() ? null : getEmbeddedAccess();
		if (null == rf)
			throw new EOFException("No file available");

		final long	startPos=Math.max(0L, getStartOffset());
		rf.seek(startPos);
		_curPos = 0L;
	}

	public void seek (long n) throws IOException
	{
		@SuppressWarnings("resource")
		final RandomAccessFile	rf=isClosed() ? null : getEmbeddedAccess();
		if (null == rf)
			throw new EOFException("No file available");

		if (n < 0L)
			throw new IOException("seek(" + n + ") negative values not allowed");

		rf.seek(n);
		_curPos = n;
	}
	/*
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip (long n) throws IOException
	{
		if (n < 0L)
			throw new IOException("skip(" + n + ") negative values not allowed");

		@SuppressWarnings("resource")
		final RandomAccessFile	rf=isClosed() ? null : getEmbeddedAccess();
		if (null == rf)
			throw new EOFException("No file available");

		if (0L == n)
			return 0L;

		final long	startPos=Math.max(0L, getStartOffset()),
					curPos=Math.max(0L, getPos()),
					curOffset=curPos + startPos,
					totalLen=Math.max(0L, rf.length()),
					maxLen=getMaxReadValue(),
					newOffset;
		if (maxLen >= 0)
		{
			final long	remAvail=Math.max(0L, maxLen - curPos),
						effSkip=Math.min(n, remAvail); 
			newOffset = Math.min(totalLen, curOffset + effSkip);
		}
		else
			newOffset = curOffset + n;

		rf.seek(newOffset);

		final long	actSkip=newOffset - curOffset;
		_curPos += actSkip;
		return actSkip;
	}
}
