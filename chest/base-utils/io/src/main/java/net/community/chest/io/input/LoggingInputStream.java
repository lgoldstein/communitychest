/*
 * 
 */
package net.community.chest.io.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>This {@link InputStream} &quot;logs&quot; whatever is read through it
 * via an {@link Appendable} instance (if not <code>null</code>)</P>
 * @author Lyor G.
 * @since Aug 2, 2011 2:30:25 PM
 */
public class LoggingInputStream extends FilterInputStream {
	private Appendable	_out;
	public Appendable getAppender ()
	{
		return _out;
	}

	public void setAppender (Appendable out)
	{
		_out = out;
	}

	public LoggingInputStream (Appendable out, InputStream inp)
	{
		super(inp);
		_out = out;
	}
	/*
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read () throws IOException
	{
		final int			result=super.read();
		final Appendable	out=getAppender();
		if ((result != (-1)) && (out != null))
			out.append((char) (result & 0x00FF));
		return result;
	}
	/*
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read (byte[] b, int off, int len) throws IOException
	{
		final int			result=super.read(b, off, len);
		final Appendable	out=getAppender();
		if ((result > 0) && (out != null))
			out.append(CharBuffer.wrap(convertToChars(b, off, result)));
		return result;
	}

	private char[]	_workBuf;
	protected char[] convertToChars (byte[] b, int off, int len)
	{
		final int	curLen=(_workBuf == null) ? 0 : _workBuf.length;
		if (curLen < len)
			_workBuf = new char[len + Byte.MAX_VALUE];
		for (int	curOffset=off, lIndex=0; lIndex < len; curOffset++, lIndex++)
			_workBuf[lIndex] = (char) (b[curOffset] & 0x00FF);
		return _workBuf;
	}
}
