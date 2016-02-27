/*
 * 
 */
package net.community.chest.web.servlet.framework;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.servlet.ServletOutputStream;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2010 11:47:23 AM
 */
public abstract class AbstractServletResponse implements XServletResponse {
	protected AbstractServletResponse ()
	{
		super();
	}
	/*
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	@Override
	public void resetBuffer ()
	{
		// do nothing
	}
	/*
	 * @see javax.servlet.ServletResponse#reset()
	 */
	@Override
	public void reset ()
	{
		resetBuffer();
	}
	/*
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	@Override
	public void flushBuffer () throws IOException
	{
		// do nothing
	}

	private int	_bufSize;
	/*
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	@Override
	public int getBufferSize ()
	{
		return _bufSize;
	}
	/*
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	@Override
	public void setBufferSize (int size)
	{
		if (_bufSize != size)
			_bufSize = size;	// debug breakpoint
	}

	private String	_charenc=Charset.defaultCharset().name();
	/*
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding ()
	{
		return _charenc;
	}
	/*
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding (String charset)
	{
		_charenc = charset;
	}

	private String	_ctType;
	/*
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	@Override
	public String getContentType ()
	{
		return _ctType;
	}
	/*
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType (String type)
	{
		_ctType = type;
	}

	private Locale	_locale=Locale.getDefault();
	/*
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	@Override
	public Locale getLocale ()
	{
		return _locale;
	}
	/*
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale (Locale loc)
	{
		_locale = loc;
	}

	private ServletOutputStream	_output;
	/*
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream () throws IOException
	{
		return _output;
	}

	private PrintWriter	_writer;
	/*
	 * @see net.community.chest.web.servlet.framework.XServletResponse#setOutputStream(javax.servlet.ServletOutputStream)
	 */
	@Override
	public void setOutputStream (ServletOutputStream value) throws IOException
	{
		_output = value;
		
		if (_writer != null)
		{
			try
			{
				_writer.close();
			}
			finally
			{
				_writer = null;
			}
		}
	}
	/*
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	@Override
	public PrintWriter getWriter () throws IOException
	{
		if (null == _writer)
		{
			final OutputStream	out=getOutputStream();
			if (null == out)
				return null;

			_writer = new PrintWriter(out, true);
		}

		return _writer;
	}

	private boolean	_commited;
	/*
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	@Override
	public boolean isCommitted ()
	{
		return _commited;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletResponse#setCommitted(boolean)
	 */
	@Override
	public void setCommitted (boolean value)
	{
		if (_commited != value)
			_commited = value;	// debug breakpoint
	}

	private int	_ctLength;
	/*
	 * @see net.community.chest.web.servlet.framework.XServletResponse#getContentLength()
	 */
	@Override
	public int getContentLength ()
	{
		return _ctLength;
	}
	/*
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	@Override
	public void setContentLength (int len)
	{
		if (_ctLength != len)
			_ctLength = len;	// debug breakpoint
	}
}
