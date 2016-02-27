/*
 * 
 */
package net.community.chest.math;

/**
 * Provides some extra functionality not available via {@link Math} class
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 22, 2011 1:30:57 PM
 */
public abstract class ExtraMath {
	private ExtraMath ()
	{
		// no instance
	}
	/**
	 * Provides an alternative handling of {@link Integer#MIN_VALUE} in which
	 * case it returns {@link Integer#MAX_VALUE}. Otherwise, behaves same as
	 * {@link Math#abs(int)}
	 * @param value Original value
	 * @return Absolute <U>positive</U> value
	 * @see Math#abs(int)
	 */
	public static final int abs (final int value)
	{
		if (value == Integer.MIN_VALUE)
			return Integer.MAX_VALUE;
		else
			return Math.abs(value);
	}
	/**
	 * Provides an alternative handling of {@link Long#MIN_VALUE} in which
	 * case it returns {@link Long#MAX_VALUE}. Otherwise, behaves same as
	 * {@link Math#abs(long)}
	 * @param value Original value
	 * @return Absolute <U>positive</U> value
	 * @see Math#abs(long)
	 */
	public static final long abs (final long value)
	{
		if (value == Long.MIN_VALUE)
			return Long.MAX_VALUE;
		else
			return Math.abs(value);
	}

	public static final int sign (final long value)
	{
		if (value == 0L)
			return 0;
		else if (value < 0L)
			return (-1);
		else
			return (+1);
	}
    /**
     * @param v A <tt>double</tt> value
     * @return An <tt>int</tt> hash value for it using {@link #hashValue(long)}
     * on the {@link #getLongBits(double)} result
     */
    public static int hashValue (double v) {
        return hashValue(getLongBits(v));
    }

    /**
     * @param v A <tt>long</tt> value
     * @return An <tt>int</tt> hash value for it
     */
    public static int hashValue (long v) {
        return (int) (v ^ (v >>> 32));
    }
    /**
     * @param v The <code>double</code> value
     * @return If value is not zero then returns its {@link Double#doubleToLongBits(double)},
     * zero otherwise
     */
    public static long getLongBits (double v) {
        if (v != +0.0d)
            return Double.doubleToLongBits(v);
        else
            return 0L;
    }
    
    public static int signOf (int value) {
        if (value < 0)
            return (-1);
        if (value > 0)
            return 1;
        return 0;
    }

    public static int signOf (long value) {
        if (value < 0L)
            return (-1);
        if (value > 0L)
            return 1;
        return 0;
    }
}
