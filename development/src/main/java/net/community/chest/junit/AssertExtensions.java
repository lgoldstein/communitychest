/*
 * 
 */
package net.community.chest.junit;

import java.util.Comparator;

import net.community.chest.math.compare.ComparisonExecutor;

import org.junit.Assert;

/**
 * <P>Copyright as per GPLv2</P>
 * Some extensions for {@link Assert} 
 * @author Lyor G.
 * @since Aug 24, 2010 10:58:16 AM
 */
public class AssertExtensions extends Assert {
	protected AssertExtensions ()
	{
		super();
	}

	public static String formatClassAndValue (Object value, String valueString)
	{
		final String className;
		if (value == null)
			className = "null";
		else if (value instanceof Class<?>)
			className = ((Class<?>) value).getName();
		else
			className = value.getClass().getName();
		return className + "<" + valueString + ">";
	}

	public static String format (String message, Object expected, Object actual)
	{
		String formatted = "";
		if (message != null && !message.equals(""))
			formatted = message + " ";
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		if (expectedString.equals(actualString))
			return formatted + "expected: "
					+ formatClassAndValue(expected, expectedString)
					+ " but was: " + formatClassAndValue(actual, actualString);
		else
			return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
	}

	public static void assertInstanceOf (String msg, Class<?> expected, Object actual)
	{
		assertAssignable(msg, expected, (null == actual) ? null : actual.getClass());
	}

	public static void assertAssignable (String msg, Class<?> expected, Class<?> actual)
	{
		if ((actual != null) && (expected != null)
		  && expected.isAssignableFrom(actual))
			return;

		fail(format(msg, expected, actual));
	}
	
	public static void assertComparisonResult (String msg, long v1, long v2, ComparisonExecutor result)
	{
		assertComparisonResult(msg, Long.valueOf(v1), Long.valueOf(v2), result);
	}

	public static void assertComparisonResult (String msg, int v1, int v2, ComparisonExecutor result)
	{
		assertComparisonResult(msg, Integer.valueOf(v1), Integer.valueOf(v2), result);
	}

	public static void assertComparisonResult (String msg, short v1, short v2, ComparisonExecutor result)
	{
		assertComparisonResult(msg, Short.valueOf(v1), Short.valueOf(v2), result);
	}

	public static void assertComparisonResult (String msg, byte v1, byte v2, ComparisonExecutor result)
	{
		assertComparisonResult(msg, Byte.valueOf(v1), Byte.valueOf(v2), result);
	}

	public static void assertComparisonResult (String msg, float v1, float v2, ComparisonExecutor result)
	{
		assertComparisonResult(msg, Float.valueOf(v1), Float.valueOf(v2), result);
	}

	public static void assertComparisonResult (String msg, double v1, double v2, ComparisonExecutor result)
	{
		assertComparisonResult(msg, Double.valueOf(v1), Double.valueOf(v2), result);
	}
	/**
	 * @param <V> The type of {@link Comparable} value being compared
	 * @param msg The assertion error message
	 * @param v1 The 1st value
	 * @param v2 The 2nd value
	 * @param result The expected result as a {@link ComparisonExecutor}
	 */
	public static <V extends Comparable<V>> void assertComparisonResult (String msg, V v1, V v2, ComparisonExecutor result)
	{
		final Boolean	nRes=result.invoke(v1, v2);
		assertTrue(msg + ": " + v1 + " (not) " + result + " " + v2, (nRes != null) && nRes.booleanValue());
	}
	/**
	 * @param <V> The type of {@link Comparable} value being compared
	 * @param msg The assertion error message
	 * @param v1 The 1st value
	 * @param v2 The 2nd value
	 * @param c The {@link Comparator} to use
	 * @param result The expected result as a {@link ComparisonExecutor}
	 */
	public static <V> void assertComparisonResult (String msg, V v1, V v2, Comparator<? super V> c, ComparisonExecutor result)
	{
		final Boolean	nRes=result.invoke(c, v1, v2);
		assertTrue(msg + ": " + v1 + " not " + result + " " + v2, (nRes != null) && nRes.booleanValue());
	}
}
