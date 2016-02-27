/*
 * 
 */
package net.community.chest.lang;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since May 19, 2011 3:16:26 PM
 */
public class SysPropsEnumTest extends AbstractTestSupport {
	public SysPropsEnumTest ()
	{
		super();
	}

	@Test
	public void testSysPropValueRetrieval ()
	{
		for (final SysPropsEnum expProp : SysPropsEnum.VALUES)
		{
			final String	propName=expProp.getPropertyName();
			final String	expValue=System.getProperty(propName), actValue=expProp.getPropertyValue();
			// Make sure that {@link SysPropsEnum#getPropertyValue()} matches System#getProperty
			assertEquals("Mismatched values for property=" + propName, expValue, actValue);

			final SysPropsEnum	actProp=SysPropsEnum.fromProperty(propName);
			// Make sure SysPropsEnum#fromProperty returns the exact same instance
			assertSame("Mismatched property enumeration for " + propName, expProp, actProp);
		}
	}
}
