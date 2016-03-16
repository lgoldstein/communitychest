/*
 *
 */
package net.community.chest.util.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2012 3:50:09 PM
 */
public class CollectionsUtilsTest extends AbstractTestSupport {
    public CollectionsUtilsTest ()
    {
        super();
    }

    @Test
    public void testPickSample() {
        Random r = new Random(0L);
        @SuppressWarnings("boxing")
        List<Integer> population = Arrays.asList(0, 1, 2, 3, 4);
        List<Integer> samples = CollectionsUtils.pickSample(population, 3, r);
        assertEquals(3, new HashSet<Integer>(samples).size());

        samples = CollectionsUtils.pickSample(population, 5, r);
        assertEquals(5, new HashSet<Integer>(samples).size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPickSampleSamplesTooHigh() {
        Random r = new Random(0L);
        @SuppressWarnings("boxing")
        List<Integer> population = Arrays.asList(0, 1, 2, 3, 4);
        Collection<?>    result=CollectionsUtils.pickSample(population, 6, r);
        fail("Unexpected result: " + result);
    }
    @Test
    public void testCompareCollections () {
        @SuppressWarnings("boxing")
        List<Integer> l1=Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8), l2=new ArrayList<Integer>(l1);
        for (int    index=0; index < Byte.SIZE; index++) {
            Collections.shuffle(l2);
            assertTrue("Non-equivalent shuffle: " + l2, CollectionsUtils.compareCollections(l1, l2));
        }
    }

    @Test
    public void testSize () {
        assertEquals("Mismatched null size", 0, CollectionsUtils.size(null));
        assertEquals("Mismatched empty list size", 0, CollectionsUtils.size(Collections.<Object>emptyList()));
        assertEquals("Mismatched empty set size", 0, CollectionsUtils.size(Collections.<Object>emptySet()));

        Random  rnd=new Random(System.currentTimeMillis());
        for (int index=0; index < Long.SIZE; index++) {
            int numItems=1 + rnd.nextInt(Long.SIZE);
            assertEquals("Mismatched collection size", numItems, CollectionsUtils.size(Collections.nCopies(numItems, Integer.valueOf(index))));
        }
    }

    @Test
    public void testGetFirstMemberOnNullOrEmpty () {
        Collection<?>[]    colls={ null, Collections.<Object>emptyList() };
        for (Collection<?> c : colls) {
            Object    result=CollectionsUtils.getFirstMember(c);
            assertNull("Unexpected result for " + c, result);
        }
    }

    @Test
    public void testGetFirstMemberOnVariousCollections () {
        Object    value=Long.valueOf(System.currentTimeMillis());
        Collection<?>[]    colls={ Collections.singleton(value), Collections.singletonList(value), Arrays.asList(value) };
        for (Collection<?> c : colls) {
            Object        result=CollectionsUtils.getFirstMember(c);
            Class<?>    cc=c.getClass();
            assertSame("Mismatched result for " + cc.getSimpleName(), value, result);
        }
    }

    @Test
    public void testCalculateSyncActions () {
        Collection<String>    TOADD=Arrays.asList("a", "b", "c"),
                            TOREMOVE=Arrays.asList("g", "h", "i"),
                            COMMON=Arrays.asList("d", "e", "f"),
                            SRC=CollectionsUtils.unionToList(TOADD, COMMON),
                            DST=CollectionsUtils.unionToList(TOREMOVE, COMMON);
        assertSyncResult("testCalculateSyncActions", CollectionsUtils.calculateSyncActions(SRC, DST), TOADD, TOREMOVE);
    }

    @Test
    public void testCalculateSyncActionsOnEmptyInputs () {
        Collection<String>    EMPTY=Collections.emptyList(), NON_EMPTY=Arrays.asList("a", "b", "c");
        assertSyncResult("SyncBothNull", CollectionsUtils.<String>calculateSyncActions(null,null), EMPTY, EMPTY);
        assertSyncResult("SyncSourceNull", CollectionsUtils.<String>calculateSyncActions(null,EMPTY), EMPTY, EMPTY);
        assertSyncResult("SyncDestinationNull", CollectionsUtils.<String>calculateSyncActions(EMPTY, null), EMPTY, EMPTY);
        assertSyncResult("SyncBothEmpty", CollectionsUtils.calculateSyncActions(EMPTY,EMPTY), EMPTY, EMPTY);

        assertSyncResult("SyncSourceEmpty", CollectionsUtils.calculateSyncActions(EMPTY,NON_EMPTY), EMPTY, NON_EMPTY);
        assertSyncResult("SyncDestinationEmpty", CollectionsUtils.calculateSyncActions(NON_EMPTY, EMPTY), NON_EMPTY, EMPTY);
    }

    @Test
    public void testFindFirstNonMatchingIndex () {
        final List<String>    values=Arrays.asList(
                    getClass().getSimpleName(),
                    "testFindFirstNonMatchingIndex",
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(Math.random()));
        final int    NUM_VALUES=values.size();
        assertEquals("List not matches itself", -1, CollectionsUtils.findFirstNonMatchingIndex(values, values));
        assertEquals("List not matches sub-list of itself", -1, CollectionsUtils.findFirstNonMatchingIndex(values, values, NUM_VALUES / 2));
        assertEquals("List not matches itself using comparator", -1, CollectionsUtils.findFirstNonMatchingIndex(values, values, String.CASE_INSENSITIVE_ORDER));

        final List<String>    subList=values.subList(0, NUM_VALUES-1);
        assertEquals("Mismatched sub-list first index", subList.size(), CollectionsUtils.findFirstNonMatchingIndex(subList, values));
        assertEquals("Mismatched sub-list second index", subList.size(), CollectionsUtils.findFirstNonMatchingIndex(values, subList));
    }

    private static <E> void assertSyncResult (String                    testName,
                                                  CollectionSyncResult<E>    result,
                                                  Collection<E>                expAddValues,
                                                  Collection<E>                expDelValues) {
        assertNotNull(testName + ": No result calculated", result);

        Collection<? extends E>    addValues=result.getValuesToAdd(), delValues=result.getValuesToRemove();
        if (!CollectionsUtils.compareCollections(addValues, expAddValues)) {
            fail(testName + ": Mismatched values to add - expected: " + expAddValues + ", actual: " + addValues);
        }

        if (!CollectionsUtils.compareCollections(delValues, expDelValues)) {
            fail(testName + ": Mismatched values to remove - expected: " + expDelValues + ", actual: " + delValues);
        }
    }
}
