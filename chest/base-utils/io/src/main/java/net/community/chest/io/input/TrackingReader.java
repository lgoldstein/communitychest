package net.community.chest.io.input;

import java.io.IOException;
import java.io.Reader;

import net.community.chest.io.IOPositionTracker;

/**
 * Copyright 2007 as per GPLv2
 * 
 * @author Lyor G.
 * @since Jul 12, 2007 5:24:04 PM
 */
public class TrackingReader extends ReaderEmbedder implements IOPositionTracker {
	private long	_curPos	/* =0L */;
	/*
	 * @see net.community.chest.io.IOPositionTracker#getPos()
	 */
	@Override
	public long getPos ()
	{
		return _curPos;
	}

	public TrackingReader (Reader inReader, boolean realClosure)
	{
		super(inReader, realClosure);
	}

	/*
	 * @see java.io.FilterReader#read()
	 */
	@Override
	public int read () throws IOException
	{
		final int	ret=super.read();
		_curPos++;
		return ret;
	}
	/*
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	@Override
	public int read (char[] cbuf, int off, int len) throws IOException
	{
		final int	ret=super.read(cbuf, off, len);
		_curPos += ret;
		return ret;
	}
	/*
	 * @see java.io.FilterReader#reset()
	 */
	@Override
	public void reset () throws IOException
	{
		super.reset();
		_curPos = 0L;
	}
	/*
	 * @see java.io.FilterReader#skip(long)
	 */
	@Override
	public long skip (long n) throws IOException
	{
		final long	ret=super.skip(n);
		_curPos += ret;
		return ret;
	}
}
