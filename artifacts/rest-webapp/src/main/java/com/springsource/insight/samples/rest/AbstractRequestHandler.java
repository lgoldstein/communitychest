/*
 * 
 */
package com.springsource.insight.samples.rest;

import java.util.Random;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since May 19, 2011 11:52:14 AM
 */
public abstract class AbstractRequestHandler {
	public static final String	DEFAULT_DELAY="0";

	protected final Random	_randomizer;
	protected AbstractRequestHandler ()
	{
		_randomizer = new Random(System.nanoTime());
	}

	/**
	 * Uses a {@link Random} value to select a delay between zero and the
	 * specified maximum value
	 * @param maxDelay Max. delay (msec.) - no delay is executed if non-positive
	 * @return Actual delay (msec.)
	 */
	protected long delay (int maxDelay)
	{
		final long	actualDelay=(maxDelay <= 0) ? maxDelay : _randomizer.nextInt(maxDelay);
		if (actualDelay <= 0L)
			return 0L;

		final long	startTime=System.currentTimeMillis();
		try
		{
			Thread.sleep(actualDelay);
		}
		catch(InterruptedException e)
		{
			// ignored
		}

		return System.currentTimeMillis() - startTime;
	}
}
