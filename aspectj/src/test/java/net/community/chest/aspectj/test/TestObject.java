/*
 * 
 */
package net.community.chest.aspectj.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.community.chest.io.ApplicationIOUtils;
import net.community.chest.lang.StringUtil;

/**
 * @author Lyor G.
 * @since Jul 19, 2010 8:47:17 AM
 */
public class TestObject extends ApplicationIOUtils implements Serializable {
	private static final long serialVersionUID = 5869851726141897384L;

	private String	_value;
	public String getValue ()
	{
		return _value;
	}

	public void setValue (String value)
	{
		_value = value;
	}

	public TestObject (String value)
	{
		_value = value;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getValue(), true);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof TestObject))
			return false;
		if (this == obj)
			return true;

		return (0 == StringUtil.compareDataStrings(getValue(), ((TestObject) obj).getValue(), true)); 
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getValue();
	}

	//////////////////////////////////////////////////////////////////////////

	private static final void showReflectedFields (
			final PrintStream out, final Object o)
		throws IllegalArgumentException, IllegalAccessException
	{
		final Class<?>	c=(null == o) ? null : o.getClass();
		final Field[]	fa=(null == c) ? null : c.getDeclaredFields();
		if ((null == fa) || (fa.length <= 0))
			return;

		out.append(c.getName()).append(" fields").println();
		for (final Field f : fa)
		{
			final String	n=(null == f) ? null : f.getName();
			if ((null == n) || (n.length() <= 0))
				continue;

			if (!f.isAccessible())
				f.setAccessible(true);

			final Object	v=f.get(o);
			out.append('\t').append(n).append('=').append(String.valueOf(v)).println();
		}
	}

	/* -------------------------------------------------------------------- */

	private static final void showImplementedInterfaces (
			final PrintStream out, final Object o)
	{
		final Class<?>		c=(null == o) ? null : o.getClass();
		final Class<?>[]	ia=(null == c) ? null : c.getInterfaces();
		if ((null == ia) || (ia.length <= 0))
			return;

		out.append(c.getName()).append(" interfaces").println();
		for (final Class<?> i : ia)
		{
			final String	n=(null == i) ? null : i.getName();
			if ((null == n) || (n.length() <= 0))
				continue;
			out.append('\t').append(n).println();
		}
	}

	/* -------------------------------------------------------------------- */

	private static final void showReflectedMethods (
			final PrintStream out, final Object o)
	{
		final Class<?>	c=(null == o) ? null : o.getClass();
		final Method[]	ma=(null == c) ? null : c.getMethods();
		if ((null == ma) || (ma.length <= 0))
			return;

		out.append(c.getName()).append(" methods").println();
		for (final Method m : ma)
			out.append('\t').println(m);
	}

	/* -------------------------------------------------------------------- */

	public static final void runTestObjectAspectTest (
			final BufferedReader in, final PrintStream out, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		TestObject	prev=null;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	value=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "value string/(A)spect/(or Quit)");
			if ((null == value) || (value.length() <= 0))
				continue;
			if (isQuit(value)) break;

			try
			{
				if ("A".equalsIgnoreCase(value) || "aspect".equalsIgnoreCase(value))
				{
					final Object	o=TestAspect.aspectOf();
					showReflectedMethods(out, o);
					showReflectedFields(out, o);
					showImplementedInterfaces(out, o);
				}
				else
				{
					final TestObject	o=new TestObject(value);
					out.append(value)
					   .append(" => ")
					   .append(o.toString())
					   .println();
					showReflectedMethods(out, o);
					showReflectedFields(out, o);
					showImplementedInterfaces(out, o);
	
					if (prev != null)
					{
						final int	nRes=prev.compareTo(o);
						out.append(prev.toString())
						   .append("#compareTo(")
						   .append(o.toString())
						   .append(")=")
						   .println(nRes);
					}
	
					prev = o;
				}
			}
			catch(Exception e)
			{
				System.err.append(e.getClass().getName())
						  .append(" while handle value=").append(value)
						  .append(": ").append(e.getMessage())
					.println() ;
			}
		}
	}

	public static final void main (String[] args)
	{
		runTestObjectAspectTest(getStdin(), System.out, args);
	}
}
