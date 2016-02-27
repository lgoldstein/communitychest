/*
 * 
 */
package org.apache.commons.collections4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections4.CollectionSyncResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ExtraCollectionUtils;
import org.junit.ExtendedAssert;
import org.junit.Test;

/**
 * @author Lyor G.
 * @since Jan 25, 2012 9:26:04 AM
 */
public class CollectionSyncResultTest extends ExtendedAssert {
	public CollectionSyncResultTest ()
	{
		super();
	}

	@Test
	public void testExecuteActions ()
	{
		final Collection<String>	TOADD=Arrays.asList("a", "b", "c"),
									TOREMOVE=Arrays.asList("g", "h", "i"),
									COMMON=Arrays.asList("d", "e", "f");
		assertSyncExecutionResult("testExecuteActions",
								  CollectionUtils.union(TOADD, COMMON),
								  CollectionUtils.union(TOREMOVE, COMMON),
								  true);
	}
	@Test
	public void testExecuteActionsOnEmptyInputs ()
	{
		final Collection<String>	EMPTY=Collections.emptyList(),
									NON_EMPTY=Arrays.asList("a", "b", "c");
		assertSyncExecutionResult("ExecBothEmpty", EMPTY, EMPTY, false);
		assertSyncExecutionResult("ExecSourceEmpty", EMPTY, NON_EMPTY, true);
		assertSyncExecutionResult("ExecDestinationEmpty", NON_EMPTY, EMPTY, true);
	}

	static <E> void assertSyncExecutionResult (final String						testName,
											   final Collection<? extends E>	src,
											   final Collection<? extends E>	orgDst,
											   final boolean					expReturnValue)
	{
		final CollectionSyncResult<E>	result=ExtraCollectionUtils.calculateSyncActions(src, orgDst);
		final Collection<E>				dst=new ArrayList<E>(orgDst);	// enable manipulation
		final boolean					returnValue=result.executeActions(dst);
		assertMatches(testName + ": Mismatched execution return value", expReturnValue, returnValue);
		
		if (!CollectionUtils.isEqualCollection(dst, src))
			fail(testName + ": Mismatched values to add - expected: " + src + ", actual: " + dst);
	}
}
