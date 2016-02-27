/*
 * 
 */
package net.community.chest.aspectj.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Logger;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 25, 2010 9:20:14 AM
 *
 */
public class RunnableAspectTester extends TestBase implements Runnable {
	private static final Random	_r=new Random(System.currentTimeMillis());
	private final int	_tIndex;
	public RunnableAspectTester (final int tIndex)
	{
		_tIndex = tIndex;
	}

	public static final long	MAX_SLEEP_TIME=15000L;
	private static final Logger	_logger=Logger.getLogger(RunnableAspectTester.class.getSimpleName());
	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run ()
	{
		/*
		 * NOTE !!! attempts to use a random boolean to generate an
		 * exception exit causes "Got error code in reply:35 occurred
		 * retrieving 'this' from stack frame." when run from debugger 
		 */
		final long	sleepTime;
		synchronized(_r)
		{
			sleepTime = Math.abs(_r.nextLong()) % MAX_SLEEP_TIME;
		}

		final Thread	t=Thread.currentThread();
		_logger.info("\t" + t.getName() + ": started - sleep time=" +  sleepTime);
		final long		sleepStart=System.currentTimeMillis();
		try
		{
			Thread.sleep(sleepTime);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace(System.err);
		}

		final long	sleepEnd=System.currentTimeMillis(), sleepDuration=sleepEnd - sleepStart;
		_logger.info("\t" + t.getName() + ": ended - sleep duration=" +  sleepDuration);

		if (0 == (_tIndex % 5))
			throw new UnsupportedOperationException("Synthetic exception");
	}

	public static final void main (String[] args)
	{
		final Collection<Thread>	tl=new ArrayList<Thread>();
		for (int	tIndex=0; tIndex < 10; tIndex++)
			tl.add(new Thread(new RunnableAspectTester(tIndex), "t" + RunnableAspectTester.class.getSimpleName() + "#" + tIndex));

		for (final Thread t : tl)
			t.start();

		for (final Thread t : tl)
		{
			try
			{
				t.join(2 * MAX_SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				System.err.append("Interrupted while waiting for thread=")
						  .append(t.getName())
						  .println()
						  ;
			}

			if (t.isAlive())
				System.err.append(t.getName())
				  		  .append(" is still alive")
				  		  .println()
				  		  ;
			else
				System.out.append("Joined ")
						  .append(t.getName())
				  		  .println()
				  		  ;
		}
	}
}
