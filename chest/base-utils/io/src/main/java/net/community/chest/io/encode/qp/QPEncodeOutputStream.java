package net.community.chest.io.encode.qp;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.encode.hex.Hex;
import net.community.chest.io.output.OutputStreamEmbedder;

/**
 * Helper class - everything written to it is quoted-printable encoded
 * and then written to the real output stream
 * @author lyorg
 * 08/03/2004
 */
public class QPEncodeOutputStream extends OutputStreamEmbedder {
	private final int	_nOptions /* =0 */;
	/**
	 * Builds the QP encoding stream using specified options
	 * @param os real output stream to write result to
	 * @param nOptions encoding options
	 * @param realClose if TRUE then call to {@link #close()} also closes
	 * the underlying stream
	 * @see QuotedPrintable#ENCOPT_NOSPACE
	 * @see QuotedPrintable#ENCOPT_SEPARATORS
	 * @see QuotedPrintable#ENCOPT_SPACE_AS_UNDERLINE
	 * @see QuotedPrintable#ENCOPT_BREAKLINES
	 */
	public QPEncodeOutputStream (OutputStream os, int nOptions, boolean realClose)
	{
		super(os, realClose);
		_nOptions = nOptions;
	}
	/**
	 * Builds the QP encoding stream using {@link QuotedPrintable#ENCOPT_SEPARATORS}
	 * and {@link QuotedPrintable#ENCOPT_BREAKLINES} option as default
	 * @param os real output stream to write data to
	 * @param realClose if TRUE then call to {@link #close()} also closes
	 * the underlying stream
	 */
	public QPEncodeOutputStream (OutputStream os, boolean realClose)
	{
		this(os, QuotedPrintable.ENCOPT_SEPARATORS | QuotedPrintable.ENCOPT_BREAKLINES, realClose);
	}
	// tracks current line length so we can break it down
	private int	_curLength	/* =0 */;
	/**
	 * Q-P encoding soft break sequence
	 */
	private static final byte[]	SOFTBREAK={ (byte) QuotedPrintable.QPDELIM, (byte) '\r', (byte) '\n' };
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] buf, int offset, int len) throws IOException
	{
		if (null == this.out)
			throw new IOException("No underlying stream to write QP encoding to");
		if ((offset < 0) || (len < 0))
			throw new IOException("Bad/Illegal QP buffer to encode");
		if (0 == len)
			return;

		final int	maxOffset=offset+len;
		if ((null == buf) || (maxOffset > buf.length))
			throw new IOException("Bad/Illegal QP buffer limits to encode");

		int	lastOffset=0;
		for (int	curOffset=offset; curOffset < maxOffset; curOffset++)
		{
			final char		c=(char) (buf[curOffset] & 0x00FF);
			final boolean	qpEncIt=QuotedPrintable.requiresEncoding(c, _nOptions);
			if (qpEncIt)
			{
				// write any "clear" data so far
				if (lastOffset < curOffset)
					this.out.write(buf, lastOffset, (curOffset - lastOffset));

				this.out.write(QuotedPrintable.QPDELIM);
				Hex.write((byte) c, this.out, true);

				_curLength += 2;	// 2 more characters written in line
				lastOffset = curOffset + 1;	// mark position of next potential "clear" value
			}

			_curLength++;	// count processed character

			// check if need to break up long lines
			if ((_curLength >= QuotedPrintable.MAX_QPENC_LINE_LEN) && (QuotedPrintable.ENCOPT_BREAKLINES == (QuotedPrintable.ENCOPT_BREAKLINES & _nOptions)))
			{
				// if current character does not require encoding then include it in the "clear" data
				final int	extraLen=qpEncIt ? 0 : 1;
				// write any "clear" data so far
				if (lastOffset < curOffset)
					this.out.write(buf, lastOffset, extraLen + (curOffset - lastOffset));

				this.out.write(SOFTBREAK);

				_curLength = 0;	// re-start line length measurement
				// mark position of next potential "clear" value
				lastOffset = curOffset + extraLen;
			}
		}

		if (lastOffset < maxOffset)	// check if any un-encoded leftovers
			this.out.write(buf, lastOffset, (maxOffset - lastOffset));
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