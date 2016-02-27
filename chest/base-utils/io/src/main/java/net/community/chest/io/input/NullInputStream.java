package net.community.chest.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.Channel;

/**
 * Copyright 2007 as per GPLv2
 * 
 * An {@link InputStream} that returns EOF on any {@link #read()} call
 * 
 * @author Lyor G.
 * @since Jul 12, 2007 4:16:05 PM
 */
public class NullInputStream extends InputStream implements Channel {
	public NullInputStream ()
	{
		super();
	}

	private boolean	_isClosed	/* =false */;
	public boolean isClosed ()
	{
		return _isClosed;
	}
	// CAVEAT EMPTOR - you might re-open it after being closed
	public void setClosed (boolean isClosed)
	{
		_isClosed = isClosed;
	}
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return !isClosed();
	}
	/*
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available () throws IOException
	{
		if (isClosed())
			throw new EOFException("Querying closed (empty) stream for available data");

		return 0;
	}
	/*
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (!isClosed())
			setClosed(true);
		super.close();
	}
	/*
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark (int pos)
	{
		throw new IllegalStateException("{Empty) stream mark(" + pos + ") called even though N/A");
	}
	/*
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported ()
	{
		return false;
	}
	/*
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read () throws IOException
	{
		if (isClosed())
			throw new EOFException("Reading from closed (empty) stream");

		return (-1);
	}
	/*
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read (byte[] b, int off, int len) throws IOException
	{
		if (isClosed())
			throw new EOFException("read(" + off + "-" + (off + len) + ") closed");
		if ((len < 0) || (off < 0) || ((off + len) > b.length))
			throw new StreamCorruptedException("read(" + off + "-" + (off + len) + ") bad buffer");

		return (-1);
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
	public synchronized void reset() throws IOException
	{
		if (isClosed())
			throw new EOFException("Resetting closed (empty) stream");
	}
	/*
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip (long skipLen) throws IOException
	{
		if (isClosed())
			throw new EOFException("Skipping closed (empty) stream");

		return skipLen;
	}
}
