/*
 * 
 */
package net.community.chest.io.input;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.nio.channels.Channel;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Provides a {@link Reader} interface over a {@link StringBuffer} in order
 * to allow re-using the same buffer that is provided by the {@link java.io.StringWriter}
 * without transforming it into a string</P>
 * 
 * @author Lyor G.
 * @since Aug 30, 2010 4:30:55 PM
 *
 */
public class StringBufferReader extends Reader implements Channel {
	private StringBuffer	_sb;
	public StringBuffer getStringBuffer ()
	{
		return _sb;
	}

	public void setStringBuffer (StringBuffer sb)
	{
		_sb = sb;
	}

	public StringBufferReader (StringBuffer sb)
	{
		_sb = sb;
	}
	
	public StringBufferReader ()
	{
		this(null);
	}

	private boolean 	_open=true;
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return _open;
	}

	protected StringBuffer ensureOpenAndReady () throws IOException
	{
		if (!isOpen())
			throw new IOException("Reader is closed");

		final StringBuffer	sb=getStringBuffer();
		if (null == sb)
			throw new StreamCorruptedException("No data buffer");
		
		return sb;
	}

	private int	_next;
	/*
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read (char[] cbuf, int off, int len) throws IOException
	{
		final StringBuffer	sb=ensureOpenAndReady();
		if (len <= 0)
			return len;

		final int	avail=sb.length() - _next;
		if (avail <= 0)
			return (-1);

		final int	maxRead=Math.min(len, avail);
		sb.getChars(_next, _next + maxRead, cbuf, off);
		_next += maxRead;
		return maxRead;
	}
	/*
	 * @see java.io.Reader#read()
	 */
	@Override
	public int read () throws IOException
	{
		final StringBuffer	sb=ensureOpenAndReady();
		final int			avail=sb.length() - _next;
		if (avail <= 0)
			return (-1);

		final int	v=sb.charAt(_next) & 0x00FFFF;
		_next++;
		return v;
	}
	/*
	 * @see java.io.Reader#skip(long)
	 */
	@Override
	public long skip (long n) throws IOException
	{
		final StringBuffer	sb=ensureOpenAndReady();
		if (n < 0L)
		{
			final long	absSkip=0 - n, avail=_next;
			if (absSkip >= avail)
			{
				_next = 0;
				return avail;
			}
			
			_next -= (int) absSkip;
			return n;
		}
		else if (n > 0L)
		{
			final int	avail=sb.length() - _next;
			if (n >= avail)
			{
				_next += avail;
				return avail;
			}

			_next += (int) n;
			return n;
		}

		return n;
	}
	/*
	 * @see java.io.Reader#ready()
	 */
	@Override
	public boolean ready () throws IOException
	{
		final StringBuffer	sb=ensureOpenAndReady();
		return (sb != null);
	}
	/*
	 * @see java.io.Reader#reset()
	 */
	@Override
	public void reset () throws IOException
	{
		final StringBuffer	sb=ensureOpenAndReady();
		if ((_next > 0) && (sb != null))
			_next = 0;
	}
	/*
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (isOpen())
			_open = false;
	}
}
