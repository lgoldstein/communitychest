/*
 * 
 */
package net.community.chest.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channel;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 26, 2009 7:32:12 AM
 */
public class CharSequenceReader extends Reader implements Channel {
	private CharSequence	_cs;
	public CharSequence getCharSequence ()
	{
		return _cs;
	}

	public void setCharSequence (CharSequence cs)
	{
		_cs = cs;
	}

	private int	_curPos	/* =0 */;
	public int getCurPos ()
	{
		return _curPos;
	}

	public void setCurPos (int curPos)
	{
		_curPos = curPos;
	}

	public CharSequenceReader (CharSequence cs)
	{
		_cs = cs;
	}
	
	public CharSequenceReader ()
	{
		this(null);
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return String.valueOf(getCharSequence());
	}

	private boolean	_closed;
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return !_closed;
	}

	public void setOpen (boolean f)
	{
		_closed = !f;
	}
	/*
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (isOpen())
			setOpen(false);
	}
	/*
	 * @see java.io.Reader#mark(int)
	 */
	@Override
	public void mark (int readAheadLimit) throws IOException
	{
		throw new IOException("mark(" + readAheadLimit + ") N/A");
	}
	/*
	 * @see java.io.Reader#markSupported()
	 */
	@Override
	public boolean markSupported ()
	{
		return false;
	}
	/*
	 * @see java.io.Reader#read()
	 */
	@Override
	public int read () throws IOException
	{
		if (!isOpen())
			throw new EOFException("read() not open");

		final int			curPos=getCurPos();
		final CharSequence	cs=getCharSequence();
		final int			csLen=(null == cs) ? 0 : cs.length();
		if (curPos >= csLen)
			return (-1);

		final char	ch=cs.charAt(curPos);
		setCurPos(curPos + 1);
		return ch;
	}
	/*
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read (char[] cbuf, int off, int len) throws IOException
	{
		if (!isOpen())
			throw new EOFException("read(len=" + len + ") not open");

		if (len <= 0)
			return len;

		int					curPos=getCurPos();
		final CharSequence	cs=getCharSequence();
		final int			csLen=(null == cs) ? 0 : cs.length(),
							avLen=csLen - curPos,
							cpLen=Math.min(avLen, len);
		if (cpLen <= 0)
			return (-1);

		for (int	cpOffset=off, cIndex=0; cIndex < cpLen; cpOffset++, curPos++, cIndex++)
			cbuf[cpOffset] = cs.charAt(curPos);

		setCurPos(curPos);
		return cpLen;
	}
	/*
	 * @see java.io.Reader#ready()
	 */
	@Override
	public boolean ready () throws IOException
	{
		if (!isOpen())
			throw new EOFException("ready() not open");
		return true;
	}
	/*
	 * @see java.io.Reader#reset()
	 */
	@Override
	public void reset () throws IOException
	{
		if (!isOpen())
			throw new EOFException("reset() not open");

		final int	curPos=getCurPos();
		if (curPos != 0)
			setCurPos(0);	// debug breakpoint
	}
	/*
	 * @see java.io.Reader#skip(long)
	 */
	@Override
	public long skip (long n) throws IOException
	{
		if (!isOpen())
			throw new EOFException("skip(" + n + ") not open");

		if (n <= 0L)
			return 0L;

		final int			curPos=getCurPos();
		final CharSequence	cs=getCharSequence();
		final int			csLen=(null == cs) ? 0 : cs.length(),
							avLen=csLen - curPos;
		final long			effSkip=Math.min(n, avLen);
		if (effSkip <= 0)
			return 0L;	// debug breakpoint

		setCurPos(curPos + (int) effSkip);
		return effSkip;
	}
}
