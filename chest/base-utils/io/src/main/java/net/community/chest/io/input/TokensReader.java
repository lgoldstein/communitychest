package net.community.chest.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamCorruptedException;

import net.community.chest.io.EOLStyle;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to read "tokens" - where a token is defined as a set of continuous
 * non-whitespace characters</P>
 * 
 * @author Lyor G.
 * @since Oct 18, 2007 12:29:28 PM
 */
public class TokensReader extends ReaderEmbedder {
	public TokensReader (Reader inReader, boolean realClosure)
	{
		super(inReader, realClosure);
	}
	/**
	 * @param cbuf buffer to read token characters into - may NOT be null
	 * @param offset offset in buffer to place read token characters
	 * @param len max. number of characters to read - <B>Note:</B> if this
	 * number of characters is read before the actual token end is found
	 * then reading stops at this max. number. In other words, if the token
	 * length exceeds this length then it will be "split" into 2 (or more)
	 * "tokens".
	 * @return number of used characters - <0 if EOF reached with no
	 * character(s) read
	 * @throws IOException if unable to read
	 */
	public int readToken (char[] cbuf, int offset, int len) throws IOException
	{
		if ((null == cbuf) || (offset < 0) || (len < 0) || ((offset + len) > cbuf.length))
			throw new IOException("Bad/Illegal token buffer");

		int	v=read();	// skip till first non-empty char
		for (  ; v <= ' '; v=read())
		{
			if ((-1) == v)	// stop if reached EOF
				return (-1);
		}

		cbuf[offset] = (char) v;

		int	readLen=1;
		for (int	curOffset=offset+1; readLen < len; curOffset++, readLen++)
		{
			v = read();

			if (v <= ' ')	// stop at first whitespace char
				break;

			cbuf[curOffset] = (char) v;
		}

		return readLen;
	}
	/**
	 * @param cbuf buffer to read token characters into - may NOT be null.
	 * <B>Note:</B> if entire token cannot be accommodated into this buffer
	 * then it will be "split" into 2 (or more) "tokens"
	 * @return number of used characters - <0 if EOF reached with no
	 * character(s) read
	 * @throws IOException if unable to read
	 * @see #readToken(char[], int, int)
	 */
	public int readToken (char[] cbuf) throws IOException
	{
		return readToken(cbuf, 0, (null == cbuf) ? 0 : cbuf.length);
	}
	/**
	 * Determines if a read token is a comment and should be skipped by the
	 * {@link #readSkipComments(char[], int, int)} method.
	 * @param cbuf read token
	 * @param offset offset in token buffer where token data starts
	 * @param len number of characters in token
	 * @return true if this is a comment (default implementation returns
	 * always <code>false</code> - i.e., no token is a comment)
	 * @throws IOException if error while determining the token type
	 */
	public boolean isCommentToken (final char[] cbuf, int offset, int len) throws IOException
	{
		if ((null == cbuf) || (offset < 0) || (len < 0) || ((offset+len) > cbuf.length))
			throw new StreamCorruptedException("isCommentToken() bad params");

		return false;
	}
	/**
	 * Determines if a read token is a comment and should be skipped by the
	 * {@link #readSkipComments(char[], int, int)} method.
	 * @param cbuf read token
	 * @return true if this is a comment (default implementation returns
	 * always <code>false</code> - i.e., no token is a comment)
	 * @throws IOException if error while determining the token type
	 */
	public boolean isCommentToken (final char[] cbuf) throws IOException
	{
		return isCommentToken(cbuf, 0, (null == cbuf) ? 0 : cbuf.length);
	}
	/**
	 * Uses the supplied token buffer to read tokens until a non-comment
	 * one is encountered - uses the {@link #isCommentToken(char[], int, int)}
	 * method to determine if a read comment is a token. If so, then the
	 * entire line is skipped before resuming tokens reading.
	 * @param cbuf buffer into which to read tokens
	 * @param offset offset in buffer for reading tokens
	 * @param len max. number of characters available for reading tokens
	 * @return number of characters in read token - if negative then EOF
	 * @throws IOException failed to read data
	 */
	public int readSkipComments (final char[] cbuf, int offset, int len) throws IOException
    {
		for (int	readLen=readToken(cbuf, offset, len), rIndex=0;
			 rIndex < Short.MAX_VALUE;	// we skip at most ~32K comments to avoid an infinite loop
			 readLen=readToken(cbuf, offset, len), rIndex++)
		{
				// check if comment and ignore if so
			if (isCommentToken(cbuf, offset, readLen))
			{
				final long	sLen=skipLine();
				if (sLen <= 0L)
					return (-1);

				continue;
			}

			return readLen;
		}

		throw new EOFException("readSkipComments() - unexpected comments read loop exit");
    }
	/**
	 * Uses the supplied token buffer to read tokens until a non-comment
	 * one is encountered - uses the {@link #isCommentToken(char[], int, int)}
	 * method to determine if a read comment is a token. If so, then the
	 * entire line is skipped before resuming tokens reading.
	 * @param cbuf buffer into which to read tokens
	 * @return number of characters in read token - if negative then EOF
	 * @throws IOException failed to read data
	 */
	public int readSkipComments (final char[] cbuf) throws IOException
	{
		return readSkipComments(cbuf, 0, (null == cbuf) ? 0 : cbuf.length);
	}
	/**
	 * Skips current line
	 * @return number of skipped characters (<0 if EOF reached without
	 * any character(s) being skipped)
	 * @throws IOException unable to access
	 */
	public long skipLine () throws IOException
	{
		long	skipLen=0L;
		for (int	v=read(); v != (-1) /* stop at EOF */; v=read())
		{
			skipLen++;

			if ('\n' == v)
				break;
		}

		return (skipLen <= 0L) ? (-1L) : skipLen;
	}
	/**
	 * Reads from {@link Reader} till LF or EOF found ignoring CR(s), appending to
	 * the {@link Appendable} instance the read characters.
	 * @param sb The {@link Appendable} instance - may not be <code>null</code>
	 * @param r The {@link Reader} instance - may not be <code>null</code>
	 * @return The {@link EOLStyle} that was used (LF or CRLF) - <code>null</code>
	 * if last line in input and no EOL
	 * @throws IOException If failed to read/write
	 */
	public static final EOLStyle appendLine (final Appendable sb, final Reader r) throws IOException
	{
		if ((null == sb) || (null == r))
			throw new IOException("No " + Appendable.class.getSimpleName() + "/" + Reader.class.getSimpleName() + " instance(s)");

		boolean	haveCR=false;
		for (int	c=r.read(); c != (-1); c=r.read())
		{
			if (c == '\r')
			{
				if (haveCR)	// duplicate CR(s)
					sb.append((char) c);
				else
					haveCR = true;
				continue;
			}
			if (c == '\n')
				return haveCR ? EOLStyle.CRLF : EOLStyle.LF;

			// check if CR followed by other than LF
			if (haveCR)
			{
				sb.append('\r');
				haveCR = false;
			}

			sb.append((char) c);
		}

		return null;
	}
}
