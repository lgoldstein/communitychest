package net.community.chest.net.proto.text.imap4;

import net.community.chest.ParsableString;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:30:31 PM
 */
public class IMAP4ParseAtomValue implements CharSequence {
	/**
	 * The actual atom value
	 */
	ParsableString	val	/* =null */;
	/**
	 * The next index in parse buffer to be used
	 */
	int	startPos=(-1);
	/**
	 * Resets the contents to an invalid state
	 */
	void reset ()
	{
		val = null;
		startPos = (-1);
	}
	/*
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt (int index)
	{
		if (null == val)
			throw new IndexOutOfBoundsException("No current value");
		return val.getCharAt(val.getStartIndex() + index);
	}
	/*
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length ()
	{
		return (null == val) ? 0 : val.length();
	}
	/*
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence (int start, int end)
	{
		return (null == val) ? null : val.subSequence(start, end);
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return (null == val) ? "" : val.toString();
	}
}
