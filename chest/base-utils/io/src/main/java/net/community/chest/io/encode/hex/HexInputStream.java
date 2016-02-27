package net.community.chest.io.encode.hex;

import java.io.IOException;
import java.io.InputStream;

import net.community.chest.io.input.InputStreamEmbedder;

/**
 * Helper class used read hexadecimal values from the input stream and convert them to binary
 * TODO add capability to specify a delimiter char which will be ENFORCED
 * @author lyorg
 * 03/06/2004
 */
public class HexInputStream extends InputStreamEmbedder {
	private final int	_nOptions	/* =0 */;
	public final int getOptions ()
	{
		return _nOptions;
	}
	/**
	 * Read option - if used, then whitespace (e.g., TAB, CR, LF, SPACE)
	 * is ignored when reading from the underlying input stream
	 */
	public static final int HEXREAD_IGNORE_WHITESPACE=1;
	/**
	 * Read option - if used, then non-hex characters are silently ignored
	 */
	public static final int HEXREAD_IGNORE_NONHEX=(HEXREAD_IGNORE_WHITESPACE << 1);
	/**
	 * Constructor
	 * @param ist input stream from which to read
	 * @param nOptions read options
	 * @param realClose if TRUE then call to {@link #close()} also closes
	 * @throws IllegalArgumentException if null input stream
	 * the underlying stream
	 * @see #HEXREAD_IGNORE_NONHEX
	 * @see #HEXREAD_IGNORE_WHITESPACE
	 */
	public HexInputStream (InputStream ist, int nOptions, boolean realClose) throws IllegalArgumentException
	{
		super(ist, realClose);
		_nOptions = nOptions;
	}
	/**
	 * Constructor
	 * @param ist input stream from which to read - uses HEXREAD_IGNORE_WHITESPACE option as default
	 * @param realClose if TRUE then call to {@link #close()} also closes
	 * the underlying stream
	 */
	public HexInputStream (InputStream ist, boolean realClose)
	{
		this(ist, HEXREAD_IGNORE_WHITESPACE, realClose);
	}
	/*
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip (long n) throws IOException
	{
		if (n <= 0)
            return 0;

		for (long   p=0; p < n; p++)
			if ((-1) == read())
				return p;

		// this point is reached if end of input not reached before N characters read
		return n;
	}
	/*
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read () throws IOException
	{
		if (null == this.in)
			return (-1);

		final int	nOptions=getOptions();
		for (int nRead=0; ; nRead++)
		{
			int c=this.in.read();
			if ((-1) == c)
				return (-1);

			if (!Hex.isHexDigit((char) c))
			{
				if ((HEXREAD_IGNORE_WHITESPACE == (nOptions & HEXREAD_IGNORE_WHITESPACE))
				 && ((' ' == c) || ('\t' == c) || ('\r' == c) || ('\n' == c)))
					continue;

				if (HEXREAD_IGNORE_NONHEX == (nOptions & HEXREAD_IGNORE_NONHEX))
					continue;

				throw new IOException("Non-HEX character (" + String.valueOf(c) + ") in input stream after " + nRead + " characters");
			}

			final int    hiChar=c, loChar=in.read();
			if ((-1) == loChar)	// this cannot be tolerated
				throw new IOException("Premature EOF while attempting to read 2nd HEX char after " + nRead + " characters");

			if (!Hex.isHexDigit((char) loChar))
			{
				if (HEXREAD_IGNORE_NONHEX == (nOptions & HEXREAD_IGNORE_NONHEX))
					continue;

				throw new IOException("Non-HEX character (" + String.valueOf(c) + ") in input stream after " + nRead + " characters");
			}

			c = Hex.rebuild((char) hiChar, (char) loChar);
			return (c & 0x00FF);
		}
	}
}