/*
 * 
 */
package net.community.chest.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Oct 10, 2011 12:49:22 PM
 */
public class BlockingSetTest extends AbstractTestSupport {
	public BlockingSetTest ()
	{
		super();
	}
	/**
	 * Makes sure that {@link BlockingSet#add(Object)} behaves like a set 
	 */
	@Test
	public void testSetLikeBehavior ()
	{
		final BlockingSet<Integer>	blkSet=new BlockingSet<Integer>();
		final Set<Integer>			valSet=new TreeSet<Integer>();
		for (int	index=0; index < Byte.MAX_VALUE; index++)
		{
			final Integer	value=Integer.valueOf(RANDOMIZER.nextInt());
			assertMatches("Mismatched insertion result on " + value, blkSet.add(value), valSet.add(value));
			assertEquals("Mismatched sizes on insertion of " + value, blkSet.size(), valSet.size());
		}

		for (final Integer value : valSet)
		{
			assertTrue("Value not found: " + value, blkSet.contains(value));
			assertTrue("Failed to offer " + value, blkSet.offer(value));
			assertEquals("Mismatched sizes on offer of " + value, blkSet.size(), valSet.size());
		}
		
		assertTrue("Missing values", blkSet.containsAll(valSet));
	}

	@Test
	public void testPollingEmptySet () throws InterruptedException
	{
		final BlockingSet<Integer>	blkSet=new BlockingSet<Integer>();
		assertNull("Unexpected initial polled value", blkSet.poll());

		final long		waitTime=TimeUnit.MILLISECONDS.toNanos(107L + RANDOMIZER.nextInt(Byte.MAX_VALUE));
		final long		waitStart=System.nanoTime();
		final Object	result=blkSet.poll(waitTime, TimeUnit.NANOSECONDS);
		final long		waitEnd=System.nanoTime(), nanoWait=waitEnd - waitStart;

		assertNull("Unexpected result after " + nanoWait + " nanos: " + result, result);
		assertTrue("Wait time too small: expected=" + waitTime + "/waited=" + nanoWait, nanoWait >= waitTime);
		assertTrue("Wait time too big: expected=" + waitTime + "/waited=" + nanoWait,
				   nanoWait < (waitTime + TimeUnit.MILLISECONDS.toNanos(100L)));
	}

	@Test
	public void testMultithreadedPoll () throws InterruptedException
	{
		final BlockingSet<Integer>	blkSet=new BlockingSet<Integer>();
		final Set<Integer>			valSet=new TreeSet<Integer>();
		{
			int	numDuplicates=0;
			for (int	index=0; index < Short.MAX_VALUE; index++)
			{
				// we generate duplicate values on purpose
				final Integer	value=Integer.valueOf(RANDOMIZER.nextInt(Short.MAX_VALUE / Long.SIZE));
				assertTrue("Failed to offer value=" + value, blkSet.offer(value));
				if (!valSet.add(value))
					numDuplicates++;
			}

			assertTrue("No duplicates generated", numDuplicates > 0);
		}

		final Map<String,Set<Integer>>	valsMap=new TreeMap<String,Set<Integer>>(String.CASE_INSENSITIVE_ORDER);
		final List<Thread> 				tList=startConsumerThreads(blkSet, valsMap);
		_logger.info("Started consumers");

		waitForEmptySet(blkSet, valSet.size());
		_logger.info("Set empty");

		stopConsumerThreads(tList);
		_logger.info("Stopped consumers");

		assertMultithreadedResults(valsMap, valSet);
	}

	@Test
	public void testPipelinedPoll () throws InterruptedException
	{
		final BlockingSet<Integer>		blkSet=new BlockingSet<Integer>();
		final Map<String,Set<Integer>>	valsMap=new TreeMap<String,Set<Integer>>(String.CASE_INSENSITIVE_ORDER);
		final List<Thread> 				tList=startConsumerThreads(blkSet, valsMap);
		_logger.info("Started consumers");

		final Set<Integer>	valSet=new TreeSet<Integer>();
		for (int	index=1; index <= Short.MAX_VALUE; index++)
		{
			// we generate duplicate values on purpose
			final Integer	value=Integer.valueOf(RANDOMIZER.nextInt(Short.MAX_VALUE / Long.SIZE));
			if (!valSet.add(value))
				continue;

			final long		waitTime=1L + RANDOMIZER.nextInt(Byte.SIZE);
			Thread.sleep(waitTime);

			assertTrue("Failed to offer " + value, blkSet.offer(value));
		}

		waitForEmptySet(blkSet, valSet.size());
		_logger.info("Set empty");

		stopConsumerThreads(tList);
		_logger.info("Stopped consumers");

		assertMultithreadedResults(valsMap, valSet);
	}

	private List<Thread> startConsumerThreads (final BlockingSet<Integer>		blkSet,
											   final Map<String,Set<Integer>>	valsMap)
	{
		final int			NUM_THREADS=Byte.SIZE;
		final List<Thread> 	tList=new ArrayList<Thread>(NUM_THREADS);
		for (int	index=0; index < NUM_THREADS; index++)
		{
			final String	name="tConsumer" + index;
			assertNull("Multiple mappings for " + name, valsMap.put(name, new TreeSet<Integer>()));

			final Thread	t=new Thread(new Runnable() {
					/*
					 * @see java.lang.Runnable#run()
					 */
					@SuppressWarnings("synthetic-access")
					@Override
					public void run ()
					{
						final Thread		curThread=Thread.currentThread();
						final String		tName=curThread.getName();
						final Set<Integer>	tValues=valsMap.get(tName);
						for (int	numValues=0; ; )
						{
							final long		waitTime=TimeUnit.MILLISECONDS.toNanos(1L + RANDOMIZER.nextInt(Long.SIZE));
							try
							{
								final Integer	value=blkSet.poll(waitTime, TimeUnit.NANOSECONDS);
								if (value == null)
									continue;

								final boolean	result=tValues.add(value);
								if (result)
									numValues++;

								if (_logger.isLoggable(Level.FINE))
									_logger.fine("run(" + tName + ") process value=" + value + ": " + result);
							}
							catch(InterruptedException e)
							{
								_logger.info("Stopped after " + numValues + " distinct values");
								curThread.interrupt();
								break;
							}
						}
					}
				});
			t.setName(name);
			tList.add(t);
			t.start();
			
			if (_logger.isLoggable(Level.FINE))
				_logger.fine("Started " + name);
		}

		return tList;
	}

	private void waitForEmptySet (final BlockingSet<?> blkSet, final int maxWaitLoop)
			throws InterruptedException
	{
		for (int index=0; index < maxWaitLoop; index++)
		{
			if (blkSet.isEmpty())
			{
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("Set enmptied after " + index + " retries");
				break;
			}

			Thread.sleep(Byte.MAX_VALUE);
			if (index > 0)
				_logger.info("Sleep " + index + " out of " + maxWaitLoop);
		}
	}

	private void stopConsumerThreads (final Collection<? extends Thread> tList)
		throws InterruptedException
	{
		for (final Thread t : tList)
		{
			t.interrupt();
			t.join(2 * Byte.MAX_VALUE);
			if (t.isAlive())
				_logger.warning("Thread still alive: " + t.getName());
			else if (_logger.isLoggable(Level.FINE))
				_logger.fine("Thread stopped: " + t.getName());
		}
	}

	private void assertMultithreadedResults (final Map<String,? extends Collection<Integer>>	valsMap,
											 final Collection<Integer>							valSet)
	{
		final Set<Integer>	procSet=new HashSet<Integer>(valSet.size());
		for (final Map.Entry<String,? extends Collection<Integer>> valEntry : valsMap.entrySet())
		{
			final String				name=valEntry.getKey();
			final Collection<Integer>	accVals=valEntry.getValue();
			for (final Integer value : accVals)
				assertFalse(name + ": duplicated value " + value, procSet.contains(value));
			procSet.addAll(accVals);
		}

		assertEquals("Mismatched processed values set", valSet.size(), procSet.size());
		assertTrue("Not all values processed", procSet.containsAll(valSet));
	}

}
