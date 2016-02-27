/*
 * 
 */
package net.community.chest.javaagent.dumper.filter;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 3:23:16 PM
 */
public class MultiFilterTest extends AbstractFilterTest {
	public MultiFilterTest ()
	{
		super();
	}
	
	@Test
	public void testAcceptingMultiFilter ()
	{
		assertFilterResult(
			new MultiFilter(
					new PatternClassFilter("foo.bar.*"),
					new PatternClassFilter(".*Test*"),
					new PatternClassFilter("bar.foo.*Text*")
						),
			Boolean.TRUE,
			"TestPrefix", "SuffixTest", "InnerTestClass",
				"foo.bar.test.Tester", "bar.foo.internal.Texter");
	}

	@Test
	public void testRejectingMultiFilter ()
	{
		assertFilterResult(
			new MultiFilter(
					new PatternClassFilter("foo.bar.*"),
					new PatternClassFilter(".*Test*"),
					new PatternClassFilter("bar.foo.*Text*")
						),
			Boolean.FALSE,
			"RejectedPrefix", "foo.foo.bar.test.SomeClass", "bar.bar.internal.SomeOtherClass");
	}
}
