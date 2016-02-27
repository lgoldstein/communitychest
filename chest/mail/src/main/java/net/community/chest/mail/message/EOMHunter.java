package net.community.chest.mail.message;

import java.nio.CharBuffer;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to detected the standard <code>CRLF.CRLF</code> sequence used to
 * indicate the (E)nd-(O)f-(M)essage. The implementation is actually more
 * flexible and accepts several "versions" of this sequence (e.g., LF.LF)</P>
 * 
 * @author Lyor G.
 * @since Sep 19, 2007 9:00:32 AM
 */
public final class EOMHunter {
	private EOMHunter ()
	{
		// no instance - this is really a container class
	}

	public static final int	MIN_EOM_SEQLEN=3  /* minimum EOM is LF.LF */,
							MAX_EOM_SEQLEN=5 /* maximum EOM is CRLF.CRLF */;
	/**
	 * @param seqLen characters sequence length to be checked if contains
	 * EOM sequence
	 * @return TRUE if sequence length might (!) contain the sequence
	 */
	public static final boolean isOkEOMSequenceLength (final int seqLen)
	{
		return (seqLen >= MIN_EOM_SEQLEN) && (seqLen <= MAX_EOM_SEQLEN);
	}
	/**
	 * Checks if provided buffer contains an EOM sequence (or its variants)
	 * @param buf buffer of data to be checked - assumed to contain only
	 * ASCII/UTF-8 characters
	 * @param offset offset of bytes to be checked
	 * @param bufLen number of valid bytes in the buffer (starting at offset)
	 * @return number of characters relative to "bufLen" containing the
	 * sequence (or <=0 if not an EOM sequence) - i.e., return value (N)
	 * is the number of characters that represent the EOM sequence and
	 * reside as the <B>last</B> characters in the buffer
	 */
	public static final int checkEOMBuffer (final byte[] buf, final int offset, final int bufLen)
	{
		if (!isOkEOMSequenceLength(bufLen))
			return (-1);

		final int	maxPos=(offset + bufLen);
		if ((null == buf) || (offset < 0) || (maxPos > buf.length))
			return (-2);	// should not happen

		for (int	dotPos=offset + 1 /* '.' cannot be first */; dotPos < maxPos; dotPos++)
		{
			if (buf[dotPos] != (byte) '.')
				continue;

			// check what precedes the dot - MUST be LF (NOTE: do not care what comes BEFORE the LF)
			if (buf[dotPos-1] != (byte) '\n')
				return (-3);

			// at least one character (LF) must trail the '.' 
			if (dotPos >= (maxPos-1))
				return (-4);

			int	seqLen=2;	// we have at least an "LF." sequence
			if ((dotPos >= (offset+2)) && ((byte) '\r' == buf[dotPos-2]))
				seqLen++;	// have an "CRLF." sequence

			// check what trails the '.'
			if ((byte) '\r' == buf[dotPos+1])
			{
				// make sure EXACTLY one character follows the CR and it is LF
				if (((dotPos+3) < maxPos) || ((dotPos+2) >= maxPos) || (buf[dotPos+2] != (byte) '\n'))
					return (-5);

				return (seqLen+2);	// have a "CRLF" following the dot
			}
			else if ((byte) '\n' == buf[dotPos+1])
			{
				// make sure NOTHING follows the LF
				if ((dotPos + 2) < maxPos)
					return (-6);

				return (seqLen + 1);	// have only LF following the dot
			}
			else	// neither CR nor LF follow the '.'
				return (-7);
		}

		// this point is reached if '.' not found in buffer
		return (-8);
	}
	/**
	 * Checks if provided buffer contains an EOM sequence (or its variants)
	 * @param buf buffer of data to be checked - assumed to contain UTF-8/ASCII
	 * character in it
	 * @return number of characters relative to "bufLen" containing the
	 * sequence (or <=0 if not an EOM sequence) - i.e., return value (N)
	 * is the number of characters that represent the EOM sequence and
	 * reside as the <B>last</B> characters in the buffer
	 * @see #checkEOMBuffer(byte[] buf, int offset, int bufLen)
	 */
	public static final int checkEOMBuffer (final byte... buf)
	{
		return checkEOMBuffer(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/**
	 * Checks if provided buffer contains an EOM sequence (or its variants)
	 * @param cs character sequence to be chacked
	 * @param offset offset of characters to be checked
	 * @param len number of valid characters to be checked (starting at offset)
	 * @return number of characters relative to "len" containing the
	 * sequence (or <=0 if not an EOM sequence) - i.e., return value (N)
	 * is the number of characters that represent the EOM sequence and
	 * reside as the <B>last</B> characters in the buffer
	 */
	public static final int checkEOMBuffer (final CharSequence cs, final int offset, final int len)
	{
		if (!isOkEOMSequenceLength(len))
			return (-1);

		final int	maxPos=(offset + len);
		if ((null == cs) || (offset < 0) || (maxPos > cs.length()))
			return (-2);	// should not happen

		for (int	curPos=offset+1 /* dot cannot be first */; curPos < maxPos; curPos++)
		{
			if (cs.charAt(curPos) != '.')
				continue;

			int	startPos=curPos-1;
			// if have a CR 2 positions before the '.' then include it in copied characters
			if ((startPos > offset) && ('\r' == cs.charAt(startPos-1)))
				startPos--;

			// copy the ASCII value of the candidate sequence as byte values
			final byte[]	bb=new byte[maxPos - startPos];
			for (int	bPos=0; startPos < maxPos; startPos++, bPos++)
			{
				final char	c=cs.charAt(startPos);
				/* 	If found a character below ASCII 10 or above '.' ASCII,
				 * then obviously, this cannot be an EOM sequence.
				 */
				if ((c < '\n') || (c > '.'))
					return (-3);

				bb[bPos] = (byte) c;
			}

			return checkEOMBuffer(bb);
		}

		// this point is reached if no '.' found
		return (-4);
	}
	/**
	 * Checks if provided buffer contains an EOM sequence (or its variants)
	 * @param cs character sequence to be chacked
	 * @return number of characters relative to "bufLen" containing the
	 * sequence (or <=0 if not an EOM sequence) - i.e., return value (N)
	 * is the number of characters that represent the EOM sequence and
	 * reside as the <B>last</B> characters in the buffer
	 * @see #checkEOMBuffer(byte[] buf, int offset, int bufLen)
	 */
	public static final int checkEOMBuffer (final CharSequence cs)
	{
		return checkEOMBuffer(cs, 0, (null == cs) ? 0 : cs.length());
	}
	/**
	 * Checks if provided buffer contains an EOM sequence (or its variants)
	 * @param buf buffer of characters to be checked
	 * @param offset offset of characters to be checked
	 * @param bufLen number of valid characters in the buffer (starting at offset)
	 * @return number of characters relative to "bufLen" containing the
	 * sequence (or <=0 if not an EOM sequence) - i.e., return value (N)
	 * is the number of characters that represent the EOM sequence and
	 * reside as the <B>last</B> characters in the buffer
	 */
	public static final int checkEOMBuffer (final char[] buf, final int offset, final int bufLen)
	{
		if (!isOkEOMSequenceLength(bufLen))
			return (-1);

		final int	maxPos=(offset + bufLen);
		if ((null == buf) || (offset < 0) || (maxPos > buf.length))
			return (-2);	// should not happen

		return checkEOMBuffer(CharBuffer.wrap(buf, offset, bufLen) /* implements CharSequence */);
	}
	/**
	 * Checks if provided buffer contains an EOM sequence (or its variants)
	 * @param buf buffer of character to be checked
	 * @return number of characters relative to "bufLen" containing the
	 * sequence (or <=0 if not an EOM sequence) - i.e., return value (N)
	 * is the number of characters that represent the EOM sequence and
	 * reside as the <B>last</B> characters in the buffer
	 * @see #checkEOMBuffer(char[] buf, int offset, int bufLen)
	 */
	public static final int checkEOMBuffer (final char... buf)
	{
		return checkEOMBuffer(buf, 0, (null == buf) ? 0 : buf.length);
	}
}
