/*
 *
 */
package net.community.chest.util.compare;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 3, 2011 3:11:52 PM
 */
public class CharSequenceComparatorTest extends Assert {
    public CharSequenceComparatorTest ()
    {
        super();
    }

    @Test
    public void testDefaultComparators ()
    {
        final Random    rnd1=new Random(System.nanoTime()),    // different seeds
                        rnd2=new Random(System.currentTimeMillis());
        for (int    tIndex=0; tIndex < Short.MAX_VALUE; tIndex++)
        {
            final String    s1=String.valueOf(rnd1.nextDouble()),
                            s2=String.valueOf(rnd2.nextDouble()),
                            msgPrefix=s1 + " <==> " + s2;
            final int        expRes=s1.compareTo(s2),
                            ascRes=CharSequenceComparator.ASCENDING.compare(s1, s2),
                            dscRes=CharSequenceComparator.DESCENDING.compare(s1, s2);
            assertEquals(msgPrefix + " mismatched ASC result", expRes, ascRes);
            assertEquals(msgPrefix + " mismatched DSC result", 0 - expRes, dscRes);
            assertEquals(msgPrefix + " mismatched complementing result", 0 - ascRes, dscRes);
        }
    }

    @Test
    public void testNullOrEmptyCases ()
    {
        assertEquals("null <==> null", 0, CharSequenceComparator.ASCENDING.compare(null, null));
        assertEquals("null <==> EMPTY", 0, CharSequenceComparator.ASCENDING.compare(null, ""));
        assertEquals("EMPTY <==> null", 0, CharSequenceComparator.ASCENDING.compare("", null));
        assertEquals("EMPTY <==> EMPTY", 0, CharSequenceComparator.ASCENDING.compare("", ""));
    }

    @Test
    public void testSuffixOrPrefix ()
    {
        final String    COMMON="commonPart";
        assertTrue("Prefixed", CharSequenceComparator.ASCENDING.compare(COMMON, COMMON + System.currentTimeMillis()) < 0);
        assertTrue("Suffixed", CharSequenceComparator.ASCENDING.compare(COMMON + System.nanoTime(), COMMON) > 0);
    }
}
