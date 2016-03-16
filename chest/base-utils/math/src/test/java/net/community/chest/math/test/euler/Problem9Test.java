/*
 *
 */
package net.community.chest.math.test.euler;

import org.junit.Assert;
import org.junit.Test;

/**
 * There exists exactly one Pythagorean triplet for which <code>a + b + c = 1000</code>.
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 12, 2011 3:53:01 PM
 * @see <A HREF="http://projecteuler.net/index.php?section=problems&id=9">Problem #9</A>
 */
public class Problem9Test extends Assert {
    public Problem9Test ()
    {
        super();
    }

    @Test
    public void solve ()
    {
        final long    startTime=System.nanoTime();
        for (int    a=1, aSquared=1, aTwice=2;
             a < 999; // square(a+b)=square(a) + square(b) + 2ab
             aSquared += aTwice + 1, a++, aTwice += 2)
        {
            for (int b=a, sum=aTwice, bSquared=aSquared, c=1000 - sum, cSquared=c * c;
                 sum < 1000;
                 bSquared=nextSquare(b, bSquared), b++, sum++, cSquared=prevSquare(c, cSquared), c--)
            {
                if ((aSquared + bSquared) == cSquared)
                {
                    final long    endTime=System.nanoTime(), duration=endTime - startTime;
                    assertEquals("Violating sum constraint", a + b + c, 1000);
                    assertEquals("Non-pythagorean triplet", (a * a) + (b * b), c * c);
                    System.out.println("Result in " + duration + " nano.: " + a + "," + b + "," + c);
                    return;
                }
            }
        }

        final long    endTime=System.nanoTime(), duration=endTime - startTime;
        fail("No result found after " + duration + " nano");
    }

    // based on square(a+b)=square(a) + square(b) + 2ab
    public static final int nextSquare (final int v, final int vSquare)
    {
        return vSquare + (v << 1) + 1;
    }

    // based on square(a-b)=square(a) + square(b) - 2ab
    public static final int prevSquare (final int v, final int vSquare)
    {
        return vSquare - (v << 1) + 1;
    }
}
