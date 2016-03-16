/*
 *
 */
package net.community.chest.math.test.euler;

import java.util.Iterator;

import net.community.chest.math.FibonacciIterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * By considering the terms in the Fibonacci sequence whose
 * values do not exceed four million, find the sum of the even-valued terms.
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 12, 2011 3:27:58 PM
 * @see <A HREF="http://projecteuler.net/index.php?section=problems&id=2">Problem #2</A>
 */
public class Problem2Test extends Assert {
    public Problem2Test ()
    {
        super();
    }

    @Test
    public void solve ()
    {
        long    sum=0L;
        for (final Iterator<? extends Number>    iter=new FibonacciIterator(); ; )
        {
            assertTrue("Out of numbers before enumerating all the required ones", iter.hasNext());

            final Number    v=iter.next();
            final long        vv=v.longValue();
            if (vv > 4000000L)
                break;

            if ((vv & 0x01) == 0L)
                sum += vv;
        }

        System.out.println("Result: " + sum);
    }
}
