/*
 * 
 */
package org.apache.commons.collections4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.collections4.CollectionSyncResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ExtraCollectionUtils;
import org.junit.ExtendedAssert;
import org.junit.Test;

/**
 * @author Lyor G.
 * @since Oct 11, 2011 9:13:25 AM
 */
public class ExtraCollectionUtilsTest extends ExtendedAssert {
	private static final Random	RANDOMIZER=new Random(System.nanoTime());

	public ExtraCollectionUtilsTest ()
	{
		super();
	}

	@Test
	public void testSizeOf ()
	{
		assertEquals("Mismatched null size", 0, ExtraCollectionUtils.sizeOf(null));
		assertEquals("Mismatched empty size", 0, ExtraCollectionUtils.sizeOf(Collections.EMPTY_LIST));

		final byte[]			data=new byte[1 + RANDOMIZER.nextInt(Byte.SIZE)];
		final Collection<Byte>	coll=new ArrayList<Byte>(data.length);
		for (final byte b : data)
			coll.add(Byte.valueOf(b));
		assertEquals("Mismatched popuplated size", data.length, ExtraCollectionUtils.sizeOf(coll));
	}

	@Test
	public void testsafeCollection ()
	{
		final Collection<?>	nullResult=ExtraCollectionUtils.safeCollection(null);
		assertNotNull("Mismatched null result", nullResult);
		assertEquals("Null result not empty", 0, nullResult.size());

		final Collection<?>	instance=Arrays.asList("hello", "world");
		assertSame("Mismatched not null result", instance, ExtraCollectionUtils.safeCollection(instance));
	}

	@Test
	public void testCalculateSyncActions ()
	{
		final Collection<String>	TOADD=Arrays.asList("a", "b", "c"),
									TOREMOVE=Arrays.asList("g", "h", "i"),
									COMMON=Arrays.asList("d", "e", "f"),
									SRC=CollectionUtils.union(TOADD, COMMON),
									DST=CollectionUtils.union(TOREMOVE, COMMON);
		assertSyncResult("testCalculateSyncActions", ExtraCollectionUtils.calculateSyncActions(SRC, DST), TOADD, TOREMOVE);
	}

	@Test
	public void testCalculateSyncActionsOnEmptyInputs ()
	{
		final Collection<String>	EMPTY=Collections.emptyList(),
									NON_EMPTY=Arrays.asList("a", "b", "c");
		assertSyncResult("SyncBothNull", ExtraCollectionUtils.<String>calculateSyncActions(null,null), EMPTY, EMPTY);
		assertSyncResult("SyncSourceNull", ExtraCollectionUtils.<String>calculateSyncActions(null,EMPTY), EMPTY, EMPTY);
		assertSyncResult("SyncDestinationNull", ExtraCollectionUtils.<String>calculateSyncActions(EMPTY, null), EMPTY, EMPTY);
		assertSyncResult("SyncBothEmpty", ExtraCollectionUtils.calculateSyncActions(EMPTY,EMPTY), EMPTY, EMPTY);

		assertSyncResult("SyncSourceEmpty", ExtraCollectionUtils.calculateSyncActions(EMPTY,NON_EMPTY), EMPTY, NON_EMPTY);
		assertSyncResult("SyncDestinationEmpty", ExtraCollectionUtils.calculateSyncActions(NON_EMPTY, EMPTY), NON_EMPTY, EMPTY);
	}
	
	static <E> void assertSyncResult (final String					testName,
								  	  final CollectionSyncResult<E>	result,
								  	  final Collection<E>			expAddValues,
								  	  final Collection<E>			expDelValues)
	{
		assertNotNull(testName + ": No result calculated", result);

		final Collection<? extends E>	addValues=result.getValuesToAdd(),
										delValues=result.getValuesToRemove();
		if (!CollectionUtils.isEqualCollection(addValues, expAddValues))
			fail(testName + ": Mismatched values to add - expected: " + expAddValues + ", actual: " + addValues);
		if (!CollectionUtils.isEqualCollection(delValues, expDelValues))
			fail(testName + ": Mismatched values to remove - expected: " + expDelValues + ", actual: " + delValues);
	}
}
