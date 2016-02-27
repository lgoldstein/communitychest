/*
 * 
 */
package net.community.chest.javaagent.dumper.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 12:26:48 PM
 */
public class InfoUtilsTest extends Assert {
	public InfoUtilsTest ()
	{
		super();
	}

	@Test
	public void testEncodeMethodName ()
	{
		assertSame("Mismatched null name instances", null, InfoUtils.encodeMethodName(null));
		assertSame("Mismatched empty name instances", "", InfoUtils.encodeMethodName(""));
		assertSame("Mismatched unchanged name instances", "fooBar", InfoUtils.encodeMethodName("fooBar"));
		assertEquals("Mismatched bracketed name", "&lt;init&gt;",InfoUtils.encodeMethodName("<init>"));

		for (final String	badName : new String[] { "<missingEndBracket", "missingStartBracket>" }) {
			try
			{
				final String	encName=InfoUtils.encodeMethodName(badName);
				fail("Unexpected successful encoding of " + badName + ": " + encName);
			}
			catch(IllegalArgumentException e)
			{
				// expected, ignored
			}
		}
	}

	@Test
	public void testDecodeMethodName ()
	{
		assertSame("Mismatched null name instances", null, InfoUtils.decodeMethodName(null));
		assertSame("Mismatched empty name instances", "", InfoUtils.decodeMethodName(""));
		assertSame("Mismatched unchanged name instances", "fooBar", InfoUtils.decodeMethodName("fooBar"));
		assertEquals("Mismatched bracketed name", "<init>",InfoUtils.decodeMethodName("&lt;init&gt;"));

		for (final String	badName : new String[] { "&lt;missingEndBracket", "missingStartBracket&gt;" }) {
			try
			{
				final String	decName=InfoUtils.decodeMethodName(badName);
				fail("Unexpected successful decoding of " + badName + ": " + decName);
			}
			catch(IllegalArgumentException e)
			{
				// expected, ignored
			}
		}
	}
}
