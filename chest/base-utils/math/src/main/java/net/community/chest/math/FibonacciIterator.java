/*
 *
 */
package net.community.chest.math;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides a generalized {@link Iterator} implementation of a Fibonacci series
 * starting from any 2 initial values and proceeding as long as the next number
 * in the series can be accommodated inside a {@link Long}
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 12, 2011 2:04:29 PM
 */
public class FibonacciIterator implements Iterator<Long> {
    private final Long[]    _values;
    /**
     * Provides the startup values
     * @param v1 1st value in sequence - may not be negative
     * @param v2 2nd value in sequence - must be &ge;1st value
     * @throws IllegalArgumentException If 1st number is negative or 2nd one not &ge;1st
     */
    public FibonacciIterator (final long v1, final long v2) throws IllegalArgumentException
    {
        _values = new Long[] { Long.valueOf(v1), Long.valueOf(v2), calculateNextValue(v1, v2) };
    }
    /**
     * Default constructor - starts with 0 and 1 as the initial values
     * @see #FibonacciIterator(long, long)
     */
    public FibonacciIterator ()
    {
        this(0L, 1L);
    }

    private int    _curValue;
    /*
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext ()
    {
        if (_values[_curValue] != null)
            return true;

        return false;    // debug breakpoint
    }
    /*
     * @see java.util.Iterator#next()
     */
    @Override
    public Long next ()
    {
        if (!hasNext())
            throw new NoSuchElementException("No more elements available - check hasNext() before calling");

        final Long    retValue=_values[_curValue];
        _values[_curValue] = null;    // mark location as used
        _curValue = getEffectiveIndex(_curValue + 1);

        // check if more values can be potentially calculated
        final Long    v1=_values[_curValue], v2=_values[getEffectiveIndex(_curValue + 1)];
        if ((v1 == null) || (v2 == null))
            return retValue;

        final Long    v3=calculateNextValue(v1.longValue(), v2.longValue());
        _values[getEffectiveIndex(_curValue + 2)] = v3;
        return retValue;
    }
    /*
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove ()
    {
        throw new UnsupportedOperationException("remove() N/A for " + getClass().getSimpleName());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return Arrays.toString(_values);
    }

    private int getEffectiveIndex (final int curIndex)
    {
        return (curIndex % _values.length);
    }
    /**
     * Calculates next value in a Fibonacci sequence given the previous 2
     * @param v1 1st value in sequence - may not be negative
     * @param v2 2nd value in sequence - must be &ge;1st value
     * @return The next value in the sequence - <code>null</code> if cannot be represented as a {@link Long}
     * @throws IllegalArgumentException If 1st number is negative or 2nd one not &ge;1st
     */
    public static final Long calculateNextValue (final long v1, final long v2) throws IllegalArgumentException
    {
        if (v1 < 0L)
            throw new IllegalArgumentException("1st value (" + v1 + ") may not be negative");
        if (v1 > v2)
            throw new IllegalArgumentException("2nd value (" + v2 + ") is not greater-equals to 1st value (" + v1 + ")");

        final long    v3=v1 + v2;
        if (v3 < v2)    // means new value cannot be accommodated inside a Long
            return null;

        return Long.valueOf(v3);
    }
}
