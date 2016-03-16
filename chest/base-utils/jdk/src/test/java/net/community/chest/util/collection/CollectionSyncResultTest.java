/*
 *
 */
package net.community.chest.util.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2012 3:19:44 PM
 *
 */
public class CollectionSyncResultTest extends AbstractTestSupport {
    public CollectionSyncResultTest ()
    {
        super();
    }

    @Test
    public void testExecuteActions () {
        Collection<String>    TOADD=Arrays.asList("a", "b", "c"),
                            TOREMOVE=Arrays.asList("g", "h", "i"),
                            COMMON=Arrays.asList("d", "e", "f");
        assertSyncExecutionResult("testExecuteActions",
                                  CollectionsUtils.unionToList(TOADD, COMMON),
                                  CollectionsUtils.unionToList(TOREMOVE, COMMON),
                                  true);
    }

    @Test
    public void testExecuteActionsOnEmptyInputs () {
        Collection<String>    EMPTY=Collections.emptyList(), NON_EMPTY=Arrays.asList("a", "b", "c");
        assertSyncExecutionResult("ExecBothEmpty", EMPTY, EMPTY, false);
        assertSyncExecutionResult("ExecSourceEmpty", EMPTY, NON_EMPTY, true);
        assertSyncExecutionResult("ExecDestinationEmpty", NON_EMPTY, EMPTY, true);
    }

    private static <E> void assertSyncExecutionResult (String                    testName,
                                                          Collection<? extends E>    src,
                                                          Collection<? extends E>    orgDst,
                                                          boolean                    expReturnValue)
    {
        final CollectionSyncResult<E>    result=CollectionsUtils.calculateSyncActions(src, orgDst);
        final Collection<E>                dst=new ArrayList<E>(orgDst);    // enable manipulation
        final boolean                    returnValue=result.executeActions(dst);
        assertEquals(testName + ": Mismatched execution return value", Boolean.valueOf(expReturnValue), Boolean.valueOf(returnValue));

        if (!CollectionsUtils.compareCollections(dst, src)) {
            fail(testName + ": Mismatched values to add - expected: " + src + ", actual: " + dst);
        }
    }

}
