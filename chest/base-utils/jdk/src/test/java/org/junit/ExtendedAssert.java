/*
 * 
 */
package org.junit;

import java.util.Collection;

/**
 * @author Lyor G.
 * @since Oct 17, 2011 2:18:06 PM
 */
public class ExtendedAssert extends Assert {
	public ExtendedAssert ()
	{
		super();
	}

	public static final void assertMatches (boolean expected, boolean actual)
    {
    	assertMatches(null, expected, actual);
    }

    public static final void assertMatches (String message, boolean expected, boolean actual)
    {
    	assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    public static final void assertObjectInstanceof (String message, Class<?> expected, Object actual)
    {
    	assertInstanceof(message, expected, (actual == null) ? null : actual.getClass());
    }

    public static final void assertInstanceof (String message, Class<?> expected, Class<?> actual)
    {
    	if ((actual == null) || (!expected.isAssignableFrom(actual)))
    		assertEquals(message, expected.getName(), (actual == null) ? null : actual.getName());
    }
   
    public static final void assertContainsAll (String message, Collection<?> container, Collection<?> values)
    {
    	final int	szContainer=(container == null) ? 0 : container.size(),
    				szValues=(values == null) ? 0 : values.size();
    	if (szValues <= 0)
    		return;

    	if (szContainer <= 0)
    	{
    		assertEquals(message + "[empty container]", 0, szValues);
    		return;
    	}

    	for (final Object v : values)
    		assertTrue(message + "[" + v + "]", container.contains(v));
    }

    public static final void assertNotMatches (boolean v1, boolean v2)
    {
    	assertNotMatches(null, v1, v2);
    }

    public static final void assertNotMatches (String message, boolean v1, boolean v2)
    {
		if (v1 == v2)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (float v1, float v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, float v1, float v2)
    {
		if (Float.compare(v1, v2) == 0)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (double v1, double v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, double v1, double v2)
    {
		if (Double.compare(v1, v2) == 0)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (int v1, int v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, int v1, int v2)
    {
		if (v1 == v2)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (long v1, long v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, long v1, long v2)
    {
		if (v1 == v2)
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

    public static final void assertNotEquals (Object v1, Object v2)
    {
    	assertNotEquals(null, v1, v2);
    }

    public static final void assertNotEquals (String message, Object v1, Object v2)
    {
		if ((v1 == v2)
		 ||	((v1 != null) && v1.equals(v2))
		 || ((v2 != null) && v2.equals(v1)))
			fail(message + ": v1=" + v1 + ", v2=" + v2);
    }

	public static final void assertNotNan (final String message, final double value)
	{
		if (Double.isNaN(value))
			fail(message + ": expected: " + value + ", actual: NaN");
	}

	public static final void assertNan (final String message, final double value)
	{
		if (!Double.isNaN(value))
			fail(message + ": expected: NaN, actual: " + value);
	}
}
