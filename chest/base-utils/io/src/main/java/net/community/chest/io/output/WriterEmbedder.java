package net.community.chest.io.output;

import java.io.Closeable;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.OptionallyCloseable;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Embeds an {@link Writer} while also implementing the
 * {@link OptionallyCloseable} interface
 * 
 * @author Lyor G.
 * @since Jun 14, 2007 8:07:27 AM
 */
public class WriterEmbedder extends FilterWriter
	implements OptionallyCloseable, IOAccessEmbedder<Writer> {
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
	public Writer getEmbeddedAccess ()
	{
		return this.out;
	}
	/*
	 * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.io.Closeable)
	 */
	@Override
	public void setEmbeddedAccess (Writer c) throws IOException
	{
		this.out = c;
	}

	protected WriterEmbedder (Writer outWriter, boolean realClosure)
	{
		super(outWriter);

		_realClosure = realClosure;
	}
	/*
	 * @see java.io.Writer#append(java.lang.CharSequence)
	 */
	@Override
	public Writer append (CharSequence csq) throws IOException
	{
		return append(csq, 0, csq.length());
	}
	/*
	 * @see java.io.Writer#write(char[])
	 */
	@Override
	public void write (char[] cbuf) throws IOException
	{
		write(cbuf, 0, cbuf.length);
	}
	/*
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write (String str) throws IOException
	{
		write(str, 0, str.length());
	}
	/*
	 * @see java.io.FilterWriter#close()
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
