/*
 * 
 */
package net.community.chest.reflect;

import net.community.chest.lang.AbstractEnumTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 8:57:34 AM
 */
public class VisibilityTest extends AbstractEnumTestSupport {
	public VisibilityTest ()
	{
		super();
	}

	@Test
	public void testFromString () throws Exception
	{
		assertFromStringValidity(Visibility.class);
	}

	@Test
	public void testFromModifier () throws Exception
	{
		assertFromMethodValidity(Visibility.class, "fromModifier", new EnumTestValueAccessor<Visibility,Integer>() {
				/*
				 * @see net.community.chest.lang.EnumUtilTest.EnumTestValueAccessor#getTestArgumentType()
				 */
				@Override
				public Class<Integer> getTestArgumentType ()
				{
					return Integer.TYPE;
				}
				/*
				 * @see net.community.chest.lang.EnumUtilTest.EnumTestValueAccessor#getTestValue(java.lang.Enum)
				 */
				@Override
				public Integer getTestValue (Visibility value)
				{
					return Integer.valueOf(value.getModifier());
				}
			});
	}
}
