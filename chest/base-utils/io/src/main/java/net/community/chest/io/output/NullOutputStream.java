package net.community.chest.io.output;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.Channel;

/**
 * Copyright 2007 as per GPLv2
 * 
 * An {@link OutputStream} that discards all data written to it
 * 
 * @author Lyor G.
 * @since Jul 12, 2007 4:14:24 PM
 */
public class NullOutputStream extends OutputStream implements Channel {
	public NullOutputStream ()
	{
		super();
	}

	private boolean	_isClosed	/* =false */;
	public boolean isClosed ()
	{
		return _isClosed;
	}
	// CAVEAT EMPTOR - you might re-open it after being closed...
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
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write (int b) throws IOException
	{
		if (isClosed())
			throw new EOFException("write(" + b + ") closed");
	}
	/*
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (!isClosed())
			setClosed(true);
		super.close();
	}
	/*
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		if (isClosed())
			throw new EOFException("flush() closed");
		super.flush();
	}
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] b, int off, int len) throws IOException
	{
		if (isClosed())
			throw new EOFException("write(" + off + "-" + (off + len) + ") closed");
		if ((len < 0) || (off < 0) || ((off + len) > b.length))
			throw new StreamCorruptedException("write(" + off + "-" + (off + len) + ") bad buffer");
	}
	/*
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write (byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}
}
