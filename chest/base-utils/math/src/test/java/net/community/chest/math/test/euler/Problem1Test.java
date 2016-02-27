/*
 * 
 */
package net.community.chest.math.test.euler;

import net.community.chest.math.DivisionSigns;

import org.junit.Assert;
import org.junit.Test;

/**
 * Find the sum of all the multiples of 3 or 5 below 1000.
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 12, 2011 3:36:00 PM
 * @see <A HREF="http://projecteuler.net/index.php?section=problems&id=1">Problem #2</A>
 */
public class Problem1Test extends Assert {
	public Problem1Test ()
	{
		super();
	}

	@Test
	public void solve ()
	{
		long	sum=0L;
		for (long	value=3L; value < 1000L; value++)
		{
			final String	strValue=String.valueOf(value);
			if (DivisionSigns.isMultiple3(strValue)
			 || DivisionSigns.isMultiple5(strValue))
				sum += value;
		}

		System.out.println("Result: " + sum);
	}
}
