package net.community.chest.io.input;

import java.io.Closeable;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Embeds an {@link Reader} while also implementing the
 * {@link OptionallyCloseable} interface
 * 
  * @author Lyor G.
 * @since Jun 14, 2007 8:18:18 AM
 */
public class ReaderEmbedder extends FilterReader
		implements OptionallyCloseable, IOAccessEmbedder<Reader> {
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
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
	 */
	@Override
	public Reader getEmbeddedAccess ()
	{
		return this.in;
	}
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.io.Closeable)
	 */
	@Override
	public void setEmbeddedAccess (Reader c) throws IOException
	{
		this.in = c;
	}

	public ReaderEmbedder (Reader inReader, boolean realClosure)
	{
		super(inReader);

		_realClosure = realClosure;
	}
	
	public ReaderEmbedder (Reader inReader)
	{
		this(inReader, true);
	}
	/*
	 * @see java.io.Reader#read(char[])
	 */
	@Override
	public int read (char[] cbuf) throws IOException
	{
		return read(cbuf, 0, cbuf.length);
	}
	/*
	 * @see java.io.FilterReader#close()
	 */
	@Override
	public void close () throws IOException
	{
		final Closeable	s=getEmbeddedAccess();
		if (s != null)
		{
			try
			{
				if (isRealClosure())
					s.close();
			}
			finally
			{
				setEmbeddedAccess(null);
			}
		}
	}
}
