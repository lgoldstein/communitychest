/*
 * 
 */
package net.community.chest.io.output;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channel;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Accumulates all written data into a work buffer and calls the actual
 * writing method only when LF detected</P>
 * @author Lyor G.
 * @since Jun 30, 2009 12:15:01 PM
 */
public abstract class LineWriter extends Writer implements Channel, LineLevelAppender  {
	protected LineWriter ()
	{
		super();
	}

	private StringBuilder	_workBuf;
	/**
	 * Called in order to retrieve a work buffer. <B>Note:</B> the call occurs
	 * every time data is to be appended. It is up to the implementor to "reset"
	 * the work buffer instance after actual write takes place.
	 * @param reqSize Minimum size of requested buffer size - should be used
	 * in order to make a smart allocation
	 * @return The {@link StringBuilder} instance to be used as the work buffer.
	 * The accumulated line data is appended to it - except for the CR/LF. Once
	 * end of line is detected this instance is passed to actual write method.
	 * If <code>null</code> then same as write disabled.
	 */
	protected StringBuilder getWorkBuffer (final int reqSize)
	{
		final int	effSize=Math.max(reqSize, Byte.MAX_VALUE);
		if (null == _workBuf)
			_workBuf = new StringBuilder(effSize);
		else
			_workBuf.ensureCapacity(effSize);
		return _workBuf;
	}

	protected StringBuilder clearWorkBuffer ()
	{
		if ((_workBuf != null) && (_workBuf.length() > 0))
			_workBuf.setLength(0);

		return _workBuf;
	}

	protected void processAccumulatedMessage (final StringBuilder sb) throws IOException
	{
		int	dLen=(null == sb) ? 0 : sb.length();
		if (dLen <= 0)
			return;

		// check if data buffer ends in line separator pattern
		if (sb.charAt(dLen - 1) != '\n')
			return;

		if ((dLen > 1) && (sb.charAt(dLen - 2) == '\r'))
			dLen -= 2;
		else
			dLen--;

		writeLineData(sb, dLen);
	}

	private boolean	_closed	/* =false */;
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return !_closed;
	}

	public void setOpen (boolean s)
	{
		_closed = !s;
	}
	/*
	 * @see java.io.Writer#append(java.lang.CharSequence, int, int)
	 */
	@Override
	public Writer append (CharSequence csq, int start, int end)
			throws IOException
	{
		if (!isOpen())
			throw new IOException("append(" + csq.subSequence(start, end) + ") not open");

		if (!isWriteEnabled())
		{
			clearWorkBuffer();
			return this;
		}

		final int	wLen=end - start;
		if (wLen <= 0)
			return this;

		final StringBuilder	sb=getWorkBuffer(wLen);
		int					lOffset=start;
		for (int	cOffset=lOffset; cOffset < end; cOffset++)
		{
			final char	c=csq.charAt(cOffset);
			// if not part of the line separator then skip it
			if (c != '\n')
				continue;

			if (lOffset < cOffset)
				sb.append(csq, lOffset, cOffset + 1 /* including this character */);
			else
				sb.append(c);

			processAccumulatedMessage(sb);
			lOffset = cOffset + 1;	// skip current character
		}

		// check if have any leftovers
		if (lOffset < end)	// the leftover(s) have no line separator characters for sure
			sb.append(csq, lOffset, end);
		return this;
	}
	/*
	 * @see java.io.Writer#append(char)
	 */
	@Override
	public Writer append (final char c) throws IOException
	{
		if (!isOpen())
			throw new IOException("append(char=" + String.valueOf(c) + ") not open");

		if (!isWriteEnabled())
		{
			clearWorkBuffer();
			return this;
		}

		final StringBuilder	sb=getWorkBuffer(1);
		sb.append(c);

		if (c == '\n')
			processAccumulatedMessage(sb);

		return this;
	}
	/*
	 * @see java.io.Writer#append(java.lang.CharSequence)
	 */
	@Override
	public Writer append (final CharSequence csq) throws IOException
	{
		if (null == csq)
			return append("null");

		return append(csq, 0, csq.length());
	}
	/*
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write (char[] cbuf, int off, int len) throws IOException
	{
		if (!isOpen())
			throw new IOException("write(buf=" + String.valueOf(cbuf, off, len) + ") not open");

		if (len <= 0)
			return;

		if (!isWriteEnabled())
		{
			clearWorkBuffer();
			return;
		}

		final StringBuilder	sb=getWorkBuffer(len);
		int					lOffset=off, maxOffset=off + len;
		for (int	cOffset=lOffset; cOffset < maxOffset; cOffset++)
		{
			final char	c=cbuf[cOffset];
			// if not part of the line separator then skip it
			if (c != '\n')
				continue;

			final int	cLen=cOffset - lOffset;
			if (cLen > 0)
				sb.append(cbuf, lOffset, cLen + 1 /* including this character */);
			else
				sb.append(c);

			processAccumulatedMessage(sb);
			lOffset = cOffset + 1;	// skip current character
		}

		// check if have any leftovers
		final int	remLen=maxOffset - lOffset;
		if (remLen > 0)	// the leftover(s) have no line separator characters for sure
			sb.append(cbuf, lOffset, remLen);
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
	 * @see java.io.Writer#write(int)
	 */
	@Override
	public void write (int c) throws IOException
	{
		append((char) c);
	}
	/*
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	@Override
	public void write (String str, int off, int len) throws IOException
	{
		append(str, off, len);
	}
	/*
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write (String str) throws IOException
	{
		if (null == str)
			append("null");
		else
			append(str, 0, str.length());
	}
	/*
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (isOpen())
			setOpen(false);
	}
	/*
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		if (!isOpen())
			throw new IOException("flush() - not open");
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final CharSequence	o=getWorkBuffer(0);
		if ((null == o) || (o.length() <= 0))
			return "";
		else
			return o.toString();
	}
}
