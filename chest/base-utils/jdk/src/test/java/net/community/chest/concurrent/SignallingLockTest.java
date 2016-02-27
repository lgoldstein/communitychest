/*
 * 
 */
package net.community.chest.concurrent;

import java.util.concurrent.TimeUnit;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 */
public class SignallingLockTest extends AbstractTestSupport {
	public SignallingLockTest ()
	{
		super();
	}

    @Test
    public void testMultipleOffers () throws InterruptedException {
        SignallingLock  lock=new SignallingLock();
        assertFalse("Mismatched initial state", lock.peek());
        assertTrue("Multiple initial offer", lock.offer());
        assertFalse("Bad initial signal value", lock.offer());

        for (int    index=0; index < Byte.SIZE; index++) {
            assertFalse("Signal reset after " + index + " offers", lock.offer());
            assertTrue("Bad signal value after " + index + " offers", lock.peek());
        }

        assertTrue("No signalling", lock.poll(10L));
        for (int    index=0; index < Byte.SIZE; index++) {
            assertFalse("Unexpected signalling after " + index + " polls", lock.poll(10L));
            assertFalse("Bad signal value after " + index + " polls", lock.peek());
        }
    }

    @Test
    public void testPoll () throws InterruptedException {
        SignallingLock  lock=new SignallingLock();
        final long      WAIT_TIME=125L;
        long            waitStart=System.nanoTime();
        assertFalse("Mismatched initial state", lock.peek());
        assertFalse("Unexpected signalling", lock.poll(WAIT_TIME));

        long    waitEnd=System.nanoTime(), waitDiff=TimeUnit.NANOSECONDS.toMillis(waitEnd - waitStart);
        assertTrue("Wait time too short: " + waitDiff, waitDiff >= WAIT_TIME);
        
        assertTrue("Multiple offers", lock.offer());
        assertTrue("No signalling", lock.poll(WAIT_TIME));
        assertFalse("Bad post-poll signal value", lock.peek());
    }
}
