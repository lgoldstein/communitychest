package net.community.chest.io.output;

import java.io.IOException;
import java.io.Writer;

import net.community.chest.io.IOPositionTracker;

/**
 * Copyright 2007 as per GPLv2
 * 
 * @author Lyor G.
 * @since Jul 12, 2007 5:19:08 PM
 */
public class TrackingWriter extends WriterEmbedder implements IOPositionTracker {
	private long	_curPos	/* =0L */;
	/*
	 * @see net.community.chest.io.IOPositionTracker#getPos()
	 */
	@Override
	public long getPos ()
	{
		return _curPos;
	}

	public TrackingWriter (Writer outWriter, boolean realClosure)
	{
		super(outWriter, realClosure);
	}
	/*
	 * @see java.io.FilterWriter#write(char[], int, int)
	 */
	@Override
	public void write (char[] cbuf, int off, int len) throws IOException
	{
		super.write(cbuf, off, len);
		_curPos += len;
	}
	/*
	 * @see java.io.FilterWriter#write(int)
	 */
	@Override
	public void write (int c) throws IOException
	{
		super.write(c);
		_curPos++;
	}
}
