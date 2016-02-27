package net.community.chest.util.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.Channel;

import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Uses a {@link LoggerWrapper} instance to output anything written to it.
 * It uses an internal buffer to capture a whole "line" (up to LF but not
 * including it) and then log it as a "message".</P>
 * 
 * @author Lyor G.
 * @since Oct 3, 2007 9:50:01 AM
 */
public class LoggerWrapperOutputStream extends OutputStream implements Channel {
	/**
	 * Current {@link LoggerWrapper} in use - if not set when writing to
	 * output stream then {@link IOException} will be generated
	 */
	private LoggerWrapper	_logger	/* =null */;
	public LoggerWrapper getLogger ()
	{
		return _logger;
	}

	public void setLogger (LoggerWrapper logger)
	{
		_logger = logger;
	}

	protected void flushBuffer (final byte[] buf, final int offset, final int bufLen) throws IOException
	{
		int	len=bufLen;
		// strip CR/LF at end of buffer (if any)
		if ((len > 0) && ((byte) '\n' == buf[len-1]))
			len--;
		if ((len > 0) && ((byte) '\r' == buf[len-1]))
			len--;
		if (len <= 0)	// ignore if no current data/empty line
			return;

		final String	cs=getCharset(),
						msg=((null == cs) || (cs.length() <= 0))
							? new String(buf, offset, len)
							: new String(buf, offset, len, cs)
							;

		final LoggerWrapper		l=getLogger();
		final LogLevelWrapper	lvl=getLevel();
		if ((null == l) || (null == lvl))
			throw new StreamCorruptedException("flushBuffer(" + getDisplayInfo() + ") missing logger/level for msg: " + msg);

		l.log(lvl, msg);
	}
	/**
	 * Current position in accumulating buffer where new data is to be written
	 */
	private int		_curPos	/* =0 */;
	/**
	 * Line data accumulating buffer 
	 */
	private byte[]	_buf=new byte[Byte.MAX_VALUE];
	protected byte[] growBuffer (int hint)
	{
		final int		gSize=Math.max(getGrowSize(), hint),
						newSize=_buf.length + gSize;
		final byte[]	newBuf=new byte[newSize];
		if (_curPos > 0)	// copy existing data
			System.arraycopy(_buf, 0, newBuf, 0, _curPos);

		_buf = newBuf;
		return newBuf;
	}

	protected void flushBuffer () throws IOException
	{
		if (_curPos > 0)	// just so we have a debug breakpoint
		{
			try
			{
				flushBuffer(_buf, 0, _curPos);
			}
			finally
			{
				_curPos = 0;	// re-start accumulation
			}
		}
	}
	/**
	 * Charset to be used to convert written bytes into characters for logging
	 * as a {@link String} - if null/empty then using platform specified default
	 */
	private String	_charset	/* =null */;
	public String getCharset ()
	{
		return _charset;
	}

	public void setCharset (String charset)
	{
		_charset = charset;
	}
	/**
	 * Log level to be used to generate the messages - if not set when writing to
	 * output stream then {@link IOException} will be generated
	 */
	private LogLevelWrapper	_level	/* =null */;
	public LogLevelWrapper getLevel ()
	{
		return _level;
	}

	public void setLevel (LogLevelWrapper level)
	{
		_level = level;
	}
	/**
	 * How much to grow the line data accumulating array if need to - if zero,
	 * then some best effort is done
	 */
	private int	_growSize	/* =0 */;
	public int getGrowSize ()
	{
		return _growSize;
	}

	public void setGrowSize (int growSize)
	{
		_growSize = growSize;
	}
	/**
	 * @return Useful information string summary
	 */
	public String getDisplayInfo ()
	{
		final LoggerWrapper	l=getLogger();
		final Class<?>		c=(null == l) ? null : l.getLoggingClass();
		return LoggerWrapper.class.getName()
			+ "[" + ((null == c) ? null : c.getName()) + "]"
			+ "@" + getLevel()
			+ "/" + getGrowSize()
			+ "{" + getCharset() + "}"
		;
	}

	private boolean	_open=true;
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return _open;
	}
	// CAVEAT: can re-open the stream even after being {@link #close()}-d
	public void setOpen (boolean flag)
	{
		_open = flag;
	}

	public LoggerWrapperOutputStream (LoggerWrapper logger, int growSize, String charset, LogLevelWrapper level)
	{
		_logger = logger;
		_growSize = growSize;
		_charset = charset;
		_level = level;
	}

	public LoggerWrapperOutputStream (Class<?> logClass, String clsIndex, int growSize, String charset, LogLevelWrapper level)
	{
		this((null == logClass) ? null : WrapperFactoryManager.getLogger(logClass, clsIndex), growSize, charset, level);
	}

	public LoggerWrapperOutputStream (LoggerWrapper logger, LogLevelWrapper level, int growSize)
	{
		this(logger, growSize, null, level);
	}

	public LoggerWrapperOutputStream (LoggerWrapper logger, LogLevelWrapper level)
	{
		this(logger, level, 0);
	}

	public LoggerWrapperOutputStream ()
	{
		this(null, null);
	}
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] b, int off, int len) throws IOException
	{
		if (!isOpen())
			throw new IOException("write(" + getDisplayInfo() + ") stream closed");

		final int	maxOffset=off + len;
		if (0 == len)
			return;

		int	curOffset=off;
		// check if have a partially accumulated line to complete
		if (_curPos > 0)
		{
			for ( ; curOffset < maxOffset; curOffset++, _curPos++)
			{
				final byte	bData=b[curOffset];
				if ((byte) '\n' == bData)
				{
					flushBuffer();
					curOffset++;	// skip LF
					break;
				}

				if ((null == _buf) || (_curPos >= _buf.length))
					growBuffer(Byte.MAX_VALUE);
				_buf[_curPos] = bData; 
			}
		}

		// echo lines directly from written buffer
		for (int	newOffset=curOffset; newOffset < maxOffset; newOffset++)
		{
			if ((byte) '\n' == b[newOffset])
			{
				final int	ll=newOffset - curOffset;
				flushBuffer(b, curOffset, ll);
				curOffset = newOffset + 1 /* skip the LF */;
			}
		}

		// check if have any "leftovers" - incomplete line
		if (curOffset < maxOffset)
		{
			final int	cpyLen=(maxOffset - curOffset),
						bufLen=(null == _buf) ? 0 : _buf.length;
			if (cpyLen >= bufLen)
				growBuffer(cpyLen - bufLen + 16);

			System.arraycopy(b, curOffset, _buf, 0, cpyLen);
			_curPos = cpyLen;
		}
	}
	/*
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write (byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}
	/*
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write (int b) throws IOException
	{
		if ('\n' == b)
		{
			flushBuffer();
			return;
		}

		if (_curPos >= _buf.length)
			growBuffer(Byte.MAX_VALUE);

		_buf[_curPos] = (byte) (b & 0x00FF);
		_curPos++;
	}
	/*
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		if (!isOpen())
			throw new IOException("flush(" + getDisplayInfo() + ") stream closed");
	}
	/*
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (isOpen())
		{
			try
			{
				flushBuffer();
			}
			finally
			{
				setOpen(false);	// just so we have a debug breakpoint
			}
		}
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getDisplayInfo();
	}
}
