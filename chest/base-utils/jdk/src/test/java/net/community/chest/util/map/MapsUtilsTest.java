/*
 * 
 */
package net.community.chest.util.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import net.community.chest.AbstractTestSupport;
import net.community.chest.util.collection.CollectionsUtils;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2012 4:01:44 PM
 *
 */
public class MapsUtilsTest extends AbstractTestSupport {
	public MapsUtilsTest ()
	{
		super();
	}

    @Test
    public void testCompareMaps () {
        final List<Integer> values=makeCountingList(Long.SIZE);
        Map<String,Map<?,?>>    comparedMaps=
                Collections.unmodifiableMap(new TreeMap<String, Map<?,?>>(String.CASE_INSENSITIVE_ORDER) {
                        private static final long serialVersionUID = 2410007810257486072L;
                        {
                            put("HASH", populateMap(new HashMap<Number,Number>(values.size()), values));
                            put("TREE", populateMap(new TreeMap<Number,Number>(), values));
                            put("LINKED", populateMap(new LinkedHashMap<Number,Number>(values.size()), values));
                        }
                    });

        for (int    index=0; index < Byte.SIZE; index++) {
            Collections.shuffle(values);
            // the only map whose order we can influence is the LinkedHashMap
            Map<?,?>  shuffledMap=populateMap(new LinkedHashMap<Number,Number>(values.size()), values);
            for (Map.Entry<String,Map<?,?>> ce : comparedMaps.entrySet()) {
                String      type=ce.getKey();
                Map<?,?>    orgMap=ce.getValue();

                assertTrue(type + ": Mismatched contents", MapsUtils.compareMaps(orgMap, shuffledMap));
                assertTrue(type + ": Mismatched reversed contents", MapsUtils.compareMaps(shuffledMap, orgMap));

                assertTrue(type + ": Mismatched containment", MapsUtils.containsAll(orgMap, shuffledMap));
                assertTrue(type + ": Mismatched reversed containment", MapsUtils.containsAll(shuffledMap, orgMap));
            }
        }
    }

    @Test
    public void testSize () {
        assertEquals("Mismatched null size", 0, MapsUtils.size(null));
        assertEquals("Mismatched empty list size", 0, MapsUtils.size(Collections.<Object,Object>emptyMap()));

        List<Integer> values=makeCountingList(Long.SIZE);
        Random        rnd=new Random(System.currentTimeMillis());
        for (int index=0; index < Long.SIZE; index++) {
            Collections.shuffle(values, rnd);
            int numItems=rnd.nextInt(values.size());
            if (numItems == 0)
                numItems = 1;
            List<Integer>   items=values.subList(0, numItems);
            assertEquals("Mismatched size for " + items,
                         numItems, MapsUtils.size(populateMap(new HashMap<Number,Number>(numItems), items)));
        }
    }

    @Test
    public void testPutIfNonNull () {
        Map<String,String>  map=new HashMap<String, String>() {
                private static final long serialVersionUID = -2377934973620640979L;
                {
                    put("key", "value");
                }
            };
       assertFalse("Null value mapped", MapsUtils.putIfNonNull(map, "key", null));
       assertEquals("Map size changed after null", 1, map.size());
       assertEquals("Mapped value changed after null", "value", map.get("key"));
       
       assertTrue("Non-null value not mapped", MapsUtils.putIfNonNull(map, "key", "anotherValue"));
       assertEquals("Map size changed after non-null", 1, map.size());
       assertEquals("Mapped value not changed after non-null", "anotherValue", map.get("key"));

       assertFalse("Null new key mapped", MapsUtils.putIfNonNull(map, "new-key", null));
       assertEquals("Map size changed after null new key", 1, map.size());
       assertNull("New key value exists", map.get("new-key"));

       assertTrue("New key value not mapped", MapsUtils.putIfNonNull(map, "new-key", "new-value"));
       assertEquals("Map size not changed after new key", 2, map.size());
       assertEquals("New key value mismatch", "new-value", map.get("new-key"));
    }

    @Test
    public void testCreateMapEntry () {
        final String    TEST_NAME="testCreateMapEntry";
        final Number    TEST_VALUE=Long.valueOf(System.nanoTime());
        Map.Entry<String,Number>  entry=MapsUtils.createMapEntry(TEST_NAME, TEST_VALUE);
        assertSame("Mismatched key", TEST_NAME, entry.getKey());
        assertSame("Mimstahce value", TEST_VALUE, entry.getValue());
        try {
            entry.setValue(Double.valueOf(Math.random()));
            fail("Unexpected modification of entry value: " + entry);
        } catch(UnsupportedOperationException e) {
            // expected - ignored
        }
    }

    @Test
    public void testFlip () {
    	Map<String,Long>	src=new TreeMap<String, Long>() {
			private static final long serialVersionUID = -3686693573082540693L;

			{
    			put("sysTime", Long.valueOf(7365L));
    			put("nanoTime", Long.valueOf(3777347L));
    		}
    	};
    	
    	Map<Number,CharSequence>	dst=MapsUtils.flip(src, new HashMap<Number,CharSequence>(src.size()));
    	assertEquals("Mismatched size", src.size(), dst.size());
    	for (Map.Entry<String,Long> se : src.entrySet()) {
    		String			expected=se.getKey();
    		Long			value=se.getValue();
    		CharSequence	actual=dst.remove(value);
    		assertSame("Mismatched key for value=" + value, expected, actual);
    	}
    }

    @Test
    public void testFlipNullOrEmpty () {
    	Map<Object,Object>	dst=Collections.unmodifiableMap(new HashMap<Object,Object>());
    	assertSame("Mismatached instance for null source", dst, MapsUtils.flip(null, dst));
    	assertSame("Mismatached instance for empty source", dst, MapsUtils.flip(Collections.<Object,Object>emptyMap(), dst));
    }

	@Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllNoKeys () {
    	Map<String,String>	map=Collections.unmodifiableMap(new HashMap<String,String>());
    	@SuppressWarnings("rawtypes")
		Collection[]		colls={ null, Collections.<String>emptyList() };
    	for (Collection<String> toRemove : colls) {
    		Collection<?>	values=MapsUtils.removeAll(map, toRemove);
    		assertEquals("Mismatched result for " + toRemove, 0, CollectionsUtils.size(values));
    	}
    }

	@Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllNoEntries () {
		Collection<String>	toRemove=Arrays.asList(getClass().getSimpleName(), "testRemoveAllNoEntries");
    	@SuppressWarnings("rawtypes")
		Map[]				maps={ null, Collections.<String,String>emptyMap() };
    	for (Map<String,String> m : maps) {
    		Collection<?>	values=MapsUtils.removeAll(m, toRemove);
    		assertEquals("Mismatched result for " + m, 0, CollectionsUtils.size(values));
    	}
	}

    static List<Integer> makeCountingList (int numMembers) {
    	List<Integer>	list=new ArrayList<Integer>(numMembers);
    	for (int index=0; index < numMembers; index++)
    		list.add(Integer.valueOf(index));
    	return list;
    }

    static <M extends Map<Number,Number>> M populateMap (M map, Collection<? extends Number> values) {
        for (Number n : values) {
            assertNull("Multiple mapped values for " + n, map.put(n, n));
        }

        assertTrue(map.getClass().getSimpleName() + " not equal to itself", MapsUtils.compareMaps(map, map));
        assertTrue(map.getClass().getSimpleName() + " does not contain itself", MapsUtils.containsAll(map, map));
        return map;
    }

}
