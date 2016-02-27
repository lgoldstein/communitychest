package net.community.chest.io.encode.qp;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.encode.DecodingException;
import net.community.chest.io.encode.OutputStreamDecoder;
import net.community.chest.io.output.OutputStreamEmbedder;

/**
 * Helper class that accept QP encoded data and writes it decoded to the underlying stream
 * @author lyorg
 * 04/03/2004
 */
public class QPDecodeOutputStream extends OutputStreamEmbedder implements OutputStreamDecoder {
	private int					_options;
	private final boolean		_throwExceptions;
	private DecodingException	_decExc	/* =null */;
	/*
	 * @see net.community.chest.io.encode.OutputStreamDecoder#getDecodeException()
	 */
	@Override
	public DecodingException getDecodeException ()
	{
		return _decExc;
	}
	/**
	 * Updates the last set decoding exception - provided one not already set
	 * @param exc exception to be set
	 * @throws DecodingException same as input if {@link #_throwExceptions} is TRUE
	 */
	protected void setDecodeException (final DecodingException exc) throws DecodingException 
	{
		if ((null == _decExc) && (exc != null) /* should not be otherwise */)
		{
			_decExc = exc;
			// no need to throw further exceptions after first one
			_options &= (~QuotedPrintable.DECOPT_THROW_EXCEPTION);
		}

		if (_throwExceptions)
			throw exc;
	}

	private byte[]	_qpVal=new byte[QuotedPrintable.QPENCLEN];
	private int		_qpLen /* =0 */;

	public QPDecodeOutputStream (OutputStream ost, int options, boolean realClose)
	{
		super(ost, realClose);
		_throwExceptions = (QuotedPrintable.DECOPT_THROW_EXCEPTION == (options & QuotedPrintable.DECOPT_THROW_EXCEPTION));
		// start with this option
		_options = options | QuotedPrintable.DECOPT_THROW_EXCEPTION;
	}

	public QPDecodeOutputStream (OutputStream ost, boolean realClose)
	{
		this(ost, QuotedPrintable.DECOPT_THROW_EXCEPTION, realClose);
	}
	/*
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (this.out != null)
		{
			try
			{
				super.close();

				if (_qpLen > 0)
					setDecodeException(new QuotedPrintableDecodingException("Incomplete QP value leftover (" + _qpLen + " chars", (char) _qpVal[0]));
			}
			finally
			{
				this.out = null;
			}
		}
	}
	/**
	 * @param hiChar QP hi character value
	 * @param loChar QP hi character value
	 * @throws DecodingException if bad/illegal decoding
	 * @throws IOException if unable to write decoded result to real output stream
	 */
	protected void decode (byte hiChar, byte loChar) throws IOException, DecodingException
	{
		try
		{
			// force exception so we can catch and cache it
			QuotedPrintable.decode((short) (hiChar & 0x00FF), (short) (loChar & 0x00FF), this.out, _options);
		}
		catch(QuotedPrintableDecodingException de)
		{
			setDecodeException(de);
		}
	}
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] buf, int offset, int len) throws IOException
	{
		if (null == this.out)
			throw new IOException("No underlying stream to write to");
		if ((len < 0) || (offset < 0))
			throw new IOException("Negative buffer range");
		if (0 == len)
			return;

		final int	maxOffset=(offset+len);
		if ((null == buf) || (maxOffset > buf.length))
			throw new IOException("Bad buffer range");

		int	lastOffset=offset;	// last offset to un-encoded data
		for (int curOffset=offset, remLen=len; (curOffset < maxOffset) && (remLen > 0); curOffset++, remLen--)
		{
			final byte	bVal=buf[curOffset];

			// check if in mid-accumulation of a QP value
			if (_qpLen > 0)
			{
				_qpVal[_qpLen] = bVal;
				_qpLen++;

				// check if accumulated enough data for a decoding
				if (_qpLen >= QuotedPrintable.QPENCLEN)
				{
					decode(_qpVal[1], _qpVal[2]);
					_qpLen = 0;
				}

				lastOffset = (curOffset + 1);
			}
			else if (QuotedPrintable.QPDELIM == bVal)
			{
				// "flush" any "clear" data
				if (curOffset > lastOffset)
					this.out.write(buf, lastOffset, (curOffset - lastOffset));

				// check if have enough data to decode on the spot without accumulating
				if (remLen >= QuotedPrintable.QPENCLEN)
				{
					decode(buf[curOffset+1], buf[curOffset+2]);
					curOffset += 2;
					remLen -= 2;
				}
				else	// accumulate
				{
					_qpVal[0] = bVal;
					_qpLen = 1;
				}

				lastOffset = (curOffset + 1);
			}
		}

		// check if any leftovers
		if (lastOffset < maxOffset)
			this.out.write(buf, lastOffset, maxOffset - lastOffset);
	}
	/*
	 * @see net.community.chest.io.OutputStreamEmbedder#write(int)
	 */
	@Override
	public void write (int val) throws IOException
	{
		write(new byte[] { (byte) val }, 0, 1);
	}
}