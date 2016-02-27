/*
 * 
 */
package net.community.chest.math.test;

import net.community.chest.math.AbstractMathTestSupport;
import net.community.chest.math.ExtraMath;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 22, 2011 1:36:52 PM
 */
public class ExtraMathTest extends AbstractMathTestSupport {
	public ExtraMathTest ()
	{
		super();
	}

	@Test
	public void testAbsoluteIntValue ()
	{
		assertEquals("Mismatched handling of MIN_VALUE", Integer.MAX_VALUE, ExtraMath.abs(Integer.MIN_VALUE));
		assertEquals("Mismatched handling of MAX_VALUE", Integer.MAX_VALUE, ExtraMath.abs(Integer.MAX_VALUE));
		for (int	tIndex=0; tIndex < Short.MAX_VALUE; tIndex++)
		{
			final int	value=_randomizer.nextInt();
			if (value == Integer.MIN_VALUE)
				continue;

			assertEquals("Mismatched absolute value for " + value, Math.abs(value), ExtraMath.abs(value));
		}
	}
	@Test
	public void testAbsoluteLongValue ()
	{
		assertEquals("Mismatched handling of MIN_VALUE", Long.MAX_VALUE, ExtraMath.abs(Long.MIN_VALUE));
		assertEquals("Mismatched handling of MAX_VALUE", Long.MAX_VALUE, ExtraMath.abs(Long.MAX_VALUE));
		for (int	tIndex=0; tIndex < Short.MAX_VALUE; tIndex++)
		{
			final long	value=_randomizer.nextLong();
			if (value == Long.MIN_VALUE)
				continue;

			assertEquals("Mismatched absolute value for " + value, Math.abs(value), ExtraMath.abs(value));
		}
	}
    @Test
    public void testDoubleToLongBits () {
        final double NEGATIVE_ZERO=-0.0d, POSITIVE_ZERO=+0.0d;
        assertEquals("Mismatched positive zero value", 0L, ExtraMath.getLongBits(POSITIVE_ZERO));
        assertEquals("Mismatched negative zero value", 0L, ExtraMath.getLongBits(NEGATIVE_ZERO));
        for (int index=0; index < Long.SIZE; index++) {
            double  v=Math.random();
            if (v != +0.0d)
                assertEquals("Mismatched result for " + v, Double.doubleToLongBits(v), ExtraMath.getLongBits(v));
            else
                assertEquals("Mismatched result for " + v, 0L, ExtraMath.getLongBits(v));
        }
    }
    
    @Test
    public void testIntSignof () {
        for (int    index=0; index < Byte.MAX_VALUE; index++) {
            int value=_randomizer.nextInt();
            if (value < 0)
                assertEquals("Mismatched result for negative=" + value, (-1), ExtraMath.signOf(value));
            else if (value > 0)
                assertEquals("Mismatched result for positive" + value, 1, ExtraMath.signOf(value));
            else
                assertEquals("Mismatched result for zero", 0, ExtraMath.signOf(value));
        }
    }

    @Test
    public void testLongSignof () {
        for (int    index=0; index < Byte.MAX_VALUE; index++) {
            long value=_randomizer.nextLong();
            if (value < 0L)
                assertEquals("Mismatched result for negative=" + value, (-1), ExtraMath.signOf(value));
            else if (value > 0L)
                assertEquals("Mismatched result for positive" + value, 1, ExtraMath.signOf(value));
            else
                assertEquals("Mismatched result for zero", 0, ExtraMath.signOf(value));
        }
    }
}
