/*
 * 
 */
package net.community.chest.math;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 12, 2011 8:06:31 AM
 */
public final class DivisionSigns {
	private DivisionSigns ()
	{
		// no instance
	}
	/**
	 * @param value Value to be checked
	 * @return <code>true</code> if value is a multiple of 3
	 */
	public static final boolean isMultiple3 (final long value)
	{
		if (value < 0L)	// concern ourselves only with positive values
			return isMultiple3(0 - value);
		
		if (value >= 10L)
			return isMultiple3(sumDigits(value));
		
		if ((value == 3L) || (value == 6L) || (value == 9L))
			return true;

		return false;
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @return <code>true</code> if value is a multiple of 3
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple3 (final CharSequence value) throws NumberFormatException
	{
		return isMultiple3(value, 0, (value == null) ? 0 : value.length());
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @param startPos Start position in the sequence to start checking
	 * @param maxLen Number of digits to examine
	 * @return <code>true</code> if value is a multiple of 3
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple3 (final CharSequence value, final int startPos, final int maxLen) throws NumberFormatException
	{
		return isMultiple3(sumDigits(value, startPos, maxLen));
	}
	/**
	 * @param value Value to be checked
	 * @return <code>true</code> if value is a multiple of 6
	 */
	public static final boolean isMultiple6 (final long value)
	{
		if (value < 0L)	// concern ourselves only with positive values
			return isMultiple6(0 - value);

		if (((value & 0x01) == 0L) && isMultiple3(value))
			return true;

		return false;
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @return <code>true</code> if value is a multiple of 6
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple6 (final CharSequence value) throws NumberFormatException
	{
		return isMultiple6(value, 0, (value == null) ? 0 : value.length());
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @param startPos Start position in the sequence to start checking
	 * @param maxLen Number of digits to examine
	 * @return <code>true</code> if value is a multiple of 6
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple6 (final CharSequence value, final int startPos, final int maxLen) throws NumberFormatException
	{
		final int	endPos=startPos + maxLen;
		if (maxLen <= 0)
			return false;

		// check if last digit is even
		final char	lastDigit=value.charAt(endPos - 1);
		final int	lastValue=lastDigit - '0';
		if ((lastValue & 0x01) != 0)
			return false;

		return isMultiple3(value, startPos, maxLen);
	}
	/**
	 * @param value Value to be checked
	 * @return <code>true</code> if value is a multiple of 9
	 */
	public static final boolean isMultiple9 (final long value)
	{
		if (value < 0L)	// concern ourselves only with positive values
			return isMultiple9(0 - value);
		
		if (value >= 10L)
			return isMultiple9(sumDigits(value));
		
		if (value == 9L)
			return true;

		return false;
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @return <code>true</code> if value is a multiple of 9
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple9 (final CharSequence value) throws NumberFormatException
	{
		return isMultiple9(value, 0, (value == null) ? 0 : value.length());
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @param startPos Start position in the sequence to start checking
	 * @param maxLen Number of digits to examine
	 * @return <code>true</code> if value is a multiple of 9
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple9 (final CharSequence value, final int startPos, final int maxLen) throws NumberFormatException
	{
		return isMultiple9(sumDigits(value, startPos, maxLen));
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @return <code>true</code> if value is a multiple of 5
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple5 (final CharSequence value) throws NumberFormatException
	{
		return isMultiple5(value, 0, (value == null) ? 0 : value.length());
	}
	/**
	 * @param value {@link CharSequence} to be checked - <B>Note:</B> the
	 * sequence is checked to make sure it contains only digits
	 * @param startPos Start position in the sequence to start checking
	 * @param maxLen Number of digits to examine
	 * @return <code>true</code> if value is a multiple of 5
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final boolean isMultiple5 (final CharSequence value, final int startPos, final int maxLen) throws NumberFormatException
	{
		final int	endPos=startPos + maxLen;
		if (maxLen <= 0)
			return false;

		for (int	cIndex=startPos; cIndex < endPos; cIndex++)
		{
			final char	ch=value.charAt(cIndex);
			if ((ch < '0') || (ch > '9'))
				throw new NumberFormatException("isMultiple5(" + value + ") unknown digit: " + String.valueOf(ch));
		}

		final char	ch=value.charAt(endPos - 1);
		if ((ch == '0') || (ch == '5'))
			return true;

		return false;
	}
	/**
	 * @param value The input value
	 * @return The sum of its digits - <B>Note:</B> if the number is negative
	 * then the returned value is the <U>negative</U> of the digits' sum
	 */
	public static final int sumDigits (final long value)
	{
		if (value < 0L)
			return 0 - sumDigits(0 - value);
		
		if (value == 0L)
			return 0;

		int	sum=0;
		for (long v=value; v > 0L; v = v / 10L)
		{
			int	digit=(int) (v % 10L);
			sum += digit;
		}

		return sum;
	}
	/**
	 * @param cs A {@link CharSequence} containing digits
	 * @return The sum of all the digits - <B>Note:</B> no attempt is made to
	 * make sure that the sum of digits does not exceed a <code>long</code>
	 * @throws NumberFormatException If non-digit encountered
	 * @see #sumDigits(CharSequence, int, int)
	 */
	public static final long sumDigits (CharSequence cs) throws NumberFormatException
	{
		return sumDigits(cs, 0, (cs == null) ? 0 : cs.length());
	}
	/**
	 * @param cs A {@link CharSequence} containing digits
	 * @param startPos Start position in the sequence to start summation
	 * @param csLen Number of digits to sum
	 * @return The sum of all the digits - <B>Note:</B> no attempt is made to
	 * make sure that the sum of digits does not exceed a <code>long</code>
	 * @throws NumberFormatException If non-digit encountered
	 */
	public static final long sumDigits (CharSequence cs, int startPos, int csLen) throws NumberFormatException
	{
		final int	endPos=startPos + csLen;
		if (csLen <= 0)
			return 0L;

		long	sum=0L;
		for (int	cIndex=startPos; cIndex < endPos; cIndex++)
		{
			final char	ch=cs.charAt(cIndex);
			if ((ch < '0') || (ch > '9'))
				throw new NumberFormatException("sumDigits(" + cs + ") unknown digit: " + String.valueOf(ch));

			sum += (ch - '0');
		}

		return sum;
	}
}
