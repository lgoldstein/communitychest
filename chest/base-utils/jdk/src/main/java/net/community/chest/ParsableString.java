package net.community.chest;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.NumberTables;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Helper class for parsing text strings
 * 
 * @author Lyor G.
 * @since Jun 28, 2007 2:27:11 PM
 */
public class ParsableString implements CharSequence, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6280237713951845342L;
	private char[]  _data /* =null */;
	private int     _startIndex /* =0 */;
	private int     _maxIndex /* =0 */;
	/**
	 * Initialize the object to empty value
	 */
	public void reset ()
	{
		_data = null;
		_startIndex = 0;
		_maxIndex = 0;
	}
	/**
	 * Converts supplied data into a parsable string
	 * @param data data to be used for parsing
	 * @param startIndex index of first valid character for parsing
	 * @param dataLen number of characters available for parsing (starting at specified offset)
	 * @return TRUE if successful - Note: if FALSE returned, then object is unusable and may yield exceptions
	 */
	public boolean wrap (char[] data, int startIndex, int dataLen)
	{
		if ((_data=data) != null)
		{
			if ((startIndex + dataLen) > data.length)
				return false;

			_startIndex = startIndex;
		}
		else
		{
			if ((dataLen != 0) || ((_startIndex=startIndex) != 0))
				return false;
		}

		_maxIndex = startIndex + dataLen;
		return true;
	}
	/**
	 * Converts supplied data into a parsable string
	 * @param data data to be used for parsing
	 * @return TRUE if successful - Note: if FALSE returned, then object is unusable and may yield exceptions
	 */
	public boolean wrap (char[] data)
	{
		return wrap(data, 0, (null == data) ? 0 : data.length);
	}
	/**
	 * Wraps the character sequence as a parsable string by duplicating its data
	 * @param cs character sequence to be parsed
	 * @param startPos position in sequence from which to wrap the object (inclusive)
	 * @param len number of characters to be copied in the parsable buffer
	 * @return true if successful
	 */
	public boolean wrap (CharSequence cs, int startPos, int len)
	{
		if (null == cs)
			return false;
		
		if ((startPos < 0) || ((startPos + len) > cs.length()))
			return false;

		if (len > 0)
		{
			_data = new char[len];

			for (int	index=0, pos=startPos; index < len; index++, pos++)
				_data[index] = cs.charAt(pos);
		}
		
		_startIndex = 0;
		_maxIndex = len;
		
		return true;
	}
	/**
	 * Wraps the character sequence as a parsable string by duplicating its data
	 * @param cs character sequence to be parsed
	 * @return true if successful
	 */
	public boolean wrap (CharSequence cs)
	{
		return wrap(cs, 0, (null == cs) ? 0 : cs.length());
	}
	/**
	 * Converts supplied data into a parsable string
	 * @param s String to be used for parsing
	 * @return TRUE if successful - Note: if FALSE returned, then object is unusable and may yield exceptions
	 */
	public boolean wrap (String s)
	{
		return (null == s) ? false : wrap(s.toCharArray());
	}
	/**
	 * Default constructor - Note: object cannot be used unless "wrap" called
	 * @see #wrap(char[] data, int startIndex, int dataLen)
	 * @see #wrap(char[] data)
	 * @see #wrap(String s)
	 */
	public ParsableString ()
	{
		super();
	}
	/**
	 * @param data data to be used for parsing
	 * @param startIndex index of first valid character for parsing
	 * @param dataLen number of characters available for parsing (starting at specified offset)
	 * @throws IllegalArgumentException if cannot use the buffer
	 * @see #wrap(char[] data, int startIndex, int dataLen)
     */
	public ParsableString (char[] data, int startIndex, int dataLen)
	{
		if (!wrap(data, startIndex, dataLen))
			throw new IllegalArgumentException("Bad parsable string data");
	}
	/**
	 * @param data data to be used for parsing
	 * @throws IllegalArgumentException if cannot use the buffer
	 * @see #wrap(char[] data)
     */
	public ParsableString (char[] data)
	{
		this(data, 0, (null == data) ? 0 : data.length);
	}
	/**
	 * @param s String to be used for parsing
	 * @throws IllegalArgumentException if cannot use the string
	 * @see #wrap(String s)
	 */
	public ParsableString (String s)
	{
		this((null == s) ? (char[]) null : s.toCharArray());
	}
	/**
	 * Wraps the character sequence as a parsable string by duplicating its data
	 * @param cs character sequence to be parsed
	 * @param startPos position in sequence from which to wrap the object (inclusive)
	 * @param len number of characters to be copied in the parsable buffer
	 * @throws IllegalArgumentException if cannot use the sequence
	 */
	public ParsableString (CharSequence cs, int startPos, int len)
	{
		if (!wrap(cs, startPos, len))
			throw new IllegalArgumentException("Bad parsable char sequence data");
	}
	/**
	 * Wraps the character sequence as a parsable string by duplicating its data
	 * @param cs character sequence to be parsed
	 * @throws IllegalArgumentException if cannot use the sequence
	 */
	public ParsableString (CharSequence cs)
	{
		this(cs, 0, (null == cs) ? 0 : cs.length());
	}
	/* @return current contents as string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final int		startIndex=getStartIndex(), maxIndex=getMaxIndex();
		final char[]	data=array();
		return ((null == data) || (maxIndex <= startIndex)) ? "" : new String(data, startIndex, maxIndex - startIndex);
	}
    /**
     * @return index of of first valid character in underlying data array
     * @see #getCharAt(int index)
     * @see #isAccessibleIndex(int index)
     */
	public int getStartIndex ()
	{
		return _startIndex;
	}
    /**
     * @return index at which non-valid data starts - i.e. last valid character is at this value minus 1.
     * @see #getCharAt(int index)
     * @see #isAccessibleIndex(int index)
     */
	public int getMaxIndex ()
	{
		return _maxIndex;
	}
	/**
	 * Updates the parsing range - Caveat Emptor: use with care
	 * @param startIndex index to start parsing from (inclusive)
	 * @param maxIndex index to stop parsing (exclusive)
	 * @return TRUE if range set
	 */
	public boolean setRange (int startIndex, int maxIndex)
	{
		if ((startIndex < 0) || (maxIndex < startIndex))
			return false;

		final char[]	data=array();
		if (null == data)
		{
			if ((startIndex > 0) || (maxIndex > 0))
				return false;
		}
		else
		{
			if ((startIndex > data.length) || (maxIndex > data.length))
				return false;
		}

		_startIndex = startIndex;
		_maxIndex = maxIndex;
		return true;
	}
	/**
	 * @param index index whose validity is to be checked
	 * @return TRUE if specified index is within current range
     * @see #getCharAt(int index)
	 */
	public boolean isAccessibleIndex (final int index)
	{
		final int	startIndex=getStartIndex(), maxIndex=getMaxIndex();
		return (index >= startIndex)
			&& (index < maxIndex)
			;
	}
	/**
	 * @return underlying array - Note: any access is CAVEAT EMPTOR !!!
	 */
	public char[] array ()
	{
		return _data;
	}
	/* @return number of available valid characters
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length ()
	{
		final int		startIndex=getStartIndex(), maxIndex=getMaxIndex();
		final char[]	data=array();
		return ((null == data) || (maxIndex <= startIndex)) ? 0 : (maxIndex - startIndex);
	}
	/**
	 * Returns the character at the specified index. An index ranges from
	 * "startIndex" to "maxIndex" - 1. The first character of the sequence is
	 * at index "startIndex", and so on, as for array indexing.
	 * @param index the index of the character to be returned
	 * @return the specified character
	 * @throws IndexOutOfBoundsException if the index argument is not within start/max index range
	 * @see #getStartIndex()
	 * @see #getMaxIndex()
     * @see #isAccessibleIndex(int index)
     * @see CharSequence#charAt(int)
	 */
	public char getCharAt (int index)
	{
		final int		startIndex=getStartIndex(), maxIndex=getMaxIndex();
		final char[]	data=array();
		if ((index >= maxIndex) || (index < startIndex))
			throw new IndexOutOfBoundsException(getClass().getName() + "::getCharAt(" + index + ") out of range of [" + _startIndex + "-" + _maxIndex + "] of value=" + toString());

	    return data[index];
	}

	public static final boolean compareTo (CharSequence s, int fromIndex, int toIndex, char[] buf, int nOffset, int nLen, boolean caseSensitive)
	{
		final int maxIndex=(null == s) ? 0 : s.length();
		if ((0 == maxIndex) || (fromIndex < 0) || (toIndex < fromIndex) || (maxIndex < toIndex) || (null == buf) || ((nOffset + nLen) > buf.length))
			return false;

		final int range=(toIndex - fromIndex);
		if (nLen != range)
			return false;

		if (caseSensitive)
		{
			for (int    index=fromIndex, offset=nOffset; index < toIndex; index++, offset++)
			{
				final char	sch=s.charAt(index), bch=buf[offset];
				if (sch != bch)
					return false;
			}
		}
		else
		{
			for (int    index=fromIndex, offset=nOffset; index < toIndex; index++, offset++)
			{
				final char	sch=s.charAt(index), bch=buf[offset];
				if (Character.toUpperCase(sch) != Character.toUpperCase(bch))
					return false;
			}
		}

		return true;
	}

	public boolean compareTo (int fromIndex, int toIndex, CharSequence s, int nOffset, int nLen, boolean caseSensitive)
	{
		return compareTo(s, nOffset, nOffset + nLen, array(), fromIndex, toIndex - fromIndex, caseSensitive);
	}

	public boolean compareTo (int fromIndex, int toIndex, CharSequence s, boolean caseSensitive)
	{
		return compareTo(fromIndex, toIndex, s, 0, (null == s) ? 0 : s.length(), caseSensitive);
	}

	public static final boolean compareTo (char[] data, int fromIndex, int toIndex, char[] buf, int nOffset, int nLen, boolean caseSensitive)
	{
		int maxIndex=(null == data) ? 0 : data.length;
		if ((maxIndex <= 0) || (fromIndex < 0) || (toIndex < fromIndex) || (maxIndex < toIndex) || (null == buf) || ((nOffset + nLen) > buf.length))
			return false;

		int range=(toIndex - fromIndex);
		if (nLen != range)
			return false;

		if (caseSensitive)
		{
			for (int    index=fromIndex, offset=nOffset; index < toIndex; index++, offset++)
			{
				final char	dch=data[index], bch=buf[offset];
				if (dch != bch)
					return false;
			}
		}
		else
		{
			for (int    index=fromIndex, offset=nOffset; index < toIndex; index++, offset++)
			{
				final char	dch=data[index], bch=buf[offset];
				if (Character.toUpperCase(dch) != Character.toUpperCase(bch))
					return false;
			}
		}

		return true;
	}

	public static final boolean compareTo (char[] data, char[] buf, int nOffset, int nLen, boolean caseSensitive)
	{
		return compareTo(data, 0, (null == data) ? 0 : data.length, buf, nOffset, nLen, caseSensitive);
	}

	public static final boolean compareTo (char[] data, int fromIndex, int toIndex, char[] buf, boolean caseSensitive)
	{
		return compareTo(data, fromIndex, toIndex, buf, 0, (null == buf) ? 0 : buf.length, caseSensitive);
	}

	public static final boolean compareTo (CharSequence s, int fromIndex, int toIndex, char[] buf, boolean caseSensitive)
	{
		return compareTo(s, fromIndex, toIndex, buf, 0, (null == buf) ? 0 : buf.length, caseSensitive);
	}
	/**
	 * Compares the specified range to the specified character buffer
	 * @param fromIndex start index from which to compare (inclusive)
	 * @param toIndex end index up to which to compare (exclusive)
	 * @param buf buffer of character with which to compare
	 * @param nOffset offset in characters buffer to start comparison
	 * @param nLen number of character to be compared
	 * @param caseSensitive if TRUE then comparison is case-sensitive
	 * @return TRUE if matches
	 */
	public boolean compareTo (int fromIndex, int toIndex, char[] buf, int nOffset, int nLen, boolean caseSensitive)
	{
		return compareTo(array(), fromIndex, toIndex, buf, nOffset, nLen, caseSensitive);
	}
	/**
	 * Compares the specified range to the specified character buffer
	 * @param fromIndex start index from which to compare (inclusive)
	 * @param toIndex end index up to which to compare (exclusive)
	 * @param buf buffer of character with which to compare - Note: entire
	 * buffer is checked (if null, then zero length buffer is assumed)
	 * @param caseSensitive if TRUE then comparison is case-sensitive
	 * @return TRUE if matches 
	 */
	public boolean compareTo (int fromIndex, int toIndex, char[] buf, boolean caseSensitive)
	{
		return compareTo(fromIndex, toIndex, buf, 0, (null == buf) ? 0 : buf.length, caseSensitive);
	}

	public boolean startsWith (char[] buf, int nOffset, int nLen, boolean caseSensitive)
	{
		final int	startIndex=getStartIndex();
		return compareTo(startIndex, startIndex + nLen, buf, nOffset, nLen, caseSensitive);
	}

	public boolean startsWith (char[] buf, boolean caseSensitive)
	{
		return startsWith(buf, 0, (null == buf) ? 0 : buf.length, caseSensitive);
	}

	public boolean startsWith (String s, boolean caseSensitive)
	{
		return startsWith((null == s) ? (char[]) null : s.toCharArray(), caseSensitive);
	}
	/**
	 * String of characters defined as "whitespace/empty"
	 */
	public static final String WHITESPACE_CHARS=" \t\r\n";
	/**
	 * @param c character to be checked
 	 * @return TRUE if position is either out of range or points to a
 	 * character that is defined as "whitespace"
	 * @see #WHITESPACE_CHARS
	 */
	public static final boolean isEmptyChar (final char c)
	{
		return (c > '\0') && (WHITESPACE_CHARS.indexOf(c) >= 0);
	}
	/**
	 * @param fromIndex index to start looking (inclusive)
	 * @param toIndex index to stop looking (exclusive)
	 * @return index of first non-whitespace data (or (-1) if not found)
	 */
	public int findNonEmptyDataStart (int fromIndex, int toIndex)
	{
		final char[]	data=array();
		if ((null == data) || (toIndex <= fromIndex))
			return (-1);

		for (int nRealIndex=Math.max(fromIndex,getStartIndex()), nMaxIndex=Math.min(toIndex,getMaxIndex());
			 nRealIndex < nMaxIndex;
			 nRealIndex++)
		{
			final char	dch=data[nRealIndex];
			if (!isEmptyChar(dch))
				return nRealIndex;
		}

		// this point is reached if all whitespace characters found
		return (-1);
	}
	/**
	 * @param pos position of character to be checked
	 * @return TRUE if position is either out of range or points to a character that is defined as "empty"
	 * @see #WHITESPACE_CHARS
	 */
	public boolean isEmptyChar (int pos)
	{
		if ((pos < getStartIndex()) || (pos >= getMaxIndex()))
			return true;

		return isEmptyChar(array()[pos]);
	}
	/**
	 * @param fromIndex index to start looking (inclusive)
	 * @return index of first non-whitespace data (or (-1) if not found)
	 */
	public int findNonEmptyDataStart (int fromIndex)
	{
		return findNonEmptyDataStart(fromIndex, getMaxIndex());
	}
	/**
	 * @return index of first non-whitespace data (or (-1) if not found)
	 */
	public int findNonEmptyDataStart ()
	{
		return findNonEmptyDataStart(getStartIndex());
	}
	/**
	 * @param s characters to be checked
	 * @param fromIndex index to start looking (inclusive)
	 * @param toIndex index to stop looking (exclusive)
	 * @return index of first non-whitespace data (or (-1) if not found)
	 */
	public static final int findNonEmptyDataStart (CharSequence s, int fromIndex, int toIndex)
	{
		if ((null == s) || (toIndex <= fromIndex))
			return (-1);

		for (int nRealIndex=Math.max(0,fromIndex), maxIndex=Math.min(toIndex,s.length());
			 nRealIndex < maxIndex;
			 nRealIndex++)
		{
			final char	sch=s.charAt(nRealIndex);
			if (WHITESPACE_CHARS.indexOf(sch) < 0)
				return nRealIndex;
		}

		// this point is reached if all whitespace characters found
		return (-1);
	}
	/**
	 * @param s characters to be checked
	 * @param fromIndex index to start looking (inclusive)
	 * @return index of first non-whitespace data (or (-1) if not found)
	 */
	public static final int findNonEmptyDataStart (CharSequence s, int fromIndex)
	{
		return findNonEmptyDataStart(s, fromIndex, (null == s) ? 0 : s.length());
	}
	/**
	 * @param s characters to be checked
	 * @return index of first non-whitespace data (or (-1) if not found)
	 */
	public static final int findNonEmptyDataStart (CharSequence s)
	{
		return findNonEmptyDataStart(s, 0);
	}
	/**
	 * @param fromIndex index to start looking (inclusive)
	 * @param toIndex index to stop looking (exclusive)
	 * @return index of first "empty" data
	 */
	public int findNonEmptyDataEnd (int fromIndex, int toIndex)
	{
		final int 		nMaxIndex=Math.min(getMaxIndex(),toIndex);
		final char[]	data=array();
		for (int nRealIndex=Math.max(fromIndex,getStartIndex()); nRealIndex < nMaxIndex; nRealIndex++)
		{
			final char	dch=data[nRealIndex];
			if (isEmptyChar(dch))
				return nRealIndex;
		}

		// this point is reached if exhausted data without finding any whitespace
		return nMaxIndex;
	}
	/**
	 * @param fromIndex index to start looking (inclusive)
	 * @return index of first "empty" data
	 */
	public int findNonEmptyDataEnd (int fromIndex)
	{
		return findNonEmptyDataEnd(fromIndex, getMaxIndex());
	}
	/**
	 * @return index of first "empty" data
	 */
	public int findNonEmptyDataEnd ()
	{
		return findNonEmptyDataEnd(getStartIndex());
	}
	/**
	 * @param s characters to be checked
	 * @param fromIndex index to start looking (inclusive)
	 * @param toIndex index to stop looking (exclusive)
	 * @return index of first "empty" data
	 */
	public static final int findNonEmptyDataEnd (CharSequence s, int fromIndex, int toIndex)
	{
		final int maxIndex=Math.min((null == s) ? 0 : s.length(), toIndex);
		for (int nRealIndex=Math.max(fromIndex,0); nRealIndex < maxIndex; nRealIndex++)
		{
			final char	sch=s.charAt(nRealIndex);
			if (WHITESPACE_CHARS.indexOf(sch) >= 0)
				return nRealIndex;
		}

		// this point is reached if exhausted data without finding any whitespace
		return maxIndex;
	}
	/**
	 * @param s characters to be checked
	 * @param fromIndex index to start looking (inclusive)
	 * @return index of first "empty" data
	 */
	public static final int findNonEmptyDataEnd (CharSequence s, int fromIndex)
	{
		return findNonEmptyDataEnd(s, fromIndex, (null == s) ? 0 : s.length());
	}
	/**
	 * @param s characters to be checked
	 * @return index of first "empty" data
	 */
	public static final int findNonEmptyDataEnd (CharSequence s)
	{
		return findNonEmptyDataEnd(s, 0);
	}
	/**
	 * Finds the first occurrence of a '0'-'9' digit in the specified range
	 * @param fromIndex index at which to start looking (inclusive)
	 * @param toIndex maximum index at which to stop looking (exclusive)
	 * @return index of '0'-'9' digit (>= <I>toIndex</I> if none found) - Note: may
	 * be <I>fromIndex</I> if it already points to such a digit
	 */
	public int findNumberStart (int fromIndex, int toIndex)
	{
		final int		nMaxIndex=Math.min(getMaxIndex(),toIndex);
		final char[]	data=array();
		for (int nRealIndex=Math.max(fromIndex,getStartIndex()); nRealIndex < nMaxIndex; nRealIndex++)
		{	
			final char	ch=data[nRealIndex];
			if ((ch >= '0') && (ch <= '9'))
				return nRealIndex;
		}

		return nMaxIndex;
	}
	/**
	 * Finds the first occurrence of a '0'-'9' digit starting from specified position
	 * @param fromIndex index at which to start looking (inclusive)
	 * @return index of '0'-'9' digit (>= maxIndex if none found) - Note: may
	 * be the input value if it already points to such a digit
	 */
	public int findNumberStart (int fromIndex)
	{
		return findNumberStart(fromIndex, getMaxIndex());
	}
	/**
	 * Finds the first occurrence of a '0'-'9' digit starting from first buffer position
	 * @return index of first '0'-'9' digit (>= maxIndex if none found)
	 */
	public int findNumberStart ()
	{
		return findNumberStart(getStartIndex(),getMaxIndex());
	}
	/**
	 * Finds position of last digit in a series of digits within specified range
	 * @param fromIndex index from which to start looking for (inclusive)
	 * @param toIndex index at which to stop (exclusive)
	 * @return index of first non-digit position - Note: may be same as input
	 * index if not starting with a digit. Also, may be index at which to stop
	 * if all characters in range are digits
	 */
	public int findNumberEnd (int fromIndex, int toIndex)
	{
		final int 		nMaxIndex=Math.min(getMaxIndex(),toIndex);
		final char[]	data=array();
		for (int nRealIndex=Math.max(fromIndex,getStartIndex()); nRealIndex < nMaxIndex; nRealIndex++)
		{
			final char	ch=data[nRealIndex];
			if ((ch < '0') || (ch > '9'))
				return nRealIndex;
		}

		return nMaxIndex;
	}
	/**
	 * Finds position of last digit in a series of digits starting at specified position
	 * @param fromIndex index from which to start looking for (inclusive)
	 * @return index of first non-digit position - Note: may be same as input index if not starting with a digit
	 */
	public int findNumberEnd (int fromIndex)
	{
		return findNumberEnd(fromIndex, getMaxIndex());
	}
    /**
     * @param fromIndex index to start the substring from (inclusive)
     * @param toIndex index to end the substring (exclusive)
     * @return requested data (or null if bad indices or no data) - Note:
     * if same indices, then an EMPTY string
     * returned rather than null.
     */
	public String substring (int fromIndex, int toIndex)
	{
		final char[]	data=array();
		if ((fromIndex < getStartIndex()) || (toIndex > getMaxIndex())
		 || (fromIndex > toIndex) || (null == data))
			return null;

	    if (fromIndex == toIndex)
	        return "";

		return new String(data, fromIndex, (toIndex - fromIndex));
	}
	/**
	 * @param fromIndex index to start the substring from (inclusive)
	 * @return substring from start index to end of parse buffer (or null
	 * if bad indices or no data) - Note: if same indices, then EMPTY string
     * returned rather than null.
	 */
	public String substring (int fromIndex)
	{
		return substring(fromIndex, getMaxIndex());
	}
    /**
     * Creates an object for the specified sub-range - Note: the object shares its data, so any changes in
     * the data or the original object from which it was derived will be reflected (and vice-versa)
     * @param startIndex index from which to derive the sub-object (inclusive)
     * @param endIndex index at which to derive the sub-object (exclusive)
     * @return object representing the specified sub-range. If no current data
     * or the specified range is invalid, then an empty object is returned
     * @throws IllegalStateException if unable to complete the action
     */
	public ParsableString subParse (int startIndex, int endIndex)
	{
		final int             	subStart=Math.max(startIndex,getStartIndex()),
								subEnd=Math.min(endIndex,getMaxIndex());
		final ParsableString	ps=new ParsableString();
		final char[]			curData=array();
		if ((null == curData) || (subStart >= subEnd))
			return ps;

		if (!ps.wrap(curData, subStart, subEnd - subStart))
			throw new IllegalStateException("Unable to wrap sub-parse[" + subStart + "-" + subEnd + "] of value=" + toString()); 

		return ps;
	}
	/*
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence (int start, int end)
	{
		return subParse(start, end);
	}
	/*
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt (int index)
	{
		return getCharAt(index + getStartIndex());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (final Object obj)
	{
		if (!(obj instanceof ParsableString))
			return false;
		if (this == obj)
			return true;

		final ParsableString	ps=(ParsableString) obj;
		final int				curLen=length();
		if (ps.length() != curLen)
			return false;

		for (int	curPos=0; curPos < curLen; curPos++)
		{
			final char	psc=ps.charAt(curPos), csc=charAt(curPos);
			if (psc != csc)
				return false;
		}

		return true;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final int	curLen=length();
		int			codeVal=0;
		for (int	curPos=0; curPos < curLen; curPos++)
			codeVal += charAt(curPos);

		return codeVal;
	}
	/**
	 * @param pos position to be checked
	 * @return true if position contains a digit in the range '0'-'9'
	 * @throws IndexOutOfBoundsException if the position argument is not within start/max index range
	 */
	public boolean isDigit (int pos)
	{
		if ((pos >= getMaxIndex()) || (pos < getStartIndex()))
			throw new IndexOutOfBoundsException("::isDigit(" + pos + ") out range");

		final char	ch=array()[pos];
		return ((ch >= '0') && (ch <= '9'));
	}
	/**
	 * Checks if specified range contains a number
	 * @param data data to be checked - if null then FALSE returned
	 * @param fromIndex index to start checking from (inclusive)
	 * @param toIndex index to stop checking (exclusive)
	 * @return true if section contains only digits
	 */
	public static final boolean isUnsignedNumber (char[] data, int fromIndex, int toIndex)
	{
		if ((null == data)
		 || (fromIndex < 0)
		 || (fromIndex >= toIndex)
		 || (toIndex > data.length))
			return false;

		for (int    index=fromIndex; index < toIndex; index++)
		{
			final char	chVal=data[index];
			if ((chVal < '0') || (chVal > '9'))
				return false;
		}

		return true;
	}
	/**
	 * @param data data to be checked - if null/empty then FALSE returned
	 * @return TRUE if entire buffer contains only digits
	 */
	public static final boolean isUnsignedNumber (char[] data)
	{
		return (null == data) ? false : isUnsignedNumber(data, 0, data.length);
	}
	/**
	 * Checks if specified range contains a number
	 * @param fromIndex index to start checking (inclusive)
	 * @param toIndex index to stop checking (exclusive)
	 * @return true if section contains only digits
	 */
	public boolean isUnsignedNumber (int fromIndex, int toIndex)
	{
		final char[]	data=array();
		if ((fromIndex < getStartIndex())
		 || (fromIndex >= toIndex)
		 || (toIndex > getMaxIndex())
		 || (null == data))
			return false;

		for (int    index=fromIndex; index < toIndex; index++)
		{
			final char	chVal=data[index];
			if ((chVal < '0') || (chVal > '9'))
				return false;
		}

		return true;
	}
	/**
	 * Checks if specified range contains a number
	 * @param s characters to be checked
	 * @param fromIndex index to start checking (inclusive)
	 * @param toIndex index to stop checking (exclusive)
	 * @return true if section non-empty contains only digits
	 */
	public static final boolean isUnsignedNumber (CharSequence s, int fromIndex, int toIndex)
	{
		final int maxIndex=(null == s) ? 0 : s.length();
		if ((fromIndex < 0) || (fromIndex >= toIndex) || (toIndex > maxIndex) || (0 == maxIndex))
			return false;

		for (int    index=fromIndex; index < toIndex; index++)
		{
			final char	chVal=s.charAt(index);
			if ((chVal < '0') || (chVal > '9'))
				return false;
		}

		return true;
	}
	/**
	 * Checks if specified range contains a number, and if so, returns its "type"
	 * @param fromIndex index to start checking (inclusive)
	 * @param toIndex index to stop checking (exclusive)
	 * @return 0 if not a number, >0 if positive, <0 if negative
	 */
	public int qualifyNumber (int fromIndex, int toIndex)
	{
		final char[]	data=array();
		if ((fromIndex < getStartIndex()) || (fromIndex >= toIndex)
		 || (toIndex > getMaxIndex()) || (null == data))
			return 0;

		int 		numIndex=fromIndex, retVal=0;
		final char	signChar=data[numIndex];
		if ('+' == signChar)
		{
			numIndex++;
			retVal = 1;
		}
		else if ('-' == signChar)
		{
			numIndex--;
			retVal = (-1);
		}
		else
			retVal = 1;

		if (isUnsignedNumber(numIndex, toIndex))
			return retVal;

		return 0;
	}
	/**
	 * Checks if specified range contains a number, and if so, returns its "type"
	 * @param s characters to be checked
	 * @param fromIndex index to start checking (inclusive)
	 * @param toIndex index to stop checking (exclusive)
	 * @return 0 if not a number, >0 if positive, <0 if negative
	 */
	public static final int qualifyNumber (CharSequence s, int fromIndex, int toIndex)
	{
		final int maxIndex=(null == s) ? 0 : s.length();
		if ((fromIndex < 0) || (fromIndex >= toIndex) || (toIndex > maxIndex) || (maxIndex <= 0))
			return 0;

		int     	numIndex=fromIndex, retVal=0;
		final char  signChar=s.charAt(numIndex);
		// check if have a sign, and take it into account
		if ('+' == signChar)
		{
			numIndex++;
			retVal = 1;
		}
		else if ('-' == signChar)
		{
			numIndex--;
			retVal = (-1);
		}
		else    // if no sign, then assume positive
			retVal = 1;

		// check the rest for being a "simple" number
		if (isUnsignedNumber(s, numIndex, toIndex))
			return retVal;

		return 0;
	}
    /**
     * Validates that encoded number does exceed specified maximum allowed number
     * @param data data to be checked
     * @param fromIndex index where number starts
     * @param maxDigits digits of the maximum allowed number
     * @return true if value does not exceed allowed maximum
     */
	public static final boolean checkNumberLimit (char[] data, int fromIndex, char[] maxDigits)
	{
		for (int    index=fromIndex, digitIndex=0; digitIndex < maxDigits.length; index++, digitIndex++)
		{
			final char    c=data[index];

	        // since we go from most-significant "down" then if it is less than allowed, the rest is OK
			if (c < maxDigits[digitIndex])
				break;
			if (c > maxDigits[digitIndex])
				return false;
		}

		return true;
	}
	/**
	 * Validates that encoded number does exceed specified maximum allowed number
	 * @param fromIndex index where number starts
	 * @param maxDigits digits of the maximum allowed number
	 * @return true if value does not exceed allowed maximum
	 */
	public final boolean checkNumberLimit (int fromIndex, char[] maxDigits)
	{
		return checkNumberLimit(array(), fromIndex, maxDigits);
	}
	/**
	 * Validates that encoded number does exceed specified maximum allowed number
	 * @param s data to be checked
	 * @param fromIndex index where number starts
	 * @param maxDigits digits of the maximum allowed number
	 * @return true if value does not exceed allowed maximum
	 */
	public static final boolean checkNumberLimit (CharSequence s, int fromIndex, char[] maxDigits)
	{
		for (int    index=fromIndex, digitIndex=0; digitIndex < maxDigits.length; index++, digitIndex++)
		{
			final char    c=s.charAt(index);

	        // since we go from most-significant "down" then if it is less than allowed, the rest is OK
			if (c < maxDigits[digitIndex])
				break;

			if (c > maxDigits[digitIndex])
				return false;
		}

		return true;
	}
    /**
     * Extracts a value and converts it to an (unsigned) integer
     * @param fromIndex index to start conversion (inclusive)
     * @param toIndex index to stop conversion (exclusive)
     * @return (unsigned) integer value
     * @throws NumberFormatException if not a valid number (e.g. too many digits, not only digits, etc.)
     */
	public int getUnsignedInt (int fromIndex, int toIndex)
	{
	    final int range=(toIndex - fromIndex);
	    if (range <= 0)
	    	throw new NumberFormatException("Invalid unsigned int number range");
	    if (range > NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM)
	        throw new NumberFormatException("Range contains a number larger than an integer");

	    // if exactly maximum allowed digits, then make sure it is not larger than maxium allowed value
	    if (NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM == range)
	    {
			if (!checkNumberLimit(fromIndex, NumberTables.MAX_UNSIGNED_INT_DIGITS))
		        throw new NumberFormatException("Range contains a number larger than an unsigned integer");
	    }

	    final char[]	data=array();
	    int 			nVal=0;
	    for (int    index=fromIndex; index < toIndex; index++)
	    {
	    	final char	ch=data[index];
	    	if ((ch < '0') || (ch > '9'))
	    		throw new NumberFormatException("Invalid non-digit value in range");
	    	// "shift" and add
	    	nVal = (nVal * 10) + (ch - '0');
	    }

	    return nVal;
	}
	/**
	 * Extracts a value and converts it to an (unsigned) integer
	 * @param s characters array to convert from
	 * @param fromIndex index to start conversion (inclusive)
	 * @param toIndex index to stop conversion (exclusive)
	 * @return (unsigned) integer value
	 * @throws NumberFormatException if not a valid number (e.g. too many digits, not only digits, etc.)
	 */
	public static final int getUnsignedInt (CharSequence s, int fromIndex, int toIndex)
	{
		final int range=(toIndex - fromIndex);
		if (range <= 0)
			throw new NumberFormatException("Invalid unsigned int number range");
		if (range > NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM)
		    throw new NumberFormatException("Range contains a number larger than an integer");

		// if exactly maximum allowed digits, then make sure it is not larger than maxium allowed value
		if (NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM == range)
		{
			if (!checkNumberLimit(s, fromIndex, NumberTables.MAX_UNSIGNED_INT_DIGITS))
				throw new NumberFormatException("Range contains a number larger than an unsigned integer");
		}

		int nVal=0;
		for (int    index=fromIndex; index < toIndex; index++)
		{	
			final char	ch=s.charAt(index);
			if ((ch < '0') || (ch > '9'))
				throw new NumberFormatException("Invalid non-digit value in range");
			// "shift" and add
			nVal = (nVal * 10) + (ch - '0');
		}

		return nVal;
	}
	/**
	 * Extracts a value and converts it to an (unsigned) long value
	 * @param fromIndex index to start conversion (inclusive)
	 * @param toIndex index to stop conversion (exclusive)
	 * @return (unsigned) long value
	 * @throws NumberFormatException if not a number (e.g. too many digits, not only digits, etc.)
	 */
	public long getUnsignedLong (int fromIndex, int toIndex)
	{
		final int range=(toIndex - fromIndex);
		if (range <= 0)
			throw new NumberFormatException("Invalid unsigned long number range");
		if (range < NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM)	// more efficient to do INT calculations
			return getUnsignedInt(fromIndex, toIndex);
		if (range > NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM)
		    throw new NumberFormatException("Range contains a number larger than a long");

		// if exactly maximum allowed digits, then make sure it is not larger than maxium allowed value
		if (NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM == range)
		{
			if (!checkNumberLimit(fromIndex, NumberTables.MAX_UNSIGNED_LONG_DIGITS))
				throw new NumberFormatException("Range contains a number larger than an unsigned long");
		}

	    final char[]	data=array();
		long 			nVal=0L;
		for (int    index=fromIndex; index < toIndex; index++)
		{	
			final char	ch=data[index];
			if ((ch < '0') || (ch > '9'))
				throw new NumberFormatException("Invalid non-digit value in range");
			// "shift" and add
			nVal = (nVal * 10L) + (ch - '0');
		}

		return nVal;
	}
	/**
	 * Extracts a value and converts it to an (unsigned) long value
	 * @param s characters array to convert from
	 * @param fromIndex index to start conversion (inclusive)
	 * @param toIndex index to stop conversion (exclusive)
	 * @return (unsigned) long value
	 * @throws NumberFormatException if not a number (e.g. too many digits, not only digits, etc.)
	 */
	public static final long getUnsignedLong (CharSequence s, int fromIndex, int toIndex)
	{
		final int range=(toIndex - fromIndex);
		if (range <= 0)
			throw new NumberFormatException("Invalid unsigned long number range");
		if (range < NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM)	// more efficient to do INT calculations
			return getUnsignedInt(s, fromIndex, toIndex);
		if (range > NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM)
		    throw new NumberFormatException("Range contains a number larger than a long");

		// if exactly maximum allowed digits, then make sure it is not larger than maxium allowed value
		if (NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM == range)
		{
			if (!checkNumberLimit(s, fromIndex, NumberTables.MAX_UNSIGNED_LONG_DIGITS))
				throw new NumberFormatException("Range contains a number larger than an unsigned long");
		}

		long nVal=0L;
		for (int    index=fromIndex; index < toIndex; index++)
		{	
			final char	ch=s.charAt(index);
			if ((ch < '0') || (ch > '9'))
				throw new NumberFormatException("Invalid non-digit value in range");
			// "shift" and add
			nVal = (nVal * 10L) + (ch - '0');
		}

		return nVal;
	}
	/**
	 * Searches for specified character in the data
	 * @param c character to be looked for
	 * @param fromIndex start index to look for (inclusive)
	 * @param toIndex end index to look for (exclusive)
	 * @return character index (or (-1) if not found)
	 */
	public int indexOf (char c, int fromIndex, int toIndex)
	{
		final int		startIndex=getStartIndex(),
						maxIndex=getMaxIndex();
		final char[]	data=array();
		int 			srchIndex=Math.max(startIndex,fromIndex),
						lastIndex=Math.min(maxIndex,toIndex);
		if ((null == data)
		 || (srchIndex >= maxIndex)
		 || (lastIndex < startIndex)
		 || (srchIndex >= lastIndex)
		 || (fromIndex >= toIndex))
			return (-1);

		for ( ; srchIndex < lastIndex; srchIndex++)
		{
			final char	dch=data[srchIndex];
			if (c == dch)
				return srchIndex;
		}

		return (-1);
	}
	/**
	 * Searches for specified character in the data
	 * @param c character to be looked for
	 * @param fromIndex start index to look for (inclusive)
	 * @return character index (or (-1) if not found)
	 */
	public int indexOf (char c, int fromIndex)
	{
		return indexOf(c, fromIndex, getMaxIndex());
	}
	/**
	 * Searches for specified character in the data
	 * @param c character to be looked for
	 * @return character index (or (-1) if not found)
	 */
	public int indexOf (char c)
	{
		return indexOf(c, getStartIndex());
	}
	/**
	 * Searches for specified character in the data
	 * @param s characters array to be looked for
	 * @param c character to be looked for
	 * @param fromIndex start index to look for (inclusive)
	 * @param toIndex end index to look for (exclusive)
	 * @return character index (or (-1) if not found)
	 */
	public static final int indexOf (CharSequence s, char c, int fromIndex, int toIndex)
	{
		int maxIndex=(null == s) ? 0 : s.length(), srchIndex=Math.max(0,fromIndex), lastIndex=Math.min(maxIndex,toIndex);
		if ((0 == maxIndex)
		 || (srchIndex >= maxIndex)
		 || (lastIndex < 0)
		 || (srchIndex >= lastIndex)
		 || (fromIndex >= toIndex))
			return (-1);

		for ( ; srchIndex < lastIndex; srchIndex++)
		{
			final char	sch=s.charAt(srchIndex);
			if (c == sch)
				return srchIndex;
		}

		return (-1);
	}
	/**
	 * Searches for specified character in the data
	 * @param s characters array to be looked for
	 * @param c character to be looked for
	 * @param fromIndex start index to look for (inclusive)
	 * @return character index (or (-1) if not found)
	 */
	public static final int indexOf (CharSequence s, char c, int fromIndex)
	{
		return indexOf(s, c, fromIndex, (null == s) ? 0 : s.length());
	}
	/**
	 * Searches for specified character in the data
	 * @param s sequence to look for character
	 * @param c character to be looked for
	 * @return character index (or (-1) if not found)
	 */
	public static final int indexOf (CharSequence s, char c)
	{
		if (s instanceof String)
			return ((String) s).indexOf(c);

		return indexOf(s, c, 0, (null == s) ? 0 : s.length());
	}
	/**
	 * Looks BACKWARDS from the specified index for the character
	 * @param c character to be looked for
	 * @param fromIndex index to start looking (inclusive)
	 * @param toIndex last index to be checked (exclusive)
	 * @return index of character in buffer (<= toIndex if not found)
	 */
	public int lastIndexOf (char c, int fromIndex, int toIndex)
	{
		final int		minIndex=Math.max(toIndex, getStartIndex());
		final char[]	data=array();
		for (int	curIndex=Math.min(fromIndex, getMaxIndex()-1); curIndex >= minIndex; curIndex--)
		{
			final char	dch=data[curIndex];
			if (c == dch)
				return curIndex;
		}

		return toIndex;
	}
	/**
	 * Looks BACKWARDS from the specified index for the character
	 * @param c character to be looked for
	 * @param fromIndex index to start looking (inclusive)
	 * @return index of character in buffer (<getStartIndex() if not found)
	 */
	public int lastIndexOf (char c, int fromIndex)
	{
		return lastIndexOf(c, fromIndex, getStartIndex() - 1);
	}
	/**
	 * Looks for the specified character starting at the END of the buffer
	 * @param c character to be looked for
	 * @return index of character in buffer (<0 if not found)
	 */
	public int lastIndexOf (char c)
	{
		return lastIndexOf(c, getMaxIndex() - 1);
	}
	/**
	 * Class used to return information about a "split" request (<B>CAVEAT EMPTOR</B>: do not abuse the fact that the
	 * members are <I>public</I> (!!!)
	 */
	public static class SplitInfo implements PubliclyCloneable<SplitInfo>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3177435115246992389L;
		/**
		 * Index in parse buffer where value data starts
		 */
		public int     valStart=(-1);
		/**
		 * Index in parse buffer where value data ends
		 */
		public int     valEnd=(-1);
		/**
		 * Delimiter due to which value end has been detected
		 */
		public char    endDelim /* ='\0' */;
		/**
		 * Initializes the contents to a bad/illegal value that points to nowhere
		 */
		public void reset ()
		{
			valStart = (-1);
			valEnd = (-1);
			endDelim = '\0';
		}
		/**
		 * @return number of characters in the split
		 */
		public int length ()
		{
			if ((valStart >= 0) && (valEnd >= valStart))
				return (valEnd - valStart);
			else
				return 0;
		}
		/*
		 * @see java.lang.Object#clone()
		 */
		@Override
		@CoVariantReturn
		public SplitInfo clone () throws CloneNotSupportedException
		{
			return getClass().cast(super.clone());
		}
		/*
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals (final Object obj)
		{
			if (!(obj instanceof SplitInfo))
				return false;
			if (this == obj)
				return true;

			final SplitInfo	si=(SplitInfo) obj;
			return (valStart == si.valStart)
				&& (valEnd == si.valEnd)
				&& (endDelim == si.endDelim);
		}
		/*
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode ()
		{
			return valStart + valEnd + endDelim;
		}
	}
	/**
	 * @param spInfo split info structure
	 * @return sub-parsable string (or null if no info structure supplied)
	 */
	public ParsableString subParse (SplitInfo spInfo)
	{
		return (null == spInfo) ? null : subParse(spInfo.valStart, spInfo.valEnd);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the specified range
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @param toIndex index to stop tokenizing (exclusive)
	 * @param delims delimiters to be used as non-empty sequences stoppers (besides whitespace) - may be NULL/empty.
	 * @return A {@link List} of {@link SplitInfo} information - may be NULL if none found
	 * @see SplitInfo
	 */
	public List<SplitInfo> split (final int fromIndex, final int toIndex, final String delims)
	{
		final int		numStops=(null == delims) ? 0 : delims.length();
		List<SplitInfo>	tokens=null;
		final char[]	data=array();
		for (int    firstIndex=Math.max(getStartIndex(),fromIndex),
					endIndex=Math.min(getMaxIndex(),toIndex),
					curIndex=firstIndex, nextIndex=curIndex;
			 curIndex < endIndex;
			 curIndex=nextIndex)
		{
			curIndex = findNonEmptyDataStart(curIndex, endIndex);
			if ((curIndex < firstIndex) || (curIndex >= endIndex))
				break;

			if (numStops > 0)
			{
				for (nextIndex=curIndex+1; nextIndex < endIndex; nextIndex++)
				{
					final char    c=data[nextIndex];
					if ((delims.indexOf(c) >= 0) || isEmptyChar(c))
						break;
				}
			}
			else
				nextIndex = findNonEmptyDataEnd(curIndex+1, endIndex);

			final SplitInfo   tokenInfo=new SplitInfo();
			tokenInfo.valStart = curIndex;
			tokenInfo.valEnd = nextIndex;

			if (nextIndex < endIndex)
				tokenInfo.endDelim = data[nextIndex];

			if (null == tokens)
				tokens = new ArrayList<SplitInfo>();
			tokens.add(tokenInfo);
		}

		return tokens;
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the specified range. Note:
	 * uses ONLY whitespace as tokenizing stoppers
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @param toIndex index to stop tokenizing (exclusive)
	 * @return A {@link List} of {@link SplitInfo} information - may be NULL if none found
	 * @see SplitInfo
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 */
	public List<SplitInfo> split (int fromIndex, int toIndex)
	{
		return split(fromIndex, toIndex, null);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) starting at the specified index. Note:
	 * uses ONLY whitespace as tokenizing stoppers
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @return A {@link List} of {@link SplitInfo} information - may be NULL if none found
	 * @see SplitInfo
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 * @see ParsableString#split(int fromIndex, int toIndex)
	 */
	public List<SplitInfo> split (int fromIndex)
	{
		return split(fromIndex, getMaxIndex(), null);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) starting from specified index
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @param delims delimiters to be used as non-empty sequences stoppers (besides whitespace) - may be NULL/empty.
	 * @return A {@link List} of {@link SplitInfo} information - may be NULL if none found
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 * @see SplitInfo
	 */
	public List<SplitInfo> split (int fromIndex, String delims)
	{
		return split(fromIndex, getMaxIndex(), delims);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the specified data buffer
	 * @param delims delimiters to be used as non-empty sequences stoppers (besides whitespace) - may be NULL/empty.
	 * @return A {@link List} of {@link SplitInfo} information - may be NULL if none found
	 * @see SplitInfo
	 */
	public List<SplitInfo> split (String delims)
	{
		return split(getStartIndex(), delims);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the data buffer. Note:
	 * uses ONLY whitespace as tokenizing stoppers
	 * @return A {@link List} of {@link SplitInfo} information - may be NULL if none found
	 * @see SplitInfo
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 */
	public List<SplitInfo> split ()
	{
		return split(null);
	}
	/**
	 * Returns a token that is a result of a split information
	 * @param tkInfo token information - start/end index
	 * @return token string (or null if invalid data)
	 * @see ParsableString#substring(int fromIndex, int toIndex)
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 * @see SplitInfo
	 */
	public String substring (SplitInfo tkInfo)
	{
		return (null == tkInfo) ? null : substring(tkInfo.valStart, tkInfo.valEnd);
	}

	public boolean compareTo (SplitInfo sp, char[] buf, boolean caseSensitive)
	{
		return (null == sp) ? false : compareTo(sp.valStart, sp.valEnd, buf, caseSensitive);
	}

	public boolean compareTo (SplitInfo sp, char[] buf, int nOffset, int nLen, boolean caseSensitive)
	{
		return (null == sp) ? false : compareTo(sp.valStart, sp.valEnd, buf, nOffset, nLen, caseSensitive);
	}

	public boolean compareTo (SplitInfo sp, String s, boolean caseSensitive)
	{
		return (null == sp) ? false : compareTo(sp.valStart, sp.valEnd, s, caseSensitive);
	}

	public int getUnsignedInt (SplitInfo sp)
	{
		if (null == sp)
			throw new NumberFormatException("Null/empty unsigned int split info");

		return getUnsignedInt(sp.valStart, sp.valEnd);
	}

	public long getUnsignedLong (SplitInfo sp)
	{
		if (null == sp)
			throw new NumberFormatException("Null/empty unsigned long split info");

		return getUnsignedLong(sp.valStart, sp.valEnd);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the specified range
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @param toIndex index to stop tokenizing (exclusive)
	 * @param delims delimiters to be used as non-empty sequences stoppers (besides whitespace) - may be NULL/empty.
	 * @return A {@link List} of {@link String} values obtained from the tokenizing
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 */
	public List<String> tokenize (int fromIndex, int toIndex, String delims)
	{
		final Collection<? extends SplitInfo>	tokens=split(fromIndex, toIndex, delims);
		final int								numTokens=(null == tokens) ? 0 : tokens.size();
		final List<String>						tkl=(numTokens <= 0) ? null : new ArrayList<String>(numTokens);
		if (numTokens > 0)
		{
			for (final SplitInfo sp : tokens)
			{
				final String	s=substring(sp);
				tkl.add(s);
			}
		}

		return tkl;
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the specified range
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @param toIndex index to stop tokenizing (exclusive)
	 * @return A {@link List} of {@link String} values obtained from the tokenizing
	 * @see ParsableString#split(int fromIndex, int toIndex)
	 */
	public List<String> tokenize (int fromIndex, int toIndex)
	{
		return tokenize(fromIndex, toIndex, null);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) starting at the specified index
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @param delims delimiters to be used as non-empty sequences stoppers (besides whitespace) - may be NULL/empty.
	 * @return A {@link List} of {@link String} values obtained from the tokenizing
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 */
	public List<String> tokenize (int fromIndex, String delims)
	{
		return tokenize(fromIndex, getMaxIndex(), delims);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) starting at the specified index
	 * @param fromIndex index to start tokenizing (inclusive)
	 * @return A {@link List} of {@link String} values obtained from the tokenizing
	 * @see ParsableString#split(int fromIndex)
	 */
	public List<String> tokenize (int fromIndex)
	{
		return tokenize(fromIndex, getMaxIndex(), null);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the data buffer
	 * @param delims delimiters to be used as non-empty sequences stoppers (besides whitespace) - may be NULL/empty.
	 * @return A {@link List} of {@link String} values obtained from the tokenizing
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 */
	public List<String> tokenize (String delims)
	{
		return tokenize(getStartIndex(), delims);
	}
	/**
	 * Returns all available tokens (non-empty sequences of characters) in the data buffer
	 * @return A {@link List} of {@link String} values obtained from the tokenizing
	 * @see ParsableString#split(int fromIndex, int toIndex, String delims)
	 */
	public List<String> tokenize ()
	{
		return tokenize(null);
	}
	/**
	 * Extracts a (new) sub-array of the underlying buffer within specified limits
	 * @param fromIndex start index from which to start extracting (inclusive) 
	 * @param toIndex last index up to which to extract data (exclusive)
	 * @return char array with data of specified range - Note: may be null/empty if bad range
	 */
	public char[] subArray (int fromIndex, int toIndex)
	{
		final char[]	data=array();
		final int		copyStart=Math.max(getStartIndex(), fromIndex),
						copyEnd=Math.min(getMaxIndex(), toIndex),
						copyLen=(null == data) ? Integer.MIN_VALUE : (copyEnd - copyStart);
		final char[]	subData=(copyLen <= 0) ? null : new char[copyLen];
		if (copyLen > 0)
			System.arraycopy(data, copyStart, subData, 0, copyLen);
		return subData;
	}
	/**
	 * Appends specified range of data to the supplied {@link Appendable} instance
	 * @param <A> The {@link Appendable} class type
	 * @param sb The {@link Appendable} instance to which to append
	 * @param fromIndex start index from which to start appending (inclusive) 
	 * @param toIndex last index up to which to append (exclusive)
	 * @return Same as input instance
	 * @throws IOException if failed to append
	 */
	public <A extends Appendable> A append (A sb, int fromIndex, int toIndex) throws IOException
	{
		if (null == sb)
			throw new IOException("append(" + fromIndex + "-" + toIndex + ") no " + Appendable.class.getSimpleName() + " instance");

		final char[]	data=array();
		final int		copyStart=Math.max(getStartIndex(), fromIndex),
						copyEnd=Math.min(getMaxIndex(), toIndex),
						copyLen=(null == data) ? Integer.MIN_VALUE : (copyEnd - copyStart);
		if (copyLen > 0)
		{
			for (int	cIndex=copyStart; cIndex < copyEnd; cIndex++)
				sb.append(data[cIndex]);
		}

		return sb;
	}
	/**
	 * Appends current data to the supplied {@link Appendable} instance
	 * @param <A> The {@link Appendable} class type
	 * @param sb The {@link Appendable} instance to which to append
	 * @return Same as input instance
	 * @throws IOException if failed to append
	 */
	public <A extends Appendable> A append (A sb) throws IOException
	{
		return append(sb, getStartIndex(), getMaxIndex());
	}
	/**
	 * Reduces the start/end index of the parse buffer so that any
	 * trailing/starting whitespace is "removed"
	 * @return <I>this</I> object
	 * @see ParsableString#getStartIndex()
	 * @see ParsableString#getMaxIndex()
	 */
	public ParsableString trim ()
	{
		// trim prefix
		for ( ; _startIndex < _maxIndex; _startIndex++)
		{
			final char	dch=_data[_startIndex];
			if (!isEmptyChar(dch))
				break;
		}

		// trim trailers
		for ( ; _maxIndex > _startIndex; _maxIndex--)
		{
			final char	dch=_data[_maxIndex-1];
			if (!isEmptyChar(dch))
				break;
		}

		return this;
	}
	/**
	 * Inserts specified character at specified position - provided not below
	 * start index and not above max index, and at most 1 position beyond current
	 * length
	 * @param pos position for the character
	 * @param ch character to be inserted
	 * @throws IndexOutOfBoundsException if bad/illegal position
	 */
	public void insert (int pos, char ch) throws IndexOutOfBoundsException
	{
		if ((pos < _startIndex) || (pos > (_maxIndex+1)) || (null == _data))
			throw new IndexOutOfBoundsException("insert(" + pos + ")=" + String.valueOf(ch) + " bad index (range=" + _startIndex + "-" + _maxIndex + ")");

		// "shift" the array to make root for new character
		System.arraycopy(_data, pos, _data, pos+1, _maxIndex - _startIndex - pos);

		_data[pos] = ch;
		_maxIndex++;
	}
}
