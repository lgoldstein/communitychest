/*
 * 
 */
package net.community.chest.javaagent.dumper.filter;

import java.util.regex.PatternSyntaxException;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 2:44:47 PM
 */
public class PatternClassFilterTest extends AbstractFilterTest {
	public PatternClassFilterTest ()
	{
		super();
	}

	@Test
	public void testSuccessfulConvertToRegexp ()
	{
		assertSame("Mismatched include-all pattern", ".*", assertCompiledRegexpPattern(".*"));

		final String[]	PATTERNS={
				"foo.bar.*",		"foo\\.bar\\..*",
				".*Foo*Bar",		".*Foo.*Bar",
				"foo.bar.*Foo*Bar", "foo\\.bar\\..*Foo.*Bar"
			};
		for (int	pIndex=0; pIndex < PATTERNS.length; pIndex += 2)
		{
			final String	pattern=PATTERNS[pIndex],
							rgxActual=assertCompiledRegexpPattern(pattern),
							rgxExpected=PATTERNS[pIndex + 1];
			assertEquals("Mismatched conversion result for pattern=" + pattern, rgxExpected, rgxActual);
		}
	}

	@Test
	public void testConvertToRegexpFailure ()
	{
		final String[]	PATTERNS={
				null, "", "double**wildcard", "NoPackageSpec", "No.Class.Spec.",
				"Double..Dots"
			};
		for (final String badPattern : PATTERNS)
		{
			try
			{
				final String	rgx=PatternClassFilter.convertToRegexp(badPattern);
				fail("Unexpected conversion success for " + badPattern + ": " + rgx);
			}
			catch(PatternSyntaxException e)
			{
				// expected, ignored
			}
		}
	}

	@Test
	public void testFilterMatching ()
	{
		assertFilterResult(new PatternClassFilter("foo.bar.*"), Boolean.TRUE,
						   "foo.bar.TopLevelClass",
						   "foo.bar.internal.InternalClass",
						   "foo.bar.inner.Foo$InnerClass");
		assertFilterResult(new PatternClassFilter("foo.bar.*Test*"), Boolean.TRUE,
				   "foo.bar.TopTestClass",
				   "foo.bar.internal.TestPrefixClass",
				   "foo.bar.internal.SuffixClassTest",
				   "foo.bar.inner.testing.Foo$InnerTestClass");
	}

	@Test
	public void testFilterNotMatching ()
	{
		assertFilterResult(new PatternClassFilter("foo.bar.*"), Boolean.FALSE,
						   "foo.TopLevelClass",
						   "foo.other.OtherClass",
						   "net.bar.NetClass",
						   "DefaultClass");
		assertFilterResult(new PatternClassFilter("foo.bar.*Test*"), Boolean.FALSE,
				   "foo.bar.TopTextClass",
				   "foo.bar.internal.TextPrefixClass",
				   "foo.bar.internal.SuffixClassText",
				   "foo.bar.inner.test.Foo$InnerTextClass");
	}
}
