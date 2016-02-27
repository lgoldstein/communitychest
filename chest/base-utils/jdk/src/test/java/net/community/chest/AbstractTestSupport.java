/*
 * 
 */
package net.community.chest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.ExtendedAssert;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 8:59:06 AM
 */
public abstract class AbstractTestSupport extends ExtendedAssert {
	protected final Logger	_logger=Logger.getLogger(getClass().getName());
	protected AbstractTestSupport ()
	{
		super();
	}

	public static final Random	RANDOMIZER=new Random(System.nanoTime());
	public static final long randomSleep (final int  maxSleep)
	{
		if (maxSleep <= 0)
			return 0L;

		final long	sleepTime;
		synchronized(RANDOMIZER)
		{
			if ((sleepTime=RANDOMIZER.nextInt(maxSleep)) <= 0L)
				return 0L;
		}

		try
		{
			Thread.sleep(sleepTime);
		}
		catch(InterruptedException e)
		{
			// ignored
		}

		return sleepTime;
	}

	/*
     * Randomly pick a list of items out of a population.
     * Original code: http://www.javamex.com/tutorials/random_numbers/random_sample.shtml
     */
	public static final <T> List<T> pickSamples (final List<T> population, final int numSamples, final Random r)
	{
		if (numSamples > population.size())
			return population;

		final List<T> res=new ArrayList<T>(numSamples);
		for (int samplesNeeded=numSamples,i=0, nLeft=population.size(); samplesNeeded > 0; )
		{
			final int rand=r.nextInt(nLeft);
			if (rand < samplesNeeded)
			{
				res.add(population.get(i));
				samplesNeeded--;
			}
			nLeft--;
			i++;
		}

		return res;
	}

	public static final <T> T pickOneSample (final List<T> population)
	{
		return pickOneSample(population, RANDOMIZER);
	}

	public static final <T> T pickOneSample (final List<T> population, final Random r)
	{
		if ((population == null) || population.isEmpty())
			return null;

		final int	numValues=population.size();
		final int	index=(numValues == 1) ? 0 : r.nextInt(numValues);
		return population.get(index);
	}

	public static final void encourageGC ()
    {
        System.runFinalization();
        for (int i= 0 ; i < 20; i++)
        {
        	Thread.yield();
            System.gc();
        }
    }
 
    public static final <T> void assertNullComparisonBehavior (
    		final T nonNullValue, final Comparator<? super T> comp, final boolean nullIsGreater)
    {
		assertEquals("Mismatched both null(s) result", 0, comp.compare(null, null));
		if (nullIsGreater)
		{
			assertTrue("Mismatched 1st null result", comp.compare(null, nonNullValue) > 0);
			assertTrue("Mismatched 2nd null result", comp.compare(nonNullValue, null) < 0);
		}
		else
		{
			assertFalse("Mismatched 1st null result", comp.compare(null, nonNullValue) > 0);
			assertFalse("Mismatched 2nd null result", comp.compare(nonNullValue, null) < 0);
		}
    }

    public static final String randomizeCaseSensitivity (final CharSequence value)
    {
    	return randomCaseSensitivityBuilder(value).toString();
    }

    public static final StringBuilder randomCaseSensitivityBuilder (final CharSequence value)
    {
    	return appendRandomCaseSensitivity(new StringBuilder(), value);
    }

    public static final StringBuilder appendRandomCaseSensitivity (final StringBuilder sb, final CharSequence value)
    {
    	if ((value == null) || (value.length() <= 0))
    		return sb;

		for (int	cIndex=0; cIndex < value.length(); cIndex++)
		{
			final char		ch=value.charAt(cIndex);
			final boolean	cs=Character.isUpperCase(ch);
			if (RANDOMIZER.nextBoolean())
				sb.append(cs ? Character.toLowerCase(ch) : Character.toUpperCase(ch));
			else
				sb.append(ch);
		}

		return sb;
    }
}
