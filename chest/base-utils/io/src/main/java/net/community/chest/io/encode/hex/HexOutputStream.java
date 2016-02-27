package net.community.chest.io.encode.hex;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.output.OutputStreamEmbedder;

/**
 * Helper class used to output any values written to it as hexadecimal 
 * @author lyorg
 * 03/06/2004
 */
public class HexOutputStream extends OutputStreamEmbedder {
	private final char	_chDelim	/* ='\0' */;
	/**
	 * @return Delimiter used to separate HEX values ('\0' means none)
	 */
	public final char getDelimiter ()
	{
		return _chDelim;
	}

	private long	_lWritten	/* =0L */;
	/**
	 * @return number of HEX values written to the {@link OutputStream}
	 */
	public long getNumWrittenValues ()
	{
		return _lWritten;
	}

	private final boolean	_useUppercase	/* =false */;
	/**
	 * @return TRUE then HEX digits above '9' are output as uppercase ('A', 'B'...), else lowercase
	 */
	public final boolean isUseUppercase ()
	{
		return _useUppercase;
	}
    /**
     * Creates an output stream that use the specified delimiter between HEX values
     * @param os output stream to write to
     * @param chDelim delimiter to be used between successive HEX value ('\0' == no delimiter)
     * @param useUppercase if TRUE then HEX digits above '9' are output as uppercase ('A', 'B'...), else lowercase
     * @param realClose if TRUE then closing this stream also closes the
     * underlying "real" stream
     * @throws IllegalArgumentException if null underlying stream
     */
	public HexOutputStream (OutputStream os, char chDelim, boolean useUppercase, boolean realClose) throws IllegalArgumentException
	{
		super(os, realClose);

		_chDelim = chDelim;
        _useUppercase = useUppercase;
	}
    /**
     * Creates an output stream that use the specified delimiter between HEX values
     * @param os output stream to write to
     * @param chDelim delimiter to be used between successive HEX value (0 == no delimiter)
     * @param useUppercase if TRUE then HEX digits above '9' are output as uppercase ('A', 'B'...), else lowercase
     * @throws IllegalArgumentException if null underlying stream
     */
	public HexOutputStream (OutputStream os, char chDelim, boolean useUppercase) throws IllegalArgumentException
	{
		this(os, chDelim, useUppercase, true);
	}
    /**
     * Creates an output stream where all written values are in HEX format
     * @param os output stream to write to - NOTE: values are delimited by ' ' as default
     * @param useUppercase if TRUE then HEX digits above '9' are output as uppercase ('A', 'B'...), else lowercase
     * @param realClose if TRUE then closing this stream also closes the
     * underlying "real" stream
     * @throws IllegalArgumentException if null underlying stream
     */
	public HexOutputStream (OutputStream os, boolean useUppercase, boolean realClose) throws IllegalArgumentException
	{
		this(os, ' ', useUppercase, realClose);
	}
    /**
     * Creates an output stream where all written values are in HEX format
     * @param os output stream to write to - NOTE: values are delimited by ' ' as default
     * @param useUppercase if TRUE then HEX digits above '9' are output as uppercase ('A', 'B'...), else lowercase
     */
	public HexOutputStream (OutputStream os, boolean useUppercase)
	{
		this(os, useUppercase, true);
	}
	/*
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write (int i) throws IOException
	{
		if (null == this.out)
			throw new IOException("No underlying stream to write to");

		final char	ch=getDelimiter();
		if ((_lWritten > 0L) && (ch != '\0'))
			this.out.write(ch);	// separate from previous

		Hex.write((byte) i, this.out, isUseUppercase());
		_lWritten++;
	}
	/*
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] b, int off, int len) throws IOException
	{
		if (null == this.out)
			throw new IOException("No underlying stream to write to");

		final char	ch=getDelimiter();
		if ((_lWritten > 0L) && (ch != '\0'))
			this.out.write(ch);	// separate from previous

		for (int	l=0, o=off; l < len; l++, o++)
		{
			if ((l > 0) && (ch != '\0'))
				this.out.write(ch);	// separate from previous

			Hex.write(b[o], this.out, isUseUppercase());
			_lWritten++;
		}
	}
}