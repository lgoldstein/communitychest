/*
 * 
 */
package net.community.chest.util.set;

import java.util.Set;

import net.community.chest.util.compare.InstancesComparator;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since May 19, 2011 1:42:05 PM
 */
public class UniqueInstanceSetTest extends Assert {
	public UniqueInstanceSetTest ()
	{
		super();
	}

	@Test
	public void testInstancesComparator ()
	{
		final InstancesComparator<Double>	comparator=new InstancesComparator<Double>(Double.class);
		final Double						V1=Double.valueOf(1669974), V2=Double.valueOf(V1.doubleValue());
		assertEquals("Mis-initialized test values", V1, V2);
		assertNotSame("Malformed initial values", V1, V2);
		assertEquals("Mis-detected same instance", 0, comparator.compare(V1, V1));
		assertTrue("Mis-detected different instances", comparator.compare(V1, V2) != 0);
	}

	@Test
	public void testUniqueInstanceSet ()
	{
		final Set<Double>	testedSet=new UniqueInstanceSet<Double>(Double.class);
		final Double[]		values={ Double.valueOf(-7.3196E-5), Double.valueOf(3777347), Double.valueOf(1.7041690) };
		for (final Double v : values)
			assertTrue("Failed to add value=" + v, testedSet.add(v));
		assertEquals("Mismatched initialization size", values.length, testedSet.size());

		// make sure that re-adding the same instances does not modify the set 
		for (final Double v : values)
			assertFalse("Duplicated value=" + v, testedSet.add(v));
		assertEquals("Mismatched re-initialization size", values.length, testedSet.size());

		// make sure that adding new instances of the same value(s) succeeds
		for (final Double v : values)
			assertTrue("Failed to re-add value=" + v, testedSet.add(Double.valueOf(v.doubleValue())));
		assertEquals("Mismatched duplication size", values.length * 2, testedSet.size());
	}
}
