/*
 *
 */
package net.community.chest.math.test;

import java.util.Iterator;

import net.community.chest.math.FibonacciIterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 12, 2011 3:06:35 PM
 */
public class FibonacciIteratorTest extends Assert {
    public FibonacciIteratorTest ()
    {
        super();
    }

    @Test
    public void testDefaultIterator ()
    {
        assertSequence("default", new FibonacciIterator(),
                0,     1,   1,   2,   3,   5,   8,   13,   21,   34,
              55,    89, 144, 233, 377, 610, 987, 1597, 2584, 4181,
            6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418,
            317811, 514229, 832040, 1346269, 2178309, 3524578, 5702887,
            9227465, 14930352, 24157817, 39088169);
    }
    /**
     * Makes sure that the {@link Iterator#hasNext()} method returns
     * <code>false</code> if the next value in the sequence cannot be
     * contained inside a {@link Long}
     */
    @Test
    public void testEndingIterator ()
    {
        final long    v2=Long.MAX_VALUE - 17041690L - 1L,
                    v1=v2 - 3777347L;
        final Iterator<? extends Number>    iter=new FibonacciIterator(v1, v2);
        assertSequence("ending", iter, v1, v2);
        assertFalse("Unexpected continuation of end sequence", iter.hasNext());
    }
    /**
     * Makes sure that illegal values cannot be used to initialize the iterator
     */
    @Test
    public void testIllegalStartValues ()
    {
        final long[]    BAD_PAIRS={
                -1L, 2L,    // negative numbers not allowed
                5L, 2L        // 2nd value must be >= 1st one
            };
        for (int    vIndex=0; vIndex < BAD_PAIRS.length; )
        {
            final long    v1=BAD_PAIRS[vIndex], v2=BAD_PAIRS[vIndex+1];
            try
            {
                final Iterator<? extends Number>    iter=new FibonacciIterator(v1, v2);
                fail("Unexpected creation of iterator for " + iter);
            }
            catch(IllegalArgumentException e)
            {
                // ignored expected
                vIndex += 2;    // debug breakpoint
            }
        }
    }

    private void assertSequence (final String                         location,
                                 final Iterator<? extends Number>    iter,
                                 final long ...                        seqVals)
    {
        for (int    vIndex=0; vIndex < seqVals.length; vIndex++)
        {
            assertTrue(location + ": Out of numbers after " + vIndex + " numbers", iter.hasNext());

            final Number    iterVal=iter.next();
            assertEquals(location + ": Mismatched value at index=" + vIndex, seqVals[vIndex], iterVal.longValue());
        }
    }
}
