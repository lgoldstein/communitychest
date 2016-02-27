/*
 * 
 */
package net.community.chest.io.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channel;
import java.util.Formatter;
import java.util.Locale;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Accumulates all written data into a work buffer and calls the actual
 * writing method only when LF detected</P>
 * 
 * @author Lyor G.
 * @since Jun 30, 2009 12:37:20 PM
 */
public abstract class LinePrintStream extends AbstractPrintStream implements Channel, LineLevelAppender {
	protected LinePrintStream (File file, String csn)
		throws FileNotFoundException, UnsupportedEncodingException
	{
		super(file, csn);
	}

	protected LinePrintStream (File file) throws FileNotFoundException
	{
		super(file);
	}

	protected LinePrintStream (OutputStream o, boolean autoFlush, String encoding)
			throws UnsupportedEncodingException
	{
		super(o, autoFlush, encoding);
	}

	protected LinePrintStream (OutputStream o, boolean autoFlush)
	{
		super(o, autoFlush);
	}

	protected LinePrintStream (OutputStream o)
	{
		super(o);
	}

	protected LinePrintStream (String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException
	{
		super(fileName, csn);
	}

	protected LinePrintStream (String fileName) throws FileNotFoundException
	{
		super(fileName);
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
	 * @see java.io.PrintStream#append(java.lang.CharSequence, int, int)
	 */
	@Override
	public PrintStream append (CharSequence csq, int start, int end)
	{
		if (!isOpen())
		{
			setError();
			return this;
		}

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

			try
			{
				processAccumulatedMessage(sb);
			}
			catch(IOException e)
			{
				setError();
			}

			lOffset = cOffset + 1;	// skip current character
		}

		// check if have any leftovers
		if (lOffset < end)	// the leftover(s) have no line separator characters for sure
			sb.append(csq, lOffset, end);
		return this;
	}
	/*
	 * @see java.io.PrintStream#append(char)
	 */
	@Override
	public PrintStream append (char c)
	{
		if (!isOpen())
		{
			setError();
			return this;
		}

		if (!isWriteEnabled())
		{
			clearWorkBuffer();
			return this;
		}

		final StringBuilder	sb=getWorkBuffer(1);
		sb.append(c);

		if (c == '\n')
		{
			try
			{
				processAccumulatedMessage(sb);
			}
			catch(IOException e)
			{
				setError();
			}
		}

		return this;
	}
	/*
	 * @see java.io.PrintStream#append(java.lang.CharSequence)
	 */
	@Override
	public PrintStream append (CharSequence csq)
	{
		if (null == csq)
			return append("null");

		return append(csq, 0, csq.length());
	}
	/*
	 * @see net.community.chest.io.output.AbstractPrintStream#print(char[], int, int)
	 */
	@Override
	public void print (char[] cbuf, int off, int len)
	{
		if (!isOpen())
		{
			setError();
			return;
		}

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

			try
			{
				processAccumulatedMessage(sb);
			}
			catch(IOException e)
			{
				setError();
			}
			lOffset = cOffset + 1;	// skip current character
		}

		// check if have any leftovers
		final int	remLen=maxOffset - lOffset;
		if (remLen > 0)	// the leftover(s) have no line separator characters for sure
			sb.append(cbuf, lOffset, remLen);
	}

    private Formatter	_formatter;
    protected Formatter getFormatter ()
    {
    	return _formatter;
    }

    protected void setFormatter (Formatter f)
    {
    	_formatter = f;
    }
    /*
     * @see java.io.PrintStream#format(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    @Override
	public PrintStream format (Locale org, String format, Object ... args)
    {
    	final Locale	l=(null == org) ? null : Locale.getDefault();
    	Formatter		f=getFormatter();
    	if ((f == null) || (f.locale() != l))
    	{
    		f = new Formatter(this, l);
    		setFormatter(f);
    	}

    	f.format(l, format, args);
    	return this;
    }
	/*
	 * @see java.io.PrintStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte buf[], int off, int len)
	{
		if (!isOpen())
		{
			setError();
			return;
		}

		if (len <= 0)
			return;

		append(new String(buf, off, len));
	}
	/*
	 * @see java.io.PrintStream#flush()
	 */
	@Override
	public void flush ()
	{
		if (!isOpen())
			setError();
	}
	/*
	 * @see java.io.PrintStream#close()
	 */
	@Override
	public void close ()
	{
		if (isOpen())
			setOpen(false);
		super.close();
	}
}
